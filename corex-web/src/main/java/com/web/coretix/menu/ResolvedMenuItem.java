package com.web.coretix.menu;

public class ResolvedMenuItem {

    private final String id;
    private final String label;
    private final String icon;
    private final String url;
    private final boolean rendered;

    public ResolvedMenuItem(String id, String label, String icon, String url, boolean rendered) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.url = url;
        this.rendered = rendered;
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

    public boolean isRendered() {
        return rendered;
    }
}
