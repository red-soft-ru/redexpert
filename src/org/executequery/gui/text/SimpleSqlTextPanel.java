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
import org.executequery.gui.editor.QueryEditorSettings;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.underworldlabs.swing.menu.SimpleTextComponentPopUpMenu;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;

/**
 * This panel is used within those components that display
 * SQL text. Typically, this will be used within functions that
 * modify the database SQL produced as a result
 * will be displayed here with complete syntax highlighting and
 * other associated visual enhancements.<p>
 * Examples of use include within the Create Table and Browser
 * Panel features where table modifications are reflected in
 * executable SQL.
 *
 * @author Takis Diakoumis
 */
public class SimpleSqlTextPanel extends DefaultTextEditorContainer {

    private final boolean autocompleteOnlyHotKey;

    protected SQLTextArea textPane;

    private boolean appending;
    private Border defaultBorder;
    private StringBuffer sqlBuffer;
    private RTextScrollPane queryScroll;
    private SimpleTextComponentPopUpMenu popup;

    public SimpleSqlTextPanel() {
        this("SQL");
    }

    public SimpleSqlTextPanel(String title) {
        this(false, false, title);
    }

    public SimpleSqlTextPanel(boolean appending, boolean autocompleteOnlyHotKey) {
        this(appending, autocompleteOnlyHotKey, "SQL");
    }

    public SimpleSqlTextPanel(boolean appending, boolean autocompleteOnlyHotKey, String title) {
        super(new BorderLayout());

        this.appending = appending;
        this.autocompleteOnlyHotKey = autocompleteOnlyHotKey;

        if (!MiscUtils.isNull(title))
            setBorder(BorderFactory.createTitledBorder(title));

        init();
    }

    private void init() {

        textPane = new SQLTextArea(autocompleteOnlyHotKey);
        textPane.setFont(QueryEditorSettings.getEditorFont());
        textPane.setDragEnabled(true);
        textComponent = textPane;

        queryScroll = new RTextScrollPane(textPane);
        queryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        queryScroll.setLineNumbersEnabled(true);

        popup = new SimpleTextComponentPopUpMenu(textPane);
        defaultBorder = queryScroll.getBorder();
        sqlBuffer = new StringBuffer();

        add(queryScroll, BorderLayout.CENTER);
        add(textPane.getCaretPositionLabel(), BorderLayout.SOUTH);
    }

    public void setSQLText(String text) {
        textPane.deleteAll();
        textPane.setText(text == null ? Constants.EMPTY : text);

        if (appending) {
            sqlBuffer.setLength(0);
            sqlBuffer.append(text);
        }
    }

    public void appendSQLText(String text) {
        textPane.deleteAll();

        if (text == null)
            text = Constants.EMPTY;

        if (appending) {
            sqlBuffer.append(text);
            textPane.setText(sqlBuffer.toString());

        } else
            textPane.setText(text);
    }

    public int save(File file) {
        String path = file != null ? file.getAbsolutePath() : Constants.EMPTY;
        return new TextFileWriter(textPane.getText(), path).write();
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    public void setDefaultBorder() {
        queryScroll.setBorder(defaultBorder);
    }

    public void setScrollPaneBorder(Border border) {
        queryScroll.setBorder(border);
    }

    public void setCaretPosition(int position) {
        textPane.setCaretPosition(position);
    }

    public String getSQLText() {
        return textPane.getText();
    }

    public boolean isEmpty() {
        return textPane.getText().isEmpty();
    }

    public void setSQLTextEditable(boolean editable) {
        textPane.setEditable(editable);
    }

    public void setAppending(boolean appending) {
        this.appending = appending;
    }

    public SQLTextArea getTextPane() {
        return textPane;
    }

    public void cleanup() {
        textPane.cleanup();
    }

    @Override
    public void disableUpdates(boolean disable) {
        textPane.disableUpdates(disable);
    }

    @Override
    public String getPrintJobName() {
        return "Red Expert - SQL Editor";
    }

    @Override
    public boolean contentCanBeSaved() {
        return true;
    }

}
