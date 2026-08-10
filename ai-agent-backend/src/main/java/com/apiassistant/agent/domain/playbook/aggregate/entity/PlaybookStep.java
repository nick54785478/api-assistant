package com.apiassistant.agent.domain.playbook.aggregate.entity;

import com.apiassistant.agent.domain.playbook.aggregate.vo.StepParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity (or Value Object) representing a single step within a Playbook.
 */
@Getter
@Builder
@AllArgsConstructor
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
    @Builder.Default
    private List<StepParameter> customInputs = new ArrayList<>();
}
