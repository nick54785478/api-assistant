package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;

import java.util.List;
import java.util.Optional;

/**
 * Outbound Port for persisting and retrieving Playbook aggregates.
 * 負責持久化與存取 Playbook 聚合根的 Outbound Port。
 */
public interface PlaybookRepositoryPort {
    void save(Playbook playbook);
    Optional<Playbook> findById(String id);
    List<Playbook> findAll();
}
