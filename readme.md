# mcp-client

Konsolen-Chat (Spring Boot 4.1 / Spring AI 2.0 / Java 25), der Gemini mit den
Tools eines MCP Servers verbindet. Die Tools kommen nicht aus diesem Projekt,
sondern vom Server aus [mcp-server](https://github.com/hse-classes/mcp-server).

## Starten

Zuerst den Server starten (`./mvnw spring-boot:run` im mcp-server), dann:

```bash
export GEMINI_API_KEY=<dein-key>
./mvnw spring-boot:run
```

Befehle im Chat: `tools` (Tools des Servers anzeigen), `beispiele`, `exit`.
Alles andere geht als Frage an das Modell, das bei Bedarf die Tools des
Servers aufruft (im Log mit `kmo.dev`/`org.springframework.ai.mcp` auf DEBUG
zu sehen).

## Konfiguration

`src/main/resources/application.yml`: Gemini-Key und -Modell sowie die
MCP-Verbindung (`spring.ai.mcp.client.streamable-http.connections`). Weitere
Server lassen sich dort als zusätzliche Connections eintragen.

## Struktur

```
src/main/java/kmo/dev/
├── McpClientApplication.java
├── ChatClientConfig.java   ChatClient mit den MCP-Tools
└── ChatConsole.java        einfache Konsolenschleife
```
