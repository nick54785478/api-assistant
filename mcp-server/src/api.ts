import express from 'express';
import cors from 'cors';
import swaggerUi from 'swagger-ui-express';
import swaggerJsdoc from 'swagger-jsdoc';
import { getDb } from './db.js';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
let onToolsChangedCallback: (() => void) | undefined;

app.use(cors());
app.use(express.json());

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'MCP Tools API',
      version: '1.0.0',
      description: 'API for managing MCP Tools dynamically',
    },
  },
  apis: [join(__dirname, 'api.js'), join(__dirname, 'api.ts')],
};
const swaggerSpec = swaggerJsdoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

// Helper to convert JSON schema to UI parameters
function schemaToParameters(schemaStr: string): any[] {
  try {
    const schema = JSON.parse(schemaStr);
    const properties = schema.properties || {};
    const required = schema.required || [];

    return Object.keys(properties).map(key => ({
      name: key,
      type: properties[key].type || 'string',
      description: properties[key].description || '',
      required: required.includes(key)
    }));
  } catch (e) {
    return [];
  }
}

// Helper to convert UI parameters to JSON schema string
function parametersToSchema(parameters: any[]): string {
  if (!parameters || parameters.length === 0) {
    return JSON.stringify({ type: "object", properties: {}, required: [] });
  }

  const properties: any = {};
  const required: string[] = [];

  parameters.forEach(p => {
    properties[p.name] = {
      type: p.type || "string",
      description: p.description || ""
    };
    if (p.required) {
      required.push(p.name);
    }
  });

  return JSON.stringify({
    type: "object",
    properties,
    required
  });
}

/**
 * @swagger
 * /tools:
 *   get:
 *     summary: Get all tools
 *     description: Retrieve a list of available MCP tools. Can be filtered by sessionId or mode.
 *     parameters:
 *       - in: query
 *         name: sessionId
 *         schema:
 *           type: string
 *         description: The session ID to filter tools for a specific chat session.
 *       - in: query
 *         name: mode
 *         schema:
 *           type: string
 *           enum: [global, session]
 *         description: Filter mode (global or session).
 *     responses:
 *       200:
 *         description: A JSON array of tool objects
 */
// GET all tools
app.get('/tools', async (req, res) => {
  try {
    const db = await getDb();
    const sessionId = req.query.sessionId as string;
    const mode = req.query.mode as string; // 'global' | 'session' | undefined
    
    let query = 'SELECT * FROM mcp_tools';
    const params = [];
    
    if (mode === 'global') {
      query += ' WHERE session_id IS NULL';
    } else if (mode === 'session') {
      query += ' WHERE session_id = ? OR session_id IS NULL';
      params.push(sessionId);
    } else {
      // Default: Return both (for Java backend)
      if (sessionId) {
        query += ' WHERE session_id = ? OR session_id IS NULL';
        params.push(sessionId);
      } else {
        query += ' WHERE session_id IS NULL';
      }
    }
    
    const rows = await db.all(query, params);

    const tools = rows.map(row => ({
      id: row.id,
      name: row.name,
      description: row.description,
      api_url: row.api_url,
      api_method: row.api_method,
      requires_auth: Boolean(row.requires_auth),
      session_id: row.session_id,
      parameters: schemaToParameters(row.input_schema)
    }));

    res.json(tools);
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * @swagger
 * /tools:
 *   post:
 *     summary: Create a new tool
 *     description: Register a new MCP tool to be available for AI agents.
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               name:
 *                 type: string
 *               description:
 *                 type: string
 *               api_url:
 *                 type: string
 *               api_method:
 *                 type: string
 *               requires_auth:
 *                 type: boolean
 *               session_id:
 *                 type: string
 *               parameters:
 *                 type: array
 *                 items:
 *                   type: object
 *     responses:
 *       200:
 *         description: Tool created successfully
 */
// POST new tool
app.post('/tools', async (req, res) => {
  try {
    const db = await getDb();
    const tool = req.body;

    const inputSchemaStr = parametersToSchema(tool.parameters);

    const result = await db.run(`
      INSERT INTO mcp_tools (name, description, input_schema, api_url, api_method, requires_auth, session_id)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `, [
      tool.name,
      tool.description || '',
      inputSchemaStr,
      tool.api_url,
      tool.api_method,
      tool.requires_auth ? 1 : 0,
      tool.session_id || null
    ]);

    if (onToolsChangedCallback) {
      try { onToolsChangedCallback(); } catch (e) { console.error(e); }
    }

    res.json({ id: result.lastID, ...tool });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * @swagger
 * /tools/{id}:
 *   put:
 *     summary: Update an existing tool
 *     description: Update the configuration of an existing MCP tool by ID.
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *     responses:
 *       200:
 *         description: Tool updated successfully
 */
// PUT update tool
app.put('/tools/:id', async (req, res) => {
  try {
    const db = await getDb();
    const id = req.params.id;
    const tool = req.body;

    const inputSchemaStr = parametersToSchema(tool.parameters);

    await db.run(`
      UPDATE mcp_tools
      SET name = ?, description = ?, input_schema = ?, api_url = ?, api_method = ?, requires_auth = ?, session_id = ?
      WHERE id = ?
    `, [
      tool.name,
      tool.description || '',
      inputSchemaStr,
      tool.api_url,
      tool.api_method,
      tool.requires_auth ? 1 : 0,
      tool.session_id || null,
      id
    ]);

    if (onToolsChangedCallback) {
      try { onToolsChangedCallback(); } catch (e) { console.error(e); }
    }

    res.json({ id: Number(id), ...tool });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * @swagger
 * /tools/{id}:
 *   delete:
 *     summary: Delete a tool
 *     description: Remove a registered MCP tool by ID.
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Tool deleted successfully
 */
// DELETE tool
app.delete('/tools/:id', async (req, res) => {
  try {
    const db = await getDb();
    const id = req.params.id;

    await db.run('DELETE FROM mcp_tools WHERE id = ?', [id]);

    if (onToolsChangedCallback) {
      try { onToolsChangedCallback(); } catch (e) { console.error(e); }
    }

    res.json({ success: true });
  } catch (err: any) {
    res.status(500).json({ error: err.message });
  }
});

export function startApiServer(port: number = 3001, onToolsChanged?: () => void) {
  if (onToolsChanged) {
    onToolsChangedCallback = onToolsChanged;
  }
  app.listen(port, () => {
    // 使用 console.error 避免干擾 MCP 的 stdout (JSON-RPC)
    console.error(`Express API server listening on port ${port}`);
  });
}
