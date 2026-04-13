package kmo.dev;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class ChatCommands {

    private final ChatClient chatClient;
    private final Terminal terminal;
    private final ApplicationContext applicationContext;


    @ShellMethod(key = "tools", value = "Zeigt verfügbare MCP Tools")
    public String showTools() {
        try {
            var mcpClients = List.copyOf(applicationContext.getBeansOfType(McpSyncClient.class).values());

            StringBuilder result = new StringBuilder("\n📋 Verfügbare MCP Tools:\n\n");

            for (McpSyncClient client : mcpClients) {
                var toolsResult = client.listTools();
                result.append("Server: ").append(client.toString()).append("\n");
                result.append("Tools: ").append(toolsResult.tools().size()).append("\n\n");

                for (var tool : toolsResult.tools()) {
                    result.append("  🔧 ").append(tool.name()).append("\n");
                    result.append("     Beschreibung: ").append(tool.description()).append("\n");
                    result.append("     Input Schema: ").append(tool.inputSchema()).append("\n\n");
                }
            }

            return result.toString();

        } catch (Exception e) {
            return "❌ Fehler: " + e.getMessage();
        }
    }

    @ShellMethod(key = "frage", value = "Stelle eine Frage an den AI-Assistenten")
    public String ask(@ShellOption(value = {"-t", "--text"}) String question) {

        terminal.writer().println("\n🤔 Denke nach...\n");

        try {
            String response = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();

            return "\n💬 Antwort:\n" + response + "\n";

        } catch (Exception e) {
            return "\n❌ Fehler: " + e.getMessage() + "\n";
        }
    }

    @ShellMethod(key = "chat", value = "Startet einen interaktiven Chat")
    public void chat() {
        terminal.writer().println("\n🚀 Interaktiver Chat gestartet!");
        terminal.writer().println("💡 Tipp: Nutze 'exit' oder 'quit' zum Beenden\n");

        while (true) {
            terminal.writer().print("Du: ");
            terminal.writer().flush();

            String input = readLine();

            if (input == null || input.trim().isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                terminal.writer().println("\n👋 Chat beendet!\n");
                break;
            }

            terminal.writer().println("\n🤔 Denke nach...\n");

            try {
                String response = chatClient.prompt()
                        .user(input)
                        .call()
                        .content();

                terminal.writer().println("🤖 Assistent: " + response + "\n");

            } catch (Exception e) {
                terminal.writer().println("❌ Fehler: " + e.getMessage() + "\n");
            }
        }
    }

    @ShellMethod(key = "beispiele", value = "Zeigt Beispiel-Fragen")
    public String examples() {
        return """
                
                📋 Beispiel-Fragen:
                
                1️⃣  Allgemeine Abfragen:
                   - "Wie viele Studenten gibt es insgesamt?"
                   - "Zeige mir eine Statistik der Studenten pro Studiengang"
                   - "Liste alle Studenten auf"
                
                2️⃣  Studiengang-Suche:
                   - "Welche Studenten studieren Informatik?"
                   - "Zeige mir alle BWL-Studenten"
                
                3️⃣  Namens-Suche:
                   - "Suche Studenten mit Nachname Müller"
                   - "Gibt es einen Studenten namens Meyer?"
                
                4️⃣  Matrikelnummer-Suche:
                   - "Finde den Studenten mit Matrikelnummer 12345"
                   - "Wer hat die Matrikelnummer 12348?"
                
                💡 Tipp: Du kannst auch komplexere Fragen stellen!
                
                """;
    }

    private String readLine() {
        try {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = terminal.reader().read()) != -1) {
                if (c == '\n' || c == '\r') {
                    break;
                }
                sb.append((char) c);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}