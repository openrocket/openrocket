package net.sf.openrocket.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * This class formats a Markdown text (e.g. from the GitHub API) to HTML
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class MarkdownUtil {
    /**
     * Convert input Markdown text to HTML.
     * @param markdown text with Markdown styles.
     * @return HTML rendering from the Markdown
     */
    public static String toHtml(String markdown) {
        if (markdown == null) return "";

        // Convert JSON string new line to markdown newline
        markdown = markdown.replace("\\r\\n", "\n");

        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }
}
