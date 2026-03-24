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
package com.web.coretix.appgeneral;

import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.web.coretix.applicationConstants.ApplicationSessionAttributes;
import java.io.File;
import java.util.*;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import com.web.coretix.constants.CoreAppModule;
import com.web.coretix.constants.RolePrivilegeConstants;
import com.web.coretix.constants.SessionAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mindrot.jbcrypt.BCrypt;


/**
 * @since 1.0
 * @author balamurali
 */
public class GenericManagedBean 
{
    private static final Logger logger = LoggerFactory.getLogger(GenericManagedBean.class);

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"; // CBC mode with padding
    private static final int IV_LENGTH = 16;  // 16 bytes for AES
    private static final int AES_KEY_SIZE = 256;  // 256-bit AES key

    private static final Map<String, String> propertyFileMap = new HashMap<>();

    private final String serverLocation = System.getProperty("catalina.base");
    private final String fileSeparator = System.getProperty("file.separator");

    @Inject
    private IRoleAdministrationService roleAdministrationService;
        

    // Method to generate a secret AES key
    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return keyGen.generateKey();
    }

    public  String getServerLogsLocation() {
        return serverLocation + fileSeparator + "logs";
    }

    public  String getServerTempLocation() {
        return serverLocation + fileSeparator + "temp";
    }


    // ================== PASSWORD HASHING METHODS (BCrypt) ==================

    /**
     * Hash a password using BCrypt with automatic salt generation.
     * This is the recommended method for storing passwords securely.
     *
     * @param plainTextPassword The plain text password to hash
     * @return The BCrypt hashed password (includes salt)
     */
    public static String hashPassword(String plainTextPassword) {
        // BCrypt automatically generates a salt and includes it in the hash
        // The default work factor is 10 (2^10 = 1024 rounds)
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify a plain text password against a BCrypt hashed password.
     *
     * @param plainTextPassword The plain text password to verify
     * @param hashedPassword The BCrypt hashed password from database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        try {
            logger.debug("inside verifyPassword method");
            //return true;
            logger.debug("plainTextPassword : "+plainTextPassword);
            logger.debug("hashedPassword : "+hashedPassword);
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (Exception e) {
            // If hash is invalid or not a BCrypt hash, return false
            return false;
        }
    }

    /**
     * This method is used to get location (Before download, Temporary files
     * created this location)
     *
     * @return an location
     */
    public String getApplicationDownloadFilesLocation()
    {
        String applicationDownloadFileLocation = "";

        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            applicationDownloadFileLocation = httpSession.getServletContext().getRealPath("")
                    + File.separator + ApplicationSessionAttributes.TEMP.getName() + File.separator + fetchCurrentUsername();
        }

        return applicationDownloadFileLocation;
    }

    /**
     * This method is used to get the file location for bulk download files.
     *
     * @return
     */
    public String getApplicationDownloadFilesLocationForBulkFile()
    {
        String applicationDownloadFileLocationForBulk = null;

        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            applicationDownloadFileLocationForBulk = File.separator + ApplicationSessionAttributes.TEMP.getName()
                    + File.separator + fetchCurrentUsername() + File.separator;
        }

        return applicationDownloadFileLocationForBulk;
    }
    
    /**
     * This method is used to fetch the current userId return currentUserId
     *
     * @return
     */
    public String fetchCurrentUsername()
    {
        String userLoginName = "";
        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            userLoginName = (String) httpSession.getAttribute(SessionAttributes.USERNAME.getName());
        }
        logger.trace("[PL] - User login name: " + userLoginName);
        return userLoginName;
    }

    /**
     *
     */
    public int fetchCurrentUserId()
    {
        int userId = 0;
        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            userId = (int) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName());
        }
        logger.trace("[PL] - User account ID: " + userId);


        return userId;
    }

    /**
     *
     */
    public int fetchCurrentUserRoleId()
    {
        int roleId = 0;
        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            roleId = (int) httpSession.getAttribute(SessionAttributes.ROLE_ID.getName());
        }
        logger.trace("[PL] - User account ID: " + roleId);
        return roleId;
    }


    /**
     * This method is used to get Http Session
     *
     * @return HttpSession
     */
    public HttpSession getHttpSession()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession httpSession = (HttpSession) facesContext.getExternalContext().getSession(false);
        if (httpSession != null)
        {
            if (httpSession.getAttribute(SessionAttributes.USERNAME.getName()) == null)
            {
                logger.info("[PL] - Generic Managed Bean User login name does not exist in this session. Navigating to the session expired page...");
                facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, "",
                        "session_expired");
                return null;
            }
            else
            {
                return httpSession;
            }
        }
        return null;
    }

    public List<CoreAppModule> getRoleModuleList()
    {
        logger.debug("[PL] - Generic Managed Bean Role module list");
        List<CoreAppModule> roleModuleList = new ArrayList<>();
        List<Integer> moduleList = roleAdministrationService.getModulesByRoleId(fetchCurrentUserRoleId());
        logger.debug("moduleList : "+moduleList);
        if (CollectionUtils.isNotEmpty(moduleList)){
            for (Integer moduleId : moduleList) {
                roleModuleList.add(CoreAppModule.getById(moduleId));
            }
        }
        else {
            logger.debug("moduleList is null or empty !");
        }

        return roleModuleList;
    }

    /**
     *
     */
    public List<Integer> getSubModuleDetailsByRoleandModuleId(int moduleId)
    {
        List<Integer> subModuleDetailsList = roleAdministrationService.getSubmodulesByRoleandModuleId(fetchCurrentUserRoleId(), moduleId);

        return subModuleDetailsList;
    }

    /**
     *  This method return the Module and Submodule privilege list.
     * @param moduleId
     * @param subModuleId
     * @return
     */
    public List<RolePrivilegeConstants> getModulePrivilegeList(int moduleId, int subModuleId)
    {
        List<RolePrivilegeConstants> modulePrivilegeList = new ArrayList<>();
        List<RolePrivileges> rolePrivilegesList = roleAdministrationService.getRolePrivilegesByModuleAndSubModule(fetchCurrentUserRoleId(), moduleId, subModuleId);

        if (CollectionUtils.isNotEmpty(rolePrivilegesList))
        {
            for (RolePrivileges rolePrivilege : rolePrivilegesList)
            {
                logger.debug("{}", rolePrivilege.getPrivilegeId());
                modulePrivilegeList.add(RolePrivilegeConstants.getById(rolePrivilege.getPrivilegeId()));
            }
        }
        else
        {
            logger.debug("rolePrivilegesList is empty !");
        }

        return modulePrivilegeList;
    }

    public Map<String, String> getPropertyFileMap()
    {
        return propertyFileMap;
    }
    public void setPropertyFileMap(Map<String, String> propertyFileMap)
    {
        this.propertyFileMap.putAll(propertyFileMap);
    }

    public void addPropertyFileMap(String key, String value)
    {
        this.propertyFileMap.put(key, value);
    }

    
    
    
    /**
     * This method returns the managed bean object for the given class name
     *
     * @param className
     * @return Object, object of the given managed bean class name
     */
    public Object getManagedBeanObject(String className)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELResolver resolver = facesContext.getApplication().getELResolver();
        return resolver.getValue(facesContext.getELContext(), null, className);
    }

}





