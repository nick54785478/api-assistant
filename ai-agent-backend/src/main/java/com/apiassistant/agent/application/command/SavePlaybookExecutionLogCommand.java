package com.apiassistant.agent.application.command;

import java.time.Instant;

public record SavePlaybookExecutionLogCommand(
        String sessionId,
        String playbookId,
        String runId,
        Integer stepIndex,
        String status,
        String errorMessage,
        String detailMessage,
        Instant occurredAt
) {
}
