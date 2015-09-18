/*
 * TableDataTab.java
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

package org.executequery.gui.browser;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.CancelButton;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.TableDataChange;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.gui.editor.ResultSetTableContainer;
import org.executequery.gui.editor.ResultSetTablePopupMenu;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;
import org.underworldlabs.swing.UpdatableLabel;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.table.SortableHeaderRenderer;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TableDataTab extends JPanel 
    implements ResultSetTableContainer, TableModelListener, UserPreferenceListener {

    private ResultSetTableModel tableModel;

    private ResultSetTable table;

    private JScrollPane scroller;

    private DatabaseObject databaseObject;

    private boolean executing = false;
    
    private SwingWorker worker;

    private boolean cancelled;

    private GridBagConstraints scrollerConstraints;

    private GridBagConstraints errorLabelConstraints;

    private GridBagConstraints rowCountPanelConstraints;

    private GridBagConstraints canEditTableNoteConstraints;
    
//    private GridBagConstraints toolBarConstraints;
    
//    private TableDataTabToolBar toolBar;
    
    private DisabledField rowCountField;

    private JPanel rowCountPanel;

    private final boolean displayRowCount;

    private List<TableDataChange> tableDataChanges;

    private JPanel canEditTableNotePanel;
    
    private JLabel canEditTableLabel;
    
    private boolean alwaysShowCanEditNotePanel;
   
    private InterruptibleProcessPanel cancelPanel;
    
    public TableDataTab(boolean displayRowCount) {

        super(new GridBagLayout());
        this.displayRowCount = displayRowCount;

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        if (displayRowCount) {
            
            initRowCountPanel();
        }
        
//        toolBarConstraints = new GridBagConstraints(0, 0, 
//                GridBagConstraints.REMAINDER, 1, 1.0, 0,
//                GridBagConstraints.NORTHWEST,
//                GridBagConstraints.HORIZONTAL,
//                new Insets(5, 5, 1, 5), 0, 0);
//        toolBar = new TableDataTabToolBar();
        
        canEditTableNotePanel = createCanEditTableNotePanel();
        canEditTableNoteConstraints = new GridBagConstraints(1, 1, 1, 1, 1.0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 0, 5), 0, 0);

        scroller = new JScrollPane();
        scrollerConstraints = new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0,
                                GridBagConstraints.SOUTHEAST,
                                GridBagConstraints.BOTH,
                                new Insets(5, 5, 5, 5), 0, 0);

        rowCountPanelConstraints = new GridBagConstraints(1, 3, 1, 1, 1.0, 0,
                GridBagConstraints.SOUTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 5, 5, 5), 0, 0);

        errorLabelConstraints = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0);
        
        tableDataChanges = new ArrayList<TableDataChange>();
        alwaysShowCanEditNotePanel = SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "browser.always.show.table.editable.label");
        
        cancelPanel = new InterruptibleProcessPanel("Executing query for data...");
        
        EventMediator.registerListener(this);
    }

    private JPanel createCanEditTableNotePanel() {

        final JPanel panel = new JPanel(new GridBagLayout());
        
        canEditTableLabel = new UpdatableLabel();
        JButton hideButton = new LinkButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                panel.setVisible(false);
            }
        });
        hideButton.setText("Hide");
        
        JButton alwaysHideButton = new LinkButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                panel.setVisible(false);
                alwaysShowCanEditNotePanel = false;

                SystemProperties.setBooleanProperty(Constants.USER_PROPERTIES_KEY, 
                        "browser.always.show.table.editable.label", false);

                EventMediator.fireEvent(new DefaultUserPreferenceEvent(TableDataTab.this, null, UserPreferenceEvent.ALL));
                
            }
        });
        alwaysHideButton.setText("Always Hide");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(canEditTableLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(hideButton, gbc);
        gbc.gridx++;
        gbc.insets.left = 15;
        gbc.insets.right = 10;
        panel.add(alwaysHideButton, gbc);

        panel.setBorder(UIUtils.getDefaultLineBorder());
        
        return panel;
    }

    public synchronized void loadDataForTable(final DatabaseObject databaseObject) {

        if (worker != null) {

            cancel();
            worker.interrupt();
        }

        worker = new SwingWorker() {
            public Object construct() {
                try {

                    removeAll();
                    add(cancelPanel, scrollerConstraints);
                    
                    repaint();
                    cancelPanel.start();                    
                    executing = true;
                    
                    return setTableResultsPanel(databaseObject);

                } catch (Exception e) {

                    addErrorLabel(e);
                    return "done";
                }
            }
            public void finished() {

                executing = false;
                cancelled = false;
            }

        };
        worker.start();
    }

    private void cancel() {
        
        if (executing) {
            try {

                Log.debug("Cancelling open statement for data tab for table - " + databaseObject.getName());
                cancelStatement();

            } finally {
                
                cancelled = true;
            }
        }
        
    }
    
    private List<String> primaryKeyColumns = new ArrayList<String>(0);
    private List<String> foreignKeyColumns = new ArrayList<String>(0);

    private Object setTableResultsPanel(DatabaseObject databaseObject) {
        
        tableDataChanges.clear();
        primaryKeyColumns.clear();
        foreignKeyColumns.clear();
        
//        GUIUtilities.showWaitCursor();
        
        this.databaseObject = databaseObject;
        try {

            initialiseModel();
            tableModel.setCellsEditable(false);
            tableModel.removeTableModelListener(this);

            if (isDatabaseTable()) {

                DatabaseTable databaseTable = asDatabaseTable();
                if (databaseTable.hasPrimaryKey()) {

                    primaryKeyColumns = databaseTable.getPrimaryKeyColumnNames();
					canEditTableLabel.setText("This table has a primary key(s) and data may be edited here");
                }

                if (databaseTable.hasForeignKey()) {
                	
                	foreignKeyColumns = databaseTable.getForeignKeyColumnNames();
                }
                
                if (primaryKeyColumns.isEmpty()) {
                    
                    canEditTableLabel.setText("This table has no primary keys defined and is not editable here");
                }
                
                canEditTableNotePanel.setVisible(alwaysShowCanEditNotePanel);
            }

            if (!isDatabaseTable()) {
             
                canEditTableNotePanel.setVisible(false);
            }

            ResultSet resultSet = databaseObject.getData(true);
            tableModel.createTable(resultSet);
            if (table == null) {

                createResultSetTable();
            }
            tableModel.setNonEditableColumns(primaryKeyColumns);

            TableSorter sorter = new TableSorter(tableModel);
            table.setModel(sorter);
            sorter.setTableHeader(table.getTableHeader());

            if (isDatabaseTable()) {
	            
	            SortableHeaderRenderer renderer = new SortableHeaderRenderer(sorter) {
	            	
	            	private ImageIcon primaryKeyIcon = GUIUtilities.loadIcon(BrowserConstants.PRIMARY_COLUMNS_IMAGE); 
	            	private ImageIcon foreignKeyIcon = GUIUtilities.loadIcon(BrowserConstants.FOREIGN_COLUMNS_IMAGE); 
	            	
	            	@Override
	            	public Component getTableCellRendererComponent(JTable table,
	            			Object value, boolean isSelected, boolean hasFocus,
	            			int row, int column) {
	
	            		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	            		
	            		Icon keyIcon = iconForValue(value);
	            		if (keyIcon != null) {
	            		
		            		Icon icon = renderer.getIcon();
		            		if (icon != null) {
		            			
		            			BufferedImage image = new BufferedImage(icon.getIconWidth() + keyIcon.getIconWidth() + 2, 
		            					Math.max(keyIcon.getIconHeight(), icon.getIconHeight()), BufferedImage.TYPE_INT_ARGB);
		            			
		            			Graphics graphics = image.getGraphics();
		            			keyIcon.paintIcon(null, graphics, 0, 0);
		            			icon.paintIcon(null, graphics, keyIcon.getIconWidth() + 2, 5);
		
		            			setIcon(new ImageIcon(image));

		            		} else {
		            			
		            			setIcon(keyIcon);
		            		}

	            		}
	            		
						return renderer;
	            	}
	            	
	            	private ImageIcon iconForValue(Object value) {
	            		
	            		if (value != null) {
	            			
	            			String name = value.toString();
	            			if (primaryKeyColumns.contains(name)) {
	            				
	            				return primaryKeyIcon;
	            			
	            			} else if (foreignKeyColumns.contains(name)) {
	            				
	            				return foreignKeyIcon;
	            			}
	            			
	            		}
	            		
	            		return null;
	            	}
	            	
	            	
	            };
	            sorter.setTableHeaderRenderer(renderer);
            
            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            
            scroller.getViewport().add(table);
            removeAll();

//            add(toolBar, toolBarConstraints);
            add(canEditTableNotePanel, canEditTableNoteConstraints);
            add(scroller, scrollerConstraints);
            
            if (displayRowCount && SystemProperties.getBooleanProperty("user", "browser.query.row.count")) {
                
                add(rowCountPanel, rowCountPanelConstraints);
                rowCountField.setText(String.valueOf(sorter.getRowCount()));
            }
            
        } catch (DataSourceException e) {

            if (!cancelled) {
             
                addErrorLabel(e);
            
            } else {
                
                addCancelledLabel();
            }

        } finally {

//            GUIUtilities.showNormalCursor();
            tableModel.addTableModelListener(this);
        }

        setTableProperties();
        validate();
        repaint();

        return "done";
    }

    private void initialiseModel() {
    
        if (tableModel == null) {

            tableModel = new ResultSetTableModel(SystemProperties.getIntProperty("user", "browser.max.records"));
            tableModel.setHoldMetaData(false);
        }

    }

    private boolean isDatabaseTable() {

        return this.databaseObject instanceof DatabaseTable;
    }

    private void addErrorLabel(Throwable e) {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p><center>Error retrieving object data");
        String message = e.getMessage();
        if (StringUtils.isNotBlank(message)) {

            sb.append("<br />[ ").append(message);
        }

        sb.append(" ]</center></p><p><center><i>(Note: Data will not always be available for all object types)</i></center></p></body></html>");
        
        addErrorPanel(sb);
    }

    private void addErrorPanel(StringBuilder sb) {

        removeAll();

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 20, 10, 20);
        panel.add(new JLabel(sb.toString()), gbc);
        panel.setBorder(UIUtils.getDefaultLineBorder());

        add(panel, errorLabelConstraints);
    }

    private void addCancelledLabel() {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p><center>Statement execution cancelled at user request.");
        sb.append("</center></p><p><center><i>(Note: Data will not always be available for all object types)</i></center></p></body></html>");

        addErrorPanel(sb);
    }

    private void createResultSetTable() {

        table = new ResultSetTable();
        table.addMouseListener(new ResultSetTablePopupMenu(table, this));
        setTableProperties();
    }

    private void initRowCountPanel() {

        rowCountField = new DisabledField();
        rowCountPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        rowCountPanel.add(new JLabel("Data Row Count:"), gbc);
        gbc.gridx = 2;
        gbc.insets.bottom = 2;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.right = 0;
        rowCountPanel.add(rowCountField, gbc);        
    }

    
    /**
     * Whether a SQL SELECT statement is currently being executed by this class.
     *
     * @return <code>true</code> | <code>false</code>
     */
    public boolean isExecuting() {

        return executing;
    }

    /** Cancels the currently executing statement. */
    public void cancelStatement() {

        if (worker != null) {

            worker.interrupt();
        }

        worker = new SwingWorker() {
            @Override
            public Object construct() {

                databaseObject.cancelStatement();                
                return "done";
            }
        };
        worker.start();
    }

    /** Sets default table display properties. */
    public void setTableProperties() {

        if (table == null) {

            return;
        }

        table.applyUserPreferences();
        table.setCellSelectionEnabled(false);

        tableModel.setMaxRecords(SystemProperties.getIntProperty("user", "browser.max.records"));
    }

    /*
    private void showNormalCursor() {

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void showWaitCursor() {

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    */

    public JTable getTable() {

        return table;
    }

    public boolean isTransposeAvailable() {

        return false;
    }

    public void transposeRow(TableModel tableModel, int row) {

        // do nothing
    }

    public void tableChanged(TableModelEvent e) {

        if (isDatabaseTable()) {

            int row = e.getFirstRow();
            if (row >= 0) {

                List<RecordDataItem> rowDataForRow = tableModel.getRowDataForRow(row);
                for (RecordDataItem recordDataItem : rowDataForRow) {
    				
                	if (recordDataItem.isChanged()) {
                		
                		Log.debug("Change detected in column [ " + recordDataItem.getName() + " ] - value [ " + recordDataItem.getValue() + " ]");
                		
                		asDatabaseTable().addTableDataChange(new TableDataChange(rowDataForRow));
                		return;
                	}
    
    			}

            }
        }

    }

    private DatabaseTable asDatabaseTable() {
        
        if (isDatabaseTable()) {

            return (DatabaseTable) this.databaseObject;
        }
        return null;
    }

    public boolean hasChanges() {

        if (isDatabaseTable()) {

            return asDatabaseTable().hasTableDataChanges();
        }
        return false;
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof UserPreferenceEvent);
    }

    public void preferencesChanged(UserPreferenceEvent event) {

        alwaysShowCanEditNotePanel = SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "browser.always.show.table.editable.label");        
    }

    
    class InterruptibleProcessPanel extends JPanel implements ActionListener {
        
        private ProgressBar progressBar;

        public InterruptibleProcessPanel(String labelText) {

            super(new GridBagLayout());

            progressBar = ProgressBarFactory.create();
            ((JComponent) progressBar).setPreferredSize(new Dimension(260, 18));

            JButton cancelButton = new CancelButton();
            cancelButton.addActionListener(this);

            GridBagConstraints gbc = new GridBagConstraints();
            Insets ins = new Insets(0, 20, 10, 20);
            gbc.insets = ins;
            add(new JLabel(labelText), gbc);
            gbc.gridy = 1;
            gbc.insets.top = 5;
            add(((JComponent) progressBar), gbc);
            gbc.gridy = 2;
            add(cancelButton, gbc);

            setBorder(UIUtils.getDefaultLineBorder());            
        }
  
        public void start() {
            
            progressBar.start();
        }
        
        public void actionPerformed(ActionEvent e) {

            progressBar.stop();
            cancel();
        }
        
    }

    
}

