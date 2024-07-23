/*
 * RepositoryCache.java
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

import org.executequery.repository.spi.*;

import java.util.HashMap;
import java.util.Map;

public final class RepositoryCache {

    private static final Map<String, Repository> repositories;

    static {
        repositories = new HashMap<>();
        repositories.put(KeywordRepository.REPOSITORY_ID, new KeywordRepositoryImpl());
        repositories.put(SqlCommandHistoryRepository.REPOSITORY_ID, new SqlCommandHistoryRepositoryImpl());
        repositories.put(QueryBookmarkRepository.REPOSITORY_ID, new QueryBookmarkXMLRepository());
        repositories.put(EditorSQLShortcutRepository.REPOSITORY_ID, new EditorSQLShortcutXMLRepository());
        repositories.put(RecentlyOpenFileRepository.REPOSITORY_ID, new RecentlyOpenFileRepositoryImpl());
        repositories.put(LatestVersionRepository.REPOSITORY_ID, new LatestVersionRepositoryImpl());
        repositories.put(LogRepository.REPOSITORY_ID, new LogFileRepository());
        repositories.put(DatabaseConnectionRepository.REPOSITORY_ID, new DatabaseConnectionXMLRepository());
        repositories.put(ConnectionFoldersRepository.REPOSITORY_ID, new ConnectionFoldersXMLRepository());
        repositories.put(DatabaseDriverRepository.REPOSITORY_ID, new DatabaseDriverXMLRepository());
    }

    public static synchronized Repository load(String key) {
        return repositories.getOrDefault(key, null);
    }

}
