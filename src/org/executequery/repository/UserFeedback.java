/*
 * UserFeedback.java
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

package org.executequery.repository;

import java.util.HashMap;
import java.util.Map;

public class UserFeedback {

    private String name;
    
    private String email;
    
    private String remarks;
    
    private String type;

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

    public String getEmail() {
        return email;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getType() {
        return type;
    }
    
    public Map<String, String> asMap() {
        
        final Map<String, String> map = new HashMap<String, String>();

        map.put("remarks", getRemarks());
        map.put("name", getName());
        map.put("email", getEmail());
        map.put("type", getType());
        
        return map;
    }
    
}










