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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static com.graphaware.runtime.RuntimeRegistry.getStartedRuntime;

/**
 * REST API for {@link UuidModule}.
 */
@Controller
@RequestMapping("/uuid")
public class UuidApi {

    private final GraphDatabaseService database;
    private final String DEFAULT_MODULE_ID="UIDM";
    private String moduleId=DEFAULT_MODULE_ID;

    @Autowired
    public UuidApi(GraphDatabaseService database) {
        this.database = database;
    }

    public UuidApi(GraphDatabaseService database, String moduleId) {
        this.database=database;
        this.moduleId = moduleId;

    }
    /**
     * Get the Node ID by its UUID
     * @param uuid UUID assigned to the node
     * @return the internal Node Id
     */
    @RequestMapping(value = "/node/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByUuid(@PathVariable(value = "uuid") String uuid) {
        return getNodeIdByUuid(moduleId,uuid);
    }

    /**
     * Get the Node ID by its UUID
     * @param moduleId the module ID
     * @param uuid UUID assigned to the node
     * @return the internal Node Id
     */
    @RequestMapping(value = "/{moduleId}/node/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Long getNodeIdByUuid(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "uuid") String uuid) {
        try(Transaction tx = database.beginTx()) {
            Node node = getStartedRuntime(database).getModule(moduleId, UuidModule.class).getNodeByUuid(uuid);
            if(node!=null) {
               return node.getId();
            }
            tx.success();
        }
        return null;
    }

    /**
     * Get the UUID of a node by node ID
     * @param nodeId the node ID
     * @return the UUID assigned to this node or null if it does not exist
     */
    @RequestMapping(value = "/node/{nodeId}/uuid", method = RequestMethod.GET)
    @ResponseBody
    public String getUuidByNodeId(@PathVariable(value = "nodeId") long nodeId) {
       return getUuidByNodeId(moduleId,nodeId);
    }


    /**
     * Get the UUID of a node by node ID
     * @param moduleId the module ID
     * @param nodeId the node ID
     * @return the UUID assigned to this node or null if it does not exist
     */
    @RequestMapping(value = "/{moduleId}/node/{nodeId}/uuid", method = RequestMethod.GET)
    @ResponseBody
    public String getUuidByNodeId(@PathVariable(value = "moduleId") String moduleId, @PathVariable(value = "nodeId") long nodeId) {
        try(Transaction tx = database.beginTx()) {
            return getStartedRuntime(database).getModule(moduleId, UuidModule.class).getUuidForNode(nodeId);

        }
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(IllegalArgumentException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        return Collections.singletonMap("message", e.getMessage());
    }

}
