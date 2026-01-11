/*
 * Copyright 2026 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.reder.rpii2cmcp.resource;

import at.reder.rpii2cmcp.model.I2CCommand;
import at.reder.rpii2cmcp.model.I2CResponse;
import at.reder.rpii2cmcp.service.I2CService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for I2CResource MCP Server.
 */
@QuarkusTest
class I2CResourceTest {

    @InjectMock
    I2CService i2cService;

    @Test
    void testListTools() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/tools/list")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].name", is("i2cget"))
            .body("[0].description", notNullValue())
            .body("[0].inputSchema", notNullValue())
            .body("[1].name", is("i2cset"))
            .body("[1].description", notNullValue())
            .body("[1].inputSchema", notNullValue());
    }

    @Test
    void testCallI2cgetSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "0x42", null, "i2cget -y 1 0x48 0x00");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].type", is("text"))
            .body("content[0].text", is("0x42"));
    }

    @Test
    void testCallI2csetSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "Write successful", null, 
                "i2cset -y 1 0x48 0x00 0xFF");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cset\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"value\":\"0xFF\"}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].type", is("text"))
            .body("content[0].text", is("Write successful"));
    }

    @Test
    void testCallI2cgetFailure() {
        I2CResponse mockResponse = new I2CResponse(false, null, "Device not found", "");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Device not found"));
    }

    @Test
    void testCallUnknownTool() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"unknown\",\"arguments\":{}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Unknown tool: unknown"));
    }

    @Test
    void testCallI2cgetMissingArgument() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Missing required argument: address"));
    }

    @Test
    void testCallI2csetMissingValue() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cset\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Missing required argument: value"));
    }

    @Test
    void testCallToolServiceException() {
        when(i2cService.executeCommand(any(I2CCommand.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/tools/call")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", notNullValue());
    }
}
