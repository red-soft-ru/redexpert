/*
 * KeywordRepositoryImpl.java
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

package org.executequery.repository.spi;

import org.executequery.ApplicationException;
import org.executequery.EventMediator;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordRepositoryImpl implements KeywordRepository {

    /**
     * The user's added key words
     */
    private List<String> userDefinedKeyWords;

    private List<String> firebirdKeyWords = new ArrayList<>();

    private TreeSet<String> allWords;
    private boolean keyWordsListUpdated;

    public boolean contains(String word) {

        TreeSet<String> keywords = getSQLKeywords();
        return keywords.contains(word);
    }

    @Override
    public synchronized TreeSet<String> getSQLKeywords() {

        if (allWords == null || keyWordsListUpdated) {

            allWords = new TreeSet<>();
            keyWordsListUpdated = false;
            if (userDefinedKeyWords != null)
                allWords.addAll(userDefinedKeyWords);
            if (firebirdKeyWords != null)
                allWords.addAll(this.firebirdKeyWords);
        }

        return allWords;
    }

    public void addUserDefinedKeyword(String word) {

        List<String> list = getUserDefinedSQL();
        list.add(word.toUpperCase());
        setUserDefinedKeywords(list);
    }

    public List<String> getDefaultKeywords() {
        try {
            String path = "org/executequery/keywords/default.keywords";
            String values = FileUtils.loadResource(path);

            StringTokenizer st = new StringTokenizer(values, "\n");
            List<String> list = new ArrayList<String>(st.countTokens());

            while (st.hasMoreTokens()) {
                String trim = st.nextToken().trim();
                list.add(trim);
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

    @Override
    public List<String> getServerKeywords(int majorVersion, int minorVersion, String serverName) {
        try {
            if (serverName != null && serverName.toLowerCase().contains("firebird"))
                serverName = "firebird";
            else if (serverName != null && serverName.toLowerCase().contains("reddatabase"))
                serverName = "reddatabase";
            else
                serverName = "sql92";
            String path = "org/executequery/keywords/" + serverName.toLowerCase() + ".keywords";
            String values = FileUtils.loadResource(path);

            StringTokenizer st = new StringTokenizer(values, "\n");
            List<String> list = new ArrayList<String>(st.countTokens());

            while (st.hasMoreTokens()) {
                String trim = st.nextToken().trim();
                if (trim.contains("Firebird") || trim.contains("RedDatabase")) {

                    Pattern p = Pattern.compile("(\\d)(\\.)(\\d)", Pattern.CASE_INSENSITIVE);

                    Matcher m = p.matcher(trim);

                    String version = "";

                    if (m.find())
                        version = m.group(0);

                    version = version.replace(".", "");

                    int verFile = Integer.valueOf(version);
                    int verServer = Integer.valueOf(String.valueOf(majorVersion) + minorVersion);

                    if (verFile > verServer)
                        break;
                    else
                        continue;

                }
                if (trim.startsWith("-")) {
                    trim = trim.substring(1);
                    int ind = list.indexOf(trim);
                    if (ind != -1)
                        list.remove(ind);
                    else
                        Log.error("Firebird TOKEN " + trim + " not found");
                } else
                    list.add(trim);
            }

            this.firebirdKeyWords = list;
            keyWordsListUpdated = true;
            list.addAll(loadUserDefinedKeywords());
            list.addAll(getDefaultKeywords());
            return this.firebirdKeyWords;

        } catch (IOException e) {

            if (Log.isDebugEnabled()) {

                e.printStackTrace();
            }

            Log.error("Error retrieving SQL92 keyword list");

            return new ArrayList<String>(0);
        }
    }

    public List<String> getUserDefinedSQL() {

        if (userDefinedKeyWords == null) {

            userDefinedKeyWords = loadUserDefinedKeywords();
            keyWordsListUpdated = true;
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
            keyWordsListUpdated = true;

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











