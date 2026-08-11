package com.apiassistant.agent.domain.agentsession.aggregate.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing the unique identifier for an AgentSession.
 */
public final class SessionId {
    /** 內部實際的 Session ID 字串。 */
    private final String value;

    public SessionId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 產生一個全新的 SessionId。
     *
     * @return 隨機產生的 SessionId 實體
     */
    public static SessionId generate() {
        return new SessionId(UUID.randomUUID().toString());
    }

    /**
     * 從已有的字串值建立一個 SessionId 實體。
     *
     * @param value 已知的 Session ID 字串
     * @return 建立的 SessionId 實體
     * @throws IllegalArgumentException 若輸入值為空則拋出
     */
    public static SessionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
        return new SessionId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionId sessionId = (SessionId) o;
        return Objects.equals(value, sessionId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "value='" + value + '\'' +
                '}';
    }
}
