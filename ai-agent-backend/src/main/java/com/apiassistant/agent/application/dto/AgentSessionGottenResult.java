package com.apiassistant.agent.application.dto;

import lombok.Value;
import java.time.Instant;

/**
 * DTO for the AgentSession result.
 * Naming Rule: N + Gotten/Searched + Result (AgentSession + Gotten + Result)
 */
@Value
public class AgentSessionGottenResult {
    String sessionId;
    String name;
    String status;
    Instant createdAt;
    String playbookId;
}
