/*
 * RepositoryCache.java
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

import org.executequery.repository.spi.ConnectionFoldersXMLRepository;
import org.executequery.repository.spi.DatabaseConnectionXMLRepository;
import org.executequery.repository.spi.DatabaseDriverXMLRepository;
import org.executequery.repository.spi.EditorSQLShortcutXMLRepository;
import org.executequery.repository.spi.KeywordRepositoryImpl;
import org.executequery.repository.spi.LatestVersionRepositoryImpl;
import org.executequery.repository.spi.LogFileRepository;
import org.executequery.repository.spi.QueryBookmarkXMLRepository;
import org.executequery.repository.spi.RecentlyOpenFileRepositoryImpl;
import org.executequery.repository.spi.SqlCommandHistoryRepositoryImpl;

public final class RepositoryCache {

    private static Map<String, Repository> repositories;

    private RepositoryCache() {}

    public static synchronized Repository load(String key) {

        if (repositories.containsKey(key)) {
            
            return repositories.get(key);
        }

        return null;
    }

    static {

        repositories = new HashMap<String, Repository>();

        repositories.put(KeywordRepository.REPOSITORY_ID, 
                new KeywordRepositoryImpl());
        
        repositories.put(SqlCommandHistoryRepository.REPOSITORY_ID, 
                new SqlCommandHistoryRepositoryImpl());

        repositories.put(QueryBookmarkRepository.REPOSITORY_ID, 
                new QueryBookmarkXMLRepository());

        repositories.put(EditorSQLShortcutRepository.REPOSITORY_ID, 
                new EditorSQLShortcutXMLRepository());

        repositories.put(RecentlyOpenFileRepository.REPOSITORY_ID, 
                new RecentlyOpenFileRepositoryImpl());
        
        repositories.put(LatestVersionRepository.REPOSITORY_ID, 
                new LatestVersionRepositoryImpl());

        repositories.put(LogRepository.REPOSITORY_ID, 
                new LogFileRepository());

        repositories.put(DatabaseConnectionRepository.REPOSITORY_ID, 
                new DatabaseConnectionXMLRepository());
        
        repositories.put(ConnectionFoldersRepository.REPOSITORY_ID, 
                new ConnectionFoldersXMLRepository());
        
        repositories.put(DatabaseDriverRepository.REPOSITORY_ID, 
                new DatabaseDriverXMLRepository());

    }

}










