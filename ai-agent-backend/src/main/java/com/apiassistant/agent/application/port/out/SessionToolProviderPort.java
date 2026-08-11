package com.apiassistant.agent.application.port.out;

import java.util.List;

/**
 * Outbound Port for retrieving the list of allowed tools (e.g., MCP tools) for a specific session from an external service.
 * 負責向外部服務獲取特定 Session 所允許使用的工具 (例如 MCP tools) 清單的 Outbound Port。
 */
public interface SessionToolProviderPort {
    /**
     * Gets the list of tool names allowed for the specified session.
     * 
     * @param sessionId Session ID
     * @return List of tool names
     */
    List<String> getSessionToolNames(String sessionId);
}
