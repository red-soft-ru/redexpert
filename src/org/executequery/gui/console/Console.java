/*
 * Console.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.executequery.gui.console.commands.ChangeDirCommand;
import org.executequery.gui.console.commands.ClearCommand;
import org.executequery.gui.console.commands.Command;
import org.executequery.gui.console.commands.ExitCommand;
import org.executequery.gui.console.commands.HelpCommand;
import org.executequery.gui.console.commands.HomeCommand;
import org.executequery.gui.console.commands.PwdCommand;
import org.underworldlabs.util.SystemProperties;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * An internal console which provide different kinds of
 * prompts and which allows to execute both internal and
 * external (OS specific) commands. The console is embedded
 * in a <code>JScrollPane</code> and handles it by itself.
 * <p>Modified from Romain Guy's version for his text editor
 * JExt http://jext.org.
 *
 * @author   Romain Guy
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class Console extends JScrollPane {
    
    public static final int WINDOWS_OS = 0;
    public static final int UNIX_OS = 1;
    public static final int MAC_OS = 2;
    
    private int osType;
    
    /** DOS prompt: /export/home/guy > */
    public static final int DOS_PROMPT = 0;
    /** Jext prompt: Gfx@/export/home/guy > -- renamed */
    public static final int DEFAULT_PROMPT = 1;
    /** Linux prompt: guy@csdlyon$ */
    public static final int LINUX_PROMPT = 2;
    /** SunOS prompt: csdlyon% */
    public static final int SUNOS_PROMPT = 3;
    
    /** Default prompt types: DOS, default, Linux and SunOS **/
    public static final String[] DEFAULT_PROMPTS = {"$p >", "$u@$p >", "$u@$h$$ ", "$h% "};
    
    // current separators used in command lines
    private static final String COMPLETION_SEPARATORS = " \t;:/\\\"\'";
    
    private static final String[] WINDOWS_EXEC = {"cmd.exe", "/c", ""};
    
    // commands
    private Command currentCmd, firstCmd;
    
    // processes specific
    private Process process;
    private String processName;
    private StdoutThread stdout;
    private StderrThread stderr;
    
    // private fields
    private String current;
    private Document outputDocument;
    private ConsoleTextPane textArea;
    private HistoryModel historyModel = new HistoryModel(25);
    private int userLimit = 0, typingLocation = 0, index = -1;
    
    // colors
    public Color errorColor = Color.red;
    public Color promptColor = Color.blue;
    public Color outputColor = Color.black;
    public Color infoColor = new Color(0, 165, 0);
    
    // prompt
    private boolean displayPath;
    private String currentPath, hostName, prompt = "";
    private String promptPattern = DEFAULT_PROMPTS[DEFAULT_PROMPT];
    
    /** Instanciates a new console without displaying prompt. */
    public Console() {
        this(true);
    }
    
    /** Creates a new console, embedding it in a <code>JScrollPane</code>.
     *  By default console help is displayed.
     *  @param display If set on true, prompt is displayed
     */
    public Console(boolean display) {
        
        super(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // check we have the console properties loaded
        if (SystemProperties.getProperties("console") == null) {
            SystemProperties.loadPropertiesResource("console",
                    SystemProperties.getProperty("system", "console.defaults"));
        }
        
        currentPath = System.getProperty("user.home");
        
        textArea = new ConsoleTextPane(this);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        outputDocument = textArea.getDocument();
        append(SystemProperties.getProperty("console", "console.welcome"), infoColor, false, true);
        
        displayPath = true;
        
        if (display) {
            displayPrompt();
        }
        
        getViewport().setView(textArea);
        FontMetrics fm = getFontMetrics(textArea.getFont());
        setPreferredSize(new Dimension(40 * fm.charWidth('m'), 6 * fm.getHeight()));
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());
        
        initCommands();
        
        // start at the user's home dir
        //    builtInCommand("cd " + currentPath);
    }
    
    /** Return the <code>Document</code> in which output is performed. */
    public Document getOutputDocument() {
        return outputDocument;
    }
    
    /** Adds a command to the linked list of commands. */
    public void addCommand(Command command) {
        
        if (command == null)
            return;
        
        currentCmd.next = command;
        currentCmd = command;
    }
    
    /**
     * Return true if command is built-in. If command is built-in,
     * it is also executed.
     * @param command Command to check and execute
     */
    private boolean builtInCommand(String command) {
        boolean ret = false;
        Command _currentCmd = firstCmd;
        
        while (_currentCmd != null) {
            
            if (_currentCmd.handleCommand(this, command)) {
                ret = true;
                break;
            }
            
            _currentCmd = _currentCmd.next;
            
        }
        
        return ret;
    }
    
    // inits commands list
    
    private void initCommands() {
        firstCmd = currentCmd = new ClearCommand();
        addCommand(new ChangeDirCommand());
        addCommand(new ExitCommand());
        addCommand(new HomeCommand());
        addCommand(new HelpCommand());
        //    addCommand(new ListCommand());
        addCommand(new PwdCommand());
    }
    
    /** Set console background color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setBgColor(Color color) {
        textArea.setBackground(color);
    }
    
    /** Set console error color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setErrorColor(Color color) {
        errorColor = color;
    }
    
    /** Set console prompt color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setPromptColor(Color color) {
        promptColor = color;
    }
    
    /** Set console output color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setOutputColor(Color color) {
        outputColor = color;
        textArea.setForeground(color);
        textArea.setCaretColor(color);
    }
    
    /** Set console info color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setInfoColor(Color color) {
        infoColor = color;
    }
    
    /** Set console selection color.
     *
     *  @param color <code>Color</code> to be used
     */
    public void setSelectionColor(Color color) {
        textArea.setSelectionColor(color);
    }
    
    /** Save the history. */
    public void save() {
        for (int i = 0; i < historyModel.getSize(); i++) {
            SystemProperties.setProperty("console", 
                    "console.history." + i, historyModel.getItem(i));
        }        
    }
    
    /** 
     * Set the prompt pattern.
     *
     * @param type The prompt pattern
     */
    public void setPromptPattern(String prompt) {
        if (prompt == null) {
            return;
        }
        
        promptPattern = prompt;
        displayPath = false;
        buildPrompt();
    }
    
    /** Get prompt pattern. */
    public String getPromptPattern() {
        return promptPattern;
    }
    
    /** Displays the prompt according to the current selected prompt type. */
    public void displayPrompt() {
        
        if (prompt == null || displayPath)
            buildPrompt();
        
        if (outputDocument == null)
            return;
        
        append('\n' + prompt, promptColor);
        typingLocation = userLimit = outputDocument.getLength();
    }
    
    // builds the prompt according to the prompt pattern
    private void buildPrompt() {
        
        int promptType = -1;
        
        if(System.getProperty("mrj.version") != null) {
            promptType = DEFAULT_PROMPT;;
            osType = MAC_OS;
        }
        else {
            String osName = System.getProperty("os.name");
            
            if(osName.indexOf("Windows") != -1) {
                promptType = DOS_PROMPT;
                osType = WINDOWS_OS;
            }
            else if(osName.indexOf("OS/2") != -1) {
                promptType = DEFAULT_PROMPT;
                osType = UNIX_OS;
            }
            else {
                promptType = DEFAULT_PROMPT;
                osType = UNIX_OS;
            }
            
        }
        
        promptPattern = DEFAULT_PROMPTS[promptType];
        displayPath = false;
        StringBuffer buf = new StringBuffer();
        
        if (hostName == null) {
            
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException uhe) {}
            
        }
        
        for (int i = 0; i < promptPattern.length(); i++) {
            
            char c = promptPattern.charAt(i);
            
            switch(c) {
                
                case '$':
                    
                    if (i == promptPattern.length() - 1)
                        buf.append(c);
                    
                    else {
                        
                        switch (promptPattern.charAt(++i)) {
                            case 'p':                    // current path
                                buf.append(currentPath);
                                displayPath = true;
                                break;
                            case 'u':                    // user name
                                buf.append(System.getProperty("user.name"));
                                break;
                            case 'h':                    // host name
                                buf.append(hostName);
                                break;
                            case '$':
                                buf.append('$');
                                break;
                        }
                        
                    }
                    
                    break;
                    
                default:
                    buf.append(c);
                    
            }
            
        }
        
        prompt = buf.toString();
    }
    
    /** This method appends text in the text area.
     *
     *  @param text The text to append
     *  @param color The color of the text
     *  @param italic Set to true will append italic text
     *  @param bold Set to true will append bold text
     */
    private void append(String text, Color color, boolean italic, boolean bold) {
        
        if (outputDocument == null)
            return;
        
        SimpleAttributeSet style = new SimpleAttributeSet();
        
        if (color != null)
            style.addAttribute(StyleConstants.Foreground, color);
        
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        
        try {
            outputDocument.insertString(outputDocument.getLength(), text, style);
        } catch(BadLocationException bl) {}
        
        textArea.setCaretPosition(outputDocument.getLength());
    }
    
    /** This method appends text in the text area.
     *
     *  @param text The text to append in the text area
     *  @apram color The color of the text to append
     */
    public void append(String text, Color color) {
        append(text, color, false, false);
    }
    
    /** Adds a command to the history.
     *
     *  @param command Command to add in the history
     */
    public void addHistory(String command) {
        historyModel.addItem(command);
        index = -1;
    }
    
    /** Remove a char from current command line. Stands for BACKSPACE action. */
    public void removeChar() {
        
        try {
            //if (typingLocation != userLimit)
            //  outputDocument.remove(--typingLocation, 1);
            int pos = textArea.getCaretPosition();
            
            if (pos <= typingLocation && pos > userLimit) {
                outputDocument.remove(pos - 1, 1);
                typingLocation--;
            }
            
        } catch (BadLocationException ble) {}
        
    }
    
    /** Delete a char from command line. Stands for DELETE action. */
    public void deleteChar() {
        
        try {
            
            int pos = textArea.getCaretPosition();
            
            if (pos == outputDocument.getLength())
                return;
            
            if (pos < typingLocation && pos >= userLimit) {
                outputDocument.remove(pos, 1);
                typingLocation--;
            }
            
        }
        
        catch (BadLocationException ble) {}
        
    }
    
    /** Adds a <code>String</code> to the current command line.
     *
     *  @param add <code>String</code> to be added
     */
    public void add(String add) {
        
        try {
            
            int pos = textArea.getCaretPosition();
            
            if (pos <= typingLocation && pos >= userLimit)
                outputDocument.insertString(pos, add, null);
            
            typingLocation += add.length();
            
        }
        
        catch (BadLocationException ble) {}
        
    }
    
    /** Returns the position in characters at which
     *  user is allowed to type his commands.
     *
     *  @return Beginning of user typing space
     */
    public int getUserLimit() {
        return userLimit;
    }
    
    /** Returns the position of the end of the console prompt. */
    public int getTypingLocation() {
        return typingLocation;
    }
    
    /** Completes current filename if possible. */
    public void doCompletion() {
        int index = 0;
        int caret = textArea.getCaretPosition() - userLimit;
        
        String wholeText = getText();
        String text;
        
        try {
            text = outputDocument.getText(userLimit, caret);
        } catch (BadLocationException ble) { return; }
        
        for (int i = text.length() - 1; i >= 0; i--) {
            
            if (COMPLETION_SEPARATORS.indexOf(text.charAt(i)) != -1) {
                
                if (i == index)
                    return;
                
                index = i + 1;
                break;
                
            }
            
        }
        
        String current = text.substring(index);
        
        List<String> matching = new ArrayList<String>();
        String[] files = ConsoleUtilities.getWildCardMatches("*", true);
        
        if (files == null)
            return;
        
        for (int i = 0; i < files.length; i++) {
            
            if (files[i].startsWith(current))
                matching.add(files[i]);
            
        }
        
        if (matching.size() == 0)
            return;
        
        int length = 0;
        int _length = 0;
        int mIndex = 0;
        
        for (int i = 0; i < matching.size(); i++) {
            _length = ((String) matching.get(i)).length();
            length = length < _length ? _length : length;
            
            if (length == _length)
                mIndex = i;
            
        }
        
        char c;
        boolean isSame = true;
        int diffIndex = length;
        
        String compare;
        String source = (String) matching.get(mIndex);
        
        for (int i = 0; i < length; i++) {
            
            c = source.charAt(i);
            
            for (int j = 0; j < matching.size(); j++) {
                
                if (j == mIndex)
                    continue;
                
                compare = (String) matching.get(j);
                
                if (i >= compare.length())
                    continue;
                
                isSame = (compare.charAt(i) == c);
                
            }
            
            if (!isSame) {
                diffIndex = i;
                break;
            }
            
        }
        
        compare = text.substring(0, index) + source.substring(0, diffIndex);
        setText(compare +  wholeText.substring(caret));
        textArea.setCaretPosition(userLimit + compare.length());
        
    }
    
    /** Search backward in the history for a matching command,
     *  according to the command typed in the user typing space. */
    public void doBackwardSearch() {
        String text = getText();
        
        if (text == null) {
            historyPrevious();
            return;
        }
        
        for(int i = index + 1; i < historyModel.getSize(); i++) {
            String item = historyModel.getItem(i);
            
            if (item.startsWith(text)) {
                setText(item);
                index = i;
                return;
            }
            
        }
        
    }
    
    /** Get previous item in the history list. */
    public void historyPrevious() {
        
        if (index == historyModel.getSize() - 1)
            getToolkit().beep();
        
        else if (index == -1) {
            current = getText();
            setText(historyModel.getItem(0));
            index = 0;
        }
        
        else {
            int newIndex = index + 1;
            setText(historyModel.getItem(newIndex));
            index = newIndex;
        }
        
    }
    
    /** Get next item in the history list. */
    public void historyNext() {
        
        if (index == -1)
            getToolkit().beep();
        else if (index == 0)
            setText(current);
        else {
            int newIndex = index - 1;
            setText(historyModel.getItem(newIndex));
            index = newIndex;
        }
    }
    
    /** Set user's command line content.
     *
     *  @param text Text to be put on command line.
     */
    public void setText(String text) {
        try {
            outputDocument.remove(userLimit, typingLocation - userLimit);
            outputDocument.insertString(userLimit, text, null);
            typingLocation = outputDocument.getLength();
            index = -1;
        } catch (BadLocationException ble) {}
    }
    
    /** Returns current command line. */
    public String getText() {
        try {
            return outputDocument.getText(userLimit, typingLocation - userLimit);
        }
        catch (BadLocationException ble) {}
        return null;
    }
    
    /** Display a message using output color.
     *
     *  @param display <code>String</code> to be displayed
     */
    public void output(String display) {
        append('\n' + display, outputColor, false, false);
    }
    
    /** Displays console help. */
    public void help() {
        Command _current = firstCmd;
        StringBuffer buf = new StringBuffer();
        
        while (_current != null) {
            buf.append("   - ").append(_current.getCommandName());
            buf.append(ConsoleUtilities.createWhiteSpace(30 - _current.getCommandName().length())).append('(');
            buf.append(_current.getCommandSummary()).append(')').append('\n');
            _current = _current.next;
        }
        
        help(SystemProperties.getProperty("console","console.help", new String[] { buf.toString() }));
    }
    
    /** Display a message using help color.
     *
     *  @param display <code>String</code> to be displayed
     */
    public void help(String display) {
        append('\n' + display, infoColor, true, true);
    }
    
    /** Display a message using error color.
     *
     *  @param display <code>String</code> to be displayed
     */
    public void error(String display) {
        append('\n' + display, errorColor, false, false);
    }
    
    /** Stops current task. */
    public void stop() {
        
        if (stdout != null) {
            stdout.interrupt();
            stdout = null;
            //stdout.stop();
        }
        
        if (stderr != null) {
            stderr.interrupt();
            stderr = null;
            //stderr.stop();
        }
        
        if (process != null) {
            process.destroy();
            Object[] args = { processName };
            error(SystemProperties.getProperty("console", "console.killed", args));
        }
        
    }
    
    /** Parse a command. Replace internal variables by their
     *  values.
     *
     *  @param command Command to be parsed
     */
    public String parseCommand(String command) {
        
        StringBuffer buf = new StringBuffer();
        
        String userDir = System.getProperty("user.dir");
        String userHome = System.getProperty("user.home");
        
        for (int i = 0; i < command.length(); i++) {
            
            char c = command.charAt(i);
            
            switch(c) {
                
                case '$':
                    
                    if (i == command.length() - 1)
                        buf.append(c);
                    
                    else {
                        
                        switch (command.charAt(++i)) {
                            case 'd':                    // current dir
                                buf.append(currentPath);
                                break;
                            case 'h':                    // home dir
                                buf.append(userHome);
                                break;
                            case 'j':                    // program dir
                                buf.append(userDir);
                                break;
                            case '$':
                                buf.append('$');
                                break;
                        }
                        
                    }
                    
                    break;
                    
                default:
                    buf.append(c);
                    
            }
            
        }
        
        return buf.toString();
        
    }
    
    public String getCurrentPath() {
        return currentPath;
    }
    
    public void setCurrentPath(String _currentPath) {
        currentPath = _currentPath;
    }
    
    /** Execute command. First parse it then check if command
     *  is built-in. At last, a process is created and threads
     *  which handle output streams are started.
     *
     *  @param command Command to be execute
     */
    public void execute(String command) {
        stop();
        
        if (command == null || command.length() == 0) {
            return;
        }
        
        int index = command.indexOf(' ');
        
        if (index != -1)
            processName = command.substring(0, index);
        else
            processName = command;
        
        command = parseCommand(command);
        
        if (command == null || command.length() == 0)
            return;
        
        if (builtInCommand(command)) {
            displayPrompt();
            return;
        }
        
        append("\n> " + command, infoColor);
        
        try {
            
            if (osType == WINDOWS_OS) {
                WINDOWS_EXEC[2] = command;
                process = Runtime.getRuntime().exec(WINDOWS_EXEC, null,
                                                        new File(currentPath));
            }
            else {
                process = Runtime.getRuntime().exec(command, null,
                                                        new File(currentPath));
            }
            process.getOutputStream().close();
            
        }
        
        catch (IOException ioe) {
            error(SystemProperties.getProperty("console", "console.error"));
            displayPrompt();
            return;
        }
        
        stdout = new StdoutThread();
        stderr = new StderrThread();
        
        if (process == null)
            displayPrompt();
    }
    
    class StdoutThread extends Thread {
        
        StdoutThread() {
            super("----thread: stout: executequery----");
            start();
        }
        
        public void run() {
            
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                while((line = in.readLine()) != null) {
                    output(line);
                }
                in.close();
                
                int exitCode = process.waitFor();
                Object[] args = { processName, new Integer(exitCode) };
                append('\n' + SystemProperties.getProperty("console", "console.exited", args), infoColor);
                
                Thread.sleep(500);
                process.destroy();
                displayPrompt();
            }
            catch(IOException io) {}
            catch(InterruptedException ie) {}
            catch (NullPointerException npe) {}
            
        }
        
    }
    
    class StderrThread extends Thread {
        
        StderrThread() {
            super("----thread: stderr: executequery----");
            start();
        }
        
        public void run() {
            
            try {
                
                if (process == null) {
                    return;
                }
                
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                
                String line;
                while((line = in.readLine()) != null) {
                    append('\n' + line, errorColor);
                }                
                in.close();
                
            }            
            catch(IOException io) {}
            
        }
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    // NEEDED BY JavaScriptParser PLUGIN
    //////////////////////////////////////////////////////////////////////////////////////////////
    /*
    private Writer writerSTDOUT = new Writer() {
        
        public void close() {}
        
        public void flush() {
            repaint();
        }
        
        public void write(char cbuf[], int off, int len) {
            append(new String(cbuf, off, len), outputColor);
        }
        
    };
    
    private Writer writeSTDERR = new Writer() {
        
        public void close() {}
        
        public void flush() {
            repaint();
        }
        
        public void write(char cbuf[], int off, int len) {
            append(new String(cbuf, off, len), errorColor);
        }
        
    };
    */
    /** Returns a writer in which external classes can send
     *  <code>String</code> to make them being displayed in the
     *  console as standard output.
     */
    /*
    public Writer getStdOut() {
        return writerSTDOUT;
    }
    */
    /** Returns a writer in which external classes can send
     *  <code>String</code> to make them being displayed in the
     *  console as error output.
     */
    /*
    public Writer getStdErr() {
        return writeSTDERR;
    }
    */
    public void cleanup() {
        currentCmd = null;
        firstCmd = null;
        
        process = null;
        processName = null;
        stdout = null;
        stderr = null;
        
        current = null;
        outputDocument = null;
        textArea = null;
        
        if (historyModel != null) {
            historyModel.cleanup();
        }
        historyModel = null;
        
        infoColor = null;
        
        prompt = null;
        hostName = null;
        promptPattern = null;
    }
    
}

// End of Console.java














