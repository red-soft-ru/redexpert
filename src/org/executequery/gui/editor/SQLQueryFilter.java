/*
 * SQLQueryFilter.java
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

import org.executequery.Constants;
import org.underworldlabs.util.MiscUtils;

public class SQLQueryFilter {

    public String extractQueryAt(String text, int position) {

    	if (MiscUtils.isNull(text)) {
        
    		return Constants.EMPTY;
        }
        
        char[] chars = text.toCharArray();

        if (position == chars.length) {

            position--;
        }
        
        int start = -1;
        int end = -1;
        boolean wasSpaceChar = false;

        // determine the start point
        for (int i = position; i >= 0; i--) {

            if (chars[i] == Constants.NEW_LINE_CHAR) {

                if (i == 0 || wasSpaceChar) {

                	break;
                    
                } else if (start != -1) {
                    
                	if(chars[i - 1] == Constants.NEW_LINE_CHAR) {
                        
                    	break;

                    } else if (Character.isSpaceChar(chars[i - 1])) {
                
                    	wasSpaceChar = true;
                        i--;
                    }
              
                }

            } else if (!Character.isSpaceChar(chars[i])) {
            	
                wasSpaceChar = false;
                start = i;
            }

        }

        if (start < 0) { // text not found

            for (int j = 0; j < chars.length; j++) {
            
            	if (!Character.isWhitespace(chars[j])) {
                
            		start = j;
                    break;
                }
        
            }

        }

        // determine the end point 
        for (int i = start; i < chars.length; i++) {

            if (chars[i] == Constants.NEW_LINE_CHAR) {

                if (i == chars.length - 1 || wasSpaceChar) {

                	if (end == -1) {
                 
                    	end = i;
                    }
                    break;

                } else if (end != -1) {
                  
                	if(chars[i + 1] == Constants.NEW_LINE_CHAR) {
                        
                		break;

                    } else if (Character.isSpaceChar(chars[i + 1])) {
                      
                    	wasSpaceChar = true;
                        i++;
                    }

                }

            } else if (!Character.isSpaceChar(chars[i])) {
              
            	end = i;
                wasSpaceChar = false;
            }
            
        }

        String query = text.substring(start, end + 1);

        if ((MiscUtils.isNull(query) && start != 0) || start == end) {

        	return extractQueryAt(text, start);
        }
        
        return query;
    }
	
}






