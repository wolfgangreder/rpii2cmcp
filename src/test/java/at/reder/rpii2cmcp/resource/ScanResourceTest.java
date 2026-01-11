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
 * Integration tests for ScanResource (Copilot MCP server discovery endpoints).
 */
@QuarkusTest
class ScanResourceTest {

    @InjectMock
    I2CService i2cService;

    @Test
    void testScanGetTools() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/scan/tools")
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
    void testScanCallI2cgetSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "0x42", null, "i2cget -y 1 0x48 0x00");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].type", is("text"))
            .body("content[0].text", is("0x42"));
    }

    @Test
    void testScanCallI2csetSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "Write successful", null, 
                "i2cset -y 1 0x48 0x00 0xFF");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cset\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"value\":\"0xFF\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].text", is("Write successful"));
    }

    @Test
    void testScanCallUnknownTool() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"unknown\",\"arguments\":{}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Unknown tool: unknown"));
    }

    @Test
    void testScanGetServerInfo() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/api/scan/info")
        .then()
            .statusCode(200)
            .body("name", is("rpii2cmcp"))
            .body("version", is("1.0.1-SNAPSHOT"))
            .body("protocol", is("mcp"))
            .body("description", notNullValue());
    }

    @Test
    void testScanCallI2cgetWithMode() {
        I2CResponse mockResponse = new I2CResponse(true, "0x1234", null, "i2cget -y -a 1 0x48 0x00 w");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"mode\":\"w\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].text", is("0x1234"));
    }

    @Test
    void testScanCallI2cgetWithBlockMode() {
        I2CResponse mockResponse = new I2CResponse(true, "0x12 0x34 0x56 0x78", null, "i2cget -y -a 1 0x48 0x00 i 4");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"mode\":\"i 4\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].text", is("0x12 0x34 0x56 0x78"));
    }

    @Test
    void testScanCallWithAlternativeFields() {
        I2CResponse mockResponse = new I2CResponse(true, "0x42", null, "i2cget -y 1 0x48 0x00");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"tool\":\"i2cget\",\"input\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(false))
            .body("content[0].text", is("0x42"));
    }

    @Test
    void testScanCallI2cgetMissingArgument() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Missing required argument: address"));
    }

    @Test
    void testScanCallI2csetMissingValue() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cset\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", is("Missing required argument: value"));
    }

    @Test
    void testScanToolServiceException() {
        when(i2cService.executeCommand(any(I2CCommand.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"i2cget\",\"arguments\":{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\"}}")
        .when()
            .post("/api/scan/tools")
        .then()
            .statusCode(200)
            .body("isError", is(true))
            .body("content[0].text", notNullValue());
    }
}
