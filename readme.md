# mcp-client

Web-Chat (Spring Boot 4.1 / Spring AI 2.0 / Java 25 / Thymeleaf), der Gemini mit
den Tools eines MCP Servers verbindet. Die Tools kommen nicht aus diesem
Projekt, sondern vom [mcp-server](https://github.com/hse-classes/mcp-server).

Rechts neben dem Chat zeigt die Seite, welche Tools das Modell beim Server
aufgerufen hat (Name, Argumente, Ergebnis).

## Lokal starten

Zuerst den Server starten (`./mvnw spring-boot:run` im mcp-server), dann hier:

```bash
export GEMINI_API_KEY=<dein-key>
export CHAT_USERNAME=hse
export CHAT_PASSWORD_HASH='<bcrypt-hash>'
./mvnw spring-boot:run
```

Die Seite läuft auf <http://localhost:8081>. Den bcrypt-Hash (nie das Klartext-Passwort) erzeugst du so:

```bash
htpasswd -nbBC 10 "" '<dein-passwort>' | tr -d ':\n'
```

## Konfiguration

| Variable | Bedeutung |
|---|---|
| `GEMINI_API_KEY` | API-Key für Gemini |
| `CHAT_USERNAME` / `CHAT_PASSWORD_HASH` | der eine gemeinsame Login (bcrypt-Hash); ohne beides startet die App nicht |
| `MCP_SERVER_URL` | Adresse des MCP Servers, Standard `http://localhost:8080`; im Deployment `http://mcp-server:8080` |
| `PORT` | Port der Web-App, Standard 8081 |

Der Server wird erst bei der ersten Frage kontaktiert (`spring.ai.mcp.client.initialized: false`),
die App startet also auch, wenn der Server noch hochfährt.

## Schutz vor Missbrauch

Die Seite ist hinter einem Login, weil jede Frage den Gemini-Key belastet. Zusätzlich:
höchstens 10 Login-Versuche pro 5 Minuten und 20 Chat-Anfragen pro Minute je IP
(sonst Status 429), Nachrichten sind auf 1000 Zeichen begrenzt.

## Deployment

Push auf `master` baut `ghcr.io/hse-classes/mcp-client:latest`. Der Stack steht in
[hse-infrastructure](https://github.com/hse-classes/hse-infrastructure) (`apps/mcp`),
dort läuft der Server nur intern, der Chat ist der einzige öffentliche Eingang.

## Struktur

```
src/main/java/kmo/dev/
├── McpClientApplication.java
├── ChatClientConfig.java        ChatClient mit Gedächtnis (pro HTTP-Session)
├── ChatController.java          Seite + /api/chat, hängt die MCP-Tools pro Anfrage an
├── RecordingToolCallback.java   merkt sich jeden Tool-Aufruf für die Anzeige
├── SecurityConfig.java          Form-Login mit einem gemeinsamen Benutzer
├── RateLimitFilter.java         Begrenzung für /login und /api/chat
├── RateLimiter.java
└── LoginController.java
```
