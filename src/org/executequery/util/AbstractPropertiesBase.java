/*
 * AbstractPropertiesBase.java
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

package org.executequery.util;

import java.awt.Color;
import java.util.Properties;

import org.underworldlabs.util.SystemProperties;

abstract class AbstractPropertiesBase {

    /**
     * The name of the property bundle to be accessed.
     * 
     * @return property bundle name
     */
    protected abstract String propertyBundle();

    protected final boolean propertiesLoaded() {
        
        return SystemProperties.hasProperties(propertyBundle());
    }
    
    /**
     * Returns the properties object with the specified name in the cache.
     *
     * @param name - the name assigned to the properties bundle
     * @return the properties object or null if doesn't exist
     */
    public Properties getProperties(String name) {

        return SystemProperties.getProperties(name);
    }

    /**
     * Loads and stores in the cache the properties file at the 
     * specified package resource path. The properties are stored 
     * in the cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the package resource path to the properties file
     */
    protected final void loadPropertiesResource(String name, String path) {
        
        SystemProperties.loadPropertiesResource(name, path);
    }

    /**
     * Loads and stores in the cache the properties file at the 
     * specified file system path. The properties are stored in the
     * cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the file system to the properties file
     */
    protected final void loadProperties(String name, String path, Properties defaults) {
        
        SystemProperties.loadProperties(name, path, defaults);
    }

    /**
     * Loads and stores in the cache the properties file at the 
     * specified file system path. The properties are stored in the
     * cache under the specified name and accessed using that name.
     *
     * @param name - the cache name to store the props
     * @param path - the file system to the properties file
     */
    protected final void loadProperties(String name, String path) {
        
        SystemProperties.loadProperties(name, path);
    }

    /**
     * Returns the properties object with the specified name in the cache.
     *
     * @param columnName - the name assigned to the properties bundle
     * @return the properties object or null if doesn't exist
     */
    public final Properties getProperties() {

        return SystemProperties.getProperties(propertyBundle());
    }
    
    public final boolean containsKey(String key) {

        return SystemProperties.containsKey(propertyBundle(), key);
    }
    
    /**
     * Sets a property with a <code>String</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public final void setStringProperty(String key, String value) {

        SystemProperties.setProperty(propertyBundle(), key, value);
    }
    
    /**
     * Sets a property using the <code>Properties</code>
     * method <code>setProperty(key, value)</code><br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public final void setProperty(String key, String value) {
        
        SystemProperties.setProperty(propertyBundle(), key, value);
    }
    
    /**
     * Sets a property with an <code>int</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public final void setIntProperty(String key, int value) {

        SystemProperties.setIntProperty(propertyBundle(), key, value);
    }
    
    /**
     * Sets a property with a <code>boolean</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public final void setBooleanProperty(String key,  boolean value) {

        SystemProperties.setBooleanProperty(propertyBundle(), key, value);
    }
    
    /**
     * Sets a property with a <code>Color</code> value<br>
     *
     * @param key - the property key
     * @param value - the property value
     */
    public final void setColourProperty(String key, Color value) {
        
        SystemProperties.setColourProperty(propertyBundle(), key, value);
    }
    
    /**
     * Retrieves a property of type <code>int</code><br>
     *
     * @param key - the property key
     * @return - the <code>int</code> value
     */
    public final int getIntProperty(String key) {

        return SystemProperties.getIntProperty(propertyBundle(), key);
    }
    
    /**
     * Retrieves a property of type <code>boolean</code><br>
     *
     * @param key - the property key
     * @return the <code>boolean</code> value
     */
    public final boolean getBooleanProperty(String key) {

        return SystemProperties.getBooleanProperty(propertyBundle(), key);
    }
    
    /**
     * Retrieves a property of object type <code>Color</code><br>
     *
     * @param key - the property key
     * @return the <code>Color</code> value
     */
    public final Color getColourProperty(String key) {
        
        return SystemProperties.getColourProperty(propertyBundle(), key);
    }
    
    /**
     * Retrieves a property using the <code>Properties</code>
     * method <code>getProperty(key)</code><br>
     *
     * @param key - the property key
     * @return the property value
     */
    public final String getProperty(String key) {

        return SystemProperties.getProperty(propertyBundle(), key);
    }
    
    /**
     * Retrieves a property of object type <code>String</code><br>
     *
     * @param key - the property key
     * @return the <code>String</code> value
     */
    public final String getStringProperty(String key) {

        return getProperty(key);
    }
    
    /**
     * Returns the property with the specified name, formatting it with
     * the <code>java.text.MessageFormat.format()</code> method.
     *
     * @param key - the property key
     * @param args The positional parameters
     */
    public final String getProperty(String name, Object[] args) {

        return SystemProperties.getProperty(propertyBundle(), name, args);
    }

}










