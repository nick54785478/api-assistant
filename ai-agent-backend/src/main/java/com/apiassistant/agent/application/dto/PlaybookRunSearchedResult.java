package com.apiassistant.agent.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class PlaybookRunSearchedResult {
    private final String runId;
    private final String playbookId;
    private final String status;
    private final int totalSteps;
    private final Instant startedAt;
    private final List<PlaybookExecutionLogSearchedResult> logs;
}
