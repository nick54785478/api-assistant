# API Assistant MCP Server 測試文件

本文件描述了如何透過 AI Assistant (MCP Server) 進行系統登入並查詢使用者資料的完整測試流程。

## 核心機制：有狀態 (Stateful) 的 MCP 認證設計

為了解決小型 LLM (如 7B 模型) 在傳遞長字串（如 JWT Token）時容易產生幻覺、截斷或格式錯誤的問題，本 MCP Server 採用**有狀態 (Stateful)** 的設計：

1. **登入暫存**：呼叫 `login` 工具成功後，MCP Server 會自動將取得的 JWT Token 儲存在記憶體的全域變數 (`currentAuthToken`) 中，不依賴 AI 回傳。
2. **自動攜帶**：後續呼叫需要授權的 API 工具 (如 `get_user_by_email`) 時，不再需要 AI 傳遞 Token 參數，MCP Server 會自動將記憶體中的 Token 帶入 HTTP Header (`Authorization: Bearer <token>`)。

---

## 測試情境一：UI 聊天室自然語言測試

啟動 Web UI 與 Java 後端後，請在聊天室中依照以下順序向 AI 下達指令：

### 步驟 1：執行登入
*   **使用者輸入**：`「請幫我登入 (租戶 CW, 帳號 nickgh.zhang@cw.com , 密碼 password123)」`
*   **預期行為**：AI 會呼叫 MCP Server 的 `login` 工具。
*   **預期回覆**：AI 回報登入成功，並提示 Token 已經暫存於系統中，接下來可直接查詢。

### 步驟 2：查詢資料
*   **使用者輸入**：`「太好了，請幫我查詢 nickgh.zhang@cw.com 的資料」`
*   **預期行為**：AI 會呼叫 MCP Server 的 `get_user_by_email` 工具（不需要傳遞 Token 參數）。
*   **預期回覆**：AI 會列出從外部 API (`localhost:8088`) 查詢到的完整使用者資料，例如：
    ```json
    {
      "id": 2,
      "name": "張耿豪",
      "email": "nickgh.zhang@cw.com",
      "address": "台北市南港區"
    }
    ```

---

## 測試情境二：MCP 本機程式碼測試 (不依賴 AI)

如果您想驗證 MCP Server 與外部 API 的連線是否正常，且排除 AI 模型的干擾，可以使用專屬的 Node.js 測試腳本。

### 執行方式
進入 `mcp-server` 目錄並執行測試腳本：
```bash
cd mcp-server
node test-client.js
```

### 腳本執行流程
1. **啟動連線**：模擬 MCP Client 透過 `stdio` 連線至編譯好的 `build/index.js`。
2. **呼叫 `login`**：傳送帳密至外部 API，驗證是否能成功取得 JWT Token。
3. **萃取 Token**：腳本會使用正規表達式擷取回傳的 Token。
4. **呼叫 `get_user_by_email`**：攜帶剛才獲取的 Token 去請求使用者資料，並印出外部 API 回傳的結果（200 OK 且包含使用者 JSON）。

此腳本若能順利跑完並印出 JSON，即代表外部 API (`localhost:8088`) 與 MCP Server 兩者之間的介接 100% 完美，所有權限與 Token 解析皆正常運作。
