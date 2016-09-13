package edu.usc.sql.analyses.layoutanalysis;

import org.dom4j.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mian on 8/17/16.
 */
public class WidgetNode {
    private Element xmlElement;
    private boolean isRoot;
    private Set<WidgetNode> children;
    private int width;
    private int height;
    private String textSize;
    private int displayedSize;

    public WidgetNode(Element element, boolean isRoot) {
        this.xmlElement = element;
        this.isRoot = isRoot;
        children = new HashSet<WidgetNode>();
    }

    public void addChild(WidgetNode child) {
        children.add(child);
    }

    public String getName() {
        return xmlElement.getName();
    }

    public Set<WidgetNode> getChildren() {
        return children;
    }

    public int getSize() {
        return width * height;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Element getXmlElement() {
        return xmlElement;
    }

    public String getTextSize() {
        return textSize;
    }

    public void setTextSize(String textSize) {
        this.textSize = textSize;
    }

    public int getDisplayedSize() {
        return displayedSize;
    }

    public void setDisplayedSize(int displayedSize) {
        this.displayedSize = displayedSize;
    }
}
