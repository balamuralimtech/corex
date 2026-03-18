/*
 * SessionAttributeListener.java
 *
 * Created on Jun 01, 2018, 6:44:12 PM
 *
 * Copyright © 2013-2014 BMSolutions.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * BMSolutions.("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered
 * into with BMSolutions.
 */
package com.web.coretix.general;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


public class SessionAttributeListener implements HttpSessionAttributeListener
{

    private final Logger logger = Logger.getLogger(getClass());

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
