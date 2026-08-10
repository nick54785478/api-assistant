import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

async function main() {
  console.log("啟動 MCP 測試客戶端...");

  // 設定要執行的 MCP Server 指令 (啟動剛剛編譯好的 index.js)
  const transport = new StdioClientTransport({
    command: "node",
    args: ["build/index.js"],
  });

  const client = new Client(
    { name: "test-client", version: "1.0.0" },
    { capabilities: {} }
  );

  console.log("連線中...");
  await client.connect(transport);
  console.log("✅ 成功連線到 MCP Server！");

  // 1. 測試列出所有 tools
  const toolsResult = await client.listTools();
  console.log("\n📦 取得 Tools 列表：");
  console.log(toolsResult.tools.map(t => t.name));

  // 2. 測試呼叫 login tool
  console.log("\n🚀 正在測試呼叫 login tool (打向 localhost:8088)...");
  let tokenStr = "";
  try {
    const result = await client.callTool({
      name: "login",
      arguments: {
        tenant: "CW",
        username: "nickgh.zhang@cw.com",
        password: "password123"
      }
    });
    console.log("✅ 呼叫完成，回傳結果：");
    console.dir(result, { depth: null });
    
    // 從回傳的純文字中萃取 JWT (簡單正規表達式)
    const textContent = result.content[0].text;
    tokenStr = textContent.match(/([a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+)/)?.[1] || "";
    console.log("👉 萃取到的 Token: ", tokenStr);
  } catch (error) {
    console.error("❌ 呼叫失敗：", error);
  }

  // 3. 測試呼叫 get_user_by_email
  console.log("\n🚀 正在測試呼叫 get_user_by_email tool...");
  try {
    const result2 = await client.callTool({
      name: "get_user_by_email",
      arguments: {
        email: "nickgh.zhang@cw.com"
      }
    });
    console.log("✅ 呼叫完成，回傳結果：");
    console.dir(result2, { depth: null });
  } catch (error) {
    console.error("❌ get_user_by_email 呼叫失敗：", error);
  }

  // 關閉連線並退出
  console.log("\n測試結束，關閉連線。");
  process.exit(0);
}

main().catch(console.error);
