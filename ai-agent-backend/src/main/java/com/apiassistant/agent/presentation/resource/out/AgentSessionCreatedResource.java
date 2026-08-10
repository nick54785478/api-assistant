package com.apiassistant.agent.presentation.resource.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response Resource after an AgentSession is created.
 * Naming Rule: N + Ved + Resource (AgentSessionCreatedResource)
 * Strict Rule: Pure data carrier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "會話建立後的回應資源 (亦用於列表與查詢)")
public class AgentSessionCreatedResource {
    @Schema(description = "會話 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;
    
    @Schema(description = "會話名稱", example = "測試會話")
    private String name;
    
    @Schema(description = "會話狀態", example = "ACTIVE")
    private String status;
    
    @Schema(description = "建立時間")
    private Instant createdAt;

    @Schema(description = "綁定的劇本 ID (可選)")
    private String playbookId;
}
