package com.web.coretix.menu;

public class AppMenuItem {

    private final String id;
    private final String label;
    private final String icon;
    private final String url;
    private final int order;
    private final String renderedExpression;

    public AppMenuItem(String id, String label, String icon, String url, int order, String renderedExpression) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.url = url;
        this.order = order;
        this.renderedExpression = renderedExpression;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getUrl() {
        return url;
    }

    public int getOrder() {
        return order;
    }

    public String getRenderedExpression() {
        return renderedExpression;
    }
}
