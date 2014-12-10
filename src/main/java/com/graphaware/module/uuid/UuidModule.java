/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.uuid;

import com.graphaware.runtime.config.TxDrivenModuleConfiguration;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.Change;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.single.TransactionCallback;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.List;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that assigns UUID's to nodes in the graph.
 */
public class UuidModule extends BaseTxDrivenModule<Void> {

    private final static int BATCH_SIZE = 1000;

    private final UuidGenerator uuidGenerator;
    private final UuidConfiguration uuidConfiguration;
    private final GraphDatabaseService database;
    /**
     * Construct a new UUID module.
     *
     * @param moduleId ID of the module.
     */
    public UuidModule(String moduleId, UuidConfiguration configuration, GraphDatabaseService database) {
        super(moduleId);
        this.uuidGenerator = new EaioUuidGenerator();
        this.uuidConfiguration = configuration;
        this.database = database;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TxDrivenModuleConfiguration getConfiguration() {
        return uuidConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new TransactionCallback<Iterable<Node>>() {
                    @Override
                    public Iterable<Node> doInTransaction(GraphDatabaseService database) throws Exception {
                        return GlobalGraphOperations.at(database).getAllNodes();
                    }
                },
                new UnitOfWork<Node>() {
                    @Override
                    public void execute(GraphDatabaseService database, Node node, int batchNumber, int stepNumber) {
                        if (getConfiguration().getInclusionPolicies().getNodeInclusionPolicy().include(node)) {
                            assignUuid(node);
                            indexUuid(node);
                        }
                    }
                }
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {

        //Set the UUID on all created nodes
        for (Node node : transactionData.getAllCreatedNodes()) {
            assignUuid(node);
            indexUuid(node);
        }

        //Check if the UUID has been modified or removed from the node and throw an error
        for (Change<Node> change : transactionData.getAllChangedNodes()) {
            if (!change.getCurrent().hasProperty(uuidConfiguration.getUuidProperty())) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + uuidConfiguration.getUuidProperty() + " property");
            }

            if (!change.getPrevious().getProperty(uuidConfiguration.getUuidProperty()).equals(change.getCurrent().getProperty(uuidConfiguration.getUuidProperty()))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + uuidConfiguration.getUuidProperty() + " property");
            }
        }
        for (Node deletedNode : transactionData.getAllDeletedNodes()) {
            database.index().forNodes(uuidConfiguration.getUuidLegacyIndexName()).remove(deletedNode);
        }
        return null;
    }

    /**
     * Get a Node by its uuid
     * @param uuid the uuid
     * @return the node with the uuid or null if a node with the uuid does not exist
     */
    public Node getNodeByUuid(String uuid) {
        if(uuid==null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Node node =  database.index().forNodes(uuidConfiguration.getUuidLegacyIndexName()).get(uuidConfiguration.getUuidProperty(), uuid).getSingle();
        if(node!=null) {
            return node;
        }
        else {
            throw new IllegalArgumentException("Node with UUID " + uuid + " does not exist");
        }
    }

    /**
     * Get the UUID of a node
     * @param nodeId the node id
     * @return theuuid or null if a node if it does not exist
     */
    public String getUuidForNode(long nodeId) {

        Node node =  database.getNodeById(nodeId);
        if(node.hasProperty(uuidConfiguration.getUuidProperty())) {
            return (String)node.getProperty(uuidConfiguration.getUuidProperty());
        }
        return null;
    }

    private void assignUuid(Node node) {
        if (!node.hasProperty(uuidConfiguration.getUuidProperty())) {
            String uuid = uuidGenerator.generateUuid();
            node.setProperty(uuidConfiguration.getUuidProperty(), uuid);
        }
    }

    private void indexUuid(Node node) {
        database.index().forNodes(uuidConfiguration.getUuidLegacyIndexName()).add(node, uuidConfiguration.getUuidProperty(), node.getProperty(uuidConfiguration.getUuidProperty()));
    }


}
