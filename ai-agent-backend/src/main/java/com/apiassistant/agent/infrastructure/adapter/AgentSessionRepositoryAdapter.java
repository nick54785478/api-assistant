package com.apiassistant.agent.infrastructure.adapter;

import com.apiassistant.agent.application.port.out.AgentSessionRepositoryPort;
import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.AgentStatus;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import com.apiassistant.agent.infrastructure.persistence.agentsession.entity.AgentSessionJpaEntity;
import com.apiassistant.agent.infrastructure.persistence.agentsession.repository.AgentSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.Cacheable;

/**
 * Outbound Adapter implementing the AgentSessionRepositoryPort.
 * Visibility: package-private to prevent outer layers from directly instantiating this class.
 */
@Component
@RequiredArgsConstructor
class AgentSessionRepositoryAdapter implements AgentSessionRepositoryPort {

    private final AgentSessionJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void save(AgentSession session) {
        AgentSessionJpaEntity entity = new AgentSessionJpaEntity(
                session.getId().getValue(),
                session.getName(),
                session.getUsername(),
                session.getStatus().name(),
                session.getCreatedAt(),
                session.getPlaybookId(),
                session.getCurrentStepIndex(),
                session.getCurrentRunId()
        );
        jpaRepository.save(entity);
        
        for (Object event : session.pullDomainEvents()) {
            eventPublisher.publishEvent(event);
        }
    }

    @Override
    @Cacheable(value = "agentSessions", key = "#id.value", unless = "#result == null")
    public Optional<AgentSession> findById(SessionId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public List<AgentSession> findByUsername(String username) {
        return jpaRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(this::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }
    
    private AgentSession toDomain(AgentSessionJpaEntity entity) {
        return AgentSession.restore(
                SessionId.of(entity.getId()),
                entity.getName(),
                entity.getUsername(),
                AgentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getPlaybookId(),
                entity.getCurrentStepIndex(),
                entity.getCurrentRunId()
        );
    }
}
