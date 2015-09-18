/*
 * ConsoleUtilities.java
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

package org.executequery.gui.console;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * ConsoleUtilities.java
 *
 * Copyright (C) 2002, 2003, 2004, 2005, 2006 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */


/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ConsoleUtilities {
    
    private ConsoleUtilities() {}
    
    public static String createWhiteSpace(int len) {
        return createWhiteSpace(len, 0);
    }
    
    /**
     * Create a blank String made of tabs.
     * @param len Amount of spaces contained in the String
     * @param tabSize Tabulation size
     * @return A blank <code>String</code>
     */
    public static String createWhiteSpace(int len, int tabSize) {
        StringBuffer buf = new StringBuffer();
        
        if (tabSize == 0) {
            
            while(len-- > 0)
                buf.append(' ');
        }
        
        else {
            int count = len / tabSize;
            
            while(count-- > 0)
                buf.append('\t');
            
            count = len % tabSize;
            
            while(count-- > 0)
                buf.append(' ');
        }
        
        return buf.toString();
    }
    
    public static File[] listFiles(String[] names, boolean construct) {
        return listFiles(names, System.getProperty("user.home"), construct);
    }
    
    /**
     * Lists content of a directory.
     * @param names Names of the files
     * @param path Base path for files
     * @param construct Set it to true if names does not contain full paths
     * @return An array of Files
     */
    public static File[] listFiles(String[] names, String path, boolean construct) {
        File[] files = new File[names.length];
        
        if (construct) {
            
            if (!path.endsWith(File.separator))
                path += File.separator;
            
        }
        
        for (int i = 0; i < files.length; i++) {
            
            if (construct)
                files[i] = new File(path + names[i]);
            else
                files[i] = new File(names[i]);
            
        }
        
        return files;
    }
    
    /**
     * When the user has to specify file names, he can use wildcards (*, ?). This methods
     * handles the usage of these wildcards.
     * @param s Wilcards
     * @param sort Set to true will sort file names
     * @return An array of String which contains all files matching <code>s</code>
     * in current directory.
     */
    public static String[] getWildCardMatches(String s, boolean sort) {
        return getWildCardMatches(null, s, sort);
    }
    
    /**
     * When the user has to specify file names, he can use wildcards (*, ?). This methods
     * handles the usage of these wildcards.
     * @param path The path were to search
     * @param s Wilcards
     * @param sort Set to true will sort file names
     * @return An array of String which contains all files matching <code>s</code>
     * in current directory.
     */
    public static String[] getWildCardMatches(String path, String s, boolean sort) {
        
        if (s == null)
            return null;
        
        String files[];
        String filesThatMatch[];
        String args = s.trim();
        List<String> filesThatMatchList = new ArrayList<String>();
        
        if (path == null) {
            path = System.getProperty("user.home");
        }
        
        files = (new File(path)).list();
        
        if (files == null) {
            return null;
        }
        
        for (int i = 0; i < files.length; i++) {
            
            if (match(args, files[i])) {
                File temp = new File(System.getProperty("user.home"), files[i]);
                filesThatMatchList.add(temp.getName());
            }
            
        }
        
        Object[] o = filesThatMatchList.toArray();
        filesThatMatch = new String[o.length];
        
        for (int i = 0; i < o.length; i++)
            filesThatMatch[i] = o[i].toString();
        
        o = null;
        filesThatMatchList = null;
        
        if (sort)
            Arrays.sort(filesThatMatch);
        
        return filesThatMatch;
    }
    
    public static String constructPath(String change) {
        return constructPath(change, System.getProperty("user.home"));
    }
    /**
     * Constructs a new path from current user path. This is an easy way to get a path
     * if the user specified, for example, "..\Java" as new path. This method will return
     * the argument if this one is a path to a root (i.e, if <code>change</code> is equal
     * to C:\Jdk, constructPath will return C:\Jdk).
     * @param change The modification to apply to the path
     */
    public static String constructPath(String change, String currentPath) {
        
        if (beginsWithRoot(change))
            return change;
        
        StringBuffer newPath = new StringBuffer(currentPath);//System.getProperty("user.home"));
        
        char current;
        char lastChar = '\0';
        boolean toAdd = false;
        change = change.trim();
        StringBuffer buf = new StringBuffer(change.length());
        
        for (int i = 0; i < change.length(); i++) {
            
            switch ((current = change.charAt(i))) {
                
                case '.':
                    
                    if (lastChar == '.') {
                        String parent = (new File(newPath.toString())).getParent();
                        
                        if (parent != null)
                            newPath = new StringBuffer(parent);
                        
                    }
                    
                    else if ((lastChar != '\0' && lastChar != '\\' && lastChar != '/') ||
                    (i < change.length() - 1 && change.charAt(i + 1) != '.'))
                        buf.append('.');
                    
                    lastChar = '.';
                    break;
                    
                case '\\':
                case '/':
                    
                    if (lastChar == '\0') {
                        newPath = new StringBuffer(getRoot(newPath.toString()));
                    }
                    
                    else {
                        char c = newPath.charAt(newPath.length() - 1);
                        
                        if (c != '\\' && c != '/')
                            newPath.append(File.separator).append(buf.toString());
                        else
                            newPath.append(buf.toString());
                        
                        buf = new StringBuffer();
                        toAdd = false;
                        
                    }
                    
                    lastChar = '\\';
                    break;
                    
                case '~':
                    
                    if (i < change.length() - 1) {
                        
                        if (change.charAt(i + 1) == '\\' || change.charAt(i + 1) == '/')
                            newPath = new StringBuffer(System.getProperty("user.home"));
                        else
                            buf.append('~');
                        
                    }
                    
                    else if (i == 0)
                        newPath = new StringBuffer(System.getProperty("user.home"));
                    else
                        buf.append('~');
                    
                    lastChar = '~';
                    break;
                    
                default:
                    lastChar = current;
                    buf.append(current);
                    toAdd = true;
                    break;
            }
            
        }
        
        if (toAdd) {
            char c = newPath.charAt(newPath.length() - 1);
            
            if (c != '\\' && c != '/')
                newPath.append(File.separator).append(buf.toString());
            else
                newPath.append(buf.toString());
            
        }
        
        return newPath.toString();
    }
    
    /**
     * It can be necessary to check if a path specified by the user is an absolute
     * path (i.e C:\Gfx\3d\Utils is absolute whereas ..\Jext is relative).
     * @param path The path to check
     * @return <code>true</code> if <code>path</code> begins with a root name
     */
    public static boolean beginsWithRoot(String path) {
        
        if (path.length() == 0)
            return false;
        
        File file = new File(path);
        File[] roots = file.listRoots();
        
        for (int i = 0; i < roots.length; i++)
            if (path.regionMatches(true, 0, roots[i].getPath(), 0, roots[i].getPath().length()))
                return true;
        
        return false;
        
    }
    
    /**
     * It can be necessary to determine which is the root of a path.
     * For example, the root of D:\Projects\Java is D:\.
     * @param path The path used to get a root
     * @return The root which contais the specified path
     */
    public static String getRoot(String path) {
        File file = new File(path);
        File[] roots = file.listRoots();
        
        for (int i = 0; i < roots.length; i++)
            if (path.startsWith(roots[i].getPath()))
                return roots[i].getPath();
        
        return path;
    }
    
    /**
     * Some String can be too long to be correctly displayed on the screen.
     * Mainly when it is a path to a file. This method truncate a String.
     * @param longString The <code>String</code> to be truncated
     * @param maxLength The maximum length of the <code>String</code>
     * @return The truncated string
     */
    public static String getShortStringOf(String longString, int maxLength) {
        
        int len = longString.length();
        
        if (len <= maxLength)
            return longString;
        
        else if (longString.indexOf('\\') == -1 && longString.indexOf('/') == -1) {
            StringBuffer buff = new StringBuffer(longString.substring(longString.length() - maxLength));
            
            for(int i =0; i < 3; i++)
                buff.setCharAt(i, '.');
            
            return  buff.toString();
            
        }
        
        else {
            int first = len / 2;
            int second = first;
            
            for (int i = first - 1; i >= 0; i--) {
                
                if (longString.charAt(i) == '\\' || longString.charAt(i) == '/') {
                    first = i;
                    break;
                }
                
            }
            
            for (int i = second + 1; i < len; i++) {
                
                if (longString.charAt(i) == '\\' || longString.charAt(i) == '/') {
                    second = i;
                    break;
                }
                
            }
            
            loop: while ((len - (second - first)) > maxLength) {
                out:    for (int i = first - 1; i >= 0; i--) {
                    switch (longString.charAt(i)) {
                        case '\\': case '/':
                            first = i;
                            break out;
                    }
                }
                
                if ((len - (second - first)) < maxLength)
                    break loop;
                
                out2:   for (int i = second + 1; i < len; i++) {
                    switch (longString.charAt(i)) {
                        case '\\': case '/':
                            second = i;
                            break out2;
                    }
                }
            }
            
            return longString.substring(0, first + 1) + "..." + longString.substring(second);
            
            //return longString.substring(0, maxLength / 2) + "..." +
            //       longString.substring(len - (maxLength / 2));
        }
    }
    
    /**
     * This method can determine if a String matches a pattern of wildcards
     * @param pattern The pattern used for comparison
     * @param string The String to be checked
     * @return true if <code>string</code> matches <code>pattern</code>
     */
    public static boolean match(String pattern, String string) {
        
        for (int p = 0; ; p++) {
            
            for (int s = 0; ; p++, s++) {
                
                boolean sEnd = (s >= string.length());
                boolean pEnd = (p >= pattern.length() || pattern.charAt(p) == '|');
                
                if (sEnd && pEnd)
                    return true;
                
                if (sEnd || pEnd)
                    break;
                
                if (pattern.charAt(p) == '?')
                    continue;
                
                if (pattern.charAt(p) == '*') {
                    
                    int i;
                    p++;
                    
                    for (i = string.length(); i >= s; --i)
                        if (match(pattern.substring(p), string.substring(i))) return true;
                    
                    break;
                    
                }
                
                if (pattern.charAt(p) != string.charAt(s))
                    break;
                
            }
            
            p = pattern.indexOf('|', p);
            
            if (p == -1)
                return false;
            
        }
        
    }
    
} // class














