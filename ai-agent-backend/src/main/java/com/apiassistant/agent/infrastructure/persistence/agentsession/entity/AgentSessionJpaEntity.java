package com.apiassistant.agent.infrastructure.persistence.agentsession.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA Entity representing the persistent state of an AgentSession.
 * Naming Rule: ... + JpaEntity
 */
@Entity
@Table(name = "agent_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionJpaEntity {
    
    @Id
    private String id;
    
    private String name;
    
    private String username;
    
    private String status;
    
    private Instant createdAt;
    
    private String playbookId;
    
    @jakarta.persistence.Column(columnDefinition = "integer default 0 not null")
    private int currentStepIndex;
}
