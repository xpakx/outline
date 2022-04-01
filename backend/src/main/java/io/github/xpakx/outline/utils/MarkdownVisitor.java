package io.github.xpakx.outline.utils;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

public class MarkdownVisitor implements NodeVisitor {
    private final StringBuilder builder;
    private int listDepth = 0;

    public MarkdownVisitor() {
        this.builder = new StringBuilder();
    }

    @Override
    public void head(Node node, int i) {
        if(node instanceof TextNode) {
            String text = ((TextNode) node).text();
            builder.append(text);
        } else if(node instanceof Element) {
            processTag((Element) node);
        }
    }

    private void processTag(Element element) {
        if(isBlockQuote(element)) {
            builder.append("\n> ");
        } else if(isList(element)) {
            listDepth+=1;
        } else if(isBlock(element)) {
            builder.append("\n");
        } else if(isTitle(element, 1)) {
            builder.append("\n#");
        } else if(isTitle(element, 2)) {
            builder.append("\n##");
        } else if(isTitle(element, 3)) {
            builder.append("\n###");
        } else if(isTitle(element, 4)) {
            builder.append("\n####");
        } else if(isBold(element)) {
            builder.append("**");
        } else if(isCursive(element)) {
            builder.append("_");
        } else if(isCode(element)) {
            builder.append("`");
        } else if(isListElem(element)) {
            builder.append(" ".repeat(listDepth)).append("* ");
        }
    }

    private boolean isBlockQuote(Element element) {
        return element.tagName().equals("blockquote");
    }

    private boolean isTitle(Element element, int lvl) {
        return element.tagName().equals("h"+lvl);
    }

    private boolean isTitle(Element element) {
        return element.tagName().equals("h1") ||
                element.tagName().equals("h2") ||
                element.tagName().equals("h3")||
                element.tagName().equals("h4");
    }

    private boolean isList(Element element) {
        return element.tagName().equals("ol") || element.tagName().equals("ul");
    }

    private boolean isListElem(Element element) {
        return element.tagName().equals("li");
    }

    private boolean isBold(Element element) {
        return element.tagName().equals("b") || element.tagName().equals("strong");
    }

    private boolean isCursive(Element element) {
        return element.tagName().equals("i") || element.tagName().equals("u") || element.tagName().equals("cite");
    }

    private boolean isCode(Element element) {
        return element.tagName().equals("code");
    }

    private boolean isBlock(Element element) {
        return element.tagName().equals("br") || element.tagName().equals("p") || element.tagName().equals("div");
    }

    @Override
    public void tail(Node node, int i) {
        if(node instanceof Element) {
            postProcessTag((Element) node);
        }
    }

    private void postProcessTag(Element element) {
        if(isBlockQuote(element)) {
            builder.append("\n");
        } else if(isList(element)) {
            listDepth-=1;
        } else if(isBlock(element)) {
            builder.append("\n");
        } else if(isTitle(element)) {
            builder.append("\n");
        } else if(isBold(element)) {
            builder.append("**");
        } else if(isCursive(element)) {
            builder.append("_");
        } else if(isCode(element)) {
            builder.append("`");
        } else if(isListElem(element)) {
            builder.append("\n");
        }
    }

    public String getResult() {
        return builder.toString();
    }
}
