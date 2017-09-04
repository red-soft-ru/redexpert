/*
 * QueryWithPosition.java
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

package org.executequery.gui.editor;

public class QueryWithPosition {

    private int start;
    private int end;
    private int position;
    private String query;
    
    public QueryWithPosition(int position, int start, int end, String query) {
        super();
        this.position = position;
        this.start = start;
        this.end = end;
        this.query = query;
    }

    public int getPosition() {
        return position;
    }
    
    public String getQuery() {
        
        /*
        QueryTokenizer tokenizer = new QueryTokenizer();
        List<DerivedQuery> queries = tokenizer.tokenize(query);
        if (queries.size() == 1) {
            
            return query;
        }
        
        int length = 0;
        int offset = position - start;
        
        System.out.println("offset " + offset);
        
        for (DerivedQuery derivedQuery : queries) {
            
            String originalQuery = derivedQuery.getOriginalQuery();
            length += originalQuery.length();

            System.out.println("length " + length + " - " + originalQuery);
            
            if (length >= offset) {
                
                return originalQuery;
            }

        }
        */
        
        return query;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
}


