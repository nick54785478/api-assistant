#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import { getDb } from "./db.js";
import { startApiServer } from "./api.js";

// Initialize the MCP Server
const server = new Server(
  {
    name: "api-assistant-mcp-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 全域變數：用來暫存登入後取得的 Token，避免 LLM 在傳遞過程中竄改或截斷
let currentAuthToken = "";

// Register the tool list
server.setRequestHandler(ListToolsRequestSchema, async () => {
  const db = await getDb();
  const tools = await db.all("SELECT * FROM mcp_tools");

  return {
    tools: tools.map(t => {
      const toolName = t.session_id ? `s_${t.session_id.replace(/[^a-zA-Z0-9_]/g, '').substring(0, 4)}_${t.name}` : t.name;
      return {
        name: toolName,
        description: t.description,
        inputSchema: JSON.parse(t.input_schema)
      };
    })
  };
});

// Handle tool execution
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const db = await getDb();
  const toolName = request.params.name;
  
  if (toolName === "query_knowledge_base") {
    const question = String(request.params.arguments?.question || request.params.arguments?.query || '未知');
    return {
      content: [
        {
          type: "text",
          text: `目前知識庫尚在建置中，請直接回覆使用者：「抱歉，我目前還無法查詢關於『${question}』的內部知識，我會在後續版本升級時學會這項技能！」`,
        },
      ],
    };
  }

  if (toolName === "set_auth_token") {
    const token = String(request.params.arguments?.token || '');
    if (token) {
      currentAuthToken = token.replace(/^Bearer\s+/i, '');
      return {
        content: [{
          type: "text",
          text: `成功設定全域 Token！後續呼叫需要授權的 API 將會自動使用此 Token。`
        }]
      };
    } else {
      return {
        content: [{
          type: "text",
          text: `設定 Token 失敗：未提供 Token 參數。`
        }]
      };
    }
  }

  let actualToolName = toolName;
  let toolDef;
  
  const allTools = await db.all("SELECT * FROM mcp_tools");
  for (const t of allTools) {
    const computedName = t.session_id ? `s_${t.session_id.replace(/[^a-zA-Z0-9_]/g, '').substring(0, 4)}_${t.name}` : t.name;
    if (computedName === toolName) {
      toolDef = t;
      actualToolName = t.name;
      break;
    }
  }
  
  if (!toolDef) {
    throw new Error(`未知的工具名稱: ${toolName}`);
  }

  const args = request.params.arguments || {};
  let apiUrl = toolDef.api_url;

  if (toolDef.api_method.toUpperCase() === 'GET') {
    const urlObj = new URL(apiUrl);
    for (const [key, value] of Object.entries(args)) {
      if (['token', 'jwt', 'jwtoken', 'authorization'].includes(key.toLowerCase())) continue;
      urlObj.searchParams.append(key, String(value));
    }
    apiUrl = urlObj.toString();
  }

  const fetchOptions: RequestInit = {
    method: toolDef.api_method,
    headers: {
      'Content-Type': 'application/json'
    }
  };

  if (toolDef.requires_auth) {
    let tokenToUse = currentAuthToken;
    
    // 允許透過 LLM 傳入的參數來覆寫全域 Token
    const providedToken = args.token || args.jwtoken || args.jwt || args.authorization;
    if (providedToken) {
      tokenToUse = String(providedToken);
      if (tokenToUse.toLowerCase().startsWith('bearer ')) {
        tokenToUse = tokenToUse.substring(7);
      }
    }

    if (!tokenToUse) {
      throw new Error(`執行 ${toolName} 失敗：尚未登入，系統中沒有暫存的 Token，且未手動提供 Token。請先呼叫 login 工具或提供 Token。`);
    }
    (fetchOptions.headers as any)['Authorization'] = `Bearer ${tokenToUse}`;
  }

  if (toolDef.api_method.toUpperCase() !== 'GET' && toolDef.api_method.toUpperCase() !== 'HEAD') {
    const bodyArgs = { ...args };
    // 移除用於 Auth 的參數，避免污染 Request Body
    delete bodyArgs.token;
    delete bodyArgs.jwt;
    delete bodyArgs.jwtoken;
    delete bodyArgs.authorization;
    
    fetchOptions.body = JSON.stringify(bodyArgs);
  }

  // --- 準備寫入 Logging Table 的輔助函式 ---
  const logApiCall = async (status: number | null, responseBody: string | null, errorMsg: string | null) => {
    try {
      await db.run(`
        INSERT INTO mcp_api_logs (tool_name, request_url, request_method, request_headers, request_body, response_status, response_body, error_message)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `, [
        actualToolName,
        apiUrl,
        toolDef.api_method,
        JSON.stringify(fetchOptions.headers),
        fetchOptions.body ? String(fetchOptions.body) : null,
        status,
        responseBody,
        errorMsg
      ]);
    } catch (logErr) {
      console.error("Failed to log API call to SQLite:", logErr);
    }
  };

  try {
    const response = await fetch(apiUrl, fetchOptions);

    if (!response.ok) {
      let errorMsg = `呼叫 ${toolName} 失敗: ${response.status} ${response.statusText}`;
      if (toolDef.requires_auth) {
        errorMsg += ` (發送的 Token 為: ${currentAuthToken.substring(0, 20)}...)`;
      }
      
      const errorBody = await response.text().catch(() => null);
      await logApiCall(response.status, errorBody, errorMsg);
      
      throw new Error(errorMsg);
    }

    const dataText = await response.text();
    let data;
    try {
      data = JSON.parse(dataText);
    } catch(e) {
      data = dataText;
    }
    
    await logApiCall(response.status, dataText, null);

    if (actualToolName === 'login') {
      currentAuthToken = data.token;
      return {
        content: [{
          type: "text",
          text: `登入成功！已將 Token 暫存於系統中。\n接下來可以直接呼叫相關 API 工具，不需再傳遞 Token 參數。`
        }]
      };
    }

    return {
      content: [{
        type: "text",
        text: `呼叫成功！\n取得的資料：\n${typeof data === 'object' ? JSON.stringify(data, null, 2) : data}`
      }]
    };
  } catch (error: any) {
    if (!error.message.startsWith('呼叫')) {
      // 避免重複 log
      await logApiCall(null, null, error.message);
    }
    throw new Error(`API 呼叫失敗: ${error.message}`);
  }
});

// Start the server using stdio transport
async function main() {
  // 啟動 Express API Server，供前端介面管理工具
  startApiServer(3001, () => {
    try {
      server.sendToolListChanged();
      console.error("Sent sendToolListChanged notification to MCP client.");
    } catch (e) {
      console.error("Failed to send sendToolListChanged notification:", e);
    }
  });

  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("API Assistant MCP Server is running on stdio.");
}

main().catch((error) => {
  console.error("Server error:", error);
  process.exit(1);
});
