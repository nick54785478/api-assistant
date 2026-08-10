package com.apiassistant.agent.presentation.resource.in;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Request payload for updating an existing playbook")
public record UpdatePlaybookResource(
        @Schema(description = "Agent Session ID this playbook belongs to", example = "session-123")
        String agentSessionId,
        
        @Schema(description = "Playbook name", example = "Customer Support SOP")
        String name,
        
        @Schema(description = "Playbook description", example = "Standard operating procedure for handling customer support tickets")
        String description,
        
        @Schema(description = "Ordered list of steps in the playbook")
        List<PlaybookStepResource> steps
) {
    public record PlaybookStepResource(
            @Schema(description = "Step name", example = "Gather Information")
            String name,
            
            @Schema(description = "Description of what this step does", example = "Ask the customer for their order ID.")
            String description,
            
            @Schema(description = "Tool name required for this step (optional)", example = "search_order_mcp_tool")
            String requiredTool,
            
            @Schema(description = "Instructions for how the AI should respond after this step", example = "Format the order details in a markdown table.")
            String responseInstructions,
            
            @Schema(description = "Custom input variables or context for this step")
            java.util.List<StepParameterResource> customInputs
    ) {}

    public record StepParameterResource(
            @Schema(description = "Type of the parameter", example = "REQUEST_BODY")
            String type,
            @Schema(description = "Key of the parameter", example = "username")
            String key,
            @Schema(description = "Value of the parameter", example = "admin")
            String value
    ) {}
}
