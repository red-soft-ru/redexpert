/*
 * ApplicationVersion.java
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

package org.executequery;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Takis Diakoumis
 */
public final class ApplicationVersion {

    public static final int RC = 0;
    public static final int RELEASE = RC + 1;
    public static final int SNAPSHOT = RELEASE + 1;

    private final static String VERSION_PATTERN =
            "^(?<x>\\d+)\\.(?<y>\\d+)(\\.(?<z>\\d+))?(-(?<tag>[a-zA-Z]+)(\\.(?<build>\\d+))?)?$";

    private final static Map<String, Integer> TAGS_VALUES = new HashMap<String, Integer>() {{
        put("RC", RC);
        put("", RELEASE);
        put("SNAPSHOT", SNAPSHOT);
    }};

    private int build;
    private int xValue;
    private int yValue;
    private int zValue;
    private String tag;

    private String version;

    public ApplicationVersion(String version) {
        tag = "";
        build = -1;
        xValue = -1;
        yValue = -1;
        zValue = -1;

        setVersion(version);
    }

    public void setVersion(String version) {
        Matcher matcher = Pattern.compile(VERSION_PATTERN).matcher(version);

        if (matcher.matches()) {
            xValue = Integer.parseInt(matcher.group("x"));
            yValue = Integer.parseInt(matcher.group("y"));

            if (matcher.group("z") != null)
                zValue = Integer.parseInt(matcher.group("z"));

            if (matcher.group("tag") != null) {
                tag = matcher.group("tag");
                if (matcher.group("build") != null)
                    build = Integer.parseInt(matcher.group("build"));
            }

        } else
            throw new java.lang.RuntimeException("Unable to parse version string " + version);

        this.version = version;
    }

    public boolean isNewerThan(String version) {
        ApplicationVersion comparedVersion = new ApplicationVersion(version);

        if (!Objects.equals(xValue, comparedVersion.xValue))
            return xValue > comparedVersion.xValue;

        if (!Objects.equals(yValue, comparedVersion.yValue))
            return yValue > comparedVersion.yValue;

        if (!Objects.equals(zValue, comparedVersion.zValue))
            return zValue > comparedVersion.zValue;

        if (!Objects.equals(tag, comparedVersion.tag))
            return getTagValue() > comparedVersion.getTagValue();

        if (!Objects.equals(build, comparedVersion.build))
            return build > comparedVersion.build;

        return false;
    }

    public String getVersion() {
        return version;
    }

    public int getTagValue() {
        return TAGS_VALUES.getOrDefault(tag.toUpperCase(), -1);
    }

}
