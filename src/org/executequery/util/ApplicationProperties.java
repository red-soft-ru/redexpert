/*
 * ApplicationProperties.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
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

public final class ApplicationProperties extends AbstractPropertiesBase {

    private static final String PROPERTY_BUNDLE_NAME = "system";

    private static final String PROPERTY_BUNDLE_PATH = "org/executequery/eq.system.properties";
    
    private static final ApplicationProperties instance = new ApplicationProperties();

    private ApplicationProperties() {
        
        if (!propertiesLoaded()) {

            loadPropertiesResource(propertyBundle(), PROPERTY_BUNDLE_PATH);
        }
        
    }

    public static synchronized ApplicationProperties getInstance() {

        return instance;
    }

    protected String propertyBundle() {

        return PROPERTY_BUNDLE_NAME;
    }

}











