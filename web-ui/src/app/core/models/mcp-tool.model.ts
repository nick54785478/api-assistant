export interface McpToolParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'object' | 'array';
  description: string;
  required: boolean;
}

export interface McpTool {
  id?: number;
  name: string;
  description: string;
  api_url: string;
  api_method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  requires_auth: boolean;
  session_id?: string;
  parameters: McpToolParameter[];
}

// Utility to convert Visual Parameters to JSON Schema
export function parametersToJsonSchema(parameters: McpToolParameter[]): any {
  const schema: any = {
    type: 'object',
    properties: {},
    required: []
  };

  parameters.forEach(param => {
    schema.properties[param.name] = {
      type: param.type,
      description: param.description
    };
    if (param.required) {
      schema.required.push(param.name);
    }
  });

  return schema;
}

// Utility to convert JSON Schema back to Visual Parameters
export function jsonSchemaToParameters(schema: any): McpToolParameter[] {
  if (!schema || !schema.properties) return [];

  const parameters: McpToolParameter[] = [];
  const requiredFields: string[] = schema.required || [];

  for (const [key, value] of Object.entries<any>(schema.properties)) {
    parameters.push({
      name: key,
      type: value.type || 'string',
      description: value.description || '',
      required: requiredFields.includes(key)
    });
  }

  return parameters;
}
