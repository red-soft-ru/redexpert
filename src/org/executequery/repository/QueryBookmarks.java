/*
 * QueryBookmarks.java
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author   Takis Diakoumis
 */
public class QueryBookmarks {

    private static QueryBookmarks instance;
    
    private QueryBookmarkRepository queryBookmarkRepository;

    private List<QueryBookmark> queryBookmarks;

    private QueryBookmarks() {

        queryBookmarkRepository = (QueryBookmarkRepository) 
            RepositoryCache.load(QueryBookmarkRepository.REPOSITORY_ID);
    }

    public static synchronized QueryBookmarks getInstance() {

        if (instance == null) {

            instance = new QueryBookmarks();
        }

        return instance;
    }
    
    public void addBookmark(QueryBookmark queryBookmark) throws RepositoryException {
        loadBookmarks();
        queryBookmark.setOrder(queryBookmarks.size() + 1);
        queryBookmarks.add(queryBookmark);
        save(queryBookmarks);
    }

    public void save() throws RepositoryException {
        save(queryBookmarks);
    }
    
    public void save(List<QueryBookmark> bookmarks) throws RepositoryException {
        queryBookmarkRepository.save(bookmarks);
        this.queryBookmarks = bookmarks;
    }

    public List<QueryBookmark> getQueryBookmarks() {        
        loadBookmarks();
        return queryBookmarks;
    }

    public boolean hasQueryBookmarks() {
        loadBookmarks();
        return (queryBookmarks != null && !queryBookmarks.isEmpty());
    }

    public boolean nameExists(String name) {

        if (!hasQueryBookmarks()) {

            return false;
        }
        
        return (findBookmarkByName(name) != null);
    }

    public void removeBookmarkByName(String name) {
        
        QueryBookmark bookmark = findBookmarkByName(name);
        
        if (bookmark != null) {
            
            removeBookmark(bookmark);
        }
        
    }

    public void removeBookmark(QueryBookmark bookmark) {
        queryBookmarks.remove(bookmark);
    }

    public QueryBookmark findBookmarkByName(String name) {
        
        loadBookmarks();
        
        for (QueryBookmark bookmark : queryBookmarks) {
            
            if (bookmark.getName().equals(name)) {

                return bookmark;
            }
            
        }
        
        return null;
    }

    private void loadBookmarks() {

        if (queryBookmarks == null || queryBookmarks.isEmpty()) {

            try {

                queryBookmarks = queryBookmarkRepository.open();

            } catch (RepositoryException e) {

                queryBookmarks = new ArrayList<QueryBookmark>();
            }

        }
    }

}











