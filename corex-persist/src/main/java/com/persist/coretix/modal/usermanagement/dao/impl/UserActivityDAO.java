/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
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
 * Project: app.name
 */
package com.persist.coretix.modal.usermanagement.dao.impl;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author Pragadeesh
 */
@Named
public class UserActivityDAO implements IUserActivityDAO 
{
    
     private static final Logger logger = LoggerFactory.getLogger(UserActivityDAO.class);
     
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addUserActivity(UserActivities useractivity) {
        logger.debug("inside dao save useractivity !!");
        Session session = getSessionFactory().openSession();
        Transaction trans = null;
        try {
            trans = session.beginTransaction();
            session.save(useractivity);
            trans.commit();
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Error saving user activity", e);
        } finally {
            session.close();
        }
    }


    public Map<String, Integer> getActivityTypeCounts() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = null;
        Map<String, Integer> activityTypeCounts = new HashMap<>();

        try {
            trans = session.beginTransaction();

            // Optimized query to get count of each activity type
            List<Object[]> results = session.createQuery(
                    "SELECT LOWER(activityType), COUNT(activityType) FROM UserActivities " +
                            "WHERE LOWER(activityType) IN ('login', 'logout', 'add', 'update', 'delete') " +
                            "GROUP BY LOWER(activityType)"
            ).list();

            // Process results and add them to the map
            for (Object[] row : results) {
                String activityType = ((String) row[0]).toLowerCase();
                Long count = (Long) row[1];
                activityTypeCounts.put(activityType, count.intValue());
            }

            // Add missing activity types with count 0 if they are not in results
            Arrays.asList("login", "logout", "add", "update", "delete").forEach(
                    type -> activityTypeCounts.putIfAbsent(type.toLowerCase(), 0)
            );

            trans.commit();
        } catch (Exception e) {
            if (trans != null) trans.rollback();
            e.printStackTrace();
        }

        return activityTypeCounts;
    }


    public List<UserActivities> getUserActivitiesList() {
        Session session = getSessionFactory().openSession();
        Transaction trans = null;
        List<UserActivities> list = null;
        try {
            trans = session.beginTransaction();
            list = (List<UserActivities>) session.createQuery("from UserActivities order by createdAt desc").list();
            trans.commit();
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            logger.error("Error fetching user activities list", e);
        } finally {
            session.close();
        }
        return list;
    }
    
}





