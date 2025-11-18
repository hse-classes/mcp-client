package kmo.dev;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Konfiguration für den ChatClient mit MCP Tool Integration
 */
@Configuration
public class ChatClientConfig {

    /**
     * Erstellt einen ChatClient der die MCP Tools nutzen kann.
     *
     * @param chatModel  Das verwendete AI-Modell (z.B. OpenAI)
     * @param mcpClients Liste aller konfigurierten MCP Clients
     * @return Konfigurierter ChatClient
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, List<McpSyncClient> mcpClients) {
        // McpToolCallbackProvider macht die MCP Tools für den ChatClient verfügbar
        SyncMcpToolCallbackProvider toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpClients);

        return ChatClient.builder(chatModel).defaultSystem("""
                                                             Du bist ein hilfreicher Assistent für eine Hochschul-Verwaltung.
                                                             Du hast Zugriff auf Tools zur Abfrage von Studenten-Daten.

                                                             WICHTIG: Nutze die verfügbaren Tools DIREKT, um Informationen abzurufen.
                                                             Erstelle KEINEN Code! Rufe die Tools auf und antworte mit den Ergebnissen auf Deutsch.

                                                             Verfügbare Tools:
                                                             - findeStudentenNachStudiengang: Findet Studenten nach Studiengang
                                                             - alleStudenten: Gibt alle Studenten zurück
                                                             - studentenStatistik: Zeigt Statistik
                                                             - findeStudentNachMatrikelnummer: Findet Student nach Matrikelnummer
                                                             - sucheStudentenNachNachname: Sucht nach Nachname
                """).defaultToolCallbacks(toolCallbackProvider).build();
    }
}