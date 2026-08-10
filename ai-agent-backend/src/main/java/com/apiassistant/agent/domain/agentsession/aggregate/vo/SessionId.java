package com.apiassistant.agent.domain.agentsession.aggregate.vo;

import lombok.Value;
import java.util.UUID;

/**
 * Value Object representing the unique identifier for an AgentSession.
 */
@Value
public class SessionId {
    /** 儲存實際的 Session ID 字串值 */
    String value;

    /**
     * 隨機生成一個新的 SessionId。
     *
     * @return 新生成的 SessionId 實例
     */
    public static SessionId generate() {
        return new SessionId(UUID.randomUUID().toString());
    }

    /**
     * 根據已有的字串值建立對應的 SessionId 實例。
     *
     * @param value 既有的 Session ID 字串
     * @return 建立的 SessionId 實例
     * @throws IllegalArgumentException 當輸入值為空時拋出
     */
    public static SessionId of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("SessionId cannot be null or empty");
        }
        return new SessionId(value);
    }
}
