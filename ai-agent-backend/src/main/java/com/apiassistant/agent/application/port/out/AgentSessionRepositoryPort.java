package com.apiassistant.agent.application.port.out;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import java.util.List;
import java.util.Optional;

/**
 * Outbound Port for persisting and retrieving Agent Sessions.
 * 負責持久化與存取 Agent Session 的 Outbound Port。
 * 
 * Strict Rule: Port methods MUST NOT accept or return technology-specific objects (like JPA entities).
 */
public interface AgentSessionRepositoryPort {
    
    /**
     * Save an AgentSession aggregate.
     * @param session The aggregate to save.
     */
    void save(AgentSession session);
    
    /**
     * Find an AgentSession by its SessionId.
     * @param id The unique session id.
     * @return Optional containing the session if found.
     */
    Optional<AgentSession> findById(SessionId id);

    /**
     * Find all AgentSessions for a specific username.
     * @param username The username of the owner.
     * @return List of AgentSessions.
     */
    List<AgentSession> findByUsername(String username);
}
