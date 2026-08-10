package com.apiassistant.agent.infrastructure.persistence.playbook.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "playbook_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaybookStepJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playbook_id", nullable = false)
    private PlaybookJpaEntity playbook;
    
    private int stepIndex;
    
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String requiredTool;
    
    @Column(length = 2000)
    private String responseInstructions;
    
    @Column(length = 4000)
    private String customInputs;
}
