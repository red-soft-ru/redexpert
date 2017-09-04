/*
 * MetaKeyPanel.java
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

package org.executequery.gui.browser;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.table.AbstractTableModel;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.NamedObject;
import org.underworldlabs.jdbc.DataSourceException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class MetaKeyPanel extends BrowserNodeBasePanel {
    
    public static final String NAME = "MetaKeyPanel";
    
    private MetaKeyModel model;
    
    private JLabel noValuesLabel;
    
    private Map cache;
    
    private static String HEADER_PREFIX = "Database Object: ";
    
    /** the browser's control object */
    private BrowserController controller;

    public MetaKeyPanel(BrowserController controller) {

        super("Object Type Name:");
        this.controller = controller;

        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {

        noValuesLabel = new JLabel("No objects of this type are available.", JLabel.CENTER);
        
        tablePanel().setBorder(BorderFactory.createTitledBorder("Available Objects"));
        
        model = new MetaKeyModel();
        table().setModel(model);
        
        // add the mouse listener
        table().addMouseListener(new MouseHandler());
        
        setHeaderIcon(GUIUtilities.loadIcon("DatabaseObject24.png"));

        cache = new HashMap();
    }
    
    public String getLayoutName() {
        return NAME;
    }
    
    public void refresh() {
        cache.clear();
    }
    
    public void cleanup() {}
    
    protected String getPrintablePrefixLabel() {

        return "";
    }

    public boolean hasObject(Object object) {

        return cache.containsKey(object);
    }

    public void setValues(NamedObject metaTag) {
        String[] values = null;
        try {
            List<NamedObject> objects = metaTag.getObjects();
            if (objects != null) {
                values = new String[objects.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = objects.get(i).getName();
                }
            }
        } catch (DataSourceException e) {
            controller.handleException(e);
        }
        setValues(metaTag.getName(), values);
    }
    
    public void setValues(String name) {

        setValues(name, (String[])cache.get(name));
    }
    
    public void setValues(String name, String[] values) {
        tablePanel().removeAll();
        typeField().setText(name);
        
        if (values == null || values.length == 0) {
            tablePanel().add(noValuesLabel, getPanelConstraints());
        }
        else {
            model.setValues(values);
            tablePanel().add(scroller(), getPanelConstraints());
        }
        
        setHeaderText(HEADER_PREFIX + name);
    }
    
    private class MouseHandler extends MouseAdapter {

        public MouseHandler() {}

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() < 2) {
                return;
            }

            int mouseX = e.getX();
            int mouseY = e.getY();

            int row = table().rowAtPoint(new Point(mouseX, mouseY));
            Object object = model.getValueAt(row, 0);
            if (object == null) {
                return;
            }

            controller.selectBrowserNode(object.toString());
        }
    }

    private class MetaKeyModel extends AbstractTableModel {
        
        private String[] values;
        private String header = "Object Name";
        
        public MetaKeyModel() {
            values = new String[0];
        }
        
        public void setValues(String[] values) {
            this.values = values;
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            return values.length;
        }
        
        public int getColumnCount() {
            return 1;
        }
        
        public String getColumnName(int col) {
            return header;
        }
        
        public Object getValueAt(int row, int col) {
            return values[row];
        }
        
    }
    
}






