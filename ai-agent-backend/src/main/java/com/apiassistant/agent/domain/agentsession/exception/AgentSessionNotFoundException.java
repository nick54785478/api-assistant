package com.apiassistant.agent.domain.agentsession.exception;

import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;

/**
 * Domain exception thrown when an AgentSession is not found.
 */
public class AgentSessionNotFoundException extends RuntimeException {
    public AgentSessionNotFoundException(SessionId id) {
        super("Agent session not found with ID: " + id.getValue());
    }
}
