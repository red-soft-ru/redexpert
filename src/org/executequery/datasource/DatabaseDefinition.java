/*
 * DatabaseDefinition.java
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

package org.executequery.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a database definition with appropriate JDBC url patterns.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class DatabaseDefinition implements java.io.Serializable {

	private static final long serialVersionUID = -5412871239099800368L;

	public static final int INVALID_DATABASE_ID = -1;
	
	private int id;
    private String name;
    private List<String> urls;
    
    /**
     * Creates a new instance of DatabaseDefinition
     */
    public DatabaseDefinition() {}

    public DatabaseDefinition(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addUrlPattern(String urlPattern) {
        if (urls == null) {
            urls = new ArrayList<String>();
        }
        urls.add(urlPattern);
    }

    public int getUrlCount() {
        if (urls == null) {
            return 0;
        }
        return urls.size();
    }

    public boolean hasUrl(String url) {
        if (urls == null || urls.isEmpty()) {
            return false;
        }
        for (int i = 0, n = urls.size(); i < n; i++) {
            if (url.equals(urls.get(i))) {
                return true;
            }
        }
        return false;
    }

    public int getUrlIndex(String url) {
        if (urls == null || urls.isEmpty()) {
            return -1;
        }
        for (int i = 0, n = urls.size(); i < n; i++) {
            if (url.equals(urls.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public String getUrl(int index) {
        return urls.get(index);
    }
    
    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
    
    public String toString() {
        return name;
    }
    
    public boolean isValid() {
        
        return (getId() != INVALID_DATABASE_ID);
    }
    
}











