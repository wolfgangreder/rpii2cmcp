package at.reder.rpii2cmcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an I2C command to be executed on the Raspberry Pi.
 * This class encapsulates the parameters needed to execute i2c-tools commands.
 * 
 * @author Wolfgang Reder
 * @version 1.0.0
 */
public class I2CCommand {

    /**
     * The I2C bus number (typically 0 or 1 on Raspberry Pi).
     */
    @JsonProperty("bus")
    private int bus;

    /**
     * The I2C device address (7-bit address, typically 0x03 to 0x77).
     */
    @JsonProperty("address")
    private String address;

    /**
     * The register address to read from or write to.
     */
    @JsonProperty("register")
    private String register;

    /**
     * The value to write (null for read operations).
     */
    @JsonProperty("value")
    private String value;

    /**
     * The operation type: "read" or "write".
     */
    @JsonProperty("operation")
    private String operation;

    /**
     * Default constructor for Jackson deserialization.
     */
    public I2CCommand() {
    }

    /**
     * Creates a new I2C command.
     *
     * @param bus       the I2C bus number
     * @param address   the device address in hex format (e.g., "0x48")
     * @param register  the register address in hex format (e.g., "0x00")
     * @param value     the value to write in hex format (null for read)
     * @param operation the operation type ("read" or "write")
     */
    public I2CCommand(int bus, String address, String register, String value, String operation) {
        this.bus = bus;
        this.address = address;
        this.register = register;
        this.value = value;
        this.operation = operation;
    }

    /**
     * Gets the I2C bus number.
     *
     * @return the bus number
     */
    public int getBus() {
        return bus;
    }

    /**
     * Sets the I2C bus number.
     *
     * @param bus the bus number
     */
    public void setBus(int bus) {
        this.bus = bus;
    }

    /**
     * Gets the device address.
     *
     * @return the device address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the device address.
     *
     * @param address the device address in hex format
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the register address.
     *
     * @return the register address
     */
    public String getRegister() {
        return register;
    }

    /**
     * Sets the register address.
     *
     * @param register the register address in hex format
     */
    public void setRegister(String register) {
        this.register = register;
    }

    /**
     * Gets the value to write.
     *
     * @return the value to write, or null for read operations
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value to write.
     *
     * @param value the value in hex format
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the operation type.
     *
     * @return the operation type ("read" or "write")
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets the operation type.
     *
     * @param operation the operation type ("read" or "write")
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }
}
