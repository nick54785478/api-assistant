package com.apiassistant.agent.presentation.resource.in;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request Resource for creating an AgentSession.
 * Naming Rule: V + N + Resource (CreateAgentSessionResource)
 * Strict Rule: Pure data carrier, no domain logic or transformation methods.
 */
@Data
@Schema(description = "建立會話的請求資源")
public class CreateAgentSessionResource {
    @Schema(description = "使用者名稱", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;
    
    @Schema(description = "初始訊息 (可選)", example = "你好，請幫我查詢資料。")
    private String initialMessage;
}
