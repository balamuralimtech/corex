package com.web.coretix.general;

import com.web.coretix.constants.LoginConstants;
import com.web.coretix.constants.SessionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionAuditServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuditServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");

        if (session == null || session.getAttribute(SessionAttributes.USERNAME.getName()) == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if ("heartbeat".equalsIgnoreCase(action)) {
            SessionAuditSupport.touchSession(session);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if ("browser-close".equalsIgnoreCase(action)) {
            logger.debug("Received browser close audit for session {}", session.getId());
            SessionAuditSupport.auditSessionTermination(session, LoginConstants.LOGOUT_SUCCESSFUL, "BROWSER_CLOSE", true);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported session audit action");
    }
}
