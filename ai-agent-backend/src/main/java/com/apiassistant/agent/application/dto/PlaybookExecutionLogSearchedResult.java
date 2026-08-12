package com.apiassistant.agent.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaybookExecutionLogSearchedResult {
    private final UUID id;
    private final String sessionId;
    private final String playbookId;
    private final String runId;
    private final Integer stepIndex;
    private final String status;
    private final String errorMessage;
    private final String detailMessage;
    private final Instant createdAt;
}
