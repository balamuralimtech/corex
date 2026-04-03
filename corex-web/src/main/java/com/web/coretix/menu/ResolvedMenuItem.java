package com.web.coretix.menu;

import java.util.Collections;
import java.util.List;

public class ResolvedMenuItem {

    private final String id;
    private final String label;
    private final String icon;
    private final String url;
    private final boolean rendered;
    private final List<ResolvedMenuItem> items;

    public ResolvedMenuItem(String id, String label, String icon, String url, boolean rendered) {
        this(id, label, icon, url, rendered, Collections.emptyList());
    }

    public ResolvedMenuItem(String id, String label, String icon, String url, boolean rendered, List<ResolvedMenuItem> items) {
        this.id = id;
        this.label = label;
        this.icon = icon;
        this.url = url;
        this.rendered = rendered;
        this.items = items;
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

    public List<ResolvedMenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
