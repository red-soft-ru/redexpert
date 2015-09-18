/*
 * PropertiesEditorFonts.java
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

package org.executequery.gui.prefs;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.underworldlabs.util.SystemProperties;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.GUIUtils;


/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class PropertiesEditorFonts extends PropertiesBasePanel
                                   implements ListSelectionListener {
    
    private JLabel normalSample;
    private JLabel italicSample;
    private JLabel boldSample;
    private JLabel italicBoldSample;
    
    private JList fontList;
    private JList sizeList;
    
    private DisabledField selectedFontField;
    private DisabledField selectedSizeField;
    
    public PropertiesEditorFonts() {        
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void jbInit() throws Exception {
        selectedFontField = new DisabledField();
        selectedSizeField = new DisabledField();
        
        Vector<String> fontNames = GUIUtils.getSystemFonts();
        String[] fontSizes = {"8", "9", "10", "11", "12", "13", "14", "15", "16"};
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
        panel.add(new JLabel("Font Name:"), gbc);
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
        panel.add(new JLabel("Font Size:"), gbc);
        gbc.gridy++;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectedSizeField, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sizeScroll, gbc);

        // setup samples
        normalSample = new JLabel(" Sample normal text");
        italicSample = new JLabel(" Sample italic text");
        boldSample = new JLabel(" Sample bold text");
        italicBoldSample = new JLabel(" Sample bold and italic text");

        JPanel samplePanel = new JPanel();
        samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.Y_AXIS));
        samplePanel.add(normalSample);
        samplePanel.add(italicSample);
        samplePanel.add(boldSample);
        samplePanel.add(italicBoldSample);
        samplePanel.setBackground(Color.white);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(new JLabel("Sample Text:"), gbc);
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
                getProperty("defaults","sqlsyntax.font.name"), true);
        sizeList.setSelectedValue(SystemProperties.
                getProperty("defaults","sqlsyntax.font.size"), true);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        
        if (fontList.getSelectedIndex() == -1)
            return;
        
        String fontName = (String)fontList.getSelectedValue();
        int fontSize = Integer.parseInt((String)sizeList.getSelectedValue());
        
        int italicBold = Font.BOLD + Font.ITALIC;
        
        normalSample.setFont(new Font(fontName, Font.PLAIN, fontSize));
        italicSample.setFont(new Font(fontName, Font.ITALIC, fontSize));
        boldSample.setFont(new Font(fontName, Font.BOLD, fontSize));
        italicBoldSample.setFont(new Font(fontName, italicBold, fontSize));
        
        selectedFontField.setText(fontName);
        selectedSizeField.setText(Integer.toString(fontSize));
        
    }
    
    public void save() {
        SystemProperties.setProperty("user", "sqlsyntax.font.size",
                                (String)sizeList.getSelectedValue());
        SystemProperties.setProperty("user", "sqlsyntax.font.name",
                                (String)fontList.getSelectedValue());
    }
    
}














