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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.graphaware.test.integration.DatabaseIntegrationTest;
import com.graphaware.test.integration.NeoServerIntegrationTest;
import static com.graphaware.test.util.TestUtils.executeCypher;
import static com.graphaware.test.util.TestUtils.get;
import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UuidApiTest extends NeoServerIntegrationTest {

    @Override
    protected String neo4jConfigFile() {
        return "neo4j-uuid-all.properties";
    }

    @Test
    public void nodeShouldBeFetchedByUuid() {
        executeCypher(baseUrl(),
                "create (c:Company {name:'GraphAware'})");
        String result = executeCypher(baseUrl(),
        "match (c:Company) return c.uuid");

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(result).getAsJsonObject();
        String uuid = obj.get("results").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").getAsJsonArray().get(0).getAsString();
        assertEquals("0", get(baseUrl() + "/graphaware/uuid/UIDM/node/" + uuid, HttpStatus.SC_OK));
    }


}
