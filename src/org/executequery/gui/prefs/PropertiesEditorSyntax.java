/*
 * PropertiesEditorSyntax.java
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


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Vector;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Scrollable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.util.SystemProperties;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.ColourTableCellRenderer;
import org.underworldlabs.swing.table.ComboBoxCellRenderer;

/** <p>Query Editor syntax highlighting preferences panel.
 *
 *  @author   Takis Diakoumis
 */
public class PropertiesEditorSyntax extends AbstractPropertiesBasePanel
                                    implements Constants {
    
    private JTable table;
    private ColorTableModel tableModel;
    private SamplePanel samplePanel;
    
    public PropertiesEditorSyntax() {
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        tableModel = new ColorTableModel();
        table = new JTable(tableModel);
        table.setFont(AbstractPropertiesBasePanel.panelFont);
        table.addMouseListener(new MouseHandler());

        TableColumnModel tcm = table.getColumnModel();
        ColourTableCellRenderer colourRenderer = new ColourTableCellRenderer();
        colourRenderer.setFont(AbstractPropertiesBasePanel.panelFont);
        
        tcm.getColumn(1).setCellRenderer(colourRenderer);
        tcm.getColumn(2).setCellRenderer(new ComboBoxCellRenderer());
        
        tcm.getColumn(0).setPreferredWidth(150);
        tcm.getColumn(1).setPreferredWidth(120);
        tcm.getColumn(2).setPreferredWidth(70);
        
        table.setRowHeight(20);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        
        ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(
                                            new String[]{PLAIN, ITALIC, BOLD});
        comboEditor.setFont(AbstractPropertiesBasePanel.panelFont);
        tcm.getColumn(2).setCellEditor(comboEditor);

        JScrollPane tableScroller = new JScrollPane();
        tableScroller.getViewport().add(table);
        
        samplePanel = new SamplePanel();
        JScrollPane sampleScroller = new JScrollPane(
                                            samplePanel,
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.bottom = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Syntax Styles:"), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(tableScroller, gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Editor Sample:"), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 0.9;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sampleScroller, gbc);
        
        addContent(panel);
        
    }
    
    private String getLabelText(int styleIndex) {
        
        switch (styleIndex) {
            
            case 0:
                return "Sample normal text";
            case 1:
                return "Sample keyword text";
            case 2:
                return "Sample quote text";
            case 3:
                return "Sample single line comment text";
            case 4:
                return "Sample multi-line comment text";
            case 5:
                return "Sample number text";
            case 6:
                return "Sample operator text";
            case 7:
                return "Sample braces text";
            case 8:
                return "Sample literal text";
            case 9:
                return "Sample brace match";
            case 10:
                return "Sample brace match error";
            default:
                return "Sample text";
                
        }
        
    }
    
    public void restoreDefaults() {
        
        for (int i = 0; i < SYNTAX_TYPES.length; i++) {
            tableModel.setValueAt(
            SystemProperties.getColourProperty("defaults",
                    STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]), i, 1);
                    tableModel.setValueAt(PLAIN, i, 2);
        }
        
    }
    
    public void save() {
        tableModel.save();
    }
    
    class SamplePanel extends JPanel
                      implements Scrollable {
        int size;
        String fontName;
        Dimension dim;
        
        public SamplePanel() {
            size = 12;
            fontName = "monospaced";
        }
        
        public void paintComponent(Graphics g) {
        	
        	UIUtils.antialias(g);
        	
            SyntaxColour[] labels = tableModel.getSyntaxColours();
            
            int row = size + 5;
            int width = getWidth();
            
            g.setColor(SystemProperties.getColourProperty("user", "editor.text.background.colour"));
            g.fillRect(0, 0, width, (size + 5) * labels.length);
            
            for (int i = 0; i < labels.length; i++) {
                g.setFont(new Font(fontName, labels[i].fontStyle, size));
                g.setColor(labels[i].color);
                
                if (labels[i].isBackgroundPainted()) {
                    g.fillRect(0, (row * i) + 3, width, row);
                    g.setColor(Color.BLACK);
                }
                
                g.drawString(getLabelText(i), 5, (row * (i + 1)));
            }
            
        }
        
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        
        public Dimension getPreferredSize() {
            return new Dimension(
                            getWidth(),
                            3 + ((size + 5) * tableModel.getRowCount()));
        }
        
        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation,
                                              int direction) {
            return size + 5;
        }
        
        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation,
                                               int direction) {
            return size + 5;
        }
        
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        
    }
    
    
    class ColorTableModel extends AbstractTableModel {
        
        private Vector<SyntaxColour> syntaxColours;
        private String[] columnHeaders = {"Syntax Style",
                                          "Colour",
                                          "Font Style"};
        
        ColorTableModel() {
            syntaxColours = new Vector<SyntaxColour>(SYNTAX_TYPES.length);
            
            for (int i = 0; i < SYNTAX_TYPES.length; i++) {
                addSyntaxColour(
                    getTableValueText(i),
                    SystemProperties.getColourProperty(
"user",                         STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]),
                        SystemProperties.getIntProperty(
"user",                             STYLE_NAME_PREFIX + SYNTAX_TYPES[i]),
                    SYNTAX_TYPES[i]);
            }
            
        }
        
        private String getTableValueText(int styleIndex) {
            
            switch (styleIndex) {
                
                case 0:
                    return "Normal Text";
                case 1:
                    return "Keywords";
                case 2:
                    return "Quote string";
                case 3:
                    return "Single-line comment";
                case 4:
                    return "Multi-line comment";
                case 5:
                    return "Number";
                case 6:
                    return "Operator";
                case 7:
                    return "Braces";
                case 8:
                    return "Literal";
                case 9:
                    return "Braces match";
                case 10:
                    return "Braces error";
                default:
                    return "Text";
                    
            }
            
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public int getRowCount() {
            return syntaxColours.size();
        }
        
        public Object getValueAt(int row, int col) {
            SyntaxColour ch = (SyntaxColour)syntaxColours.elementAt(row);
            
            switch(col) {
                case 0:
                    return ch.label;
                case 1:
                    return ch.color;
                case 2:
                    return ch.style;
                default:
                    return null;
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
            SyntaxColour ch = (SyntaxColour)syntaxColours.elementAt(row);
            
            if(col == 1) {
                ch.color = (Color)value;
            }
            
            else if (col == 2) {
                ch.style = (String)value;
                
                if (ch.style.equals(PLAIN)) {
                    ch.fontStyle = 0;
                }
                else if (ch.style.equals(BOLD)) {
                    ch.fontStyle = 1;
                }
                else if (ch.style.equals(ITALIC)) {
                    ch.fontStyle = 2;
                }
                
            }
            
            if (col == 1 || col == 2) {
                samplePanel.repaint();
            }
            
            fireTableRowsUpdated(row, row);
        }
        
        public boolean isCellEditable(int nRow, int nCol) {
            if (nCol == 2)
                return true;
            else
                return false;
        }
        
        public String getColumnName(int col) {
            return columnHeaders[col];
        }
        
        public SyntaxColour[] getSyntaxColours() {
            return (SyntaxColour[])syntaxColours.toArray(
            new SyntaxColour[syntaxColours.size()]);
        }
        
        public void save() {
            
            for(int i = 0; i < syntaxColours.size(); i++) {
                SyntaxColour ch = (SyntaxColour)syntaxColours.elementAt(i);
                SystemProperties.setColourProperty("user", 
                        STYLE_COLOUR_PREFIX + ch.property, ch.color);
                SystemProperties.setIntProperty("user", 
                        STYLE_NAME_PREFIX + ch.property, ch.fontStyle);
            }
            
        }
        
        private void addSyntaxColour(String label, Color color, int style, String property) {
            syntaxColours.addElement(new SyntaxColour(label, color, style, property));
        }
        
    } // ColorTableModel
    
    static class SyntaxColour {
        String label;
        int fontStyle;
        String style;
        String property;
        Color color;
        
        SyntaxColour(String label, Color color, int fontStyle, String property) {
            this.label = label;
            this.fontStyle = fontStyle;
            this.color = color;
            this.property = property;
            
            switch(fontStyle) {
                case 0:
                    style = PLAIN;
                    break;
                case 1:
                    style = BOLD;
                    break;
                case 2:
                    style = ITALIC;
                    break;
            }
        }
        
        public boolean isBackgroundPainted() {
            return property.indexOf("braces.") != -1;
        }
        
        public String toString() {
            return label;
        }
    } // SyntaxColour
    
    static class ColorRenderer extends JLabel
                               implements TableCellRenderer {
        
        public ColorRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean cellHasFocus,
                                                       int row, int col) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }	else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            if (value != null) {
                setBackground((Color)value);
            }
            
            return this;
        }
    } // ColorRenderer
    
    class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent evt) {

            int row = table.rowAtPoint(evt.getPoint());
            if (row == -1) {

                return;
            }

            int col = table.columnAtPoint(evt.getPoint());
            
            if (col == 1) {

                Color color = JColorChooser.showDialog(
                                    GUIUtilities.getInFocusDialogOrWindow(),
                                    "Select Colour",
                                    (Color)tableModel.getValueAt(row, 1));
                
                if(color != null) {
                    tableModel.setValueAt(color, row, 1);
                }

            } else if (col == 2) {

                tableModel.setValueAt(tableModel.getValueAt(row, col), row, 2);
            }

        }
    } // MouseHandler
    
}






