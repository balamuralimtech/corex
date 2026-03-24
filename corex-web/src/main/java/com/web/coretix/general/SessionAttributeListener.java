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
package com.web.coretix.general;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SessionAttributeListener implements HttpSessionAttributeListener
{

    private static final Logger logger = LoggerFactory.getLogger(SessionAttributeListener.class);

    @Override
    public void attributeAdded(HttpSessionBindingEvent event)
    {
        String attributeName = event.getName();
        Object attributeValue = event.getValue();

        if (StringUtils.equalsIgnoreCase(attributeName, "notifyProperty")
                && attributeValue == Boolean.TRUE)
        {
            List<String> activeSessionList = new ArrayList<String>();
            if (CollectionUtils.isNotEmpty(activeSessionList))
            {
                int count = 0;
                for (String sessionObjectId : activeSessionList)
                {
//                    HttpSession session
//                            = SessionLifeCycleListener.getAssociatedSession(sessionObjectId);
//                    if (session != null && session.getId() != null)
//                    {
//                        session.setAttribute("notificationCount", count);
//                    }
                }

            }
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event)
    {
        logger.trace("Attribute Removed");
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event)
    {
        logger.trace("Attribute Replaced");
    }

}




