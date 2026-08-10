package com.apiassistant.agent.infrastructure.persistence.agentsession.repository;

import com.apiassistant.agent.infrastructure.persistence.agentsession.entity.AgentSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for AgentSessionJpaEntity.
 */
@Repository
public interface AgentSessionJpaRepository extends JpaRepository<AgentSessionJpaEntity, String> {

    List<AgentSessionJpaEntity> findByUsernameOrderByCreatedAtDesc(String username);
}
