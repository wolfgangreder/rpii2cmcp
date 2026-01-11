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
package at.reder.rpii2cmcp.service;

import at.reder.rpii2cmcp.model.I2CCommand;
import at.reder.rpii2cmcp.model.I2CResponse;
import io.quarkus.test.junit.QuarkusTest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.mockito.Mockito.*;

/**
 * Unit tests for I2CService.
 */
@QuarkusTest
class I2CServiceTest {

  private I2CService service;

  @BeforeEach
  void setUp()
  {
    service = new I2CService();
    service.i2cEnabled = true;
    service.i2cgetPath = "/usr/sbin/i2cget";
    service.i2csetPath = "/usr/sbin/i2cset";
  }

  @Test
  void testValidateCommandNull()
  {
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(null));
  }

  @Test
  void testValidateCommandInvalidBusNegative()
  {
    I2CCommand cmd = new I2CCommand(-1, "0x48", "0x00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidBusTooHigh()
  {
    I2CCommand cmd = new I2CCommand(11, "0x48", "0x00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidAddressNull()
  {
    I2CCommand cmd = new I2CCommand(1, null, "0x00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidAddressFormat()
  {
    I2CCommand cmd = new I2CCommand(1, "48", "0x00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidAddressNoHex()
  {
    I2CCommand cmd = new I2CCommand(1, "0xGG", "0x00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidRegisterNull()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", null, null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidRegisterFormat()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "00", null, "read");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidOperationNull()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, null);
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandInvalidOperationType()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "delete");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWriteWithoutValue()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "write");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWriteWithInvalidValue()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", "FF", "write");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandValidRead()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandValidWrite()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", "0xFF", "write");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandCaseInsensitiveOperation()
  {
    I2CCommand cmd1 = new I2CCommand(1, "0x48", "0x00", null, "READ");
    I2CCommand cmd2 = new I2CCommand(1, "0x48", "0x00", "0xFF", "WRITE");
    assertDoesNotThrow(() -> service.validateCommand(cmd1));
    assertDoesNotThrow(() -> service.validateCommand(cmd2));
  }

  @Test
  void testReadProcessOutput() throws IOException
  {
    String testOutput = "0x42\nline2\n";
    InputStream inputStream = new ByteArrayInputStream(testOutput.getBytes());
    Process mockProcess = mock(Process.class);
    when(mockProcess.getInputStream()).thenReturn(inputStream);

    String result = service.readProcessOutput(mockProcess);
    assertEquals("0x42\nline2\n", result);
  }

  @Test
  void testReadProcessOutputEmpty() throws IOException
  {
    InputStream inputStream = new ByteArrayInputStream("".getBytes());
    Process mockProcess = mock(Process.class);
    when(mockProcess.getInputStream()).thenReturn(inputStream);

    String result = service.readProcessOutput(mockProcess);
    assertEquals("", result);
  }

  @Test
  void testExecuteCommandWhenDisabled()
  {
    service.i2cEnabled = false;
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read");

    I2CResponse response = service.executeCommand(cmd);

    assertFalse(response.isSuccess());
    assertEquals("I2C commands are disabled", response.getError());
  }

  @Test
  void testExecuteCommandInvalidOperation()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "invalid");

    I2CResponse response = service.executeCommand(cmd);

    assertFalse(response.isSuccess());
    assertNotNull(response.getError());
    assertTrue(response.getError().contains("Invalid operation"));
  }

  @Test
  void testExecuteCommandInvalidBus()
  {
    I2CCommand cmd = new I2CCommand(-1, "0x48", "0x00", null, "read");

    I2CResponse response = service.executeCommand(cmd);

    assertFalse(response.isSuccess());
    assertNotNull(response.getError());
    assertTrue(response.getError().contains("Invalid bus number"));
  }

  @Test
  void testValidateCommandEdgeCases()
  {
    // Test boundary values
    I2CCommand cmd1 = new I2CCommand(0, "0x48", "0x00", null, "read");
    assertDoesNotThrow(() -> service.validateCommand(cmd1));

    I2CCommand cmd2 = new I2CCommand(10, "0x48", "0x00", null, "read");
    assertDoesNotThrow(() -> service.validateCommand(cmd2));

    // Test single digit hex
    I2CCommand cmd3 = new I2CCommand(1, "0x3", "0x0", null, "read");
    assertDoesNotThrow(() -> service.validateCommand(cmd3));

    // Test uppercase hex
    I2CCommand cmd4 = new I2CCommand(1, "0XFF", "0XAA", "0XBB", "write");
    assertDoesNotThrow(() -> service.validateCommand(cmd4));
  }

  @Test
  void testExecuteCommandNullCommand()
  {
    I2CResponse response = service.executeCommand(null);

    assertFalse(response.isSuccess());
    assertNotNull(response.getError());
    assertTrue(response.getError().contains("Command cannot be null"));
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteReadCommandWithRealProcess()
  {
    // Use a command that will always fail (non-existent i2c device)
    service.i2cgetPath = "/bin/false";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read");

    I2CResponse response = service.executeCommand(cmd);

    // Should fail because the command returns non-zero exit code
    assertFalse(response.isSuccess());
    assertNotNull(response.getError());
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteWriteCommandWithRealProcess()
  {
    // Use a command that will always fail (non-existent i2c device)
    service.i2csetPath = "/bin/false";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", "0xFF", "write");

    I2CResponse response = service.executeCommand(cmd);

    // Should fail because the command returns non-zero exit code
    assertFalse(response.isSuccess());
    assertNotNull(response.getError());
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteReadCommandSuccess()
  {
    // Use echo command to simulate successful read
    service.i2cgetPath = "/bin/echo";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read");

    I2CResponse response = service.executeCommand(cmd);

    // Should succeed because echo returns 0
    assertTrue(response.isSuccess());
    assertNotNull(response.getData());
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteWriteCommandSuccess()
  {
    // Use true command to simulate successful write
    service.i2csetPath = "/bin/true";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", "0xFF", "write");

    I2CResponse response = service.executeCommand(cmd);

    // Should succeed because true returns 0
    assertTrue(response.isSuccess());
    assertEquals("Write successful", response.getData());
  }

  @Test
  void testReadProcessOutputMultipleLines() throws IOException
  {
    String testOutput = "line1\nline2\nline3\n";
    InputStream inputStream = new ByteArrayInputStream(testOutput.getBytes());
    Process mockProcess = mock(Process.class);
    when(mockProcess.getInputStream()).thenReturn(inputStream);

    String result = service.readProcessOutput(mockProcess);
    assertEquals("line1\nline2\nline3\n", result);
  }

  // Mode validation tests

  @Test
  void testValidateCommandWithByteMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "b");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithWordMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "w");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithBlockMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i 4");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithBlockModeNoSpace()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i4");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithBlockModeMinValue()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i1");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithBlockModeMaxValue()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i 32");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithNullMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", null);
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithEmptyMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithBlankMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "   ");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithInvalidMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "x");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithInvalidBlockModeZero()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i 0");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithInvalidBlockModeTooHigh()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i 33");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithInvalidBlockModeNegative()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i -1");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  void testValidateCommandWithInvalidBlockModeNoNumber()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i");
    assertThrows(IllegalArgumentException.class, () -> service.validateCommand(cmd));
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteReadCommandWithMode()
  {
    service.i2cgetPath = "/bin/echo";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "w");

    I2CResponse response = service.executeCommand(cmd);

    assertTrue(response.isSuccess());
    assertNotNull(response.getData());
    assertTrue(response.getCommand().contains("w"));
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  void testExecuteReadCommandWithBlockMode()
  {
    service.i2cgetPath = "/bin/echo";
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", null, "read", "i 4");

    I2CResponse response = service.executeCommand(cmd);

    assertTrue(response.isSuccess());
    assertNotNull(response.getData());
    assertTrue(response.getCommand().contains("i 4"));
  }

  @Test
  void testValidateCommandWithWriteAndMode()
  {
    I2CCommand cmd = new I2CCommand(1, "0x48", "0x00", "0xFF", "write", "w");
    assertDoesNotThrow(() -> service.validateCommand(cmd));
  }
}
