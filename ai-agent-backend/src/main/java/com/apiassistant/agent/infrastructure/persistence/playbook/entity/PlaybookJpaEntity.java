package com.apiassistant.agent.infrastructure.persistence.playbook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playbooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaybookJpaEntity {
    
    @Id
    private String id;
    
    @Column(name = "agent_session_id")
    private String agentSessionId;
    
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private Instant createdAt;
    
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "playbook", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("stepIndex ASC")
    private List<PlaybookStepJpaEntity> steps = new ArrayList<>();
}
