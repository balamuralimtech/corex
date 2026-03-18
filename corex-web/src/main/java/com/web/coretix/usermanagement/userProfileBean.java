package com.web.coretix.usermanagement;

import com.persist.coretix.modal.usermanagement.UserDetails;
import com.module.coretix.usermanagement.IUserAdministrationService;
import org.apache.log4j.Logger;
import com.web.coretix.constants.AccessRightConstants;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("userProfileBean")
@Scope("session")
public class userProfileBean implements Serializable {

    private static final long serialVersionUID = 13543439334535435L;
    private final Logger logger = Logger.getLogger(getClass());
    
    @Inject
    private IUserAdministrationService userAdministrationService;

    private UserDetails userDetails;
    private String accessRight;



    public void initializePageAttributes() {
        userDetails = userAdministrationService.getUserDetailById(1);

        accessRight = AccessRightConstants.getById(userDetails.getAccessRight()).getValue();
        logger.debug(userDetails.getUserName());
        logger.debug("accessRight : "+accessRight);

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
