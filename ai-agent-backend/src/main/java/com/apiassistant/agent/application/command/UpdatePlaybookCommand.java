package com.apiassistant.agent.application.command;

import java.util.List;

public record UpdatePlaybookCommand(
        String id,
        String agentSessionId,
        String name,
        String description,
        List<PlaybookStepCommand> steps
) {
    public record PlaybookStepCommand(
            String name,
            String description,
            String requiredTool,
            String responseInstructions,
            List<StepParameterCommand> customInputs
    ) {}

    public record StepParameterCommand(
            String type,
            String key,
            String value
    ) {}
}
