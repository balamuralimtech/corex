package com.web.coretix.customhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.ViewExpiredException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ApplicationExceptionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionFilter.class);
    private static final String INTERNAL_SERVER_ERROR_VIEW = "/pages/errorandwarningpages/500internalservererror.xhtml";
    private static final String SESSION_TIMEOUT_REDIRECT = "/login";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (Throwable throwable) {
            if (isErrorPageRequest(httpRequest)) {
                rethrow(throwable);
                return;
            }

            logger.error("Unhandled web exception for request {}", httpRequest.getRequestURI(), throwable);
            Throwable rootCause = resolveRootCause(throwable);
            boolean sessionExpired = rootCause instanceof ViewExpiredException;
            String targetView = sessionExpired ? null : INTERNAL_SERVER_ERROR_VIEW;

            if (httpResponse.isCommitted()) {
                rethrow(throwable);
                return;
            }

            request.setAttribute("javax.servlet.error.exception", throwable);
            request.setAttribute("javax.servlet.error.request_uri", httpRequest.getRequestURI());
            request.setAttribute("javax.servlet.error.status_code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.reset();
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            if ("partial/ajax".equals(httpRequest.getHeader("Faces-Request"))) {
                String redirectTarget = httpRequest.getContextPath()
                        + (sessionExpired ? SESSION_TIMEOUT_REDIRECT : "/internal-server-error");
                httpResponse.setContentType("text/xml");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                httpResponse.getWriter().write("<partial-response><redirect url=\"" + redirectTarget + "\"/></partial-response>");
                httpResponse.getWriter().flush();
                return;
            }

            if (sessionExpired) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + SESSION_TIMEOUT_REDIRECT);
                return;
            }

            RequestDispatcher dispatcher = request.getRequestDispatcher(targetView);
            dispatcher.forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isErrorPageRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/pages/errorandwarningpages/");
    }

    private Throwable resolveRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private void rethrow(Throwable throwable) throws IOException, ServletException {
        if (throwable instanceof IOException) {
            throw (IOException) throwable;
        }
        if (throwable instanceof ServletException) {
            throw (ServletException) throwable;
        }
        throw new ServletException(throwable);
    }
}
