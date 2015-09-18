/*
 * KeywordRepositoryImpl.java
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

package org.executequery.repository.spi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.executequery.ApplicationException;
import org.executequery.EventMediator;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;

public class KeywordRepositoryImpl implements KeywordRepository {

    /** All SQL92 key words */
    private List<String> sql92KeyWords;
    
    /** The user's added key words */
    private List<String> userDefinedKeyWords;

    public List<String> getSQL92() {

        if (sql92KeyWords == null) {

            sql92KeyWords = loadSQL92();
        }

        return sql92KeyWords;
    }

    public boolean contains(String word) {
        
        List<String> keywords = getSQLKeywords();
        return Collections.binarySearch(keywords, word) >= 0;
    }
    
    public List<String> getSQLKeywords() {

        int sql92Size = getSQL92().size();
        int userSize = getUserDefinedSQL().size();

        List<String> allWords = new ArrayList<String>(sql92Size + userSize);
        
        allWords.addAll(sql92KeyWords);
        allWords.addAll(userDefinedKeyWords);
        
        Collections.sort(allWords);
        
        return allWords;
    }

    public void addUserDefinedKeyword(String word) {
        
        List<String> list = getUserDefinedSQL();
        list.add(word.toUpperCase());
        setUserDefinedKeywords(list);
    }
    
    public List<String> getUserDefinedSQL() {
        
        if (userDefinedKeyWords == null) {
        
            userDefinedKeyWords = loadUserDefinedKeywords();
        }

        return userDefinedKeyWords;
    }

    public void setUserDefinedKeywords(List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {

            return;
        }

        try {

            String delimeter = "|";
            StringBuilder sb = new StringBuilder();

            for (int i = 0, k = keywords.size(); i < k; i++) {

                sb.append(keywords.get(i));

                if (i != k - 1) {

                    sb.append(delimeter);
                }

            }

            String path = getUserDefinedKeywordsPath();
            FileUtils.writeFile(path, sb.toString());

            userDefinedKeyWords = keywords;
            
            // fire the event to registered listeners
            EventMediator.fireEvent(new DefaultKeywordEvent("KeywordProperties", 
                            DefaultKeywordEvent.KEYWORDS_ADDED));

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {
                
                e.printStackTrace();
            }

            Log.error("Error saving keywords to file");
            throw new ApplicationException(e);
        }
        

    }

    public String getId() {

        return REPOSITORY_ID;
    }

    /**
     * Loads the SQL92 keywords from file.
     *
     * @return the list of keywords
     */
    private List<String> loadSQL92() {

        try {

            String path = "org/executequery/sql.92.keywords";
            String values = FileUtils.loadResource(path);

            StringTokenizer st = new StringTokenizer(values, "|");
            List<String> list = new ArrayList<String>(st.countTokens());

            while (st.hasMoreTokens()) {

                list.add(st.nextToken().trim());
            }

            return list;

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {
                
                e.printStackTrace();
            }

            Log.error("Error retrieving SQL92 keyword list");

            return new ArrayList<String>(0);
        }

    }

    /**
     * Loads the user added keywords from file.
     *
     * @return the list of keywords
     */
    private List<String> loadUserDefinedKeywords() {

        File file = new File(getUserDefinedKeywordsPath());

        if (file.exists()) {

            try {

                String values = FileUtils.loadFile(file, false);
                StringTokenizer st = new StringTokenizer(values, "|");
                List<String> list = new ArrayList<String>(st.countTokens());

                while (st.hasMoreTokens()) {

                    list.add(st.nextToken().trim());
                }
                
                return list;

            } catch (IOException e) {

                if (Log.isDebugEnabled()) {
                    
                    e.printStackTrace();
                }

                Log.error("Error opening user defined keywords");

                return new ArrayList<String>(0);
            }

        } else {

            try {

                Log.info("Creating file for user defined keywords list");
                file.createNewFile();

            } catch (IOException e) {

                if (Log.isDebugEnabled()) {
                    
                    e.printStackTrace();
                }

                Log.error("Error creating file for user defined keywords");
            }

            return new ArrayList<String>(0);
        }
        
    }

    /** 
     * Returns the path to the user keywords file.
     */
    private String getUserDefinedKeywordsPath() {
        
        UserSettingsProperties settings = new UserSettingsProperties();

        return settings.getUserSettingsDirectory() + "sql.user.keywords";
    }

}










