package com.apiassistant.agent.domain.agentsession.event;

import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain Event triggered when a new AgentSession is created.
 */
public final class AgentSessionCreatedEvent {
    private final SessionId sessionId;
    private final String username;
    private final String initialMessage;
    private final Instant occurredOn;

    public AgentSessionCreatedEvent(SessionId sessionId, String username, String initialMessage) {
        this.sessionId = sessionId;
        this.username = username;
        this.initialMessage = initialMessage;
        this.occurredOn = Instant.now();
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public String getInitialMessage() {
        return initialMessage;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentSessionCreatedEvent that = (AgentSessionCreatedEvent) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(initialMessage, that.initialMessage) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, username, initialMessage, occurredOn);
    }

    @Override
    public String toString() {
        return "AgentSessionCreatedEvent{" +
                "sessionId=" + sessionId +
                ", username='" + username + '\'' +
                ", initialMessage='" + initialMessage + '\'' +
                ", occurredOn=" + occurredOn +
                '}';
    }
}
