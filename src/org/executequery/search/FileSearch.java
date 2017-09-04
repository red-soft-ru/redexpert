/*
 * FileSearch.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.util.SwingWorker;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class FileSearch {
    
    /** Wild card extension */
    public static final String WILDCARD = "*";
    
    /** Worker thread */
    private SwingWorker worker;
    /** The results vector */
    private Vector searchResults;
    /** Total find count */
    private int totalFindCount;
    /** Total file count */
    private int fileCount;
    
    // --------------------------------------
    // -------- Saved search details --------
    // --------------------------------------
    /** Previously held path values */
    private static Vector pathValues;
    /** Previously held file types */
    private static Vector typesValues;
    
    // --------------------------------------
    // ----- Search details and options -----
    // --------------------------------------
    /** The text to search */
    private static String searchText;
    /** The text to search */
    private static String replaceText;
    /** The file search extension type */
    private static String searchExtension;
    /** The file search path */
    private static String searchPath;
    /** Whether to search for whole words */
    private static boolean findWholeWords;
    /** Whether to replace text */
    private static boolean replacingText;
    /** Whether to search subdirectories */
    private static boolean searchingSubdirs;
    /** Whether to match case */
    private static boolean matchingCase;
    /** Whether to use regex matching */
    private static boolean usingRegex;
    /** The search view GUI object */
    private FileSearchView searchView;
    
    /** New line char */
    private static final String NEW_LINE = "\n";
    
    /** The search pattern */
    private Pattern pattern;
    
    public FileSearch(FileSearchView searchView) {
        this.searchView = searchView;
        
        pattern = null;
        
        if (pathValues == null)
            pathValues = new Vector();
        
        if (typesValues == null) {
            typesValues = new Vector();
            typesValues.add(WILDCARD);
        }
        
    }
    
    private void addToList(File file) {
        if (searchResults == null) {
            searchResults = new Vector();
        }
        searchResults.add(file);
        searchView.setListData(searchResults);
    }
    
    private void addToList(String text) {
        if (searchResults == null) {
            searchResults = new Vector();
        }
        searchResults.add(text);
        searchView.setListData(searchResults);        
    }
    
    public void doSearch() {
        worker = new SwingWorker() {
            public Object construct() {
                clearResults();                
                int extLength = searchExtension.length();
                if (extLength > 0 && searchExtension.charAt(0) == '*') {
                    searchExtension = searchExtension.substring(1);
                }
                
                return startFind(new File(searchPath));
            }
            
            public void finished() {                
                searchView.setListData(searchResults);
                searchView.finished();
                
                if (replacingText) {
                    searchView.setResultsSummary("Replaced " + totalFindCount +
                        " occurrences in " + fileCount + " files.");
                } else {
                    searchView.setResultsSummary("Found " + totalFindCount +
                        " occurrences in " + fileCount + " files.");
                }

                pattern = null;
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();
    }
    
    private String startFind(File file) {
        
        if (file.isDirectory()) {
            
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                
                if (files[i].isDirectory()) {
                    
                    if (searchingSubdirs) {
                        startFind(files[i]);
                    } else {
                        continue;
                    }

                }                
                else {
                    
                    if (!searchExtension.equals(WILDCARD)) {
                        if (!files[i].getName().endsWith(searchExtension)) {
                            continue;
                        }                        
                    }

                    fileCount++;
                    searchFile(files[i]);                    
                }
                
            }
            
        }
        else {            
            fileCount++;
            searchFile(file);
        }
        
        return "done";
        
    }
    
    private void searchFile(File file) {
        
        Matcher matcher = null;
        StringBuffer fileText = readFile(file);
        
        int findCount = 0;
        
        try {
            
            if (pattern == null) {                
                String regexPattern = null;
                if (!usingRegex) {
                    regexPattern = TextAreaSearch.formatRegularExpression(searchText,
                    findWholeWords);
                } else {
                    regexPattern = searchText;
                }
                
                if (matchingCase) {
                    pattern = Pattern.compile(regexPattern);
                } else {
                    pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
                }                
                regexPattern = null;
            }
            
            matcher = pattern.matcher(fileText);
            while (matcher.find()) {
                findCount++;
            }
            
            if (findCount > 0) {
                
                if (!replacingText) {
                    addToList(file);
                }
                else {
                    String text = "Replaced " + findCount + " occurrences in " +
                            file.getAbsolutePath();
                    addToList(text);
                }
            }
            
            if (replacingText) {
                String newText = matcher.replaceAll(replaceText);
                writeFile(file, newText);
            }
            totalFindCount += findCount;            
        }
        catch (PatternSyntaxException pExc) {
            if (usingRegex) {
                GUIUtilities.displayErrorMessage(
                    "The regular expression search pattern is invalid.");
            }
        }
        
    }
    
    private void writeFile(File file, String text) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(file, false), true);
            writer.println(text);
            writer.close();
        }        
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    private StringBuffer readFile(File file) {
        StringBuffer sb = null;
        String text = null;

        try {
            FileInputStream input = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            sb = new StringBuffer(10000);
            
            char newLine = '\n';
            
            while((text = reader.readLine()) != null) {
                sb.append(text).append(newLine);
            }

            reader.close();
            input.close();
            return sb;
        }        
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            sb = null;
            text = null;
            return null;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void addToValues(Vector values, String text) {
        if (values.size() == 5) {
            values.removeElementAt(4);
        }
        
        if (!values.contains(text)) {
            values.add(0, text);
        }        
    }
    
    private void clearResults() {
        totalFindCount = 0;
        fileCount = 0;
        
        if (searchResults == null) {
            searchResults = new Vector();
        }
        
        searchResults.clear();
        searchView.setListData(searchResults);
        searchView.setResultsSummary("");
    }
    
    public void setSearchText(String _searchText) {
        searchText = _searchText;
    }
    
    public String getSearchText() {
        return searchText;
    }
    
    public String getReplaceText() {
        return replaceText;
    }
    
    public void setReplaceText(String _replaceText) {
        replaceText = _replaceText;
    }
    
    public String getSearchExtension() {
        return searchExtension;
    }
    
    public void setSearchExtension(String _searchExtension) {
        searchExtension = _searchExtension;
        addToValues(typesValues, searchExtension);
    }
    
    public void setSearchPath(String _searchPath) {
        searchPath = _searchPath;
        addToValues(pathValues, searchPath);
    }
    
    public Vector getSearchResults() {
        return searchResults;
    }
    
    public String getSearchPath() {
        return searchPath;
    }
    
    public void setFindWholeWords(boolean _findWholeWords) {
        findWholeWords = _findWholeWords;
    }
    
    public boolean setFindWholeWords() {
        return findWholeWords;
    }
    
    public boolean isReplacingText() {
        return replacingText;
    }
    
    public void setReplacingText(boolean _replacingText) {
        replacingText = _replacingText;
    }
    
    public boolean isSearchingSubdirs() {
        return searchingSubdirs;
    }
    
    public void setSearchingSubdirs(boolean _searchingSubdirs) {
        searchingSubdirs = _searchingSubdirs;
    }
    
    public boolean isUsingRegex() {
        return usingRegex;
    }
    
    public void setUsingRegex(boolean _usingRegex) {
        usingRegex = _usingRegex;
    }
    
    public boolean isMatchingCase() {
        return matchingCase;
    }
    
    public void setMatchingCase(boolean _matchingCase) {
        matchingCase = _matchingCase;
    }
    
    public static Vector getTypesValues() {
        
        if (typesValues == null) {
            typesValues = new Vector();
            typesValues.add(WILDCARD);
        }
        
        return typesValues;
    }
    
    public static Vector getPathValues() {
        
        if (pathValues == null)
            pathValues = new Vector();
        
        return pathValues;
    }
    
}















