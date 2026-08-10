package com.apiassistant.agent.presentation.resource.out;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Response payload for a Playbook")
public record PlaybookResource(
        @Schema(description = "Playbook ID", example = "pb-1234")
        String id,
        
        @Schema(description = "Agent Session ID this playbook belongs to", example = "session-123")
        String agentSessionId,
        
        @Schema(description = "Playbook name", example = "Customer Support SOP")
        String name,
        
        @Schema(description = "Playbook description")
        String description,
        
        @Schema(description = "Ordered list of steps in the playbook")
        List<PlaybookStepResource> steps,
        
        @Schema(description = "Creation timestamp")
        Instant createdAt,
        
        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public record PlaybookStepResource(
            String name,
            String description,
            String requiredTool,
            String responseInstructions,
            java.util.List<StepParameterResource> customInputs
    ) {}

    public record StepParameterResource(
            String type,
            String key,
            String value
    ) {}
}
