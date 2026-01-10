package at.reder.rpii2cmcp.model;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
    void testParameterizedConstructor() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", "0xFF", "write");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertEquals("0xFF", command.getValue());
        assertEquals("write", command.getOperation());
    }

    @Test
    void testSettersAndGetters() {
        I2CCommand command = new I2CCommand();
        
        command.setBus(2);
        command.setAddress("0x50");
        command.setRegister("0x10");
        command.setValue("0xAA");
        command.setOperation("read");
        
        assertEquals(2, command.getBus());
        assertEquals("0x50", command.getAddress());
        assertEquals("0x10", command.getRegister());
        assertEquals("0xAA", command.getValue());
        assertEquals("read", command.getOperation());
    }

    @Test
    void testNullValues() {
        I2CCommand command = new I2CCommand();
        
        command.setAddress(null);
        command.setRegister(null);
        command.setValue(null);
        command.setOperation(null);
        
        assertNull(command.getAddress());
        assertNull(command.getRegister());
        assertNull(command.getValue());
        assertNull(command.getOperation());
    }

    @Test
    void testReadCommandWithoutValue() {
        I2CCommand command = new I2CCommand(1, "0x48", "0x00", null, "read");
        
        assertEquals(1, command.getBus());
        assertEquals("0x48", command.getAddress());
        assertEquals("0x00", command.getRegister());
        assertNull(command.getValue());
        assertEquals("read", command.getOperation());
    }
}
