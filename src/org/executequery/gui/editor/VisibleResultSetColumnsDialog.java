/*
 * VisibleResultSetColumnsDialog.java
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

package org.executequery.gui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.resultset.ResultSetColumnHeader;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.table.TableSorter;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VisibleResultSetColumnsDialog extends BaseDialog {

    private ResultSetTable table;
    private List<ResultSetColumn> columns = new ArrayList<VisibleResultSetColumnsDialog.ResultSetColumn>();
    
    public VisibleResultSetColumnsDialog(ResultSetTable table) {

        super("Visible Result Set Columns", true);
        this.table = table;
        this.columns = createColumns(tableModel());
        init();
        
        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    private void init() {

        final JList list = new ResultSetColumnList(columns);
        Action selectAllAction = new AbstractAction("Select All") {
            public void actionPerformed(ActionEvent e) {
                for (ResultSetColumn column : columns) {                    
                    column.visible = true;
                }
                list.repaint();
            }
        };        
        Action selectNoneAction = new AbstractAction("Select None") {
            public void actionPerformed(ActionEvent e) {
                for (ResultSetColumn column : columns) {                    
                    column.visible = false;
                }                
                list.repaint();
            }
        };
        
        LinkButton selectAllCheck = new LinkButton(selectAllAction);
        LinkButton selectNoneCheck = new LinkButton(selectNoneAction);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy++;
        gbc.gridx++;
        gbc.insets.top = 10;
        gbc.insets.left = 7;
        gbc.insets.right = 5;
        gbc.insets.bottom = 5;
        panel.add(selectAllCheck, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets.left = 20;
        panel.add(selectNoneCheck, gbc);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        gbc.insets.top = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(list), gbc);
        
        ReflectiveAction action = new ReflectiveAction(this);
        
        JPanel base = new JPanel(new BorderLayout());
        base.setPreferredSize(new Dimension(400, 350));

        base.add(panel, BorderLayout.CENTER);
        base.add(buttonPanel(action), BorderLayout.SOUTH);
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                            new Insets(5, 5, 5, 5), 0, 0));

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private JPanel buttonPanel(ReflectiveAction action) {

        JButton okButton = new DefaultPanelButton(action, Bundles.get("common.ok.button"), "update");
        JButton cancelButton = new DefaultPanelButton(action, Bundles.get("common.cancel.button"), "cancel");

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.top = 5;
        gbc.weightx = 1.0;
        buttonPanel.add(okButton, gbc);
        gbc.weightx = 0;
        gbc.gridx = 1;
        gbc.insets.left = 5;
        buttonPanel.add(cancelButton, gbc);

        return buttonPanel;
    }

    private ResultSetTableModel tableModel() {
        
        return (ResultSetTableModel) ((TableSorter) table.getModel()).getTableModel();
    }

    public void update(ActionEvent e) {

        int visibleCount = 0;
        for (ResultSetColumn column : columns) {

            for (ResultSetColumnHeader resultSetColumnHeader : tableModel().getColumnHeaders()) {
                
                if (column.id.equals(resultSetColumnHeader.getId())) {

                    if (column.visible) {

                        visibleCount++;
                    }
                    resultSetColumnHeader.setVisible(column.visible);
                    break;
                }

            }

        }
        
        if (visibleCount == 0) {

            GUIUtilities.displayErrorMessage("At least one column from this result set must be visible");
            return;
        }
        
        table.columnVisibilityChanged();
        dispose();
    }

    public void cancel(ActionEvent e) {
        
        dispose();
    }

    private List<ResultSetColumn> createColumns(ResultSetTableModel tableModel) {

        List<ResultSetColumn> list = new ArrayList<VisibleResultSetColumnsDialog.ResultSetColumn>();
        for (ResultSetColumnHeader resultSetColumnHeader : tableModel.getColumnHeaders()) {
            
            list.add(new ResultSetColumn(resultSetColumnHeader.getId(), resultSetColumnHeader.getLabel(), resultSetColumnHeader.isVisible()));
        }

        return list;
    }

    class ResultSetColumnList extends JList {
        
        public ResultSetColumnList(List<ResultSetColumn> columns) {

            super(columns.toArray(new ResultSetColumn[columns.size()]));
            setCellRenderer(new CheckboxListRenderer());
            
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {

                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {

                        int index = getSelectedIndex();
                        if (index != -1) {

                            ResultSetColumn checkbox = (ResultSetColumn) getModel().getElementAt(index);
                            checkbox.setVisible(!checkbox.visible);
                            repaint();
                        }
                    }
                }
            });

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {

                    int index = locationToIndex(e.getPoint());
                    if (index != -1) {

                        ResultSetColumn checkbox = (ResultSetColumn) getModel().getElementAt(index);
                        checkbox.setVisible(!checkbox.visible);
                        repaint();
                    }
                }
            });
            
        }
        
    }
    
    
    class CheckboxListRenderer extends JCheckBox implements ListCellRenderer {
        
        private Border noFocusBorder = new EmptyBorder(3, 5, 3, 3);
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            ResultSetColumn resultSetColumn = (ResultSetColumn) value;

            setText(resultSetColumn.name);
            setSelected(resultSetColumn.visible);

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setFocusPainted(false);

            setBackground(list.getBackground());
            setBorderPainted(true);
//            setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            setBorder(noFocusBorder);
            
            return this;
        }

    }
    
    
    class ResultSetColumn {        
        
        private String id;
        private String name;
        private boolean visible;
        
        public ResultSetColumn(String id, String name, boolean visible) {
            
            this.id = id;
            this.name = name;
            this.visible = visible;
        }

        public String getId() {
         
            return id;
        }
        
        public void setVisible(boolean visible) {

            this.visible = visible;
        }
        
        @Override
        public String toString() {

            return name;
        }
    }
    
}

