package com.apiassistant.agent.domain.agentsession.aggregate.root;

import com.apiassistant.agent.domain.agentsession.aggregate.vo.AgentStatus;
import com.apiassistant.agent.domain.agentsession.aggregate.vo.SessionId;
import com.apiassistant.agent.domain.agentsession.event.AgentSessionCreatedEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root representing a chat session with the AI Assistant.
 * Pure Java, no framework dependencies.
 */
@Getter
public class AgentSession {
    /**
     * 唯一識別碼
     */
    private final SessionId id;

    /**
     * 會話名稱
     */
    private String name;

    /**
     * 擁有此會話的使用者名稱
     */
    private final String username;

    /**
     * 會話的當前狀態 (ACTIVE, PROCESSING, CLOSED)
     */
    private AgentStatus status;

    /**
     * 會話建立時間
     */
    private final Instant createdAt;

    /**
     * 綁定的劇本 ID (若無則為 null)
     */
    private String playbookId;

    /**
     * 當前執行到的劇本步驟索引 (從 0 開始)
     */
    private int currentStepIndex;

    // Internal list to track domain events raised by this aggregate
    private final List<Object> domainEvents = new ArrayList<>();

    private AgentSession(SessionId id, String name, String username, AgentStatus status, Instant createdAt, String playbookId, int currentStepIndex) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.status = status;
        this.createdAt = createdAt;
        this.playbookId = playbookId;
        this.currentStepIndex = currentStepIndex;
    }

    /**
     * 建立新的 AgentSession 實體 (無綁定劇本)。
     *
     * @param username       使用者名稱
     * @param initialMessage 初始訊息 (用於自動產生會話名稱)
     * @return 新的 AgentSession 實體
     */
    public static AgentSession create(String username, String initialMessage) {
        SessionId newId = SessionId.generate();
        
        // Derive name from initial message (max 20 chars)
        String sessionName = (initialMessage != null && !initialMessage.isBlank()) 
                ? initialMessage.trim() 
                : "New Chat";
        if (sessionName.length() > 20) {
            sessionName = sessionName.substring(0, 20) + "...";
        }
        
        AgentSession session = new AgentSession(newId, sessionName, username, AgentStatus.ACTIVE, Instant.now(), null, 0);
        
        // Publish creation event
        session.domainEvents.add(new AgentSessionCreatedEvent(newId, username, initialMessage));
        
        return session;
    }

    /**
     * 從儲存庫 (Repository) 還原既有的 AgentSession 實體。
     * 此方法不會觸發任何 Domain Event。
     *
     * @param id               會話 ID
     * @param name             會話名稱
     * @param username         使用者名稱
     * @param status           當前狀態
     * @param createdAt        建立時間
     * @param playbookId       綁定的劇本 ID
     * @param currentStepIndex 當前執行到的劇本步驟索引
     * @return 還原後的 AgentSession 實體
     */
    public static AgentSession restore(SessionId id, String name, String username, AgentStatus status, Instant createdAt, String playbookId, int currentStepIndex) {
        return new AgentSession(id, name, username, status, createdAt, playbookId, currentStepIndex);
    }

    /**
     * 重新命名會話。
     *
     * @param newName 新名稱
     * @throws IllegalArgumentException 當新名稱為空時拋出
     * @throws IllegalStateException    當會話已關閉時拋出
     */
    public void rename(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Session name cannot be empty");
        }
        if (this.status == AgentStatus.CLOSED) {
            throw new IllegalStateException("Cannot rename a closed session");
        }
        this.name = newName.trim();
    }

    /**
     * 將會話狀態設定為處理中 (PROCESSING)。
     *
     * @throws IllegalStateException 當會話已關閉時拋出
     */
    public void processQuery() {
        if (this.status == AgentStatus.CLOSED) {
            throw new IllegalStateException("Cannot process query on a closed session.");
        }
        this.status = AgentStatus.PROCESSING;
    }

    /**
     * 綁定劇本。
     * 
     * @param playbookId 劇本 ID
     * @throws IllegalStateException 當會話已關閉時拋出
     */
    public void bindPlaybook(String playbookId) {
        if (this.status == AgentStatus.CLOSED) {
            throw new IllegalStateException("Cannot bind playbook to a closed session");
        }
        this.playbookId = playbookId;
        this.currentStepIndex = 0; // 重置步驟
    }

    /**
     * 解除綁定劇本。
     *
     * @throws IllegalStateException 當會話已關閉時拋出
     */
    public void unbindPlaybook() {
        if (this.status == AgentStatus.CLOSED) {
            throw new IllegalStateException("Cannot unbind playbook from a closed session");
        }
        this.playbookId = null;
        this.currentStepIndex = 0;
    }

    /**
     * 結束處理狀態，恢復為活躍 (ACTIVE)。
     */
    public void completeProcessing() {
        this.status = AgentStatus.ACTIVE;
    }

    /**
     * 推進劇本的步驟。
     */
    public void advanceStep() {
        this.currentStepIndex++;
    }

    /**
     * 關閉會話，關閉後無法再進行互動。
     */
    public void close() {
        this.status = AgentStatus.CLOSED;
    }

    /**
     * Get uncommitted domain events and clear them.
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }
}
