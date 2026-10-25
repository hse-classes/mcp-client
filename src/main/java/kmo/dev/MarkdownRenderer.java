package kmo.dev;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * Turns the model's Markdown answer into HTML for the chat page. The model output is
 * untrusted: raw HTML is escaped and unsafe link targets (javascript: ...) are dropped.
 */
@Component
public class MarkdownRenderer {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build();

    public String toHtml(String markdown) {
        Node document = parser.parse(markdown == null ? "" : markdown);
        return renderer.render(document);
    }
}
