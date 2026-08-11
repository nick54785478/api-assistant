package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.PlaybookDtoAssembler;
import com.apiassistant.agent.application.command.ClonePlaybookCommand;
import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.port.in.ClonePlaybookUseCase;
import com.apiassistant.agent.application.port.out.PlaybookRepositoryPort;
import com.apiassistant.agent.domain.playbook.aggregate.entity.PlaybookStep;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ClonePlaybookService implements ClonePlaybookUseCase {

    private final PlaybookRepositoryPort playbookRepository;

    @Override
    @Transactional
    public PlaybookGottenResult execute(ClonePlaybookCommand command) {
        // Fetch source playbook
        Playbook sourcePlaybook = playbookRepository.findById(command.getPlaybookId())
                .orElseThrow(() -> new IllegalArgumentException("Playbook not found: " + command.getPlaybookId()));

        // Deep copy steps
        List<PlaybookStep> clonedSteps = sourcePlaybook.getSteps().stream()
                .map(step -> PlaybookStep.builder()
                        .name(step.getName())
                        .description(step.getDescription())
                        .requiredTool(step.getRequiredTool())
                        .responseInstructions(step.getResponseInstructions())
                        .customInputs(step.getCustomInputs())
                        .build())
                .collect(Collectors.toList());

        // Create new playbook
        Playbook newPlaybook = Playbook.create(
                command.getTargetAgentSessionId(),
                sourcePlaybook.getName() + " - Copy",
                sourcePlaybook.getDescription(),
                clonedSteps
        );

        // Save new playbook
        playbookRepository.save(newPlaybook);

        // Map to result DTO
        return new PlaybookGottenResult(
                newPlaybook.getId(),
                newPlaybook.getAgentSessionId(),
                newPlaybook.getName(),
                newPlaybook.getDescription(),
                newPlaybook.getSteps().stream()
                        .map(s -> new PlaybookGottenResult.PlaybookStepResult(
                                s.getName(),
                                s.getDescription(),
                                s.getRequiredTool(),
                                s.getResponseInstructions(),
                                s.getCustomInputs() != null ? s.getCustomInputs().stream().map(p -> new PlaybookGottenResult.StepParameterResult(
                                        p.getType() != null ? p.getType().name() : null,
                                        p.getKey(),
                                        p.getValue()
                                )).collect(Collectors.toList()) : null
                        ))
                        .collect(Collectors.toList()),
                newPlaybook.getCreatedAt(),
                newPlaybook.getUpdatedAt()
        );
    }
}
