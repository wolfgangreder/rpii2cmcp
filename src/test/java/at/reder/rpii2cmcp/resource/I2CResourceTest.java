package at.reder.rpii2cmcp.resource;

import at.reder.rpii2cmcp.model.I2CCommand;
import at.reder.rpii2cmcp.model.I2CResponse;
import at.reder.rpii2cmcp.service.I2CService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests for I2CResource.
 */
@QuarkusTest
class I2CResourceTest {

    @InjectMock
    I2CService i2cService;

    @Test
    void testExecuteReadCommandSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "0x42", null, "i2cget -y 1 0x48 0x00");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"operation\":\"read\"}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data", is("0x42"))
            .body("command", notNullValue());
    }

    @Test
    void testExecuteWriteCommandSuccess() {
        I2CResponse mockResponse = new I2CResponse(true, "Write successful", null, 
                "i2cset -y 1 0x48 0x00 0xFF");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"value\":\"0xFF\",\"operation\":\"write\"}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(200)
            .body("success", is(true))
            .body("data", is("Write successful"));
    }

    @Test
    void testExecuteCommandFailure() {
        I2CResponse mockResponse = new I2CResponse(false, null, "Device not found", "");
        when(i2cService.executeCommand(any(I2CCommand.class))).thenReturn(mockResponse);

        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"operation\":\"read\"}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(400)
            .body("success", is(false))
            .body("error", is("Device not found"));
    }

    @Test
    void testExecuteCommandInvalidInput() {
        when(i2cService.executeCommand(any(I2CCommand.class)))
                .thenThrow(new IllegalArgumentException("Invalid bus number"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":-1,\"address\":\"0x48\",\"register\":\"0x00\",\"operation\":\"read\"}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(400)
            .body("success", is(false))
            .body("error", notNullValue());
    }

    @Test
    void testExecuteCommandServerError() {
        when(i2cService.executeCommand(any(I2CCommand.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":1,\"address\":\"0x48\",\"register\":\"0x00\",\"operation\":\"read\"}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(500)
            .body("success", is(false))
            .body("error", notNullValue());
    }

    @Test
    void testExecuteCommandMissingFields() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"bus\":1}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(500);  // Will fail due to missing required fields
    }

    @Test
    void testExecuteCommandEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/i2c/execute")
        .then()
            .statusCode(500);  // Will fail due to missing required fields
    }
}
