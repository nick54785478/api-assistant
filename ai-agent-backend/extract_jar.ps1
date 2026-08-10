$jarPath = (Get-ChildItem -Path "$HOME\.m2\repository\org\springframework\ai\spring-ai-core" -Filter "spring-ai-core-*.jar" -Recurse | Select-Object -First 1).FullName
Copy-Item -Path $jarPath -Destination "d:\桌面\VibeCodingWorkSpace\api-assistant\ai-agent-backend\temp.zip"
Expand-Archive -Path "d:\桌面\VibeCodingWorkSpace\api-assistant\ai-agent-backend\temp.zip" -DestinationPath "d:\桌面\VibeCodingWorkSpace\api-assistant\ai-agent-backend\jar_extracted" -Force
