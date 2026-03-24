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

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.util.WidgetBuilder;

public class LayoutWidgetBuilder extends WidgetBuilder {

    public LayoutWidgetBuilder(FacesContext context) {
        super(context, null);
    }

    public LayoutWidgetBuilder ready(String widgetClass, String widgetVar, String id) throws IOException {
        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String pathname = req.getContextPath() + req.getServletPath();

        ResponseWriter rw = context.getResponseWriter();
        rw.startElement("script", null);
        rw.writeAttribute("id", id + "_s", null);
        rw.writeAttribute("type", "text/javascript", null);
        rw.write("$(function(){");
            rw.write("PrimeFaces.cw(\"");
            rw.write(widgetClass);
            rw.write("\",\"");
            rw.write(widgetVar);
            rw.write("\",{id:\"");
            rw.write(id);
            rw.write("\"");
            //attrs
            rw.write(",");
            rw.write("pathname:\"");
            rw.write(pathname);
            rw.write("\"");
        return this;
    }

    @Override
    public void finish() throws IOException {
        ResponseWriter rw = context.getResponseWriter();
        rw.write("});");

        rw.write("});");
        rw.endElement("script");
    }
}




