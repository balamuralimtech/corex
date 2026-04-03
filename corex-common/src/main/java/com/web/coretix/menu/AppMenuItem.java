package com.web.coretix.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppMenuItem {

    private final String id;
    private final String label;
    private final String icon;
    private final String url;
    private final int order;
    private final String renderedExpression;
    private final List<AppMenuItem> items = new ArrayList<>();

    public AppMenuItem(String id, String label, String icon, String url, int order, String renderedExpression) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.url = url;
        this.order = order;
        this.renderedExpression = renderedExpression;
    }

    public AppMenuItem addItem(AppMenuItem item) {
        items.add(item);
        return this;
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

    public List<AppMenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
