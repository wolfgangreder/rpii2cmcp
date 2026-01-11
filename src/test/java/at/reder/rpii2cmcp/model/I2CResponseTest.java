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
package at.reder.rpii2cmcp.model;

import io.quarkus.test.junit.QuarkusTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for I2CResponse model.
 */
@QuarkusTest
class I2CResponseTest {

    @Test
    void testDefaultConstructor() {
        I2CResponse response = new I2CResponse();
        assertNotNull(response);
    }

    @Test
    void testSuccessConstructor() {
        I2CResponse response = new I2CResponse("0x42", "i2cget -y 1 0x48 0x00");
        
        assertTrue(response.isSuccess());
        assertEquals("0x42", response.getData());
        assertEquals("i2cget -y 1 0x48 0x00", response.getCommand());
        assertNull(response.getError());
    }

    @Test
    void testFullConstructor() {
        I2CResponse response = new I2CResponse(false, null, "Error message", "command");
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Error message", response.getError());
        assertEquals("command", response.getCommand());
    }

    @Test
    void testSettersAndGetters() {
        I2CResponse response = new I2CResponse();
        
        response.setSuccess(true);
        response.setData("0xFF");
        response.setError("Test error");
        response.setCommand("test command");
        
        assertTrue(response.isSuccess());
        assertEquals("0xFF", response.getData());
        assertEquals("Test error", response.getError());
        assertEquals("test command", response.getCommand());
    }

    @Test
    void testSuccessResponse() {
        I2CResponse response = new I2CResponse(true, "data", null, "cmd");
        
        assertTrue(response.isSuccess());
        assertEquals("data", response.getData());
        assertNull(response.getError());
        assertEquals("cmd", response.getCommand());
    }

    @Test
    void testErrorResponse() {
        I2CResponse response = new I2CResponse(false, null, "Device not found", "cmd");
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("Device not found", response.getError());
        assertEquals("cmd", response.getCommand());
    }

    @Test
    void testNullValues() {
        I2CResponse response = new I2CResponse();
        
        response.setData(null);
        response.setError(null);
        response.setCommand(null);
        
        assertNull(response.getData());
        assertNull(response.getError());
        assertNull(response.getCommand());
    }
}
