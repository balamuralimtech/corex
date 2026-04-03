package com.web.coretix.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

@Named("dynamicMenu")
@RequestScoped
public class DynamicMenuBean {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMenuBean.class);

    @Inject
    private Instance<MenuContributor> contributors;

    public MenuModel getModel() {
        List<AppMenuGroup> groups = new ArrayList<>();
        int contributorCount = 0;

        for (MenuContributor contributor : contributors) {
            contributorCount++;
            logger.debug("Dynamic menu contributor detected: {}", contributor.getClass().getName());
            groups.addAll(contributor.contribute());
        }

        List<ResolvedMenuGroup> resolvedGroups = groups.stream()
                .sorted(Comparator.comparingInt(AppMenuGroup::getOrder))
                .map(this::resolveGroup)
                .collect(Collectors.toList());

        DefaultMenuModel model = new DefaultMenuModel();
        int renderedGroups = 0;

        for (ResolvedMenuGroup group : resolvedGroups) {
            if (!group.isRendered()) {
                continue;
            }

            DefaultSubMenu subMenu = DefaultSubMenu.builder()
                    .id(safeId(group.getId(), "group_" + renderedGroups))
                    .label(group.getLabel())
                    .icon(group.getIcon())
                    .rendered(true)
                    .build();

            int renderedItems = 0;
            for (ResolvedMenuItem item : group.getItems()) {
                if (!item.isRendered()) {
                    continue;
                }

                addMenuElement(subMenu, item, subMenu.getId() + "_item_" + renderedItems);
                renderedItems++;
            }

            if (!subMenu.getElements().isEmpty()) {
                model.getElements().add(subMenu);
                renderedGroups++;
                logger.debug("Menu group [{}] rendered with {} items", group.getLabel(), subMenu.getElements().size());
            }
        }

        logger.debug("Dynamic menu built {} rendered groups from {} contributors", renderedGroups, contributorCount);
        return model;
    }

    private ResolvedMenuGroup resolveGroup(AppMenuGroup group) {
        List<ResolvedMenuItem> items = group.getItems().stream()
                .sorted(Comparator.comparingInt(AppMenuItem::getOrder))
                .map(this::resolveItem)
                .collect(Collectors.toList());

        return new ResolvedMenuGroup(
                group.getId(),
                resolveText(group.getLabel()),
                group.getIcon(),
                resolveBoolean(group.getRenderedExpression()),
                items);
    }

    private ResolvedMenuItem resolveItem(AppMenuItem item) {
        List<ResolvedMenuItem> items = item.getItems().stream()
                .sorted(Comparator.comparingInt(AppMenuItem::getOrder))
                .map(this::resolveItem)
                .collect(Collectors.toList());

        return new ResolvedMenuItem(
                item.getId(),
                resolveText(item.getLabel()),
                item.getIcon(),
                item.getUrl(),
                resolveBoolean(item.getRenderedExpression()),
                items);
    }

    private void addMenuElement(DefaultSubMenu parent, ResolvedMenuItem item, String fallbackId) {
        if (!item.getItems().isEmpty()) {
            DefaultSubMenu nestedMenu = DefaultSubMenu.builder()
                    .id(safeId(item.getId(), fallbackId))
                    .label(item.getLabel())
                    .icon(item.getIcon())
                    .rendered(true)
                    .build();

            int renderedChildren = 0;
            for (ResolvedMenuItem child : item.getItems()) {
                if (!child.isRendered()) {
                    continue;
                }

                addMenuElement(nestedMenu, child, nestedMenu.getId() + "_item_" + renderedChildren);
                renderedChildren++;
            }

            if (!nestedMenu.getElements().isEmpty()) {
                parent.getElements().add(nestedMenu);
            }
            return;
        }

        DefaultMenuItem menuItem = DefaultMenuItem.builder()
                .id(safeId(item.getId(), fallbackId))
                .value(item.getLabel())
                .icon(item.getIcon())
                .url(buildContextRelativeUrl(item.getUrl()))
                .ajax(false)
                .rendered(true)
                .build();
        parent.getElements().add(menuItem);
    }

    private boolean resolveBoolean(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        Object value = evaluate(expression, Boolean.class);
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }

    private String resolveText(String value) {
        if (value == null) {
            return "";
        }

        if (!value.trim().startsWith("#{")) {
            return value;
        }

        Object evaluated = evaluate(value, Object.class);
        return evaluated == null ? "" : String.valueOf(evaluated);
    }

    private Object evaluate(String expression, Class<?> expectedType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = facesContext.getApplication().getExpressionFactory();
        ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, expression, expectedType);
        return valueExpression.getValue(elContext);
    }

    private String buildContextRelativeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "#";
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        String contextPath = facesContext.getExternalContext().getRequestContextPath();
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith(contextPath)) {
            return url;
        }
        return contextPath + url;
    }

    private String safeId(String candidate, String fallback) {
        String value = (candidate == null || candidate.trim().isEmpty()) ? fallback : candidate.trim();
        return value.replaceAll("[^A-Za-z0-9_\\-]", "_");
    }
}
