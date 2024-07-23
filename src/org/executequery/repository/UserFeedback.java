/*
 * UserFeedback.java
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

package org.executequery.repository;

import java.util.HashMap;
import java.util.Map;

public class UserFeedback {

    private final String name;
    private final String type;
    private final String email;
    private final String remarks;

    public UserFeedback(String name, String email, String remarks, String type) {
        super();
        this.name = name;
        this.email = email;
        this.remarks = remarks;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, String> asMap() {

        Map<String, String> map = new HashMap<>();
        map.put("body", getRemarks());
        map.put("name", getName());
        map.put("email", getEmail());
        map.put("type", getType());
        map.put("project", "/api/website/projects/3/");
        map.put("version", System.getProperty("executequery.minor.version"));

        return map;
    }

}
