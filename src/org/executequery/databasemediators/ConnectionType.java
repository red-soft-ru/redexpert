package org.executequery.databasemediators;

import org.executequery.localization.Bundles;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public enum ConnectionType {

    PURE_JAVA,
    NATIVE,
    EMBEDDED;

    public static boolean contains(String name) {
        return Arrays.stream(ConnectionType.values()).anyMatch(type -> Objects.equals(type.name(), name));
    }

    public static ConnectionType getConnType(boolean isNative, boolean isEmbedded) {
        return isEmbedded ? EMBEDDED : isNative ? NATIVE : PURE_JAVA;
    }

    public String value(boolean useOOApi) {
        return useOOApi && !Objects.equals(name(), PURE_JAVA.name()) ?
                "FBOO" + name() :
                name();
    }

    public String label() {
        return Bundles.get(ConnectionType.class, name());
    }

    @Override
    public String toString() {
        return label();
    }

}
