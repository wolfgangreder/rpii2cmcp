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
package at.reder.rpii2cmcp.model.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the result of an MCP tool call.
 *
 * @author Wolfgang Reder
 * @version 1.0.0
 */
public class McpToolResult {

    @JsonProperty("content")
    private List<McpContent> content;

    @JsonProperty("isError")
    private boolean isError;

    public McpToolResult() {
    }

    public McpToolResult(List<McpContent> content, boolean isError) {
        this.content = content;
        this.isError = isError;
    }

    public static McpToolResult success(String text) {
        return new McpToolResult(List.of(new McpContent("text", text)), false);
    }

    public static McpToolResult error(String text) {
        return new McpToolResult(List.of(new McpContent("text", text)), true);
    }

    public List<McpContent> getContent() {
        return content;
    }

    public void setContent(List<McpContent> content) {
        this.content = content;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }
}
