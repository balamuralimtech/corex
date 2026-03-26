package com.web.coretix.menu;

import java.util.Collections;
import java.util.List;

public class ResolvedMenuGroup {

    private final String id;
    private final String label;
    private final String icon;
    private final boolean rendered;
    private final List<ResolvedMenuItem> items;

    public ResolvedMenuGroup(String id, String label, String icon, boolean rendered, List<ResolvedMenuItem> items) {
        this.id = id;
        this.label = label;
        this.icon = icon;
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

    public boolean isRendered() {
        return rendered;
    }

    public List<ResolvedMenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
