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

import org.executequery.log.Log;
import org.executequery.repository.KeywordRepository;
import org.underworldlabs.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordRepositoryImpl implements KeywordRepository {

    private List<String> firebirdKeyWords = new ArrayList<>();
    private boolean keyWordsListUpdated;
    private TreeSet<String> allWords;

    @Override
    public boolean contains(String word) {
        return getSQLKeywords().contains(word);
    }

    @Override
    public synchronized TreeSet<String> getSQLKeywords() {

        if (allWords == null || keyWordsListUpdated) {

            allWords = new TreeSet<>();
            keyWordsListUpdated = false;
            if (firebirdKeyWords != null)
                allWords.addAll(this.firebirdKeyWords);
        }

        return allWords;
    }

    public List<String> getDefaultKeywords() {
        try {

            String path = "org/executequery/keywords/default.keywords";
            String values = FileUtils.loadResource(path);

            StringTokenizer st = new StringTokenizer(values, "\n");
            List<String> list = new ArrayList<>(st.countTokens());

            while (st.hasMoreTokens()) {
                String trim = st.nextToken().trim();
                list.add(trim);
            }

            return list;

        } catch (IOException e) {
            Log.error("Error retrieving SQL92 keyword list");
            Log.debug(e.getMessage(), e);

            return new ArrayList<>(0);
        }
    }

    @Override
    public List<String> getServerKeywords(int majorVersion, int minorVersion, String serverName) {
        try {

            if (serverName != null && serverName.toLowerCase().contains("firebird")) {
                serverName = "firebird";
            } else if (serverName != null && serverName.toLowerCase().contains("reddatabase")) {
                serverName = "reddatabase";
            } else
                serverName = "sql92";

            String path = "org/executequery/keywords/" + serverName.toLowerCase() + ".keywords";
            String values = FileUtils.loadResource(path);

            StringTokenizer st = new StringTokenizer(values, "\n");
            List<String> list = new ArrayList<>(st.countTokens());

            while (st.hasMoreTokens()) {
                String trim = st.nextToken().trim();
                if (trim.contains("Firebird") || trim.contains("RedDatabase")) {

                    Pattern p = Pattern.compile("(\\d)(\\.)(\\d)", Pattern.CASE_INSENSITIVE);

                    Matcher m = p.matcher(trim);

                    String version = "";

                    if (m.find())
                        version = m.group(0);

                    version = version.replace(".", "");

                    int verFile = Integer.parseInt(version);
                    int verServer = Integer.parseInt(String.valueOf(majorVersion) + minorVersion);

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
            list.addAll(getDefaultKeywords());
            return this.firebirdKeyWords;

        } catch (IOException e) {
            Log.error("Error retrieving SQL92 keyword list");
            Log.debug(e.getMessage(), e);

            return new ArrayList<>(0);
        }
    }

    @Override
    public String getId() {
        return REPOSITORY_ID;
    }

}
