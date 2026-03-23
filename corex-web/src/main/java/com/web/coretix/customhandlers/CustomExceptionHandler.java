    package com.web.coretix.customhandlers;

import java.io.IOException;
import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import org.apache.log4j.Logger;

/**
 * Custom exception handler to gracefully handle JSF view state restoration errors
 * Handles IndexOutOfBoundsException and ViewExpiredException that occur during view state restoration
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    private static final Logger logger = Logger.getLogger(CustomExceptionHandler.class);
    private static final String SESSION_TIMEOUT_PAGE = "/session-timeout";
    private static final String INTERNAL_SERVER_ERROR_PAGE = "/internal-server-error";
    private ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

        while (iterator.hasNext()) {
            ExceptionQueuedEvent event = iterator.next();
            Throwable throwable = event.getContext().getException();

            // Check if it's a ViewExpiredException (session timeout or view state lost)
            if (throwable instanceof ViewExpiredException) {
                FacesContext facesContext = FacesContext.getCurrentInstance();

                try {
                    ViewExpiredException vee = (ViewExpiredException) throwable;
                    logger.warn("ViewExpiredException detected for view: " + vee.getViewId()
                            + ". Redirecting to session timeout page.", throwable);
                    redirectToPage(facesContext, SESSION_TIMEOUT_PAGE);
                } catch (IOException e) {
                    logger.error("Failed to redirect after ViewExpiredException", e);
                    throw new FacesException("Error handling ViewExpiredException", e);
                } finally {
                    // Remove the exception from the queue
                    iterator.remove();
                }
            }
            // Check if it's an IndexOutOfBoundsException (state restoration issue)
            else if (isViewStateRestorationError(throwable)) {
                FacesContext facesContext = FacesContext.getCurrentInstance();

                try {
                    logger.warn("View state restoration error detected. Redirecting to session timeout page.",
                            throwable);
                    redirectToPage(facesContext, SESSION_TIMEOUT_PAGE);
                } catch (IOException e) {
                    logger.error("Failed to redirect after view state error", e);
                    throw new FacesException("Error handling view state restoration failure", e);
                } finally {
                    // Remove the exception from the queue
                    iterator.remove();
                }
            } else {
                FacesContext facesContext = FacesContext.getCurrentInstance();

                try {
                    logger.error("Unhandled JSF exception detected. Redirecting to internal server error page.",
                            throwable);
                    redirectToPage(facesContext, INTERNAL_SERVER_ERROR_PAGE);
                } catch (IOException e) {
                    logger.error("Failed to redirect after unhandled JSF exception", e);
                    throw new FacesException("Error handling unhandled JSF exception", e);
                } finally {
                    iterator.remove();
                }
            }
        }

        if (!FacesContext.getCurrentInstance().getResponseComplete()) {
            getWrapped().handle();
        }
    }

    /**
     * Checks if the exception is related to view state restoration
     * @param throwable The exception to check
     * @return true if it's a view state restoration error
     */
    private boolean isViewStateRestorationError(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        // Check if it's an IndexOutOfBoundsException
        if (throwable instanceof IndexOutOfBoundsException) {
            // Check the stack trace to confirm it's from JSF state restoration
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (className.contains("AttachedObjectListHolder")
                    || className.contains("restoreState")
                    || className.contains("FaceletPartialStateManagementStrategy")) {
                    return true;
                }
            }
        }

        // Check cause recursively
        return isViewStateRestorationError(throwable.getCause());
    }

    private void redirectToPage(FacesContext facesContext, String pagePath) throws IOException {
        String contextPath = facesContext.getExternalContext().getRequestContextPath();
        String redirectUrl = contextPath + pagePath;
        facesContext.getExternalContext().redirect(redirectUrl);
        facesContext.responseComplete();
        logger.debug("Successfully redirected to: " + redirectUrl);
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }
}
