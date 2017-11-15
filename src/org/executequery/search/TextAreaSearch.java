/*
 * TextAreaSearch.java
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

package org.executequery.search;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;

/** <p>Provides for text area searches.
 *
 *  @author   Takis Diakoumis
 */
public class TextAreaSearch {
    
    /** Denotes an upward search */
    public static final int SEARCH_UP = 0;
    
    /** Denotes a downward search */
    public static final int SEARCH_DOWN = 1;
    
    /** Previously used 'Find' text */
    private static ArrayList<String> prevFindValues;
    
    /** Previously used 'Replace' text */
    private static ArrayList<String> prevReplaceValues;
    
    /** The text area */
    private static JTextComponent textComponent;
    
    /** The text to search for */
    private static String findText;
    
    /** The replacement text */
    private static String replacementText;
    
    /** Whether this is a user defined regex search */
    private static boolean useRegex;
    
    /** Whether to match case */
    private static boolean matchCase;
    
    /** Search whole words only */
    private static boolean wholeWords;
    
    /** Wrap the search */
    private static boolean wrapSearch;
    
    /** Search direction */
    private static int searchDirection;
    
    public static final char[] REGEX_SPECIAL = {'.', '(', ')', '[', ']', '{',
    '}', '^', '$', '*', '|', '+', '?'};
    
    public static int findNext(boolean replacing, boolean showErrorDialog) {
        
        GUIUtilities.scheduleGC();
        if (textComponent == null) {
            
            GUIUtilities.displayWarningMessage("Search text not found.");
            return -1;
        }

        if (StringUtils.isBlank(findText)) {

            return -1;
        }
        
        String text = textComponent.getText();
        if (StringUtils.isBlank(text)) {
        
            GUIUtilities.displayWarningMessage("Search text not found.");
            return -1;
        }

        if (replacing) {
            
            if (replacementText == null) {
             
                replacementText = "";
            }
            
            textComponent.replaceSelection(replacementText);
        }

        String regexPattern = null;
        if (!useRegex) {
            
            regexPattern = formatRegularExpression(findText, wholeWords);

        } else {

            regexPattern = findText;
        }

        Pattern pattern = null;
        Matcher matcher = null;
        try {
            
            if (matchCase) {
                
                pattern = Pattern.compile(regexPattern);

            } else {

                pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            }

            regexPattern = null;
            text = textComponent.getText();
            matcher = pattern.matcher(text);

            boolean found = false;
            int textLength = text.length();
            int caretPosition = textComponent.getCaretPosition();
            
            int foundStart = -1;
            int foundEnd = -1;
            
            if (searchDirection == SEARCH_UP) {
                
                while (matcher.find()) {
                    
                    if (matcher.end() > caretPosition) {
                        
                        if (!found && wrapSearch) {

                            caretPosition = textLength - 1;

                        } else {
                         
                            break;
                        }
                        
                    }
                    
                    found = true;
                    foundStart = matcher.start();
                    foundEnd = matcher.end();
                }
                
                if (!found && showErrorDialog) {
                    
                    GUIUtilities.displayWarningMessage("Search text not found.");

                } else {
                
                    setSelection(foundStart, foundEnd);
                }
                
            } else {
                
                if (matcher.find(caretPosition)) {
                
                    found = true;
                    foundStart = matcher.start();
                    foundEnd = matcher.end();
                    setSelection(foundStart, foundEnd);

                } else {
                    
                    if (wrapSearch && matcher.find(0)) {
                    
                        found = true;
                        foundStart = matcher.start();
                        foundEnd = matcher.end();
                        setSelection(foundStart, foundEnd);
                    }
                    
                }
                
                if (!found && showErrorDialog) {
                 
                    GUIUtilities.displayWarningMessage("Search text not found.");
                }
                
            }
            
            if (replacing) {
                
//                setSelection(foundStart, foundStart + replacementText.length());
            }
            
//            if (replacing) {
//                
//                if (replacementText == null)
//                    replacementText = "";
//                
//                textComponent.replaceSelection(replacementText);
//                setSelection(foundStart, foundStart + replacementText.length());
//                
//            }
            
        }
        
        catch (PatternSyntaxException pExc) {
            
            if (useRegex) {
                GUIUtilities.displayErrorMessage(
                        "The regular expression search pattern is invalid.");
            }
            
        }
        
        return 0;
        
    }
    
    public static String formatRegularExpression(String input, boolean useWholeWords) {
        
        boolean firstCharSpecial = false;
        StringBuilder regexPattern = new StringBuilder();
        
        char space = ' ';
        char regexChar = '\\';
        String regexSpace = "\\s";
        
        char[] chars = input.toCharArray();
        firstCharSpecial = chars[0] == space;
        
        for (int i = 0; i < chars.length; i++) {
            
            for (int j = 0; j < REGEX_SPECIAL.length; j++) {
                
                if (chars[i] == REGEX_SPECIAL[j]) {
                    regexPattern.append(regexChar);
                    
                    if (i == 0)
                        firstCharSpecial = true;
                    
                }
                
            }
            
            if (chars[i] == space)
                regexPattern.append(regexSpace);
            else
                regexPattern.append(chars[i]);
            
        }
        
        if (useWholeWords) {
            
            String rx = "\\b";
            
            if (!firstCharSpecial)
                regexPattern.insert(0,rx);
            
            regexPattern.append(rx);
            
        }
        
        String _regexPattern = regexPattern.toString();
        regexPattern = null;
        return _regexPattern;
        
    }
    
    public static int replaceAll() {
        
        if (textComponent == null) {
            GUIUtilities.displayWarningMessage("Search text not found.");
            return -1;
        }
        
        if (findText == null || findText.length() == 0)
            return -1;
        
        String _text = null;
        String text = textComponent.getText();
        
        if (text == null || text.length() == 0) {
            GUIUtilities.displayWarningMessage("Search text not found.");
            return -1;
        }
        
        int caretPosition = textComponent.getCaretPosition();
        
        if (replacementText == null)
            replacementText = "";
        
        String regexPattern = null;
        
        if (!useRegex)
            regexPattern = formatRegularExpression(findText, wholeWords);
        else
            regexPattern = findText;
        
        Pattern pattern = null;
        Matcher matcher = null;
        StringBuilder resultText = null;
        
        try {
            
            if (matchCase)
                pattern = Pattern.compile(regexPattern);
            else
                pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
            
            if (wrapSearch)
                matcher = pattern.matcher(text);
            
            else {
                
                if (searchDirection == SEARCH_UP)
                    matcher = pattern.matcher(text.substring(0, caretPosition));
                else
                    matcher = pattern.matcher(text.substring(caretPosition));
                
            }
            
            if (matcher.find()) {

                _text = matcher.replaceAll(replacementText);

            } else {
            
                GUIUtilities.displayWarningMessage("Search text not found.");
                return -1;
            }
            
            
            if (wrapSearch) {

                resultText = new StringBuilder(_text);
            
            } else {
                
                resultText = new StringBuilder(text);
                
                if (searchDirection == SEARCH_UP)
                    resultText.replace(0, caretPosition, _text);
                else
                    resultText.replace(caretPosition, text.length() - 1, _text);
                
            }
            
            textComponent.setText(resultText.toString());
            return 0;
            
        } catch (PatternSyntaxException pExc) {
            
            if (useRegex)
                GUIUtilities.displayErrorMessage(
                "The regular expression search pattern is invalid.");
            
            return -1;
            
        } finally {
            
            if (resultText != null) {
                int length = resultText.length();
                textComponent.setCaretPosition(length < caretPosition ?
                length : caretPosition);
            }
            
            GUIUtilities.scheduleGC();
        }
        
    }
    
    private static void setSelection(int start, int end) {
        
        if (start == -1)
            return;
        
        if (searchDirection == SEARCH_UP) {
            textComponent.setCaretPosition(end);
            textComponent.moveCaretPosition(start);
        }
        else
            textComponent.select(start, end);
        
    }
    
    /** <p>Returns an array of objects containing the
     *  previously used values within the find text of the
     *  Find/Replace dialog.
     *
     *  @return the used find values
     */
    public static Object[] getPrevFindValues() {
        
        return previousFindValues().toArray();
    }

    private static ArrayList<String> previousFindValues() {
        
        if (prevFindValues == null) {
        
            prevFindValues = new ArrayList<String>();
        }
        
        return prevFindValues;
    }
    
    /** <p>Retrieves any previously entered replace values
     *  within the Find/Replace dialog for the Query Editor.
     *
     *  @return previous replace values as an
     *          <code>Object</code> array
     */
    public static Object[] getPrevReplaceValues() {
        
        return previousReplaceValues().toArray();
    }
    
    /** <p>Adds a value to the find list after a search
     *  is performed using the Find/Replace dialog.
     *
     *  @param the find value to add
     */
    public static void addPrevFindValue(String value) {
        
        if (previousFindValues().contains(value)) {
         
            return;
        }
        
        // maintain only 5 previous values
        if (prevFindValues.size() == 5) {
         
            prevFindValues.remove(4);
        }
        
        prevFindValues.add(0, value);
    }
    
    /** <p>Adds a value to the replace list after a search
     *  is performed using the Find/Replace dialog.
     *
     *  @param the replace value to add
     */
    public static void addPrevReplaceValue(String value) {
        
        if (previousReplaceValues().contains(value)) {
         
            return;
        }
        
        if (prevReplaceValues.size() == 5) {
         
            prevReplaceValues.remove(4);
        }
        
        prevReplaceValues.add(0, value);
    }

    private static ArrayList<String> previousReplaceValues() {
        
        if (prevReplaceValues == null) {
         
            prevReplaceValues = new ArrayList<String>();
        }
        
        return prevReplaceValues;
    }
    
    public static void setTextComponent(JTextComponent _textComponent) {
        textComponent = _textComponent;
    }
    
    public static JTextComponent getTextComponent() {
        return textComponent;
    }
    
    public static void setFindText(String _findText) {
        findText = _findText;
    }
    
    public static String getFindText() {
        return findText;
    }
    
    public static void setReplacementText(String _replacementText) {
        replacementText = _replacementText;
    }
    
    public static String getReplacementText() {
        return replacementText;
    }
    
    public static void setUseRegex(boolean _useRegex) {
        useRegex = _useRegex;
    }
    
    public static boolean isUseRegex() {
        return useRegex;
    }
    
    public static void setMatchCase(boolean _matchCase) {
        matchCase = _matchCase;
    }
    
    public static boolean isMatchCase() {
        return matchCase;
    }
    
    public static void setWholeWords(boolean _wholeWords) {
        wholeWords = _wholeWords;
    }
    
    public static boolean isWholeWords() {
        return wholeWords;
    }
    
    public static void setWrapSearch(boolean _wrapSearch) {
        wrapSearch = _wrapSearch;
    }
    
    public static boolean getWrapSearch() {
        return wrapSearch;
    }
    
    public static void setSearchDirection(int _searchDirection) {
        searchDirection = _searchDirection;
    }
    
    public static int getSearchDirection() {
        return searchDirection;
    }
    
}

















