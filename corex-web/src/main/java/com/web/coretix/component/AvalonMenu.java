/*
 * Copyright (c) 2026 `company.name`. All rights reserved.
 *
 * This software and its associated documentation are proprietary to `company.name`.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: `app.name`
 */
package com.web.coretix.component;

import javax.faces.component.UIComponent;
import org.primefaces.component.menu.AbstractMenu;
import javax.faces.context.FacesContext;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import org.primefaces.component.api.Widget;

@ListenerFor(sourceClass = AvalonMenu.class, systemEventClass = PostAddToViewEvent.class)
public class AvalonMenu extends AbstractMenu implements Widget,ComponentSystemEventListener {

    public static final String COMPONENT_TYPE = "com.web.coretix.component.AvalonMenu";
    public static final String COMPONENT_FAMILY = "com.web.coretix.component";
    private static final String DEFAULT_RENDERER = "com.web.coretix.component.AvalonMenuRenderer";
    private static final String[] LEGACY_RESOURCES = new String[]{"primefaces.css","jquery/jquery.js","jquery/jquery-plugins.js","primefaces.js"};
    private static final String[] MODERN_RESOURCES = new String[]{"components.css","jquery/jquery.js","jquery/jquery-plugins.js","core.js"};
    
    protected enum PropertyKeys {

        widgetVar, model, style, styleClass, statefulScroll;

        String toString;

        PropertyKeys(String toString) {
            this.toString = toString;
        }

        PropertyKeys() {
        }

        public String toString() {
            return ((this.toString != null) ? this.toString : super.toString());
        }
    }

    public AvalonMenu() {
        setRendererType(DEFAULT_RENDERER);
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public java.lang.String getWidgetVar() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.widgetVar, null);
    }

    public void setWidgetVar(java.lang.String _widgetVar) {
        getStateHelper().put(PropertyKeys.widgetVar, _widgetVar);
    }

    public org.primefaces.model.menu.MenuModel getModel() {
        return (org.primefaces.model.menu.MenuModel) getStateHelper().eval(PropertyKeys.model, null);
    }

    public void setModel(org.primefaces.model.menu.MenuModel _model) {
        getStateHelper().put(PropertyKeys.model, _model);
    }

    public java.lang.String getStyle() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.style, null);
    }

    public void setStyle(java.lang.String _style) {
        getStateHelper().put(PropertyKeys.style, _style);
    }

    public java.lang.String getStyleClass() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.styleClass, null);
    }

    public void setStyleClass(java.lang.String _styleClass) {
        getStateHelper().put(PropertyKeys.styleClass, _styleClass);
    }

    public java.lang.Boolean isStatefulScroll() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.statefulScroll, true);
    }

    public void setStatefulScroll(boolean _statefulScroll) {
        getStateHelper().put(PropertyKeys.statefulScroll, _statefulScroll);
    }

    public String resolveWidgetVar() {
        FacesContext context = getFacesContext();
        String userWidgetVar = (String) getAttributes().get("widgetVar");

        if (userWidgetVar != null) {
            return userWidgetVar;
        } else {
            return "widget_" + getClientId(context).replaceAll("-|" + UINamingContainer.getSeparatorChar(context), "_");
        }
    }
    
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        if(event instanceof PostAddToViewEvent) {
            FacesContext context = getFacesContext();
            UIViewRoot root = context.getViewRoot();
            
            boolean isPrimeConfig;
            try {
                isPrimeConfig = Class.forName("org.primefaces.config.PrimeConfiguration") != null;
            } catch (ClassNotFoundException e) {
                isPrimeConfig = false;
            }

            String[] resources = (isPrimeConfig) ? MODERN_RESOURCES : LEGACY_RESOURCES;

            for(String res : resources) {
                UIComponent component = context.getApplication().createComponent(UIOutput.COMPONENT_TYPE);
                if(res.endsWith("css"))
                    component.setRendererType("javax.faces.resource.Stylesheet");
                else if(res.endsWith("js"))
                    component.setRendererType("javax.faces.resource.Script");

                component.getAttributes().put("library", "primefaces");
                component.getAttributes().put("name", res);

                root.addComponentResource(context, component);
            }
        }
    }
}



