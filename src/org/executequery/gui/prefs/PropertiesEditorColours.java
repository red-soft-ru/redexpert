/*
 * PropertiesEditorColours.java
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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Scrollable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.table.ColourTableCellRenderer;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.ComboBoxCellRenderer;
import org.underworldlabs.util.SystemProperties;

/**
 *
 *  @author   Takis Diakoumis
 */
public class PropertiesEditorColours extends AbstractPropertiesColours implements Constants {
    
    private SamplePanel samplePanel;
    
    private SyntaxColorTableModel syntaxColoursTableModel;
    private EditorColourPreferencesTableModel editorColoursPreferencesTableModel;

    public PropertiesEditorColours() {
        
        super();
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        
        // editor background etc colours
        
        Map<String, String> editorColourKeys = new HashMap<String, String>();
        editorColourKeys.put("editor.caret.colour", "Caret colour");
        editorColourKeys.put("editor.linenumber.background", "Gutter background");
        editorColourKeys.put("editor.linenumber.foreground", "Gutter foreground");
        editorColourKeys.put("editor.text.background.colour", "Editor background");
        editorColourKeys.put("editor.results.background.colour", "Results panel background");
        editorColourKeys.put("editor.text.selection.foreground", "Text selection foreground");
        editorColourKeys.put("editor.text.selection.background", "Text selection background");
        editorColourKeys.put("editor.display.linehighlight.colour", "Current Line Highlight");
        
        List<UserPreference> editorColoursPreferences = new ArrayList<>();
        for (Map.Entry<String, String> entry : editorColourKeys.entrySet()) {
            
            editorColoursPreferences.add( 
                    new UserPreference(
                            UserPreference.COLOUR_TYPE,
                            entry.getKey(),
                            entry.getValue(),
                            SystemProperties.getColourProperty("user", entry.getKey())));
        }
        
        editorColoursPreferencesTableModel = new EditorColourPreferencesTableModel(editorColoursPreferences);
        JTable editorColoursPreferencesTable = createTableWithModel(editorColoursPreferencesTableModel);
        editorColoursPreferencesTable.setTableHeader(null);
        editorColoursPreferencesTable.addMouseListener(new MouseHandler(editorColoursPreferencesTable));        

        ColourTableCellRenderer colourRenderer = new ColourTableCellRenderer();
        colourRenderer.setFont(AbstractPropertiesBasePanel.panelFont);

        TableColumnModel tcm = editorColoursPreferencesTable.getColumnModel();

        int valueColumnWidth = 200;
        TableColumn column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(valueColumnWidth);
        column.setMaxWidth(valueColumnWidth);
        column.setMinWidth(valueColumnWidth);

        // syntax colours
                
        syntaxColoursTableModel = new SyntaxColorTableModel();
        JTable syntaxColoursTable = createTableWithModel(syntaxColoursTableModel);
        syntaxColoursTable.addMouseListener(new MouseHandler(syntaxColoursTable));
        
        tcm = syntaxColoursTable.getColumnModel();
        
        column = tcm.getColumn(0);
        column.setPreferredWidth(150);
        
        column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(120);

        column = tcm.getColumn(2);
        column.setCellRenderer(new ComboBoxCellRenderer());
        column.setPreferredWidth(70);
        
        ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(new String[]{PLAIN, ITALIC, BOLD});
        comboEditor.setFont(AbstractPropertiesBasePanel.panelFont);
        tcm.getColumn(2).setCellEditor(comboEditor);

        samplePanel = new SamplePanel();
        JScrollPane sampleScroller = new JScrollPane(
                                            samplePanel,
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets.bottom = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Query Editor Colours:"), gbc);
        gbc.weighty = 0.4;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(editorColoursPreferencesTable), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Syntax Styles:"), gbc);
        gbc.weighty = 0.5;
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(syntaxColoursTable), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Editor Sample:"), gbc);
        gbc.gridy++;
        gbc.weighty = 0.4;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sampleScroller, gbc);
        
        addContent(panel);
    }
    
    private JTable createTableWithModel(TableModel tableModel) {
        
        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(AbstractPropertiesBasePanel.panelFont);
        
        return table;
    }

    private String getLabelText(int index) {

        String[] sampleLabels = {
                "Sample normal text",
                "Sample selected text",
                "Sample current line highlight",
                "Sample keyword text",
                "Sample quote text",
                "Sample single line comment text",
                "Sample multi-line comment text",
                "Sample number text",
                "Sample operator text",
                "Sample braces text",
                "Sample literal text",
                "Sample brace match",
                "Sample brace match error",
                "Sample text"};
        
        return sampleLabels[index];
    }
    
    public void restoreDefaults() {
        
        editorColoursPreferencesTableModel.restoreDefaults();
        syntaxColoursTableModel.restoreDefaults();
    }
    
    public void save() {
        syntaxColoursTableModel.save();
        editorColoursPreferencesTableModel.save();
    }

    private String styleNameForValue(Integer value) {
        
        switch(value) {
            case 0:
                return PLAIN;
            case 1:
                return BOLD;
            case 2:
                return ITALIC;
        }
        return PLAIN;
    }

    class SamplePanel extends JPanel
                      implements Scrollable {
        int size;
        String fontName;
        Dimension dim;
        
        private Timer timer;
        private boolean showCaret;
        
        public SamplePanel() {
            size = 14;
            fontName = "monospaced";
            
            final Runnable caret = new Runnable() {
                public void run() {
                    showCaret = !showCaret;
                    repaint();
                }
            };
            
            TimerTask caretTimer = new TimerTask() {
                public void run() {
                    EventQueue.invokeLater(caret);
                }
            };
            
            timer = new Timer();
            timer.schedule(caretTimer, 0, 500);
        }
        
        public void paintComponent(Graphics g) {
            
            UIUtils.antialias(g);
            
            List<SyntaxColour> labels = new ArrayList<>(syntaxColoursTableModel.getSyntaxColours());
            labels.add(1, new SyntaxColour("Sample selected text", 
                    editorColoursPreferencesTableModel.getColorForKey("editor.text.selection.foreground"), 
                    editorColoursPreferencesTableModel.getColorForKey("editor.text.selection.background"), 0, ""));
            
            labels.add(2, new SyntaxColour("Sample current line highlight", 
                    syntaxColoursTableModel.getColorForKey("normal"),
                    editorColoursPreferencesTableModel.getColorForKey("editor.display.linehighlight.colour"), 0, ""));
            
            int row = size + 5;
            int width = getWidth();
            
            g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.text.background.colour"));
            g.fillRect(0, 0, width, (size + 5) * labels.size());
            
            int gutterWidth = 40;

            g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.linenumber.background"));
            g.fillRect(0, 0, gutterWidth, (size + 5) * (labels.size() + 2));
            
            g.setColor(GUIUtilities.getDefaultBorderColour().darker());
            g.drawLine(gutterWidth, 0, gutterWidth, getHeight() - 1);
            
            Color gutterForeground = editorColoursPreferencesTableModel.getColorForKey("editor.linenumber.foreground");
            for (int i = 0, k = labels.size(); i < k; i++) {
                
                SyntaxColour syntaxColour = labels.get(i);
                
                g.setColor(gutterForeground);
                int y1 = row * (i + 1);
                g.drawString(String.valueOf(i + 1), i < 9 ? 31 : 24, y1);

                Font font = new Font(fontName, syntaxColour.fontStyle, size);
                g.setFont(font);
                g.setColor(syntaxColour.color);
                
                if (syntaxColour.isBraceMatch()) {
                    
                    g.fillRect(gutterWidth + 4, (row * i) + 3, width, row);
                    g.setColor(Color.BLACK);

                } else if (syntaxColour.hasBackgroundColour()) {
                    
                    g.setColor(syntaxColour.background);
                    g.fillRect(gutterWidth + 4, (row * i) + 3, width, row);
                    g.setColor(syntaxColour.color);
                }
                
                String text = getLabelText(i);
                g.drawString(text, gutterWidth + 5, y1);
                
                if (showCaret) {
                    
                    FontMetrics fm = g.getFontMetrics(font);
                    int lineHeight = fm.getHeight() + 5;

                    g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.caret.colour"));
                    int carotX = fm.stringWidth(text) + gutterWidth + 6;
                    g.drawLine(carotX, y1 - lineHeight + 6, carotX, y1 + 2);
                }

            }
            
        }
        
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        
        public Dimension getPreferredSize() {
            return new Dimension(
                            getWidth(),
                            3 + ((size + 5) * (syntaxColoursTableModel.getRowCount() + 2)));
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
    
    
    class SyntaxColorTableModel extends AbstractTableModel {
        
        private Vector<SyntaxColour> syntaxColours;
        private String[] columnHeaders = {"Syntax Style",
                                          "Colour",
                                          "Font Style"};
        
        SyntaxColorTableModel() {
            syntaxColours = new Vector<SyntaxColour>(SYNTAX_TYPES.length);
            
            for (int i = 0; i < SYNTAX_TYPES.length; i++) {
                addSyntaxColour(
                    getTableValueText(i),
                    SystemProperties.getColourProperty("user", STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]), 
                    SystemProperties.getIntProperty("user", STYLE_NAME_PREFIX + SYNTAX_TYPES[i]),
                    SYNTAX_TYPES[i]);
            }
            
        }
        
        public void restoreDefaults() {

            Properties defaults = defaultsForTheme();
            for (int i = 0; i < SYNTAX_TYPES.length; i++) {

                syntaxColoursTableModel.setValueAt(
                        asColour(defaults.getProperty(STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i])), i, 1);

                syntaxColoursTableModel.setValueAt(
                        styleNameForValue(Integer.parseInt(defaults.getProperty(STYLE_NAME_PREFIX + SYNTAX_TYPES[i]))), i, 2);
            }

        }
        
        public Color getColorForKey(String key) {
            
            for (SyntaxColour syntaxColour : syntaxColours) {

                if (key.equals(syntaxColour.property)) {
                    
                    return (Color) syntaxColour.color;
                }
                
            }
            return null;
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
        
        public Vector<SyntaxColour> getSyntaxColours() {
            return syntaxColours;
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
    
    class SyntaxColour {
        String label;
        int fontStyle;
        String style;
        String property;
        Color color;
        Color background;
        
        SyntaxColour(String label, Color color, int fontStyle, String property) {
            this(label, color, null, fontStyle, property);
        }
        
        public boolean hasBackgroundColour() {
            return (background != null);
        }

        SyntaxColour(String label, Color color, Color background, int fontStyle, String property) {

            this.label = label;
            this.background = background;
            this.fontStyle = fontStyle;
            this.color = color;
            this.property = property;

            style = styleNameForValue(fontStyle);
        }
        
        public boolean isBraceMatch() {
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
            }   else {
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
        
        private JTable table;
        
        public MouseHandler(JTable table) {

            this.table = table;
        }
        
        public void mouseClicked(MouseEvent evt) {

            int row = table.rowAtPoint(evt.getPoint());
            if (row == -1) {

                return;
            }

            TableModel model = table.getModel();
            int col = table.columnAtPoint(evt.getPoint());
            if (col == 1) {

                Color color = JColorChooser.showDialog(
                                    GUIUtilities.getInFocusDialogOrWindow(),
                                    "Select Colour",
                                    (Color)model.getValueAt(row, 1));
                
                if(color != null) {

                    model.setValueAt(color, row, 1);
                }

            } else if (col == 2) {

                model.setValueAt(model.getValueAt(row, col), row, 2);
            }

        }
    } // MouseHandler

    
    class EditorColourPreferencesTableModel extends AbstractTableModel {
        
        private List<UserPreference> editorColoursPreferences;

        public EditorColourPreferencesTableModel(List<UserPreference> editorColoursPreferences) {
            this.editorColoursPreferences = editorColoursPreferences;
        }

        public Color getColorForKey(String key) {
            
            for (UserPreference userPreference : editorColoursPreferences) {
                
                if (key.equals(userPreference.getKey())) {
                    
                    return (Color) userPreference.getValue();
                }
                
            }
            return null;
        }
        
        public void setValueAt(Object value, int row, int column) {
            UserPreference preference = editorColoursPreferences.get(row);
            preference.setValue(value);
            samplePanel.repaint();
            fireTableRowsUpdated(row, row);
        }

        public int getRowCount() {
            return editorColoursPreferences.size();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int row, int column) {
            
            UserPreference preference = editorColoursPreferences.get(row);
            switch (column) {
                case 0:
                    return preference.getDisplayedKey();
                case 1:
                    return preference.getValue();
                default:
                    return Constants.EMPTY;
            }

        }
        
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }
        
        public void restoreDefaults() {

            Properties defaults = defaultsForTheme();
            for (UserPreference userPreference : editorColoursPreferences) {
            
                userPreference.setValue(asColour(defaults.getProperty(userPreference.getKey())));
            }
            fireTableDataChanged();                
        }

        public void save() {
            
            for (UserPreference userPreference : editorColoursPreferences) {
                
                SystemProperties.setProperty("user", userPreference.getKey(), userPreference.getSaveValue());
            }
            
        }
        
    } // class EditorColourPreferencesTableModel
    
    
    
}


