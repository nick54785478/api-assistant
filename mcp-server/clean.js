import sqlite3 from 'sqlite3';
import { open } from 'sqlite';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function clean() {
  const dbPath = path.resolve(__dirname, 'mcp.db');
  
  const db = await open({
    filename: dbPath,
    driver: sqlite3.Database
  });

  const result = await db.run(`
    DELETE FROM mcp_tools 
    WHERE id NOT IN (
      SELECT MIN(id) 
      FROM mcp_tools 
      GROUP BY name, IFNULL(session_id, 'GLOBAL')
    )
  `);
  console.log('Duplicates removed. Rows affected:', result.changes);
}

clean();
