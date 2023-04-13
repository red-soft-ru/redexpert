/*
 * PropertiesEditorFonts.java
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

package org.executequery.gui.prefs;

import org.executequery.gui.text.SQLTextArea;
import org.executequery.log.Log;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Vector;


/**
 * @author Takis Diakoumis
 */
public class PropertiesEditorFonts extends AbstractPropertiesBasePanel
        implements ListSelectionListener {


    protected JList fontList;
    protected JList sizeList;

    protected DisabledField selectedFontField;
    protected NumberTextField selectedSizeField;

    SQLTextArea samplePanel;

    public PropertiesEditorFonts() {
        try {
            jbInit();
        } catch (Exception e) {
            Log.error("Error init Class PropertiesEditorFonts:", e);
        }
    }

    private void jbInit() {
        selectedFontField = new DisabledField();
        selectedSizeField = new NumberTextField();

        Vector<String> fontNames = GUIUtils.getSystemFonts();
        String[] fontSizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
        fontList = new JList(fontNames);
        sizeList = new JList(fontSizes);

        JScrollPane fontScroll = new JScrollPane(fontList);
        JScrollPane sizeScroll = new JScrollPane(sizeList);

        fontScroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        String _fontName = SystemProperties.getProperty("user", "sqlsyntax.font.name");
        String _fontSize = SystemProperties.getProperty("user", "sqlsyntax.font.size");

        fontList.setSelectedValue(_fontName, true);
        sizeList.setSelectedValue(_fontSize, true);

        selectedFontField.setText(_fontName);
        selectedSizeField.setText(_fontSize);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(bundledString("FontName")), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectedFontField, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(fontScroll, gbc);
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.weighty = 0;
        gbc.insets.left = 15;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(bundledString("FontSize")), gbc);
        gbc.gridy++;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectedSizeField, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sizeScroll, gbc);

        // setup samples


        samplePanel = new SQLTextArea();
        samplePanel.setText(" Sample normal text");
        //samplePanel.setBackground(SystemProperties.getColourProperty("user", "editor.text.background.colour"));

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(new JLabel(bundledString("SampleText")), gbc);
        gbc.gridy++;
        gbc.weighty = 0.5;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(samplePanel), gbc);
        addContent(panel);

        valueChanged(null);
        fontList.addListSelectionListener(this);
        sizeList.addListSelectionListener(this);
    }

    public void restoreDefaults() {
        fontList.setSelectedValue(SystemProperties.
                getProperty("defaults", "sqlsyntax.font.name"), true);
        sizeList.setSelectedValue(SystemProperties.
                getProperty("defaults", "sqlsyntax.font.size"), true);
    }

    public void valueChanged(ListSelectionEvent e) {

        if (fontList.getSelectedIndex() == -1)
            return;

        String fontName = (String) fontList.getSelectedValue();
        int fontSize = Integer.parseInt((String) sizeList.getSelectedValue());

        //int italicBold = Font.BOLD + Font.ITALIC;


        /*normalSample.setFont();
        italicSample.setFont(new Font(fontName, Font.ITALIC, fontSize));
        boldSample.setFont(new Font(fontName, Font.BOLD, fontSize));
        italicBoldSample.setFont(new Font(fontName, italicBold, fontSize));*/
        samplePanel.setFont(new Font(fontName, Font.PLAIN, fontSize));

        selectedFontField.setText(fontName);
        selectedSizeField.setText(Integer.toString(fontSize));

    }

    public void save() {
        SystemProperties.setProperty("user", "sqlsyntax.font.size",
                selectedSizeField.getStringValue());
        SystemProperties.setProperty("user", "sqlsyntax.font.name",
                (String) fontList.getSelectedValue());
    }

}















