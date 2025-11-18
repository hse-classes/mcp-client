package kmo.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * MCP Client Anwendung mit Spring Shell CLI.
 * <p>
 * Diese Anwendung verbindet sich mit dem MCP Server und nutzt dessen Tools
 * für Chat-Interaktionen mit einem LLM.
 */
@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }
}