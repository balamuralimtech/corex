package com.web.coretix.config;

import com.web.coretix.utils.JdbcCryptoUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptablePropertyConfigurer extends PropertyPlaceholderConfigurer {

    @Override
    protected String convertPropertyValue(String originalValue) {
        if (originalValue != null && originalValue.startsWith("ENC(") && originalValue.endsWith(")")) {
            String encryptedValue = originalValue.substring(4, originalValue.length() - 1);
            return JdbcCryptoUtils.decrypt(encryptedValue);
        }
        return originalValue;
    }
}
