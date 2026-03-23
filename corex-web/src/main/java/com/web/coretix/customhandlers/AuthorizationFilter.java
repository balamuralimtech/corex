package com.web.coretix.customhandlers;

import com.web.coretix.constants.SessionAttributes;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        String login2URI = req.getContextPath() + "/login2.xhtml";
        String loginURI = req.getContextPath() + "/login.xhtml";
        String videoURI = req.getContextPath() + "/resources/avalon-layout/videos/home.mp4";
        String errorPagesURI = req.getContextPath() + "/pages/errorandwarningpages/";

        boolean loggedIn = (session != null && session.getAttribute(SessionAttributes.USERNAME.getName()) != null);
        boolean loginRequest = req.getRequestURI().equals(loginURI);
        boolean login2Request = req.getRequestURI().equals(login2URI);
        boolean videoRequest = req.getRequestURI().startsWith(videoURI);
        boolean errorPageRequest = req.getRequestURI().startsWith(errorPagesURI);
        boolean resourceRequest = req.getRequestURI().startsWith(req.getContextPath() + "/javax.faces.resource");

        if (loggedIn || loginRequest || login2Request || videoRequest || errorPageRequest || resourceRequest) {
            chain.doFilter(request, response); // User is logged in, so continue with the request.
        } else {
            res.sendRedirect(login2URI); // Not logged in, redirect to login page.
        }
    }

    @Override
    public void destroy() {
    }
}
