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
package com.web.coretix.usermanagement;

import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.usermanagement.IUserAdministrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.web.coretix.constants.AccessRightConstants;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.general.SessionUtils;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import javax.servlet.http.HttpSession;

@Named("userProfileBean")
@Scope("session")
public class userProfileBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private static final Logger logger = LoggerFactory.getLogger(userProfileBean.class);
    
    @Inject
    private IUserAdministrationService userAdministrationService;

    private UserDetails userDetails;
    private String accessRight;

    public void initializePageAttributes() {
        HttpSession session = SessionUtils.getSession();
        Integer userId = session == null
                ? null
                : (Integer) session.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());

        if (userId == null) {
            logger.warn("No user id found in session while loading user profile");
            userDetails = null;
            accessRight = null;
            return;
        }

        userDetails = userAdministrationService.getUserDetailById(userId);
        if (userDetails == null) {
            logger.warn("No user details found for user id {}", userId);
            accessRight = null;
            return;
        }

        accessRight = AccessRightConstants.getById(userDetails.getAccessRight()).getValue();
        logger.debug("{}", userDetails.getUserName());
        logger.debug("accessRight : " + accessRight);

    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }


    public String getAccessRight() {
        return accessRight;
    }

    public void setAccessRight(String accessRight) {
        this.accessRight = accessRight;
    };

}





