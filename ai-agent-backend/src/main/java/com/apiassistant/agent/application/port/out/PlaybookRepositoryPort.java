package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;

import java.util.List;
import java.util.Optional;

public interface PlaybookRepositoryPort {
    void save(Playbook playbook);
    Optional<Playbook> findById(String id);
    List<Playbook> findAll();
}
