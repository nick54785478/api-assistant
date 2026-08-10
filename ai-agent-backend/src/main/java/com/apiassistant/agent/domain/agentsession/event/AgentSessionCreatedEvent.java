package com.apiassistant.agent.domain.agentsession.event;

import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import lombok.Value;

import java.time.Instant;

/**
 * Domain Event triggered when a new AgentSession is created.
 */
@Value
public class AgentSessionCreatedEvent {
    SessionId sessionId;
    String username;
    String initialMessage;
    Instant occurredOn;

    public AgentSessionCreatedEvent(SessionId sessionId, String username, String initialMessage) {
        this.sessionId = sessionId;
        this.username = username;
        this.initialMessage = initialMessage;
        this.occurredOn = Instant.now();
    }
}
