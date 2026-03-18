package com.web.coretix.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.web.coretix.appgeneral.GenericManagedBean;
import org.apache.log4j.Logger;

public class PropertyUtils
{

    private static Logger logger = Logger.getLogger(PropertyUtils.class);
    /**
     * The file to load
     */
    private String fileName;

    /**
     * <p>
     *    Constructor which loads the properties file.
     * </p>
     *
     * @param propertyFileName - The properties file to load
     */
    public PropertyUtils(String propertyFileName)
    {
        this.fileName = propertyFileName;
    }

    /**
     * The constructor of property utils
     * @param propertyFile The property file
     */
    public PropertyUtils(File propertyFile)
    {
        this.fileName = propertyFile.toString();
    }

    /**
     * This is no-args constructor of property Utils
     */
    public PropertyUtils()
    {
    }

    public static String getPropertyKey(String property)
    {
        String propertyKey = "";
        StringTokenizer tokenizer = new StringTokenizer(property, "=");
        if (tokenizer.hasMoreTokens())
        {
            //the first token is property name
            propertyKey = tokenizer.nextToken();
        }
        return propertyKey;
    }

     public static String getPropertyValue(String property)
    {
        String propertyValue = "";
        StringTokenizer tokenizer = new StringTokenizer(property, "=");
        if (tokenizer.hasMoreTokens())
        {
            //skip the first token, it's the property name
            tokenizer.nextToken();
            //take the next token, it's the property value
            propertyValue = tokenizer.nextToken();
        }
        return propertyValue;
    }

    /**
     * <p>
     *    Used to get the property for the particular parameter
     * </p>
     * @return - The property of the passed key
     * @param key - The parameter whose property is needed
     * @throws java.io.IOException I/O Exception which occurs during read operation in the property file
     */
    public String getProperty(String key) throws IOException
    {
        String propertyValue = "";

        //Check whether the property propertyFile exists
        File propertyFile = new File(fileName);
        if (propertyFile.exists())
        {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            Properties properties = new Properties();
            properties.load(fileInputStream);
            //Get property
            propertyValue = properties.getProperty(key, "");
            //Close properties file
            fileInputStream.close();
        }

        //Return property value
        return propertyValue;
    }

 /**
     * Returns all the properties in the given property file
     * @return Properties The properties in the given file
     */
    public Properties getProperties()
    {
        Properties properties = null;
        File propertyFile = new File(fileName);
        if (propertyFile.exists())
        {
            FileInputStream fileInputStream = null;
            try
            {
                fileInputStream = new FileInputStream(fileName);
                properties = new Properties();
                properties.load(fileInputStream);

            } catch (Exception ex)
            {
                logger.error("Error", ex);
            }
            finally
            {
                if(fileInputStream != null)
                {
                    try
                    {
                        fileInputStream.close();
                    } catch (IOException ex)
                    {
                        logger.error("Error", ex);
                    }
                }
            }
        }
        return properties;
    }

    /**
     * <p>
     *    Used to set the property value of the particular
     *    parameter.
     * </p>
     * @param key - The key whose property is to be set
     * @param value - The value of the key whose property is to be set
     * @throws java.io.IOException The I/O Exception which occurs during the write operation
     */
    public void setProperty(String key, String value) throws IOException
    {
        //Check whether the properties file exists
        File propertyFile = new File(fileName);
        if (!propertyFile.exists())
        {
            propertyFile.createNewFile();
        }

        //Load properties
        FileInputStream fileInputStream = new FileInputStream(fileName);
        Properties properties = new Properties();
        properties.load(fileInputStream);

        //Set property
        properties.setProperty(key, value);

        //Store property
        FileOutputStream fileOutputStream = null;
        fileOutputStream = new FileOutputStream(fileName);
        properties.store(fileOutputStream, fileName);

        //Close properties file
        fileOutputStream.close();
    }

    /**
     * Sets the given property map in the property file.
     * @param propertyMap The property map which contains the property keys and values.
     * @throws java.io.IOException IO Exception
     */
    public void setProperty(Map propertyMap) throws IOException
    {
        //Check whether the properties file exists
        File propertyFile = new File(fileName);
        if (!propertyFile.exists())
        {
            propertyFile.createNewFile();
        }

        //Load properties
        FileInputStream fileInputStream = new FileInputStream(fileName);
        Properties properties = new Properties();
        properties.load(fileInputStream);

        //Set properties
        Iterator propertyKeyIterator = propertyMap.keySet().iterator();
        Iterator propertyValueIterator = propertyMap.values().iterator();

        while (propertyKeyIterator.hasNext())
        {
            properties.setProperty(propertyKeyIterator.next().toString(),
                    propertyValueIterator.next().toString());
        }

        //Store property
        FileOutputStream fileOutputStream = null;

        try
        {
            fileOutputStream = new FileOutputStream(fileName);
            properties.store(fileOutputStream, fileName);
        } catch (IOException ex)
        {
            throw ex;
        } finally
        {
            //Close properties file
            fileOutputStream.close();
        }
    }

    /**
     * Sets the given properties to the property file
     * @param properties The properties to be set
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     */
    public void setProperties(Properties properties) throws IOException, FileNotFoundException
    {
        File file = new File(fileName);
        /**
         * Check the file for existence. If the file not found.
         * Then create the file along and also directories, if the directories
         * not found along the path.
         *
         */
        if (!file.exists())
        {
            if (!file.getParentFile().exists())
            {
                file.getParentFile().mkdirs();
            }
        }
        try
        {
            /**
             * Here we are iterating the properties using the property keys.
             * Setting each key and value.
             */
            for (Enumeration property = properties.keys(); property.hasMoreElements();)
            {
                Object object = property.nextElement();
                setProperty(object.toString(), properties.getProperty((String) object));
                logger.info("Setting the property " + object.toString() + " = " +
                        properties.getProperty((String) object));
            }
        } catch (Exception e)
        {
            logger.error("Error: ", e);
        }
    }

    /**
     * Sets the properties loaded from the property file in the System class.
     *
     * Note: The property file is the one given when invoking this class through
     * the construcor. For example,<br/>
     * <code>
     * PropertyUtils propertyUtils = new PropertyUtils(propertyFile);
     * </code>
     */
    public void setSystemProperties()
    {
        setSystemProperties(getProperties());
    }

    /**
     * Set the properties of the given file in the System class
     * Note: This method is provided so that properties defined in multiple
     * property files can be loaded and set in the System class without having
     * to invoke the constructor of this class every time.
     * @param propertyFile, file whose properties are to be loaded
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void setSystemProperties(File propertyFile) throws IOException, FileNotFoundException
    {
        if (propertyFile.exists())
        {
            FileInputStream fileInputStream = new FileInputStream(propertyFile);
            Properties properties = new Properties();
            properties.load(fileInputStream);
            setSystemProperties(properties);
            fileInputStream.close();
        } else
        {
            throw new FileNotFoundException("Property file " + propertyFile.getAbsolutePath() + " not found");
        }
    }

    /**
     * This is overloaded method of setSystemProperties(File propertyFile).
     * Set the properties of the given file to the system properties.
     * @param propertyFile, property file object
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void setSystemProperties(String propertyFileName) throws IOException, FileNotFoundException
    {
        setSystemProperties(new File(propertyFileName));
    }

    /**
     * Sets the given properties in the System class.
     * ---------
     * Caution:-
     * ---------
     * We shouldn't use System.setProperties(Properties properties) method,
     * because it replaces the existing properties.
     *
     * @param properties The properties to be set
     */
    public void setSystemProperties(Properties properties)
    {
        Iterator iterator = properties.keySet().iterator();
        while(iterator.hasNext())
        {
            String key = iterator.next().toString();
            String value = properties.getProperty(key);
            System.setProperty(key, value);
        }
    }

    /**
     * Iterates through the given properties and prints them in the log file
     * @param properties The properties to be printed
     */
    public static void printProperties(Properties properties)
    {
        GenericManagedBean genericManagedBean = new GenericManagedBean();
        Iterator iterator = properties.keySet().iterator();
        while(iterator.hasNext())
        {
            Object key = iterator.next();
            Object value = properties.get(key);
            logger.info(key + ": " + value);
            genericManagedBean.addPropertyFileMap(key.toString(), value.toString());
            logger.debug(genericManagedBean.getPropertyFileMap().toString());
        }
    }
    /**
     * <pre>
     * validateProperties() method checks the existence of properties file ,
     *
     * i) If the file exists and the checkEveryProperty is true,
     * then it iterates every property and populates the missed default property.
     * It will not check every property when checkEveryProperty is false.
     *
     * ii)If the file not exists, then the default properties are populated
     * to the property file name.
     * </pre>
     *
     * @param propertyFileName, property file name.
     * @param defaultProperties, default properties in the property file name
     * @param checkEveryProperty, if true, this method checks every
     *                      property in the property file name.
     *
     */
    public void validateProperties(String propertyFileName,
            Properties defaultProperties, boolean checkEveryProperty)
    {
        this.fileName=propertyFileName;
        File file = new File(propertyFileName);
        if (!file.exists())
        {
            try
            {
                setProperties(defaultProperties);
            }
            catch (Exception exception)
            {
                logger.error("Error: ", exception);
            }
        }
        else
        {
//            logger.info(propertyFileName + " file found");
            try
            {
                /**
                 * When the checkEveryProperty is true, it means we need to
                 * check every property for the null or "" value.
                 * If the property value is null, it means there is no property.
                 * If the property value is "", it means there is no value set to that property.
                 * if the property value is either null or "", then we set the
                 * property for the property key.
                 */
                if (checkEveryProperty)
                {
//                    logger.info("Validating every property in the file " + propertyFileName);
                    for (Enumeration property = defaultProperties.keys(); property.hasMoreElements();)
                    {
                        Object object = property.nextElement();
                        String propertyValue = getProperty(object.toString());
                        if (propertyValue == null || propertyValue.trim().equalsIgnoreCase(""))
                        {
                            logger.info("Setting the property " + object.toString() + " = " +
                                    defaultProperties.getProperty((String) object));
                            setProperty(object.toString(),
                                    defaultProperties.getProperty((String) object));
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                logger.error("Error : ", exception);
            }
        }
    }
}
