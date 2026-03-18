
package com.web.coretix.appgeneral;

import com.module.coretix.usermanagement.IRoleAdministrationService;
import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.web.coretix.applicationConstants.ApplicationSessionAttributes;
import java.io.File;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import com.web.coretix.constants.AppModule;
import com.web.coretix.constants.RolePrivilegeConstants;
import com.web.coretix.constants.SessionAttributes;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;


/**
 * @since 1.0
 * @author balamurali
 */
public class GenericManagedBean 
{
    public Logger logger = Logger.getLogger(getClass());

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

    // Method to encrypt a plain text
    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);

        // Generate a random IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // Initialize the cipher in encryption mode
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        // Encrypt the plain text
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Combine IV and encrypted message, and Base64 encode
        byte[] combinedIvAndCipherText = new byte[IV_LENGTH + encryptedBytes.length];
        System.arraycopy(iv, 0, combinedIvAndCipherText, 0, IV_LENGTH);
        System.arraycopy(encryptedBytes, 0, combinedIvAndCipherText, IV_LENGTH, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combinedIvAndCipherText);
    }

    // Method to decrypt an encrypted text
    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);

        // Decode the Base64 encoded message
        byte[] combinedIvAndCipherText = Base64.getDecoder().decode(encryptedText);

        // Extract the IV and the encrypted message
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combinedIvAndCipherText, 0, iv, 0, IV_LENGTH);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] cipherText = new byte[combinedIvAndCipherText.length - IV_LENGTH];
        System.arraycopy(combinedIvAndCipherText, IV_LENGTH, cipherText, 0, cipherText.length);

        // Initialize the cipher in decryption mode
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        // Decrypt and return the plain text
        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, "UTF-8");
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

    public List<AppModule> getRoleModuleList()
    {
        logger.debug("[PL] - Generic Managed Bean Role module list");
        List<AppModule> roleModuleList = new ArrayList<>();
        List<Integer> moduleList = roleAdministrationService.getModulesByRoleId(fetchCurrentUserRoleId());
        logger.debug("moduleList : "+moduleList);
        if (CollectionUtils.isNotEmpty(moduleList)){
            for (Integer moduleId : moduleList) {
                roleModuleList.add(AppModule.getById(moduleId));
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
                logger.debug(rolePrivilege.getPrivilegeId());
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
