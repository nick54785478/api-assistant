import sqlite3 from 'sqlite3';
import { open } from 'sqlite';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function migrate() {
  const dbPath = path.resolve(__dirname, 'mcp.db');
  console.log('Migrating database at', dbPath);
  
  const db = await open({
    filename: dbPath,
    driver: sqlite3.Database
  });

  try {
    // 1. Rename old table
    await db.exec(`ALTER TABLE mcp_tools RENAME TO mcp_tools_old;`);
    console.log('Renamed old table');

    // 2. Create new table
    await db.exec(`
      CREATE TABLE mcp_tools (
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
    console.log('Created new table');

    // 3. Copy data
    await db.exec(`
      INSERT INTO mcp_tools (id, name, description, input_schema, api_url, api_method, requires_auth)
      SELECT id, name, description, input_schema, api_url, api_method, requires_auth FROM mcp_tools_old;
    `);
    console.log('Copied data');

    // 4. Drop old table
    await db.exec(`DROP TABLE mcp_tools_old;`);
    console.log('Migration completed successfully.');
  } catch (e) {
    console.error('Migration failed:', e);
  }
}

migrate();
