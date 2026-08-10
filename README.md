# API Assistant

API Assistant 是一個具備動態工具呼叫 (Function Calling) 與劇本 (Playbook) 執行能力的 AI 助理平台。
本專案採用微服務與模型上下文協定 (Model Context Protocol, MCP) 的概念設計，讓 AI 可以與多種後端工具、資料庫和外部 API 進行無縫互動。

## 系統架構

本專案主要由以下三個核心模組組成：

1. **AI Agent Backend (Spring Boot)**
   - 位於 `ai-agent-backend/` 目錄。
   - 作為大腦核心，負責處理使用者的對話、記憶體管理 (Chat Memory)、解析 Playbook 劇本，以及與底層的 LLM (Ollama) 溝通。
   - 透過 WebSocket 提供**即時串流 (Streaming)** 對話功能，確保極低的首個字元回應時間 (TTFT)。
   - 具備效能優化設計：使用 Caffeine Cache (TTL 30s) 減少重複查詢、並透過 `CompletableFuture` 進行 API 與 DB 的並行 I/O 處理。
   - 內建 **Tool Calling 失敗偵測與自動重試**機制，提升 Ollama 本地模型的工具呼叫成功率。

2. **Web UI (Angular)**
   - 位於 `web-ui/` 目錄。
   - 提供現代化、響應式的使用者介面。
   - 讓使用者可以與 AI 助手進行對話、管理會話 (Session)、建立與執行自訂的 Playbook 劇本，以及設定可用工具。

3. **MCP Server (Node.js)**
   - 位於 `mcp-server/` 目錄。
   - 負責管理與提供各類的外部操作工具 (Tools)。
   - 後端會透過 HTTP 向 MCP Server 動態查詢當前會話 (Session) 被授權可使用的工具，達成工具存取的動態綁定與權限隔離。

## 快速啟動 (Quick Start)

### 1. 啟動環境與資料庫
請確保你已經安裝了 Docker 與 Docker Compose。在專案根目錄下執行：
```bash
docker-compose up -d
```
這將會啟動所需的基礎設施 (如 PostgreSQL 等資料庫與 Ollama 容器，請依據 `docker-compose.yml` 內容為準)。

### 2. 啟動 Node.js MCP Server
```bash
cd mcp-server
npm install
npm start
```
MCP Server 預設將運行於 `http://localhost:3001`。

### 3. 啟動 Spring Boot 後端
```bash
cd ai-agent-backend
./mvnw spring-boot:run
```
後端預設運行於 `http://localhost:8080`。

### 4. 啟動 Angular 前端
```bash
cd web-ui
npm install
npm run start
```
前端將運行於 `http://localhost:4200`，瀏覽器開啟即可開始使用 API Assistant！

## 核心功能特色

- **Playbook 劇本執行**：將標準作業流程 (SOP) 轉換為 AI 可執行的步驟 (Steps)，並要求 AI 在特定步驟必定呼叫指定工具。
- **動態工具載入 (Dynamic Tools)**：每個 Session 可以獨立綁定不同的 MCP Tools，避免工具濫用，強化安全性。Session-scoped 工具使用精簡前綴命名（`s_{4chars}_toolName`），降低模型混淆。
- **打字機串流回覆**：採用 WebSocket + Spring Reactive (`Flux`) 實現字元級別的即時串流回傳，提升使用者體驗。
- **高效能快取與並發機制**：利用 Caffeine Cache (TTL 30 秒自動過期) 與非同步並發查詢，大幅縮短 AI 生成前的資料準備時間，並確保新註冊的工具能被即時載入。

### Tool Calling 優化機制

針對 Ollama 本地模型（如 Qwen2.5）的 Tool Calling 不穩定問題，本專案實施了以下優化：

| 優化項目 | 說明 |
|---------|------|
| **低溫度推理** | `temperature: 0.1` + `top-p: 0.9`，降低隨機性，使模型更傾向產生結構化的工具呼叫 |
| **精簡 System Prompt** | 劇本模式下的系統提示經過大幅精簡（減少約 40% token），讓模型有更多注意力處理工具呼叫 |
| **失敗偵測與重試** | 自動偵測 AI 是否將工具呼叫以純文字形式輸出（如 `portun {...}`），若偵測到則自動重試一次 |
| **精簡工具名稱** | Session-scoped 工具前綴從 8 字元縮短為 4 字元，降低模型因工具名稱過長而混淆的機率 |

## 文件與參考資料
- [前端開發規範 (FRONTEND_GUIDELINES.md)](./FRONTEND_GUIDELINES.md)
- [MCP 架構說明 (ai_assistant_mcp_architecture.md)](./ai_assistant_mcp_architecture.md)
- [Ollama 模型設定 (ollama_model_setup.md)](./ollama_model_setup.md)
