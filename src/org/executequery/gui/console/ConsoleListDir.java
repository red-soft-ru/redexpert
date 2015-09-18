/*
 * ConsoleListDir.java
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

import java.util.Date;
import java.util.StringTokenizer;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;

import org.underworldlabs.util.SystemProperties;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * A ls function for Java Shell. Adapted to Jext.
 * @author Romain Guy
 * @version 1.9.4
 */

public class ConsoleListDir {
    
    private static Console parent;
    private static int indentSize = 0;
    private static String indent = "";
    private static String pattern = "";
    private static boolean moreInfos, fullNames, longDates, hiddenFiles, noDates, onlyDirs,
    onlyFiles, recursive, noInfos, kiloBytes, sort;
    private static boolean canList = true;
    
    /**
     * Exec the equivalent of system's 'ls' or 'dir' command.
     * @param cparent Console which executed the command
     * @param args The command arguments
     */
    
    public static void list(Console cparent, String args) {
        
        parent = cparent;
        
        if (buildFlags(args)) {
            
            String old = System.getProperty("user.dir");
            run();
            
            if (recursive || !canList)
                System.getProperties().put("user.dir", old);
            
            // we reset flags
            sort = kiloBytes = recursive = onlyFiles = onlyDirs = noDates
            = moreInfos = hiddenFiles = longDates = fullNames = false;
            pattern = "";
            canList = true;
            indentSize = 0;
        }
        
    }
    
    /**
     * Output a <code>String</code> in the parent console.
     * @param print <code>String</print> to output
     */
    
    private final static void print(String print) {
        parent.append(print + "\n", parent.outputColor);
    }
    
    /**
     * Determine which options are enabled.
     * @param args The arguments containing the option flags
     */
    
    private static boolean buildFlags(String arg) {
        if (arg == null)
            return true;
        
        StringTokenizer tokens = new StringTokenizer(arg);
        String argument;
        
        while (tokens.hasMoreTokens()) {
            argument = tokens.nextToken();
            
            if (argument.startsWith("-")) {
                if (argument.equals("-help")) {
                    help();
                    return false;
                }
                
                for (int j = 1; j < argument.length(); j++) {
                    switch (argument.charAt(j)) {
                        case 'h':               // hidden files to be shown
                            hiddenFiles = true;
                            break;
                        case 'm':               // display full infos
                            moreInfos = true;
                            break;
                        case 'l':               // use long dates format
                            longDates = true;
                            break;
                        case 'f':               // display full names (don't cut with '...')
                            fullNames = true;
                            break;
                        case 'n':               // don't show last modified dates
                            noDates = true;
                            break;
                        case 'd':               // lists dirs only
                            onlyDirs = true;
                            break;
                        case 'a':               // lists files only
                            onlyFiles = true;
                            break;
                        case 'r':               // lists subdirectories
                            recursive = true;
                            break;
                        case 'i':               // don't show infos
                            noInfos = true;
                            break;
                        case 'k':               // display file sizes in kb instead of bytes
                            kiloBytes = true;
                            break;
                        case 's':               // alphabetically sort files
                            sort = true;
                            break;
                    }
                }
            } else
                pattern = argument;
        }
        
        return true;
    }
    
    /**
     * List according to the options flag activated.
     */
    
    private final static void run() {
        
        // these instances are used to improve speed of dates calculations
        Date date = new Date();
        StringBuffer buffer = new StringBuffer();
        FieldPosition field = new FieldPosition(0);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy - HH:mm:ss");
        
        // default pattern used is '*'
        File[] files = ConsoleUtilities.listFiles(
        ConsoleUtilities.getWildCardMatches(pattern.equals("") ? "*" : pattern, sort), true);
        
        long totalSize;
        int totalDir, totalFiles;
        totalSize = totalFiles = totalDir = 0;
        
        // if canList, then we have to browse a subdirectory
        if (canList && pattern.indexOf("*") == -1 && pattern.indexOf("?") == -1) {
            
            File curr = new File(ConsoleUtilities.constructPath(pattern));
            
            if (curr == null || !curr.isDirectory()) {
                parent.error(SystemProperties.getProperty("console", "console.ls.error"));
                return;
            }
            
            canList = false;
            pattern = "*";
            System.getProperties().put("user.dir", ConsoleUtilities.constructPath(curr.getAbsolutePath()));
            run();
            return;
        }
        
        print("");
        
        for (int i = 0; i < files.length; i++) {
            StringBuffer display = new StringBuffer();
            File current = files[i];
            String currentName = current.getName();
            if (!fullNames)
                currentName =  ConsoleUtilities.getShortStringOf(currentName, 24);
            int amountOfSpaces = 32 - currentName.length();
            
            int sub = 0;
            if (amountOfSpaces > 6)
                sub = 6;
            else if
            (amountOfSpaces >= 0) sub = amountOfSpaces;
            else
                sub = 0;
            // we found a directory
            if (current.isDirectory()) {
                display.append(currentName).append(ConsoleUtilities.createWhiteSpace(amountOfSpaces).substring(sub)).append("<DIR>");
                if (moreInfos)
                    display = (new StringBuffer("   ")).append(ConsoleUtilities.createWhiteSpace(8)).append(display);
                totalDir++;
            } else if (current.isFile()) {
                // and this is a file
                display.append(currentName).append(ConsoleUtilities.createWhiteSpace(amountOfSpaces)).append(current.length());
                totalSize += current.length();
                if (moreInfos) {
                    StringBuffer info = new StringBuffer();
                    info.append(current.canWrite() ? 'w' : '-');         // file is writable
                    info.append(current.canRead() ? 'r' : '-');          // file is readable
                    info.append(current.isHidden() ? 'h': '-');          // file is hidden
                    info.append(ConsoleUtilities.createWhiteSpace(8));
                    display = info.append(display);
                }
                totalFiles++;
            }
            
            StringBuffer time = new StringBuffer();
            if (!noDates) {
                date.setTime(current.lastModified());
                
                if (longDates) {
                    // we display long dates format
                    time.append(date.toString());
                } else {
                    // we display short dates
                    buffer.setLength(0);
                    time.append(formatter.format(date, buffer, field));
                }
                time.append(ConsoleUtilities.createWhiteSpace(8));
            }
            
            // determine if we must show or not (according to flags) found file
            if ((hiddenFiles && current.isHidden()) || !current.isHidden()) {
                if ((current.isDirectory() && !onlyFiles) || (current.isFile() && !onlyDirs) ||
                (onlyDirs && onlyFiles)) {
                    if (ConsoleUtilities.match(pattern, current.getName()))
                        print(indent + time.toString() + display.toString());
                }
            }
            
            // if we are dealing with a dir and -r flag is set, we browse it
            if (recursive && current.isDirectory()) {
                System.getProperties().put("user.dir", ConsoleUtilities.constructPath(current.getAbsolutePath()));
                indent = createIndent(++indentSize);
                run();
                if (!onlyDirs)
                    print("");
            }
        }
        
        // display summary infos
        StringBuffer size = new StringBuffer();
        if (kiloBytes)
            size.append(formatNumber(Long.toString(totalSize / 1024))).append('k');
        else
            size.append(formatNumber(Long.toString(totalSize))).append("bytes");
        
        if (!noInfos)
            print("\n" + indent + totalFiles + " files - " + totalDir + " directories - " +
            size.toString());
        indent = createIndent(--indentSize);
    }
    
    /**
     * Format a number from 12000123 to 12 000 123.
     * @param number Number to be formatted
     */
    private final static String formatNumber(String number) {
        StringBuffer formatted = new StringBuffer(number);
        
        for (int i = number.length(); i > 0; i -= 3)
            formatted.insert(i, ' ');
        
        return formatted.toString();
    }
    
    /**
     * Creates the indent for the recursive option.
     * An indent unit adds two '-'.
     * @param len Length of indentation
     */
    private final static String createIndent(int len) {
        StringBuffer buf = new StringBuffer();
        
        for (int i = 0; i < len; i++) {
            buf.append('-');
            buf.append('-');
        }
        
        return buf.toString();
    }
    
    /**
     * Display command help in the console.
     */
    
    public static void help() {
        parent.help(SystemProperties.getProperty("console", "console.ls.help"));
    }
    
}

// End of ConsoleListDir.java













