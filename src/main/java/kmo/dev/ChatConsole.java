package kmo.dev;

import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatConsole implements CommandLineRunner {

    private static final String HELP = """

            Befehle:
              tools      zeigt die Tools, die der MCP Server anbietet
              beispiele  zeigt Beispiel-Fragen
              exit       beendet den Chat
            Alles andere geht als Frage an den Assistenten.
            """;

    private static final String EXAMPLES = """

            Beispiel-Fragen:
              - Wie viele Studenten gibt es insgesamt?
              - Zeige mir eine Statistik der Studenten pro Studiengang
              - Welche Studenten studieren Informatik?
              - Suche Studenten mit Nachname Müller
              - Wer hat die Matrikelnummer 12348?
            """;

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public ChatConsole(ChatClient chatClient, ToolCallbackProvider mcpTools) {
        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
    }

    @Override
    public void run(String... args) {
        System.out.println(HELP);
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Du: ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String input = scanner.nextLine().trim();
            switch (input.toLowerCase()) {
                case "" -> { }
                case "exit", "quit" -> {
                    System.out.println("Chat beendet.");
                    return;
                }
                case "tools" -> printTools();
                case "beispiele" -> System.out.println(EXAMPLES);
                default -> ask(input);
            }
        }
    }

    private void printTools() {
        for (var callback : mcpTools.getToolCallbacks()) {
            var definition = callback.getToolDefinition();
            System.out.println("\n  " + definition.name());
            System.out.println("    " + definition.description());
            System.out.println("    Input Schema: " + definition.inputSchema());
        }
        System.out.println();
    }

    private void ask(String question) {
        try {
            String answer = chatClient.prompt().user(question).call().content();
            System.out.println("\nAssistent: " + answer + "\n");
        } catch (Exception e) {
            System.out.println("\nFehler: " + e.getMessage() + "\n");
        }
    }
}
