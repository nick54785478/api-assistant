package com.apiassistant.agent.domain.playbook.aggregate.entity;

import com.apiassistant.agent.domain.playbook.aggregate.vo.StepParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity (or Value Object) representing a single step within a Playbook.
 */
public class PlaybookStep {
    /** 步驟名稱 (例如：蒐集客戶資訊) */
    private String name;
    
    /** 步驟說明與指示，描述 AI 在此步驟該做什麼 */
    private String description;
    
    /** 執行此步驟需要呼叫的工具名稱 (若無可為 null) */
    private String requiredTool;
    
    /** 給 AI 的回覆指示 (例如：將結果整理成表格格式) */
    private String responseInstructions;
    
    /** 傳給此步驟的自訂參數或變數 */
    private List<StepParameter> customInputs;

    public PlaybookStep(String name, String description, String requiredTool, String responseInstructions, List<StepParameter> customInputs) {
        this.name = name;
        this.description = description;
        this.requiredTool = requiredTool;
        this.responseInstructions = responseInstructions;
        this.customInputs = customInputs != null ? customInputs : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredTool() {
        return requiredTool;
    }

    public String getResponseInstructions() {
        return responseInstructions;
    }

    public List<StepParameter> getCustomInputs() {
        return customInputs;
    }

    public static PlaybookStepBuilder builder() {
        return new PlaybookStepBuilder();
    }

    public static class PlaybookStepBuilder {
        private String name;
        private String description;
        private String requiredTool;
        private String responseInstructions;
        private List<StepParameter> customInputs = new ArrayList<>();

        PlaybookStepBuilder() {
        }

        public PlaybookStepBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PlaybookStepBuilder description(String description) {
            this.description = description;
            return this;
        }

        public PlaybookStepBuilder requiredTool(String requiredTool) {
            this.requiredTool = requiredTool;
            return this;
        }

        public PlaybookStepBuilder responseInstructions(String responseInstructions) {
            this.responseInstructions = responseInstructions;
            return this;
        }

        public PlaybookStepBuilder customInputs(List<StepParameter> customInputs) {
            this.customInputs = customInputs;
            return this;
        }

        public PlaybookStep build() {
            return new PlaybookStep(name, description, requiredTool, responseInstructions, customInputs);
        }
    }
}
