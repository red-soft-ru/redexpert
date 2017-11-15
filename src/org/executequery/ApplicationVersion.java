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

/**
 *
 * @author   Takis Diakoumis
 */
public final class ApplicationVersion {

    private final String version;

    int x = -1;

    int y = -1;

    int z = -1;

    int abc = -1;

    private final String build;

    public ApplicationVersion(String version, String build) {
        super();
        String xs = beforeDot(version);
        String temp = afterDot(version);
        x = Integer.parseInt(xs);
        if (temp.contains(".")) {
            String ys = beforeDot(temp);
            temp = afterDot(temp);
            y = Integer.parseInt(ys);
        }
        if (temp.contains(".")) {
            String zs = beforeDot(temp);
            temp = afterDot(temp);
            z = Integer.parseInt(zs);
        }
        abc = Integer.parseInt(temp);
        this.version = constructVersion();
        if (z == -1)
            z = abc;
        if (y == -1)
            y = z;
        this.build = build;
    }

    public ApplicationVersion(String version) {
        this(version, null);
    }

    public String constructVersion() {
        String s = "";
        if (x != -1)
            s += x;
        if (y != -1)
            s += "." + y;
        if (z != -1)
            s += "." + z;
        if (abc != -1)
            s += "." + abc;
        return s;
    }

    public String beforeDot(String s) {
        return s.substring(0, s.indexOf("."));
    }

    public String afterDot(String s) {
        return s.substring(s.indexOf(".") + 1);
    }

    public boolean isNewerThan(String anotherVersion) {
        if (anotherVersion != null && version != null) {
            ApplicationVersion anotherVers = new ApplicationVersion(anotherVersion);
            if (abc < 10000) {
                if (x > anotherVers.x)
                    return true;
                else if (x == anotherVers.x) {
                    if (y > anotherVers.y) {
                        return true;
                    } else if (y == anotherVers.y) {
                        if (z > anotherVers.z) {
                            return true;
                        } else if (z == anotherVers.z) {
                            return abc > anotherVers.abc;
                        }
                    }

                }
            }
        }
        return false;
    }

    public String getVersion() {

        return version;
    }

    public String getBuild() {

        return build;
    }

}

