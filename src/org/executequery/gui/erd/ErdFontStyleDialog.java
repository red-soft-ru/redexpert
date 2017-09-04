/*
 * ErdFontStyleDialog.java
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

package org.executequery.gui.erd;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.GUIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ErdFontStyleDialog extends AbstractBaseDialog 
                                implements ActionListener {
    
    /** The font list */
    private JList fontList;
    
    /** The font size options list */
    private JList sizeList;

    /** The ERD parent panel */
    private ErdViewerPanel parent;

    /** The table name style combo */
    private JComboBox tableNameCombo;
    
    /** The column name style combo */
    private JComboBox columnNameCombo;
    
    private JLabel normalSample;
    private JLabel italicSample;
    private JLabel boldSample;
    private JLabel italicBoldSample;
    
    
    public ErdFontStyleDialog(ErdViewerPanel parent) {

        super(GUIUtilities.getParentFrame(), "Font Style", true);
        
        this.parent = parent;
        
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // set the currently selected values
        sizeList.setSelectedValue(Integer.toString(parent.getTableFontSize()), true);
        fontList.setSelectedValue(parent.getTableFontName(), true);
        
        int tableNameFontStyle = parent.getTableNameFontStyle();
        int columnNameFontStyle = parent.getColumnNameFontStyle();
        
        if (tableNameFontStyle == Font.PLAIN)
            tableNameCombo.setSelectedIndex(0);
        else if (tableNameFontStyle == Font.ITALIC)
            tableNameCombo.setSelectedIndex(1);
        else if (tableNameFontStyle == Font.BOLD)
            tableNameCombo.setSelectedIndex(2);
        else if (tableNameFontStyle == Font.ITALIC + Font.BOLD)
            tableNameCombo.setSelectedIndex(3);
        
        if (columnNameFontStyle == Font.PLAIN)
            columnNameCombo.setSelectedIndex(0);
        else if (columnNameFontStyle == Font.ITALIC)
            columnNameCombo.setSelectedIndex(1);
        else if (columnNameFontStyle == Font.BOLD)
            columnNameCombo.setSelectedIndex(2);
        else if (columnNameFontStyle == Font.ITALIC + Font.BOLD)
            columnNameCombo.setSelectedIndex(3);
        
        fontLists_actionPerformed();
        
        ListSelectionListener listListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                fontLists_actionPerformed(); }
        };
        
        fontList.addListSelectionListener(listListener);
        sizeList.addListSelectionListener(listListener);
        
        pack();
        
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        
        setVisible(true);
        
    }
    
    /** <p>Initialises the state of this instance. */
    private void init() throws Exception {
        
        Vector<String> fontNames = GUIUtils.getSystemFonts();
        fontList = new JList(fontNames);
        
        String[] fontSizes = {"7", "8", "9", "10", "11", "12", "14"};
        sizeList = new JList(fontSizes);
        
        JScrollPane fontScroll = new JScrollPane(fontList);
        JScrollPane sizeScroll = new JScrollPane(sizeList);
        
        fontScroll.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        String[] fontStyles = {"Plain", "Italic", "Bold", "Bold/Italic"};
        tableNameCombo = WidgetFactory.createComboBox(fontStyles);
        columnNameCombo = WidgetFactory.createComboBox(fontStyles);
        
        Dimension comboDim = new Dimension(90, 20);
        tableNameCombo.setPreferredSize(comboDim);
        columnNameCombo.setPreferredSize(comboDim);
        
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        JButton okButton = new DefaultPanelButton(Bundles.get("common.ok.button"));
        
        cancelButton.addActionListener(this);
        okButton.addActionListener(this);
        
        JPanel stylesPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,0,5,10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        stylesPanel.add(new JLabel("Table Name Style:"), gbc);
        gbc.gridx = 1;
        gbc.insets.right = 0;
        gbc.weightx = 1.0;
        stylesPanel.add(tableNameCombo, gbc);
        gbc.insets.top = 0;
        gbc.insets.bottom = 10;
        gbc.gridy = 1;
        stylesPanel.add(columnNameCombo, gbc);
        gbc.insets.right = 10;
        gbc.gridx = 0;
        gbc.weightx = 0;
        stylesPanel.add(new JLabel("Column Name Style:"), gbc);
        
        JPanel panel = new JPanel(new GridBagLayout());
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.left = 10;
        gbc.insets.right = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Font Name:"), gbc);
        gbc.insets.left = 5;
        gbc.gridx = 1;
        panel.add(new JLabel("Font Size:"), gbc);
        gbc.insets.left = 10;
        gbc.insets.bottom = 0;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        panel.add(fontScroll, gbc);
        gbc.weightx = 0.5;
        gbc.insets.left = 5;
        gbc.insets.right = 10;
        gbc.gridx = 1;
        panel.add(sizeScroll, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.gridwidth = 2;
        gbc.insets.left = 10;
        gbc.insets.bottom = 5;
        panel.add(stylesPanel, gbc);
        
        // setup the sample panel
        normalSample = new JLabel("  SAMPLE NORMAL TEXT");
        italicSample = new JLabel("  SAMPLE ITALIC TEXT");
        boldSample = new JLabel("  SAMPLE BOLD TEXT");
        italicBoldSample = new JLabel("  SAMPLE BOLD AND ITALIC TEXT");
        
        JPanel samplePanel = new JPanel();
        samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.Y_AXIS));
        samplePanel.add(normalSample);
        samplePanel.add(italicSample);
        samplePanel.add(boldSample);
        samplePanel.add(italicBoldSample);
        samplePanel.setBackground(UIManager.getColor("TextPane.background"));
        
        JScrollPane sampleScroll = new JScrollPane(samplePanel);
        sampleScroll.setPreferredSize(new Dimension(315, 62));
        
        gbc.gridy = 3;
        gbc.weighty = 0.7;
        panel.add(sampleScroll, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.insets.right = 5;
        gbc.insets.top = 5;
        gbc.insets.bottom = 10;
        panel.add(buttonPanel, gbc);
        
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(500, 400));
        
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                            new Insets(7, 7, 7, 7), 0, 0));
        
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    /** <p>Modifies the sample labels following a list selection. */
    private void fontLists_actionPerformed() {
        String fontName = (String)fontList.getSelectedValue();
        int fontSize = Integer.parseInt((String)sizeList.getSelectedValue());
        
        int italicBold = Font.BOLD + Font.ITALIC;
        
        normalSample.setFont(new Font(fontName, Font.PLAIN, fontSize));
        italicSample.setFont(new Font(fontName, Font.ITALIC, fontSize));
        boldSample.setFont(new Font(fontName, Font.BOLD, fontSize));
        italicBoldSample.setFont(new Font(fontName, italicBold, fontSize));
    }
    
    /** <p>Performs the respective action upon selection
     *  of a button within this dialog.
     *
     *  @param the <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Cancel"))
            dispose();
        
        else if (command.equals("OK")) {
            int index = tableNameCombo.getSelectedIndex();
            int tableNameStyle = -1;
            
            if (index == 0)
                tableNameStyle = Font.PLAIN;
            else if (index == 1)
                tableNameStyle = Font.ITALIC;
            else if (index == 2)
                tableNameStyle = Font.BOLD;
            else if (index == 3)
                tableNameStyle = Font.BOLD + Font.ITALIC;
            
            index = columnNameCombo.getSelectedIndex();
            int columnNameStyle = -1;
            
            if (index == 0)
                columnNameStyle = Font.PLAIN;
            else if (index == 1)
                columnNameStyle = Font.ITALIC;
            else if (index == 2)
                columnNameStyle = Font.BOLD;
            else if (index == 3)
                columnNameStyle = Font.BOLD + Font.ITALIC;
            
            parent.setTableDisplayFont((String)fontList.getSelectedValue(),
                                        tableNameStyle, columnNameStyle,
                                        Integer.parseInt((String)sizeList.getSelectedValue()));
            
            dispose();
            
        }
        
    }
    
    
}





