package com.apiassistant.agent.application.dto;

import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
public class PlaybookGottenResult {
    private String id;
    private String agentSessionId;
    String name;
    String description;
    List<PlaybookStepResult> steps;
    Instant createdAt;
    Instant updatedAt;

    @Value
    public static class PlaybookStepResult {
        String name;
        String description;
        String requiredTool;
        String responseInstructions;
        List<StepParameterResult> customInputs;
    }

    @Value
    public static class StepParameterResult {
        String type;
        String key;
        String value;
    }
}
