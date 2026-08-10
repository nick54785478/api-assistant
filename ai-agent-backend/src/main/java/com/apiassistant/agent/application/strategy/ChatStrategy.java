package com.apiassistant.agent.application.strategy;

import com.apiassistant.agent.domain.agentsession.aggregate.root.AgentSession;
import com.apiassistant.agent.domain.playbook.aggregate.root.Playbook;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * 處理與 AI 代理對話的策略介面 (Strategy Pattern)。
 * <p>
 * 架構設計說明：
 * 1. 【免 Factory/Registry】：本系統利用 Spring 的 IoC 容器特性。所有實作此介面並標註 @Component 的類別，
 *    都會在系統啟動時被自動收集成 List&lt;ChatStrategy&gt; 注入到 ChatApplicationService 中。
 * 2. 【自動路由】：透過 {@link #supports} 方法，每個策略自行判斷當前的 Session 狀態（是否有綁定劇本、走到哪一步）
 *    是否歸自己管。ChatApplicationService 會自動選擇第一個回傳 true 的策略來處理請求。
 * 3. 【向下相容】：因為策略的切換是基於後端 Session 狀態自動推導的，前端只需維持既有的 Payload 傳送方式，
 *    無需額外新增參數或 API 端點，即可無縫支援「劇本代理人」與「一般 API 代理人」的自動切換。
 * </p>
 */
public interface ChatStrategy {
    
    /** 
     * 判斷當前狀態是否適用此策略 
     */
    boolean supports(AgentSession session, Playbook playbook);
    
    enum PreProcessResult {
        NONE,
        SAVE_SESSION,
        SAVE_AND_CLEAR
    }
    
    /** 
     * 訊息前置處理 (例如判斷使用者輸入 "OK" 時推進劇本) 
     * 
     * @return 處理結果，決定是否需要儲存 Session 或清除歷史紀錄
     */
    PreProcessResult preProcess(String sessionId, String userMessage, AgentSession session, Playbook playbook);
    
    /** 
     * 取得專屬的系統提示詞 (System Prompt) 
     */
    String getSystemPrompt(String defaultSystemPrompt, AgentSession session, Playbook playbook, List<String> allowedToolNames, String sessionId);
    
    /** 
     * 取得允許使用的工具清單 
     */
    List<ToolCallback> getActiveTools(AgentSession session, Playbook playbook, List<ToolCallback> allTools, List<String> allowedToolNames);
}
