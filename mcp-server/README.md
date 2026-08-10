# API Assistant - MCP Server

這是一個基於 TypeScript 開發的 Model Context Protocol (MCP) Server，做為 AI 助手（Claude Desktop 等）與現有後端服務（如 Java 的 `RagAssistantQueryService`）之間的溝通橋樑。

## 功能

目前實作了以下 Tool 供 API 助手使用：
- `query_knowledge_base`：模擬向後端 RAG 系統發送查詢。
- `login`：呼叫外部 AuthService 取得 JWToken。

## 如何編譯與啟動

1. 安裝相依套件：
   ```bash
   npm install
   ```

2. 編譯 TypeScript 程式碼：
   ```bash
   npm run build
   ```

3. 使用官方 MCP Inspector 進行互動式測試（推薦）：
   這是最直覺的本地測試方式，它會幫您啟動一個瀏覽器 UI，讓您可以點擊測試各個 Tool。
   ```bash
   npx @modelcontextprotocol/inspector node build/index.js
   ```

4. 本地直接執行（做為背景服務時，會占用終端機並等待 stdio 輸入，無法直接用鍵盤互動）：
   ```bash
   npm start
   ```

## 如何與 Claude Desktop 整合測試

1. 確認您已經完成 `npm install` 與 `npm run build`。
2. 找出這個專案的絕對路徑 (假設為 `D:\桌面\VibeCodingWorkSpace\api-assistant\mcp-server`)。
3. 開啟 Claude Desktop 的設定檔：
   - 編輯 `claude_desktop_config.json`
4. 加入以下設定 (請將絕對路徑替換為您的實際路徑，在 Windows 上路徑斜線請使用 `\\`，例如 `D:\\桌面\\...`)：

```json
{
  "mcpServers": {
    "api-assist": {
      "command": "node",
      "args": [
        "D:\\桌面\\VibeCodingWorkSpace\\api-assistant\\mcp-server\\build\\index.js"
      ]
    }
  }
}
```

5. 重新啟動 Claude Desktop。您將能在介面上看到新增的工具圖示，可以試著對它說：「幫我查詢知識庫關於...」。
