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

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import org.underworldlabs.util.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Formats tokenized queries so they look 'pretty'.
 *
 * @author Takis Diakoumis
 */
public class TokenizingFormatter {

    private QueryTokenizer queryTokenizer;

    public String format(String text) {

        List<DerivedQuery> queries = queryTokenizer().tokenize(MiscUtils.trimEnd(text));
        List<String> formattedQueries = formatQueries(queries);

        return rebuildQueryString(formattedQueries);
    }

    private String rebuildQueryString(List<String> formattedQueries) {
        return formattedQueries.stream().map(String::trim).collect(Collectors.joining("\n"));
    }

    private List<String> formatQueries(List<DerivedQuery> queries) {
        List<String> formattedQueries = new ArrayList<>(queries.size());

        for (DerivedQuery query : queries) {

            String formattedQuery = SqlFormatter.extend(cfg -> cfg.plusSpecialWordChars("$"))
                    .format(query.getOriginalQuery(), FormatConfig.builder().indent("\t").build());

            if (!formattedQuery.endsWith(query.getEndDelimiter()))
                formattedQuery += query.getEndDelimiter();

            if (query.isSetTerm()) {
                formattedQuery = "\nSET TERM " + query.getEndDelimiter() + ";\n"
                        + formattedQuery
                        + "\nSET TERM ;" + query.getEndDelimiter() + "\n";
            }

            formattedQueries.add(formattedQuery);
        }

        return formattedQueries;
    }

    private QueryTokenizer queryTokenizer() {
        if (queryTokenizer == null)
            queryTokenizer = new QueryTokenizer();
        return queryTokenizer;
    }

}
