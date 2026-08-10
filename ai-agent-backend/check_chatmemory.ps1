$path = (Get-ChildItem -Path $HOME\.m2\repository\org\springframework\ai\spring-ai-core -Filter spring-ai-core-*.jar -Recurse | Select-Object -First 1).FullName
if ($path) {
    javap -cp $path org.springframework.ai.chat.memory.ChatMemory
}
