/*
 * Constants.java
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

import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public interface Constants {

    String USER_PROPERTIES_KEY = "user";

    String SYSTEM_PROPERTIES_KEY = "system";

    String ACTION_CONF_PATH = "org/executequery/actions.xml";

    //----------------------------
    // syntax colours and styles
    //----------------------------

    /**
     * Recognised syntax types
     */
    String[] SYNTAX_TYPES = {"normal",
            "keyword",
            "quote",
            "singlecomment",
            "multicomment",
            "number",
            "operator",
            "literal",
            "dbobjects",
            "datatype"
    };

    /**
     * The properties file style name prefix
     */
    String STYLE_NAME_PREFIX = "sqlsyntax.style.";

    /**
     * The properties file style colour prefix
     */
    String STYLE_COLOUR_PREFIX = "sqlsyntax.colour.";

    /**
     * The literal 'Plain'
     */
    String PLAIN = "Plain";
    /**
     * The literal 'Italic'
     */
    String ITALIC = "Italic";
    /**
     * The literal 'Bold'
     */
    String BOLD = "Bold";

    /**
     * An empty string
     */
    String EMPTY = "";

    String NEW_LINE_STRING = "\n";
    String QUOTE_STRING = "'";
    char QUOTE_CHAR = '\'';
    char NEW_LINE_CHAR = '\n';
    char TAB_CHAR = '\t';
    char COMMA_CHAR = ',';

    //-------------------------
    // literal SQL keywords
    //-------------------------
    String NULL_LITERAL = "NULL";
    String TRUE_LITERAL = "TRUE";
    String FALSE_LITERAL = "FALSE";

    char[] BRACES = {'(', ')', '{', '}', '[', ']'};

    String COLOUR_PREFERENCE = "colourPreference";

    int DEFAULT_FONT_SIZE = 11;

    String[] TRANSACTION_LEVELS =
            {"TRANSACTION_NONE",
                    "TRANSACTION_READ_UNCOMMITTED",
                    "TRANSACTION_READ_COMMITTED",
                    "TRANSACTION_SNAPSHOT",
                    "TRANSACTION_SNAPSHOT_TABLE_STABILITY"};

    // tool tip html tags
    String TABLE_TAG_START =
            "<table border='0' cellspacing='0' cellpadding='2'>";

    String TABLE_TAG_END =
            "</table>";

    Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

    int DEFAULT_BUTTON_WIDTH = 75;
    int DEFAULT_BUTTON_HEIGHT = 26;

    Dimension xBUTTON_SIZE = new Dimension(75, 26);

    Insets xBUTTON_INSETS = new Insets(2, 2, 2, 2);

    Dimension FORM_BUTTON_SIZE = new Dimension(100, 25);


    // Log4J logging levels
    String[] LOG_LEVELS = {"INFO", "WARN", "DEBUG", "ERROR", "FATAL", "TRACE", "ALL"};

    /**
     * worker success result
     */
    String WORKER_SUCCESS = "success";

    /**
     * worker fail result
     */
    String WORKER_FAIL = "fail";

    /**
     * worker fail result
     */
    String WORKER_CANCEL = "cancel";

}







