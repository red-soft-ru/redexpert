package org.executequery.util;

import org.underworldlabs.util.SystemProperties;

import java.util.Properties;

public final class AuditProperties extends AbstractPropertiesBase {

    private static final String PROPERTY_BUNDLE_NAME = "audit";

    private static final String USER_SETTINGS_FILE_KEY = "audit.properties";

    private static final String DEFAULT_PROPERTIES_BUNDLE_NAME = "defaults";

    private static final String DEFAULT_PROPERTIES_BUNDLE_PATH = "org/executequery/audit.default.properties";

    private static AuditProperties instance;

    private AuditProperties() {

        if (!SystemProperties.hasProperties(DEFAULT_PROPERTIES_BUNDLE_NAME)) {

            loadPropertiesResource(
                    DEFAULT_PROPERTIES_BUNDLE_NAME, DEFAULT_PROPERTIES_BUNDLE_PATH);
        }

        if (!SystemProperties.hasProperties(PROPERTY_BUNDLE_NAME)) {

            Properties defaults = getProperties(DEFAULT_PROPERTIES_BUNDLE_NAME);

            loadProperties(PROPERTY_BUNDLE_NAME, userPropertiesPath(), defaults);
        }

    }

    public static synchronized AuditProperties getInstance() {

        if (instance == null) {

            instance = new AuditProperties();
        }

        return instance;
    }

    protected String propertyBundle() {

        return PROPERTY_BUNDLE_NAME;
    }

    private String userPropertiesPath() {

        UserSettingsProperties settings = new UserSettingsProperties();

        return settings.getUserSettingsDirectory() + USER_SETTINGS_FILE_KEY;
    }

}
