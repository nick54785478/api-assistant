package com.apiassistant.agent.application.assembler;

import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;

import java.util.List;
import java.util.stream.Collectors;

public class PlaybookDtoAssembler {
    
    public static PlaybookGottenResult toResult(Playbook playbook) {
        if (playbook == null) {
            return null;
        }
        
        List<PlaybookGottenResult.PlaybookStepResult> stepResults = playbook.getSteps().stream()
                .map(step -> new PlaybookGottenResult.PlaybookStepResult(
                        step.getName(),
                        step.getDescription(),
                        step.getRequiredTool(),
                        step.getResponseInstructions(),
                        step.getCustomInputs() != null ? step.getCustomInputs().stream().map(p -> new PlaybookGottenResult.StepParameterResult(
                                p.getType() != null ? p.getType().name() : null,
                                p.getKey(),
                                p.getValue()
                        )).collect(Collectors.toList()) : null
                ))
                .collect(Collectors.toList());
                
        return new PlaybookGottenResult(
                playbook.getId(),
                playbook.getAgentSessionId(),
                playbook.getName(),
                playbook.getDescription(),
                stepResults,
                playbook.getCreatedAt(),
                playbook.getUpdatedAt()
        );
    }
}
