package com.apiassistant.agent.infrastructure.persistence.playbook.repository;

import com.apiassistant.agent.infrastructure.persistence.playbook.entity.PlaybookJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaybookJpaRepository extends JpaRepository<PlaybookJpaEntity, String> {
}
