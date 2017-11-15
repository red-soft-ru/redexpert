/*
 * TokenizingFormatter.java
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

package org.executequery.sql;

import java.util.ArrayList;
import java.util.List;


/** 
 * Formats tokenized queries so they look 'pretty'.
 *
 * @author   Takis Diakoumis
 */
public class TokenizingFormatter {

    private static final String DELIMITER = ";";

    private QueryTokenizer queryTokenizer;
    
    public String format(String text) {

        List<DerivedQuery> queries = queryTokenizer().tokenize(text);
        List<String> formattedQueries = formatQueries(queries);

        return rebuildQueryString(formattedQueries);
    }

    private String rebuildQueryString(List<String> formattedQueries) {
        
        StringBuilder sb = new StringBuilder();

        for (String query : formattedQueries) {

            sb.append(query.trim());
            
            if (!query.endsWith(DELIMITER)) {

                sb.append(DELIMITER);
            }

            sb.append("\n\n");
        }
        
        return sb.toString();
    }

    private List<String> formatQueries(List<DerivedQuery> queries) {

        List<String> formattedQueries = new ArrayList<String>(queries.size());

        for (DerivedQuery query : queries) {
            
            String formatted = new SQLFormatter(
                    query.getOriginalQuery()).format();  

            formattedQueries.add(formatted);
        }

        return formattedQueries;
    }
    
    private QueryTokenizer queryTokenizer() {
        
        if (queryTokenizer == null) {

            queryTokenizer = new QueryTokenizer();
        }

        return queryTokenizer;
    }

}






