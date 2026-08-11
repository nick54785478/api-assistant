Add-Type -AssemblyName System.IO.Compression.FileSystem
$zipPath = 'C:\Users\nick5\.m2\repository\org\springframework\ai\spring-ai-mcp\2.0.0-M3\spring-ai-mcp-2.0.0-M3.jar'
$zip = [System.IO.Compression.ZipFile]::OpenRead($zipPath)
foreach ($entry in $zip.Entries) {
    if ($entry.FullName.Contains('McpSyncClient')) {
        Write-Host $entry.FullName
    }
}
$zip.Dispose()
