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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the response from an I2C command execution.
 * Contains the result of the operation and any relevant status information.
 * 
 * @author Wolfgang Reder
 * @version 1.0.0
 */
public class I2CResponse {

    /**
     * Indicates whether the operation was successful.
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * The result data from the operation (for read operations).
     */
    @JsonProperty("data")
    private String data;

    /**
     * Error message if the operation failed.
     */
    @JsonProperty("error")
    private String error;

    /**
     * The original command that was executed.
     */
    @JsonProperty("command")
    private String command;

    /**
     * Default constructor.
     */
    public I2CResponse() {
    }

    /**
     * Creates a successful response.
     *
     * @param data    the data read from the device
     * @param command the command that was executed
     */
    public I2CResponse(String data, String command) {
        this.success = true;
        this.data = data;
        this.command = command;
    }

    /**
     * Creates a response with success status.
     *
     * @param success whether the operation succeeded
     * @param data    the data read from the device
     * @param error   error message if operation failed
     * @param command the command that was executed
     */
    public I2CResponse(boolean success, String data, String error, String command) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.command = command;
    }

    /**
     * Gets the success status.
     *
     * @return true if operation succeeded, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets the success status.
     *
     * @param success the success status
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the data from the operation.
     *
     * @return the data, or null if not applicable
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Gets the error message.
     *
     * @return the error message, or null if operation succeeded
     */
    public String getError() {
        return error;
    }

    /**
     * Sets the error message.
     *
     * @param error the error message
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Gets the executed command.
     *
     * @return the command that was executed
     */
    public String getCommand() {
        return command;
    }

    /**
     * Sets the executed command.
     *
     * @param command the command
     */
    public void setCommand(String command) {
        this.command = command;
    }
}
