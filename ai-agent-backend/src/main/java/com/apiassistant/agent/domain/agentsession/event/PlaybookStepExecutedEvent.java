package com.apiassistant.agent.domain.agentsession.event;

import java.time.Instant;

/**
 * Domain event representing the execution result of a playbook step.
 */
public record PlaybookStepExecutedEvent(
        String sessionId,
        String playbookId,
        String runId,
        Integer stepIndex,
        String status, // "SUCCESS" or "FAILED"
        String errorMessage,
        String aiMessage,
        Instant occurredAt
) {
    public PlaybookStepExecutedEvent {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        if (playbookId == null || playbookId.isBlank()) {
            throw new IllegalArgumentException("Playbook ID cannot be null or blank");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
