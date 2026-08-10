package com.apiassistant.agent.presentation.assembler;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.command.CreatePlaybookCommand;
import com.apiassistant.agent.application.command.CreatePlaybookCommand.PlaybookStepCommand;
import com.apiassistant.agent.application.command.UpdatePlaybookCommand;
import com.apiassistant.agent.presentation.resource.in.CreatePlaybookResource;
import com.apiassistant.agent.presentation.resource.out.PlaybookResource;
import com.apiassistant.agent.presentation.resource.out.PlaybookResource.PlaybookStepResource;

import java.util.List;
import java.util.stream.Collectors;

public class PlaybookResourceAssembler {

    public static CreatePlaybookCommand toCommand(CreatePlaybookResource resource) {
        List<PlaybookStepCommand> stepCommands = resource.steps() != null ? resource.steps().stream()
                .map(step -> new PlaybookStepCommand(
                        step.name(),
                        step.description(),
                        step.requiredTool(),
                        step.responseInstructions(),
                        step.customInputs() != null ? step.customInputs().stream().map(p -> new CreatePlaybookCommand.StepParameterCommand(
                                p.type(),
                                p.key(),
                                p.value()
                        )).collect(Collectors.toList()) : java.util.Collections.emptyList()
                ))
                .collect(Collectors.toList()) : java.util.Collections.emptyList();

        return new CreatePlaybookCommand(
                resource.agentSessionId(),
                resource.name(),
                resource.description(),
                stepCommands
        );
    }

    public static UpdatePlaybookCommand toCommand(String id, com.apiassistant.agent.presentation.resource.in.UpdatePlaybookResource resource) {
        List<UpdatePlaybookCommand.PlaybookStepCommand> stepCommands = resource.steps() != null ? resource.steps().stream()
                .map(step -> new UpdatePlaybookCommand.PlaybookStepCommand(
                        step.name(),
                        step.description(),
                        step.requiredTool(),
                        step.responseInstructions(),
                        step.customInputs() != null ? step.customInputs().stream().map(p -> new UpdatePlaybookCommand.StepParameterCommand(
                                p.type(),
                                p.key(),
                                p.value()
                        )).collect(Collectors.toList()) : java.util.Collections.emptyList()
                ))
                .collect(Collectors.toList()) : java.util.Collections.emptyList();

        return new UpdatePlaybookCommand(
                id,
                resource.agentSessionId(),
                resource.name(),
                resource.description(),
                stepCommands
        );
    }

    public static PlaybookResource toResource(PlaybookGottenResult result) {
        if (result == null) {
            return null;
        }

        List<PlaybookStepResource> steps = result.getSteps().stream()
                .map(step -> new PlaybookStepResource(
                        step.getName(),
                        step.getDescription(),
                        step.getRequiredTool(),
                        step.getResponseInstructions(),
                        step.getCustomInputs() != null ? step.getCustomInputs().stream().map(p -> new PlaybookResource.StepParameterResource(
                                p.getType(),
                                p.getKey(),
                                p.getValue()
                        )).collect(Collectors.toList()) : null
                ))
                .collect(Collectors.toList());

        return new PlaybookResource(
                result.getId(),
                result.getAgentSessionId(),
                result.getName(),
                result.getDescription(),
                steps,
                result.getCreatedAt(),
                result.getUpdatedAt()
        );
    }
}
