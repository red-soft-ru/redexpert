/*
 * SystemProperties.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.underworldlabs.util;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A convenience class to store and return system properties
 * not limited to the <code>String</code> datatype provided
 * by <code>java.util.Properties</code>.<br>
 * It provides the set and get methods for integer, string,
 * boolean and colour datatypes [and objects] where applicable.
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public final class SystemProperties {
    
    /** properties cache */
    private static Map<String,Properties> map;
    
    private SystemProperties() {}
    
    public static boolean hasProperties(String name) {
        
        return (map != null && map.containsKey(name)); 
    }
    
    /**
     * Loads and stores in the cache the properties file at the 
     * specified file system path. The properties are stored in the
     * cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the file system to the properties file
     */
    public static void loadProperties(String name, String path) {
        try {
            Properties properties = FileUtils.loadProperties(path);
            if (properties != null) {
                
                createPropertiesMap();
                
                map.put(name, properties);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads and stores in the cache the properties file at the 
     * specified file system path. The properties are stored in the
     * cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the file system to the properties file
     */
    public static void loadProperties(String name, 
                                String path, Properties defaults) {
        try {
            Properties properties = FileUtils.loadProperties(path, defaults);
            if (properties != null) {
                
                createPropertiesMap();
                
                map.put(name, properties);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads and stores in the cache the properties file at the 
     * specified package resource path. The properties are stored 
     * in the cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the package resource path to the properties file
     */
    public static void loadPropertiesResource(String name, String path) {
        try {

            Properties properties = FileUtils.loadPropertiesResource(path);
            
            if (properties != null) {
                
                createPropertiesMap();

                map.put(name, properties);
            }
        }
        catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    private static void createPropertiesMap() {

        if (map == null) {

            map = new HashMap<String, Properties>();
        }

    }

    /**
     * Returns the properties object with the specified name in the cache.
     *
     * @param name - the name assigned to the properties bundle
     * @return the properties object or null if doesn't exist
     */
    public static Properties getProperties(String name) {
        
        if (map.containsKey(name)) {
        
            return map.get(name);
        }

        return null;
    }
    
    public static final boolean containsKey(String propertiesName, String key) {
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            return properties.containsKey(key);
        }
        return false;
    }
    
    /**
     * Sets a property with a <code>String</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public static final void setStringProperty(String propertiesName, 
                                               String key, 
                                               String value) {
        setProperty(propertiesName, key, value);
    }
    
    /**
     * Sets a property using the <code>Properties</code>
     * method <code>setProperty(key, value)</code><br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public static final void setProperty(String propertiesName, 
                                         String key, 
                                         String value) {
        if (key == null || value == null) {
            return;
        }
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            properties.setProperty(key, value);
        }
    }
    
    /**
     * Sets a property with an <code>int</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public static final void setIntProperty(String propertiesName, 
                                            String key, 
                                            int value) {
        setProperty(propertiesName, key, Integer.toString(value));
    }
    
    /**
     * Sets a property with a <code>boolean</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public static final void setBooleanProperty(String propertiesName, 
                                                String key, 
                                                boolean value) {
        setProperty(propertiesName, key, value ? "true" : "false");
    }
    
    /**
     * Sets a property with a <code>Color</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public static final void setColourProperty(String propertiesName, 
                                               String key, 
                                               Color value) {
        if (value != null) {
            setProperty(propertiesName, key, Integer.toString(value.getRGB()));
        }
    }
    
    /**
     * Retrieves a property of type <code>int</code><br>
     *
     * @param key - the property key
     * @return - the <code>int</code> value
     */
    public static final int getIntProperty(String propertiesName, String key) {
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            return Integer.parseInt(properties.getProperty(key));
        }
        return -1;
    }
    
    /**
     * Retrieves a property of type <code>boolean</code><br>
     *
     * @param key - the property key
     * @return the <code>boolean</code> value
     */
    public static final boolean getBooleanProperty(String propertiesName, String key) {
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            return Boolean.valueOf(properties.getProperty(key)).booleanValue();
        }
        return false;
    }
    
    /**
     * Retrieves a property of object type <code>Color</code><br>
     *
     * @param key - the property key
     * @return the <code>Color</code> value
     */
    public static final Color getColourProperty(String propertiesName, String key) {
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            return new Color(Integer.parseInt(properties.getProperty(key)));
        }
        return null;
    }
    
    /**
     * Retrieves a property using the <code>Properties</code>
     * method <code>getProperty(key)</code><br>
     *
     * @param key - the property key
     * @return the property value
     */
    public static final String getProperty(String propertiesName, String key) {
        Properties properties = getProperties(propertiesName);
        if (properties != null) {
            return properties.getProperty(key);
            
        }
        return null;
    }
    
    /**
     * Retrieves a property of object type <code>String</code><br>
     *
     * @param key - the property key
     * @return the <code>String</code> value
     */
    public static final String getStringProperty(String propertiesName, String key) {
        return getProperty(propertiesName, key);
    }
    
    /**
     * Returns the property with the specified name, formatting it with
     * the <code>java.text.MessageFormat.format()</code> method.
     *
     * @param key - the property key
     * @param args The positional parameters
     */
    public static final String getProperty(String propertiesName, 
                                           String name, 
                                           Object[] args) {

        if (name == null) {
            return null;
        }

        Properties properties = getProperties(propertiesName);
        if (properties == null) {
            return null;
        }

        if (args == null) {
            return properties.getProperty(name, name);
        }
        else {
            return java.text.MessageFormat.format(
                                properties.getProperty(name, name), args);
        }
        
    }
    
}










