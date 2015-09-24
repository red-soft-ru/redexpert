/*
 * SimplePreferencesPanel.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.components.table.CategoryHeaderCellRenderer;
import org.executequery.components.table.FileSelectionTableCell;
import org.underworldlabs.swing.table.CheckBoxTableCellRenderer;
import org.underworldlabs.swing.table.ColourTableCellRenderer;
import org.underworldlabs.swing.table.ComboBoxCellRenderer;
import org.underworldlabs.swing.table.EachRowEditor;
import org.underworldlabs.swing.table.EachRowRenderer;
import org.underworldlabs.swing.table.NumberCellEditor;
import org.underworldlabs.swing.table.PasswordCellEditor;
import org.underworldlabs.swing.table.PasswordTableCellRenderer;
import org.underworldlabs.swing.table.StringCellEditor;
import org.underworldlabs.util.SystemProperties;

/**
 * Properties panel base.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1503 $
 * @date     $Date: 2015-09-22 07:40:15 +1000 (Tue, 22 Sep 2015) $
 */
public class SimplePreferencesPanel extends JPanel 
                                    implements MouseListener {

    /** the table grid colour */
    private static Color GRID_COLOR;
    
    /** the gutter width */
    private static int GUTTER_WIDTH;
    
    /** fixed row height for a preference value */
    protected final int VALUE_ROW_HEIGHT = 20;
    
    /** fixed row height for a preference header */
    protected final int CATEGORY_ROW_HEIGHT = 18;

    /** preferences array that this panel displays */
    private UserPreference[] preferences;
    
    /** the table display */
    private JTable table;
    
    /** the table model */
    private PreferencesTableModel tableModel;

    static {
        GRID_COLOR = UIManager.getColor("Table.gridColor");// Color.LIGHT_GRAY;
        GUTTER_WIDTH = 10;
    }
    
    /** Creates a new instance of SimplePreferencesPanel */
    public SimplePreferencesPanel(UserPreference[] preferences) {
        super(new BorderLayout());
        this.preferences = preferences;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        tableModel = new PreferencesTableModel();

        table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.setFont(PropertiesBasePanel.panelFont);
        table.setTableHeader(null);

        EachRowEditor rowEditor = new EachRowEditor(table);
        
        // lazily create as required
        FileSelectionTableCell fileRenderer = null;
        ColourTableCellRenderer colourRenderer = null;
        CheckBoxTableCellRenderer checkBoxRenderer = null;
        CategoryHeaderCellRenderer categoryRenderer = null;
        ComboBoxCellRenderer comboRenderer = null;
        PasswordTableCellRenderer passwordRenderer = null;

        EachRowRenderer rowRendererKeys = null;
        EachRowRenderer rowRendererValues = new EachRowRenderer();

        for (int i = 0; i < preferences.length; i++) {
            int type = preferences[i].getType();
            DefaultCellEditor editor = null;

            switch(type) {
                case UserPreference.ENUM_TYPE:
                case UserPreference.STRING_TYPE:
                    Object[] values = preferences[i].getAvailableValues();
                    if (values != null && values.length > 0) {
                        editor = new DefaultCellEditor(new TableComboBox(values));
                        rowEditor.setEditorAt(i, editor);
                        
                        if (comboRenderer == null) {
                            comboRenderer = new ComboBoxCellRenderer();
                        }

                        rowRendererValues.add(i, comboRenderer);
                    }
                    else {
                        rowEditor.setEditorAt(i, 
                                new DefaultCellEditor(new StringCellEditor()));
                        //rowEditor.setEditorAt(i, editor);
                    }
                    break;
                case UserPreference.PASSWORD_TYPE:
                    PasswordCellEditor passwordCellEditor = new PasswordCellEditor();
                    if (passwordRenderer == null) {

                        passwordRenderer = new PasswordTableCellRenderer(passwordCellEditor.getEchoChar());
                    }
                    rowRendererValues.add(i, passwordRenderer);
                    rowEditor.setEditorAt(i, new DefaultCellEditor(passwordCellEditor));
                    break;
                case UserPreference.INTEGER_TYPE:
                    final NumberCellEditor numEditor = 
                            new NumberCellEditor(preferences[i].getMaxLength(), true);
                    numEditor.setFont(PropertiesBasePanel.panelFont);

                    editor = new DefaultCellEditor(numEditor) {
                        public Object getCellEditorValue() {
                            return numEditor.getStringValue();
                        }
                    };

                    rowEditor.setEditorAt(i, editor);
                    break;
                case UserPreference.BOOLEAN_TYPE:
                    
                    if (checkBoxRenderer == null) {
                        checkBoxRenderer = new CheckBoxTableCellRenderer();
                        checkBoxRenderer.setHorizontalAlignment(JLabel.LEFT);
                    }

                    rowRendererValues.add(i, checkBoxRenderer);
                    rowEditor.setEditorAt(i, new DefaultCellEditor(new JCheckBox()));
                    break;
                case UserPreference.COLOUR_TYPE:
                    
                    if (colourRenderer == null) {
                        colourRenderer = new ColourTableCellRenderer();
                        colourRenderer.setFont(PropertiesBasePanel.panelFont);
                        table.addMouseListener(this);
                    }

                    rowRendererValues.add(i, colourRenderer);
                    break;
                case UserPreference.CATEGORY_TYPE:
                    
                    if (categoryRenderer == null) {
                        categoryRenderer = new CategoryHeaderCellRenderer();
                    }
                    if (rowRendererKeys == null) {
                        rowRendererKeys = new EachRowRenderer();
                    }

                    rowRendererValues.add(i, categoryRenderer);
                    rowRendererKeys.add(i, categoryRenderer);
                    break;
                case UserPreference.FILE_TYPE:
                    
                    if (fileRenderer == null) {
                        fileRenderer = new FileSelectionTableCell();
                        fileRenderer.setFont(PropertiesBasePanel.panelFont);
                    }

                    rowRendererValues.add(i, fileRenderer);
                    rowEditor.setEditorAt(i, fileRenderer);
                    break;

            }

            if (type == UserPreference.CATEGORY_TYPE) {
                table.setRowHeight(i, CATEGORY_ROW_HEIGHT);
            } else {
                table.setRowHeight(i, VALUE_ROW_HEIGHT);
            }

        }

        table.setGridColor(GRID_COLOR);
        table.setRowHeight(22);
        TableColumnModel tcm = table.getColumnModel();

        int secondColumnWidth = 200;
        TableColumn column = tcm.getColumn(2);
        column.setCellRenderer(rowRendererValues);
        column.setCellEditor(rowEditor);
        column.setPreferredWidth(secondColumnWidth);
        column.setMaxWidth(secondColumnWidth);
        column.setMinWidth(secondColumnWidth);

        column = tcm.getColumn(1);
        column.setCellRenderer(rowRendererKeys);

        column = tcm.getColumn(0);
        column.setMaxWidth(GUTTER_WIDTH);
        column.setMinWidth(GUTTER_WIDTH);
        column.setPreferredWidth(GUTTER_WIDTH);
        column.setCellRenderer(categoryRenderer);

        DisplayViewport viewport = new DisplayViewport(table);
        JScrollPane scroller = new JScrollPane();
        scroller.setViewport(viewport);
        add(scroller, BorderLayout.CENTER);

    }
    
    protected void restoreDefaults() {

        for (int i = 0; i < preferences.length; i++) {
            
            switch (preferences[i].getType()) {
                case UserPreference.ENUM_TYPE:
                case UserPreference.STRING_TYPE:
                case UserPreference.INTEGER_TYPE:
                    preferences[i].reset(
                            SystemProperties.getProperty("defaults", preferences[i].getKey()));
                    break;                
                case UserPreference.BOOLEAN_TYPE:
                    preferences[i].reset(
                            new Boolean(SystemProperties.getProperty("defaults", preferences[i].getKey())));
                    break;
                case UserPreference.COLOUR_TYPE:
                    preferences[i].reset(
                            SystemProperties.getColourProperty("defaults", preferences[i].getKey()));
                    break;
            }

        }
        fireTableDataChanged();
    }
    
    protected void fireTableDataChanged() {
        
        tableModel.fireTableDataChanged();
        
        /*
        // reset the row heights
        for (int i = 1; i < preferences.length; i++) {
            
            int type = preferences[i].getType();
            if (type == UserPreference.CATEGORY_TYPE) {
            
                table.setRowHeight(i, CATEGORY_ROW_HEIGHT);

            } else {
            
                table.setRowHeight(i, VALUE_ROW_HEIGHT);
            }
        
        }
        
        */

    }
    
    public UserPreference[] getPreferences() {
        return preferences;
    }
    
    protected void savePreferences() {
        // stop table editing
        if (table.isEditing()) {
            table.editingStopped(null);
        }

        String propertiesName = "user";
        
        // set the new properties
        for (int i = 0; i < preferences.length; i++) {
            
            if (preferences[i].getType() != UserPreference.CATEGORY_TYPE) {
                SystemProperties.setProperty(propertiesName, preferences[i].getKey(),
                                             preferences[i].getSaveValue());
            }

        }
        
    }
    
    public void mouseClicked(MouseEvent evt) {
        int valueColumn = 2;
        int row = table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());
        
        if (row == -1) {
            return;
        }

        if (col == valueColumn) {
            
            if (preferences[row].getType() == UserPreference.COLOUR_TYPE) {
                Color oldColor = (Color)preferences[row].getValue();
                Color newColor = JColorChooser.showDialog(
                                        GUIUtilities.getInFocusDialogOrWindow(),
                                        "Select Colour",
                                        (Color)tableModel.getValueAt(row, valueColumn));

                if(newColor != null) {
                    tableModel.setValueAt(newColor, row, valueColumn);
                    firePropertyChange(Constants.COLOUR_PREFERENCE, oldColor, newColor);
                }

            }

        } 

    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    
    class PreferencesTableModel extends AbstractTableModel {
        
        public void setValueAt(Object value, int row, int column) {
            UserPreference preference = preferences[row];
            preference.setValue(value);
        }

        public int getRowCount() {
            return preferences.length;
        }

        public int getColumnCount() {
            return 3;
        }

        public Object getValueAt(int row, int column) {
            UserPreference preference = preferences[row];

            switch (column) {
                case 1:
                    return preference.getDisplayedKey();
                case 2:
                    return preference.getValue();
                default:
                    return Constants.EMPTY;
            }

        }
        
        public boolean isCellEditable(int row, int column) {
            UserPreference preference = preferences[row];
            return (preference.getType() !=  UserPreference.CATEGORY_TYPE)
                    && (column == 2);
        }
        
    } // class PreferencesTableModel
    
    
    class DisplayViewport extends JViewport {
        
        protected DisplayViewport(JTable _table) {
            setView(_table);
            setBackground(_table.getBackground());
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int viewHeight = getViewSize().height;
            g.setColor(GRID_COLOR);
            g.fillRect(0, viewHeight - 1, GUTTER_WIDTH, getHeight() - viewHeight + 1);
        }

    } // class DisplayViewport

    @SuppressWarnings({"rawtypes", "unchecked"})
    class TableComboBox extends JComboBox {

        public TableComboBox(Object[] values) {
            
            super(values);
            setFont(PropertiesBasePanel.panelFont);
        }

    } // class TableComboBox

}

