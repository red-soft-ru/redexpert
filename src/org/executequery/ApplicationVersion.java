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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Takis Diakoumis
 */
public final class ApplicationVersion {
    private final String version;

    int x = -1;

    int y = -1;

    int z = -1;

    int abc = 99999;

    String tag = "";

    int build = -1;

    Map<String, Integer> tagsValues = new HashMap<>();

    public ApplicationVersion(String version) {
        tagsValues.put("SNAPSHOT", 0);
        tagsValues.put("ALPHA", 1);
        tagsValues.put("BETA", 2);
        tagsValues.put("RC", 3);
        tagsValues.put("", 4);

        Pattern p = Pattern.compile("^(?<x>\\d+)\\.(?<y>\\d+)(\\.(?<z>\\d+))?(.(?<abc>\\d))?(-(?<tag>[a-zA-Z]+)(\\.(?<build>\\d+))?)?$");
        Matcher m = p.matcher(version);
        if(m.matches())
        {
            x = Integer.parseInt(m.group("x"));
            y = Integer.parseInt(m.group("y"));
            if (m.group("z") != null)
                z = Integer.parseInt(m.group("z"));
            if (m.group("abc") != null) {
                abc = Integer.parseInt(m.group("abc"));
            }
            if (m.group("tag") != null) {
                tag = m.group("tag");
                if (m.group("build") != null)
                    build = Integer.parseInt(m.group("build"));
            }
        }
        else
        {
            throw new java.lang.RuntimeException("Unable to parse version string " + version);
        }

        this.version = version;
    }

    public String constructVersion() {
        String s = null;
        if (z < 0)
            s = String.format("%d.%d", x, y);
        else
            s = String.format("%d.%d.%d", x, y, z);
        if (!tag.equals("")) {
            s += "-" + tag;
            if (build != -1)
                s += "." + build;
        }
        return s;
    }

    public boolean isNewerThan(String anotherVersion) {
        ApplicationVersion o = new ApplicationVersion(anotherVersion);
        if (x == o.x && y == o.y && z == o.z && abc == o.abc && tag.equals(o.tag) && build == o.build)
            return false;
        if (x != o.x) return x > o.x;
        if (y != o.y) return y > o.y;
        if (z != o.z) return z > o.z;
        if (abc != o.abc) return abc > o.abc;
        if (getTagValue() != o.getTagValue()) return getTagValue() > o.getTagValue();
        if (build != o.build) return build > o.build;
        return false;
    }

    public String getVersion() {

        return version;
    }

    public int getTagValue()
    {
        return tagsValues.getOrDefault(tag.toUpperCase(), -1);
    }
}
