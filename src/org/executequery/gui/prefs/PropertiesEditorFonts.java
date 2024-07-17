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
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


/**
 * @author Takis Diakoumis
 */
public class PropertiesEditorFonts extends AbstractPropertiesBasePanel
        implements ListSelectionListener {

    protected String fontNameKey;
    protected String fontSizeKey;

    private JList<String> fontList;
    private JList<String> sizeList;

    private SQLTextArea sampleTextPanel;
    private JTextField selectedFontField;
    private NumberTextField selectedSizeField;

    public PropertiesEditorFonts(PropertiesPanel parent) {
        super(parent);

        preInit();
        init();
        arrange();
        valueChanged(null);
    }

    protected void preInit() {
        fontNameKey = "sqlsyntax.font.name";
        fontSizeKey = "sqlsyntax.font.size";
    }

    private void init() {

        Vector<String> fontNames = GUIUtils.getSystemFonts();
        String[] fontSizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};

        String newFontName = SystemProperties.getProperty("user", fontNameKey);
        String newFontSize = SystemProperties.getProperty("user", fontSizeKey);

        fontList = new JList<>(fontNames);
        fontList.setSelectedValue(newFontName, true);
        fontList.addListSelectionListener(this);

        sizeList = new JList<>(fontSizes);
        sizeList.setSelectedValue(newFontSize, true);
        sizeList.addListSelectionListener(this);

        selectedFontField = new JTextField();
        selectedFontField.setText(newFontName);
        selectedFontField.addKeyListener(new KeyListenerImpl(fontList, fontNames));

        selectedSizeField = new NumberTextField();
        selectedSizeField.setText(newFontSize);
        selectedSizeField.addKeyListener(new KeyListenerImpl(sizeList, Arrays.asList(fontSizes)));

        sampleTextPanel = new SQLTextArea();
        sampleTextPanel.setText("Sample normal text");
    }

    private void arrange() {

        JScrollPane fontScroll = new JScrollPane(fontList);
        fontScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane sizeScroll = new JScrollPane(sizeList);


        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(bundledStaticString("FontName")), gbc);
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
        panel.add(new JLabel(bundledStaticString("FontSize")), gbc);
        gbc.gridy++;
        gbc.weightx = 0.5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(selectedSizeField, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sizeScroll, gbc);

        // setup samples


        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(new JLabel(bundledStaticString("SampleText")), gbc);
        gbc.gridy++;
        gbc.weighty = 0.5;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(sampleTextPanel), gbc);
        addContent(panel);
    }

    private void updateSampleTextPanel() {
        sampleTextPanel.setFont(new Font(
                selectedFontField.getText().trim(),
                Font.PLAIN,
                Integer.parseInt(selectedSizeField.getText().trim())
        ));
    }

    @Override
    public void save() {
        SystemProperties.setProperty("user", fontNameKey, selectedFontField.getText().trim());
        SystemProperties.setProperty("user", fontSizeKey, selectedSizeField.getStringValue());
    }

    @Override
    public void restoreDefaults() {
        fontList.setSelectedValue(SystemProperties.getProperty("defaults", fontNameKey), true);
        sizeList.setSelectedValue(SystemProperties.getProperty("defaults", fontSizeKey), true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {

        if (fontList.getSelectedIndex() == -1)
            return;

        selectedFontField.setText(fontList.getSelectedValue());
        selectedSizeField.setText(sizeList.getSelectedValue());
        updateSampleTextPanel();
    }

    private class KeyListenerImpl implements KeyListener {
        private final JList<String> targetList;
        private final List<String> availableValues;

        public KeyListenerImpl(JList<String> targetList, List<String> availableValues) {
            this.targetList = targetList;
            this.availableValues = availableValues;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            JTextField sourceField = (JTextField) e.getSource();
            String sourceText = sourceField.getText().trim();

            if (availableValues.contains(sourceText))
                targetList.setSelectedValue(sourceText, true);

            updateSampleTextPanel();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

    } // KeyListenerImpl class

}
