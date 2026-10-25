package kmo.dev;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

/**
 * Turns the model's Markdown answer into HTML for the chat page. The model output is
 * untrusted: raw HTML is escaped, unsafe link targets (javascript: ...) are dropped and images
 * are not rendered at all - the browser would load an image URL automatically, which lets an
 * injected answer leak data (e.g. student details) in the URL without any click.
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
        document.accept(new DropImages());
        return renderer.render(document);
    }

    /** Replaces every image by its alt text. */
    private static class DropImages extends AbstractVisitor {
        @Override
        public void visit(Image image) {
            // Images can nest in the alt text: clean those first, before the children
            // are promoted into the surrounding paragraph where nobody visits them again.
            visitChildren(image);
            Node child = image.getFirstChild();
            while (child != null) {
                Node next = child.getNext();
                image.insertBefore(child);
                child = next;
            }
            image.unlink();
        }
    }
}
