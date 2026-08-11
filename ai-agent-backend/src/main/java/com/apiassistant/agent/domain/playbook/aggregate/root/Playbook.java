package com.apiassistant.agent.domain.playbook.aggregate.root;

import com.apiassistant.agent.domain.playbook.aggregate.entity.PlaybookStep;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Playbook Aggregate Root.
 * Represents a standard operating procedure (SOP) script that an AI agent should follow.
 */
public class Playbook {
    /**
     * 劇本的唯一識別碼 (UUID)
     */
    private String id;

    /**
     * 綁定的 Agent Session ID
     */
    private String agentSessionId;

    /**
     * 劇本名稱
     */
    private String name;

    /**
     * 劇本說明與目標描述
     */
    private String description;

    /**
     * 劇本內包含的順序步驟列表
     */
    private List<PlaybookStep> steps;

    /**
     * 劇本建立時間
     */
    private Instant createdAt;

    /**
     * 劇本最後更新時間
     */
    private Instant updatedAt;

    public Playbook(String id, String agentSessionId, String name, String description, List<PlaybookStep> steps, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.agentSessionId = agentSessionId;
        this.name = name;
        this.description = description;
        this.steps = steps != null ? steps : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getAgentSessionId() {
        return agentSessionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<PlaybookStep> getSteps() {
        return steps;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 建立一個新的 Playbook 聚合根。
     *
     * @param agentSessionId 綁定的 Agent Session ID
     * @param name        劇本名稱
     * @param description 劇本說明
     * @param steps       劇本步驟列表
     * @return 建立的 Playbook 實體
     */
    public static Playbook create(String agentSessionId, String name, String description, List<PlaybookStep> steps) {
        return Playbook.builder()
                .id(UUID.randomUUID().toString())
                .agentSessionId(agentSessionId)
                .name(name)
                .description(description)
                .steps(steps != null ? steps : new ArrayList<>())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    /**
     * 更新 Playbook 內容。
     *
     * @param agentSessionId 新的綁定 Agent Session ID
     * @param name        新的劇本名稱
     * @param description 新的劇本說明
     * @param steps       新的劇本步驟列表
     */
    public void update(String agentSessionId, String name, String description, List<PlaybookStep> steps) {
        this.agentSessionId = agentSessionId;
        this.name = name;
        this.description = description;
        this.steps = steps != null ? steps : new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public static PlaybookBuilder builder() {
        return new PlaybookBuilder();
    }

    public static class PlaybookBuilder {
        private String id;
        private String agentSessionId;
        private String name;
        private String description;
        private List<PlaybookStep> steps = new ArrayList<>();
        private Instant createdAt;
        private Instant updatedAt;

        PlaybookBuilder() {
        }

        public PlaybookBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PlaybookBuilder agentSessionId(String agentSessionId) {
            this.agentSessionId = agentSessionId;
            return this;
        }

        public PlaybookBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PlaybookBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PlaybookBuilder steps(List<PlaybookStep> steps) {
            this.steps = steps;
            return this;
        }

        public PlaybookBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PlaybookBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Playbook build() {
            return new Playbook(id, agentSessionId, name, description, steps, createdAt, updatedAt);
        }
    }
}
