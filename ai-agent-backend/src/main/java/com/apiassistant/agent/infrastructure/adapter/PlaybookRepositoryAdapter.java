package com.apiassistant.agent.infrastructure.adapter;

import com.apiassistant.agent.application.port.out.PlaybookRepositoryPort;
import com.apiassistant.agent.domain.playbook.aggregate.entity.PlaybookStep;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import com.apiassistant.agent.infrastructure.persistence.playbook.entity.PlaybookJpaEntity;
import com.apiassistant.agent.infrastructure.persistence.playbook.entity.PlaybookStepJpaEntity;
import com.apiassistant.agent.infrastructure.persistence.playbook.repository.PlaybookJpaRepository;
import com.apiassistant.agent.domain.playbook.aggregate.vo.StepParameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

@Component
@RequiredArgsConstructor
class PlaybookRepositoryAdapter implements PlaybookRepositoryPort {

    private final PlaybookJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Playbook playbook) {
        PlaybookJpaEntity entity = new PlaybookJpaEntity();
        entity.setId(playbook.getId());
        entity.setAgentSessionId(playbook.getAgentSessionId());
        entity.setName(playbook.getName());
        entity.setDescription(playbook.getDescription());
        entity.setCreatedAt(playbook.getCreatedAt());
        entity.setUpdatedAt(playbook.getUpdatedAt());
        
        List<PlaybookStepJpaEntity> stepEntities = playbook.getSteps().stream().map(step -> {
            PlaybookStepJpaEntity stepEntity = new PlaybookStepJpaEntity();
            stepEntity.setPlaybook(entity);
            stepEntity.setStepIndex(playbook.getSteps().indexOf(step));
            stepEntity.setName(step.getName());
            stepEntity.setDescription(step.getDescription());
            stepEntity.setRequiredTool(step.getRequiredTool());
            stepEntity.setResponseInstructions(step.getResponseInstructions());
            try {
                stepEntity.setCustomInputs(objectMapper.writeValueAsString(step.getCustomInputs()));
            } catch (JsonProcessingException e) {
                stepEntity.setCustomInputs("[]");
            }
            return stepEntity;
        }).collect(Collectors.toList());
        
        entity.setSteps(stepEntities);
        jpaRepository.save(entity);
    }

    @Override
    @Cacheable(value = "playbooks", key = "#id", unless = "#result == null")
    public Optional<Playbook> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Playbook> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    private Playbook toDomain(PlaybookJpaEntity entity) {
        List<PlaybookStep> steps = entity.getSteps().stream()
                .map(stepEntity -> new PlaybookStep(
                        stepEntity.getName(),
                        stepEntity.getDescription(),
                        stepEntity.getRequiredTool(),
                        stepEntity.getResponseInstructions(),
                        parseCustomInputs(stepEntity.getCustomInputs())
                ))
                .collect(Collectors.toList());
                
        return new Playbook(
                entity.getId(),
                entity.getAgentSessionId(),
                entity.getName(),
                entity.getDescription(),
                steps,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private List<StepParameter> parseCustomInputs(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<StepParameter>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
