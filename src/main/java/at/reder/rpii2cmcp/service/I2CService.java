package at.reder.rpii2cmcp.service;

import at.reder.rpii2cmcp.model.I2CCommand;
import at.reder.rpii2cmcp.model.I2CResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for executing I2C commands using i2c-tools on Raspberry Pi.
 * This service provides secure execution of i2cget and i2cset commands
 * with proper validation and error handling.
 * 
 * @author Wolfgang Reder
 * @version 1.0.0
 */
@ApplicationScoped
public class I2CService {

    private static final Logger LOG = Logger.getLogger(I2CService.class);

    /**
     * Pattern for validating hexadecimal addresses.
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^0[xX][0-9A-Fa-f]{1,2}$");

    /**
     * Maximum allowed I2C bus number for security.
     */
    private static final int MAX_BUS_NUMBER = 10;

    /**
     * Path to i2cget command.
     */
    @ConfigProperty(name = "i2c.command.get", defaultValue = "/usr/sbin/i2cget")
    String i2cgetPath;

    /**
     * Path to i2cset command.
     */
    @ConfigProperty(name = "i2c.command.set", defaultValue = "/usr/sbin/i2cset")
    String i2csetPath;

    /**
     * Whether I2C commands are enabled.
     */
    @ConfigProperty(name = "i2c.enabled", defaultValue = "true")
    boolean i2cEnabled;

    /**
     * Executes an I2C command (read or write).
     *
     * @param command the I2C command to execute
     * @return the response containing the result or error
     * @throws IllegalArgumentException if command parameters are invalid
     */
    public I2CResponse executeCommand(I2CCommand command) {
        if (command == null) {
            LOG.error("Received null command");
            return new I2CResponse(false, null, "Command cannot be null", "");
        }
        
        LOG.infof("Executing I2C command: operation=%s, bus=%d, address=%s, register=%s",
                command.getOperation(), command.getBus(), command.getAddress(), command.getRegister());

        if (!i2cEnabled) {
            LOG.warn("I2C commands are disabled");
            return new I2CResponse(false, null, "I2C commands are disabled", "");
        }

        try {
            // Validate input
            validateCommand(command);
            
            if ("read".equalsIgnoreCase(command.getOperation())) {
                return executeRead(command);
            } else if ("write".equalsIgnoreCase(command.getOperation())) {
                return executeWrite(command);
            } else {
                throw new IllegalArgumentException("Invalid operation: " + command.getOperation());
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid command parameters", e);
            return new I2CResponse(false, null, e.getMessage(), "");
        } catch (Exception e) {
            LOG.error("Error executing I2C command", e);
            return new I2CResponse(false, null, "Error executing command: " + e.getMessage(), "");
        }
    }

    /**
     * Validates the I2C command parameters for security.
     *
     * @param command the command to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateCommand(I2CCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        if (command.getBus() < 0 || command.getBus() > MAX_BUS_NUMBER) {
            throw new IllegalArgumentException("Invalid bus number: " + command.getBus());
        }

        if (command.getAddress() == null || !HEX_PATTERN.matcher(command.getAddress()).matches()) {
            throw new IllegalArgumentException("Invalid address format: " + command.getAddress());
        }

        if (command.getRegister() == null || !HEX_PATTERN.matcher(command.getRegister()).matches()) {
            throw new IllegalArgumentException("Invalid register format: " + command.getRegister());
        }

        if (command.getOperation() == null || 
            (!command.getOperation().equalsIgnoreCase("read") && 
             !command.getOperation().equalsIgnoreCase("write"))) {
            throw new IllegalArgumentException("Invalid operation: " + command.getOperation());
        }

        if ("write".equalsIgnoreCase(command.getOperation())) {
            if (command.getValue() == null || !HEX_PATTERN.matcher(command.getValue()).matches()) {
                throw new IllegalArgumentException("Invalid value format: " + command.getValue());
            }
        }
    }

    /**
     * Executes an I2C read operation.
     *
     * @param command the read command
     * @return the response with the read data
     * @throws IOException if command execution fails
     */
    I2CResponse executeRead(I2CCommand command) throws IOException {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(i2cgetPath);
        cmdList.add("-y");
        cmdList.add(String.valueOf(command.getBus()));
        cmdList.add(command.getAddress());
        cmdList.add(command.getRegister());

        String cmdString = String.join(" ", cmdList);
        LOG.infof("Executing read command: %s", cmdString);

        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output = readProcessOutput(process);
        
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOG.infof("Read command successful, data: %s", output.trim());
                return new I2CResponse(true, output.trim(), null, cmdString);
            } else {
                LOG.errorf("Read command failed with exit code %d: %s", exitCode, output);
                return new I2CResponse(false, null, "Command failed: " + output, cmdString);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command interrupted", e);
        }
    }

    /**
     * Executes an I2C write operation.
     *
     * @param command the write command
     * @return the response indicating success or failure
     * @throws IOException if command execution fails
     */
    I2CResponse executeWrite(I2CCommand command) throws IOException {
        List<String> cmdList = new ArrayList<>();
        cmdList.add(i2csetPath);
        cmdList.add("-y");
        cmdList.add(String.valueOf(command.getBus()));
        cmdList.add(command.getAddress());
        cmdList.add(command.getRegister());
        cmdList.add(command.getValue());

        String cmdString = String.join(" ", cmdList);
        LOG.infof("Executing write command: %s", cmdString);

        ProcessBuilder pb = new ProcessBuilder(cmdList);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output = readProcessOutput(process);
        
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOG.info("Write command successful");
                return new I2CResponse(true, "Write successful", null, cmdString);
            } else {
                LOG.errorf("Write command failed with exit code %d: %s", exitCode, output);
                return new I2CResponse(false, null, "Command failed: " + output, cmdString);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command interrupted", e);
        }
    }

    /**
     * Reads the output from a process.
     *
     * @param process the process to read from
     * @return the output as a string
     * @throws IOException if reading fails
     */
    String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
