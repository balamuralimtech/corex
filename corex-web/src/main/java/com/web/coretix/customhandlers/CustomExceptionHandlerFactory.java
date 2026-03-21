package com.web.coretix.customhandlers;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

/**
 * Factory class for creating CustomExceptionHandler instances
 * This factory is registered in faces-config.xml to integrate with JSF
 */
public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

    private ExceptionHandlerFactory parent;

    public CustomExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler parentHandler = parent.getExceptionHandler();
        return new CustomExceptionHandler(parentHandler);
    }
}
