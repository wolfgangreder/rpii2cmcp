package at.reder.rpii2cmcp.resource;

import at.reder.rpii2cmcp.model.I2CCommand;
import at.reder.rpii2cmcp.model.I2CResponse;
import at.reder.rpii2cmcp.service.I2CService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST resource for I2C command execution.
 * Provides endpoints for executing I2C read and write operations via MCP protocol.
 * 
 * @author Wolfgang Reder
 * @version 1.0.0
 */
@Path("/api/i2c")
@Tag(name = "I2C Commands", description = "I2C command execution endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class I2CResource {

    private static final Logger LOG = Logger.getLogger(I2CResource.class);

    @Inject
    I2CService i2cService;

    /**
     * Executes an I2C command (read or write).
     *
     * @param command the I2C command to execute
     * @return HTTP response with the command result
     */
    @POST
    @Path("/execute")
    @Operation(summary = "Execute I2C command", 
               description = "Executes an I2C read or write command on the specified bus and device")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Command executed successfully",
                     content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                      schema = @Schema(implementation = I2CResponse.class))),
        @APIResponse(responseCode = "400", description = "Invalid command parameters"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response executeCommand(
            @RequestBody(description = "I2C command to execute", required = true,
                        content = @Content(schema = @Schema(implementation = I2CCommand.class)))
            I2CCommand command) {
        
        LOG.infof("Received I2C command: operation=%s, bus=%d", 
                  command.getOperation(), command.getBus());
        
        try {
            I2CResponse response = i2cService.executeCommand(command);
            
            if (response.isSuccess()) {
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
            }
        } catch (IllegalArgumentException e) {
            LOG.error("Invalid command", e);
            I2CResponse errorResponse = new I2CResponse(false, null, e.getMessage(), "");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
        } catch (Exception e) {
            LOG.error("Error executing command", e);
            I2CResponse errorResponse = new I2CResponse(false, null, 
                    "Internal server error: " + e.getMessage(), "");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse).build();
        }
    }
}
