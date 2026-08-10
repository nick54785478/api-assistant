import sqlite3 from 'sqlite3';
import { open, Database } from 'sqlite';
import path from 'path';

import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let dbInstance: Database | null = null;

export async function getDb(): Promise<Database> {
  if (dbInstance) {
    return dbInstance;
  }

  // Ensure we always use mcp-server/mcp.db, regardless of cwd (Spring Boot runs this from a different cwd)
  const dbPath = path.resolve(__dirname, '..', 'mcp.db');
  
  dbInstance = await open({
    filename: dbPath,
    driver: sqlite3.Database
  });

  await initDb(dbInstance);

  return dbInstance;
}

async function initDb(db: Database) {
  await db.exec(`
    CREATE TABLE IF NOT EXISTS mcp_tools (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT NOT NULL,
      input_schema JSON NOT NULL,
      api_url TEXT,
      api_method TEXT,
      requires_auth BOOLEAN DEFAULT 0,
      session_id TEXT DEFAULT NULL,
      UNIQUE(name, session_id)
    );
  `);

  await db.exec(`
    CREATE TABLE IF NOT EXISTS mcp_api_logs (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      tool_name TEXT NOT NULL,
      request_url TEXT NOT NULL,
      request_method TEXT NOT NULL,
      request_headers TEXT,
      request_body TEXT,
      response_status INTEGER,
      response_body TEXT,
      error_message TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );
  `);

  // 插入預設的 login 工具
  const loginExists = await db.get(`SELECT 1 FROM mcp_tools WHERE name = ? AND session_id IS NULL`, ['login']);
  if (!loginExists) {
    await db.run(`
      INSERT INTO mcp_tools (name, description, input_schema, api_url, api_method, requires_auth)
      VALUES (?, ?, ?, ?, ?, ?)
    `, [
      'login',
      '呼叫 AuthService 進行登入，以獲取 JWToken (JWT 憑證)。',
      JSON.stringify({
        type: "object",
        properties: {
          tenant: { type: "string", description: "租戶代號 (例如：'CW')" },
          username: { type: "string", description: "登入帳號" },
          password: { type: "string", description: "登入密碼" }
        },
        required: ["tenant", "username", "password"]
      }),
      'http://localhost:8088/api/v1/login',
      'POST',
      false
    ]);
  }

  // 插入預設的 get_user_by_email 工具
  const getUserExists = await db.get(`SELECT 1 FROM mcp_tools WHERE name = ? AND session_id IS NULL`, ['get_user_by_email']);
  if (!getUserExists) {
    await db.run(`
      INSERT INTO mcp_tools (name, description, input_schema, api_url, api_method, requires_auth)
      VALUES (?, ?, ?, ?, ?, ?)
    `, [
    'get_user_by_email',
    '透過 email 查詢使用者資料。呼叫前必須先執行 login 工具。MCP Server 會自動帶入 Token，你只需要提供 email 即可。',
    JSON.stringify({
      type: "object",
      properties: {
        email: { type: "string", description: "要查詢的 email" }
      },
      required: ["email"]
    }),
    'http://localhost:8088/api/v1/users/queryByEmail',
    'GET',
    true
  ]);
  }
}
