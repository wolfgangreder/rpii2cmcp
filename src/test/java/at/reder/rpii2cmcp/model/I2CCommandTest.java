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
 * Unit tests for I2CCommand model.
 */
@QuarkusTest
class I2CCommandTest {

    @Test
    void testDefaultConstructor() {
        I2CCommand command = new I2CCommand();
        assertNotNull(command);
    }

    @Test
    void testParameterizedConstructorWithoutMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", "0xFF", "write");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertEquals("0xFF", command.getValue());
        assertEquals("write", command.getOperation());
        assertNull(command.getMode());
    }

    @Test
    void testParameterizedConstructorWithMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", "0xFF", "write", "w");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertEquals("0xFF", command.getValue());
        assertEquals("write", command.getOperation());
        assertEquals("w", command.getMode());
    }

    @Test
    void testSettersAndGetters() {
        I2CCommand command = new I2CCommand();
        
        command.setBus(2);
        command.setAddress("0x50");
        command.setRegister("0x10");
        command.setValue("0xAA");
        command.setOperation("read");
        command.setMode("b");
        
        assertEquals(2, command.getBus());
        assertEquals("0x50", command.getAddress());
        assertEquals("0x10", command.getRegister());
        assertEquals("0xAA", command.getValue());
        assertEquals("read", command.getOperation());
        assertEquals("b", command.getMode());
    }

    @Test
    void testNullValues() {
        I2CCommand command = new I2CCommand();
        
        command.setAddress(null);
        command.setRegister(null);
        command.setValue(null);
        command.setOperation(null);
        command.setMode(null);
        
        assertNull(command.getAddress());
        assertNull(command.getRegister());
        assertNull(command.getValue());
        assertNull(command.getOperation());
        assertNull(command.getMode());
    }

    @Test
    void testReadCommandWithoutValue() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertNull(command.getValue());
        assertEquals("read", command.getOperation());
        assertNull(command.getMode());
    }

    @Test
    void testReadCommandWithMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read", "i 4");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertNull(command.getValue());
        assertEquals("read", command.getOperation());
        assertEquals("i 4", command.getMode());
    }

    @Test
    void testWordMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read", "w");
        assertEquals("w", command.getMode());
    }

    @Test
    void testByteMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read", "b");
        assertEquals("b", command.getMode());
    }

    @Test
    void testBlockMode() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read", "i 32");
        assertEquals("i 32", command.getMode());
    }
}
