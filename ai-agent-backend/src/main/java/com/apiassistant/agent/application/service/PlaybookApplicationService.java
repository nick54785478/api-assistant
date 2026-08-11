package com.apiassistant.agent.application.service;

import com.apiassistant.agent.application.assembler.PlaybookDtoAssembler;
import com.apiassistant.agent.application.dto.PlaybookGottenResult;
import com.apiassistant.agent.application.command.CreatePlaybookCommand;
import com.apiassistant.agent.application.command.UpdatePlaybookCommand;
import com.apiassistant.agent.application.port.in.CreatePlaybookUseCase;
import com.apiassistant.agent.application.port.in.GetPlaybookUseCase;
import com.apiassistant.agent.application.port.in.ListPlaybooksUseCase;
import com.apiassistant.agent.application.port.in.UpdatePlaybookUseCase;
import com.apiassistant.agent.application.port.out.PlaybookRepositoryPort;
import com.apiassistant.agent.domain.playbook.aggregate.entity.PlaybookStep;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class PlaybookApplicationService implements CreatePlaybookUseCase, ListPlaybooksUseCase, GetPlaybookUseCase, UpdatePlaybookUseCase {

    private final PlaybookRepositoryPort playbookRepositoryPort;

    @Override
    public PlaybookGottenResult execute(CreatePlaybookCommand command) {
        List<PlaybookStep> steps = command.steps().stream()
                .map(step -> new PlaybookStep(
                        step.name(),
                        step.description(),
                        step.requiredTool(),
                        step.responseInstructions(),
                        step.customInputs() != null ? step.customInputs().stream().map(c -> new com.apiassistant.agent.domain.playbook.aggregate.vo.StepParameter(
                                c.type() != null ? com.apiassistant.agent.domain.playbook.aggregate.vo.ParameterType.valueOf(c.type()) : null,
                                c.key(),
                                c.value()
                        )).collect(Collectors.toList()) : new java.util.ArrayList<>()
                ))
                .collect(Collectors.toList());

        Playbook playbook = Playbook.create(
                command.agentSessionId(),
                command.name(),
                command.description(),
                steps
        );
        playbookRepositoryPort.save(playbook);
        return PlaybookDtoAssembler.toResult(playbook);
    }

    @Override
    public Optional<PlaybookGottenResult> execute(String id) {
        return playbookRepositoryPort.findById(id).map(PlaybookDtoAssembler::toResult);
    }

    @Override
    public List<PlaybookGottenResult> execute() {
        return playbookRepositoryPort.findAll().stream()
                .map(PlaybookDtoAssembler::toResult)
                .collect(Collectors.toList());
    }

    @Override
    public PlaybookGottenResult execute(UpdatePlaybookCommand command) {
        Playbook playbook = playbookRepositoryPort.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Playbook not found: " + command.id()));
        
        List<PlaybookStep> steps = command.steps().stream()
                .map(step -> new PlaybookStep(
                        step.name(),
                        step.description(),
                        step.requiredTool(),
                        step.responseInstructions(),
                        step.customInputs() != null ? step.customInputs().stream().map(c -> new com.apiassistant.agent.domain.playbook.aggregate.vo.StepParameter(
                                c.type() != null ? com.apiassistant.agent.domain.playbook.aggregate.vo.ParameterType.valueOf(c.type()) : null,
                                c.key(),
                                c.value()
                        )).collect(Collectors.toList()) : new java.util.ArrayList<>()
                ))
                .collect(Collectors.toList());
                
        playbook.update(command.agentSessionId(), command.name(), command.description(), steps);
        playbookRepositoryPort.save(playbook);
        return PlaybookDtoAssembler.toResult(playbook);
    }
}
