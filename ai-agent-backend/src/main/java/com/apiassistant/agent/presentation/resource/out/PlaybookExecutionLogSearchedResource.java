package com.apiassistant.agent.presentation.resource.out;

import java.time.Instant;
import java.util.UUID;

public record PlaybookExecutionLogSearchedResource(
        UUID id,
        String sessionId,
        String playbookId,
        Integer stepIndex,
        String status,
        String errorMessage,
        Instant createdAt
) {
}
