/*
 * SimpleSqlTextPanel.java
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

package org.executequery.gui.text;

import org.executequery.Constants;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;

/**
 * This panel is used within those components that display
 * SQL text. Typically this will be used within functions that
 * modify the database schema and the SQL produced as a result
 * will be displayed here with complete syntax highlighting and
 * other associated visual enhancements.<br>
 * <p>
 * Examples of use include within the Create Table and Browser
 * Panel features where table modifications are reflected in
 * executable SQL.
 *
 * @author Takis Diakoumis
 */
public class SimpleSqlTextPanel extends DefaultTextEditorContainer
        implements KeywordListener {

    /**
     * The SQL text pane
     */
    protected SQLTextPane textPane;

    /**
     * Whether test is to be appended
     */
    private boolean appending;

    /**
     * The StringBuffer if appending
     */
    private StringBuffer sqlBuffer;

    /**
     * The text area's scroller
     */
    private JScrollPane sqlScroller;

    /**
     * The default border
     */
    private Border defaultBorder;

    private SimpleTextComponentPopUpMenu popup;

    public SimpleSqlTextPanel() {
        this(false);
    }

    public SimpleSqlTextPanel(boolean appending) {
        super(new BorderLayout());

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sqlBuffer = new StringBuffer();
        this.appending = appending;
    }

    private void init() throws Exception {

        setBorder(BorderFactory.createTitledBorder("SQL"));

        textPane = new SQLTextPane();
        textPane.setFont(new Font("monospaced", Font.PLAIN, 12));
//        textPane.setBackground(null);
        textPane.setDragEnabled(true);
        textComponent = textPane;

        popup = new SimpleTextComponentPopUpMenu(textPane);

        sqlScroller = new JScrollPane(textPane);
        defaultBorder = sqlScroller.getBorder();
        add(sqlScroller, BorderLayout.CENTER);
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    public void addPopupMenuItem(JMenuItem menuItem, int index) {
        popup.add(menuItem, index);
    }

    public void setSQLKeywords(boolean reset) {
        textPane.setSQLKeywords(true);
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        textPane.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        textPane.setSQLKeywords(true);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof KeywordEvent);
    }

    public void setDefaultBorder() {
        sqlScroller.setBorder(defaultBorder);
    }

    public void setScrollPaneBorder(Border border) {
        sqlScroller.setBorder(border);
    }

    public void setSQLText(String text) {
        textPane.deleteAll();
        textPane.setText(text == null ? Constants.EMPTY : text);

        if (appending) {
            sqlBuffer.setLength(0);
            //sqlBuffer.delete(0, sqlBuffer.length());
            sqlBuffer.append(text);
        }

    }

    public void disableUpdates(boolean disable) {
        textPane.disableUpdates(disable);
    }

    public void setCaretPosition(int position) {
        textPane.setCaretPosition(position);
    }

    /**
     * <p>Sets the SQL text pane's background colour
     * to the specified value.
     *
     * @param the background colour to apply
     */
    public void setSQLTextBackground(Color background) {
        textPane.setBackground(background);
    }

    /**
     * <p>Appends the specified text to the SQL text pane.
     *
     * @param the text to append
     */
    public void appendSQLText(String text) {
        textPane.deleteAll();

        if (text == null) {
            text = Constants.EMPTY;
        }

        if (appending) {
            sqlBuffer.append(text);
            textPane.setText(sqlBuffer.toString());
        } else {
            textPane.setText(text);
        }
    }

    /**
     * <p>Retrieves the SQL text as contained
     * within the SQL text pane.
     *
     * @return the SQL text
     */
    public String getSQLText() {
        return textPane.getText();
    }

    /**
     * <p>Returns wether the text pane contains any text.
     *
     * @return <code>true</code> if the text pane has text |
     * <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return textPane.getText().length() == 0;
    }

    /**
     * <p>Sets the SQL text pane to be editable or not
     * as specified by the passed in value.
     *
     * @param <code>true</code> to be editable |
     *                          <code>false</code> otherwise
     */
    public void setSQLTextEditable(boolean editable) {
        textPane.setEditable(editable);
    }

    /**
     * <p>Sets the SQL text pane appending as specified.
     *
     * @param <code>true</code> to append |
     *                          <code>false</code> otherwise
     */
    public void setAppending(boolean appending) {
        this.appending = appending;
    }

    public String getPrintJobName() {
        return "RedXpert - SQL Editor";
    }

    public SQLTextPane getTextPane() {
        return textPane;
    }

    public int save(File file) {

        String text = textPane.getText();

        TextFileWriter writer = null;

        if (file != null) {

            writer = new TextFileWriter(text, file.getAbsolutePath());

        } else {

            writer = new TextFileWriter(text, Constants.EMPTY);
        }

        return writer.write();
    }

    public boolean contentCanBeSaved() {

        return true;
    }

}






