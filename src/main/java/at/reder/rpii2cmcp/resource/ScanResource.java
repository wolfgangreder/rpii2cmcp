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

import at.reder.rpii2cmcp.model.mcp.McpTool;
import at.reder.rpii2cmcp.model.mcp.McpToolCall;
import at.reder.rpii2cmcp.model.mcp.McpToolResult;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * Scan API endpoints for Copilot MCP server discovery. Provides alternative paths under /api/scan that mirror the /tools
 * endpoints for compatibility with Copilot's MCP server scanning mechanism.
 *
 * @author Wolfgang Reder
 * @version 1.0.1
 */
@Path("/api/scan")
@Tag(name = "Scan API", description = "Copilot MCP server discovery endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScanResource {

  private static final Logger LOG = Logger.getLogger(ScanResource.class);

  @Inject
  I2CResource i2cResource;

  /**
   * Lists all available MCP tools. This endpoint mirrors /tools/list for Copilot compatibility.
   *
   * @return list of available tools with their schemas
   */
  @GET
  @Path("/tools")
  @Operation(summary = "List available tools",
          description = "Returns a list of all available MCP tools with their input schemas. "
          + "This endpoint mirrors /tools/list for Copilot MCP server scanning compatibility.")
  @APIResponses(value = {
    @APIResponse(responseCode = "200", description = "Tools listed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = McpTool.class)))
  })
  public List<McpTool> getTools()
  {
    LOG.info("Scan API: Listing available MCP tools");
    return i2cResource.getTools();
  }

  /**
   * Executes an MCP tool call. This endpoint mirrors /tools/call for Copilot compatibility.
   *
   * @param toolCall the tool call request containing tool name and arguments
   * @return the result of the tool execution
   */
  @POST
  @Path("/tools")
  @Operation(summary = "Execute MCP tool",
          description = "Executes an MCP tool with the provided arguments. "
          + "This endpoint mirrors /tools/call for Copilot MCP server scanning compatibility.")
  @APIResponses(value = {
    @APIResponse(responseCode = "200", description = "Tool executed successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = McpToolResult.class))),
    @APIResponse(responseCode = "400", description = "Invalid tool call parameters"),
    @APIResponse(responseCode = "500", description = "Internal server error")
  })
  public McpToolResult callTool(
          @RequestBody(description = "MCP tool call request", required = true,
                  content = @Content(schema = @Schema(implementation = McpToolCall.class))) McpToolCall toolCall)
  {
    LOG.infof("Scan API: Received MCP tool call: name=%s", toolCall.getName());
    return i2cResource.callTool(toolCall);
  }

  /**
   * Returns server information for MCP discovery.
   *
   * @return server information
   */
  @GET
  @Path("/info")
  @Operation(summary = "Get server info",
          description = "Returns server information for MCP discovery")
  @APIResponses(value = {
    @APIResponse(responseCode = "200", description = "Server info returned successfully")
  })
  public ServerInfo getServerInfo()
  {
    LOG.info("Scan API: Returning server info");
    return new ServerInfo();
  }

  /**
   * Server information for MCP discovery.
   */
  public static class ServerInfo {

    private final String name = "rpii2cmcp";
    private final String version = "1.0.1";
    private final String description = "MCP Server for executing I2C commands on Raspberry Pi 5+";
    private final String protocol = "mcp";

    public String getName()
    {
      return name;
    }

    public String getVersion()
    {
      return version;
    }

    public String getDescription()
    {
      return description;
    }

    public String getProtocol()
    {
      return protocol;
    }
  }
}
