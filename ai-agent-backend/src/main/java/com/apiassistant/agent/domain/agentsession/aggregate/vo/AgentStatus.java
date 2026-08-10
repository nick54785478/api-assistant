package com.apiassistant.agent.domain.agentsession.aggregate.vo;

/**
 * Value Object representing the status of an AgentSession.
 */
public enum AgentStatus {
    /** 活躍狀態，可接收新的對話或指令 */
    ACTIVE,
    
    /** 處理中狀態，表示 AI 正在思考或執行動作 */
    PROCESSING,
    
    /** 關閉狀態，會話已結束，不可再進行互動 */
    CLOSED
}
