package com.web.coretix.filter;

import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.general.SessionAuditSupport;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionExpiryFilter implements Filter {

    @Override
    public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        // Check if the session is null or expired
        if (session == null || session.getAttribute(SessionAttributes.USERNAME.getName()) == null) {
            res.sendRedirect(req.getContextPath() + "/home");
        } else {
            SessionAuditSupport.touchSession(session);
            chain.doFilter(request, response); // Continue with the request if session is valid
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }
}
