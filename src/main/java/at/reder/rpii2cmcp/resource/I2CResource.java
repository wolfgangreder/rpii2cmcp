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
import at.reder.rpii2cmcp.model.mcp.McpTool;
import at.reder.rpii2cmcp.model.mcp.McpToolCall;
import at.reder.rpii2cmcp.model.mcp.McpToolResult;
import at.reder.rpii2cmcp.service.I2CService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * MCP Server resource for I2C command execution.
 * Provides tools for executing I2C read and write operations via MCP protocol.
 *
 * @author Wolfgang Reder
 * @version 1.0.0
 */
@Path("/tools")
@Tag(name = "MCP Tools", description = "MCP Server tool endpoints for I2C operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class I2CResource {

  private static final Logger LOG = Logger.getLogger(I2CResource.class);

  private static final String TOOL_I2CGET = "i2cget";
  private static final String TOOL_I2CSET = "i2cset";

  @Inject
  I2CService i2cService;

  /**
   * Lists all available MCP tools.
   *
   * @return list of available tools with their schemas
   */
  @GET
  @Path("/list")
  @Operation(summary = "List available tools",
          description = "Returns a list of all available MCP tools with their input schemas")
  @APIResponses(value = {
    @APIResponse(responseCode = "200", description = "Tools listed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = McpTool.class)))
  })
  public List<McpTool> getTools() {
    LOG.info("Listing available MCP tools");
    return List.of(createI2cgetTool(), createI2csetTool());
  }

  /**
   * Executes an MCP tool call.
   *
   * @param toolCall the tool call request containing tool name and arguments
   * @return the result of the tool execution
   */
  @POST
  @Path("/call")
  @Operation(summary = "Execute MCP tool",
          description = "Executes an MCP tool with the provided arguments")
  @APIResponses(value = {
    @APIResponse(responseCode = "200", description = "Tool executed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = McpToolResult.class))),
    @APIResponse(responseCode = "400", description = "Invalid tool call parameters"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public McpToolResult callTool(
          @RequestBody(description = "MCP tool call request", required = true,
                  content = @Content(schema = @Schema(implementation = McpToolCall.class))) McpToolCall toolCall) {

    LOG.infof("Received MCP tool call: name=%s", toolCall.getName());

    try {
      return switch (toolCall.getName()) {
        case TOOL_I2CGET -> executeI2cget(toolCall.getArguments());
        case TOOL_I2CSET -> executeI2cset(toolCall.getArguments());
        default -> McpToolResult.error("Unknown tool: " + toolCall.getName());
      };
    } catch (IllegalArgumentException e) {
      LOG.error("Invalid tool call", e);
      return McpToolResult.error(e.getMessage());
    } catch (Exception e) {
      LOG.error("Error executing tool", e);
      return McpToolResult.error("Internal server error: " + e.getMessage());
    }
  }

  private McpToolResult executeI2cget(Map<String, Object> arguments) {
    int bus = getIntArgument(arguments, "bus");
    String address = getStringArgument(arguments, "address");
    String register = getStringArgument(arguments, "register");

    I2CCommand command = new I2CCommand(bus, address, register, null, "read");
    I2CResponse response = i2cService.executeCommand(command);

    if (response.isSuccess()) {
      return McpToolResult.success(response.getData());
    } else {
      return McpToolResult.error(response.getError());
    }
  }

  private McpToolResult executeI2cset(Map<String, Object> arguments) {
    int bus = getIntArgument(arguments, "bus");
    String address = getStringArgument(arguments, "address");
    String register = getStringArgument(arguments, "register");
    String value = getStringArgument(arguments, "value");

    I2CCommand command = new I2CCommand(bus, address, register, value, "write");
    I2CResponse response = i2cService.executeCommand(command);

    if (response.isSuccess()) {
      return McpToolResult.success(response.getData());
    } else {
      return McpToolResult.error(response.getError());
    }
  }

  private int getIntArgument(Map<String, Object> arguments, String name) {
    Object value = arguments.get(name);
    if (value == null) {
      throw new IllegalArgumentException("Missing required argument: " + name);
    }
    if (value instanceof Number) {
      return ((Number) value).intValue();
    }
    return Integer.parseInt(value.toString());
  }

  private String getStringArgument(Map<String, Object> arguments, String name) {
    Object value = arguments.get(name);
    if (value == null) {
      throw new IllegalArgumentException("Missing required argument: " + name);
    }
    return value.toString();
  }

  private McpTool createI2cgetTool() {
    Map<String, Object> schema = new LinkedHashMap<>();
    schema.put("type", "object");

    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("bus", Map.of(
            "type", "integer",
            "description", "I2C bus number (typically 0 or 1 on Raspberry Pi)"
    ));
    properties.put("address", Map.of(
            "type", "string",
            "description", "I2C device address in hex format (e.g., 0x48)"
    ));
    properties.put("register", Map.of(
            "type", "string",
            "description", "Register address to read from in hex format (e.g., 0x00)"
    ));
    schema.put("properties", properties);
    schema.put("required", List.of("bus", "address", "register"));

    return new McpTool(
            TOOL_I2CGET,
            "Read a byte from an I2C device register. Returns the value in hex format.",
            schema
    );
  }

  private McpTool createI2csetTool() {
    Map<String, Object> schema = new LinkedHashMap<>();
    schema.put("type", "object");

    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put("bus", Map.of(
            "type", "integer",
            "description", "I2C bus number (typically 0 or 1 on Raspberry Pi)"
    ));
    properties.put("address", Map.of(
            "type", "string",
            "description", "I2C device address in hex format (e.g., 0x48)"
    ));
    properties.put("register", Map.of(
            "type", "string",
            "description", "Register address to write to in hex format (e.g., 0x00)"
    ));
    properties.put("value", Map.of(
            "type", "string",
            "description", "Value to write in hex format (e.g., 0xFF)"
    ));
    schema.put("properties", properties);
    schema.put("required", List.of("bus", "address", "register", "value"));

    return new McpTool(
            TOOL_I2CSET,
            "Write a byte to an I2C device register.",
            schema
    );
  }
}
