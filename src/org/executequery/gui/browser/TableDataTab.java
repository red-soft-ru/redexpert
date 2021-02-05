/*
 * TableDataTab.java
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

import com.github.lgooddatepicker.components.DatePicker;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.CancelButton;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.event.*;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.editor.ResultSetTableContainer;
import org.executequery.gui.editor.ResultSetTablePopupMenu;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.resultset.ResultSetColumnHeader;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.table.SortableHeaderRenderer;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Timer;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class TableDataTab extends JPanel
        implements ResultSetTableContainer, TableModelListener, UserPreferenceListener {

    private final boolean displayRowCount;
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
    private DisabledField rowCountField;
    private JPanel rowCountPanel;
    private List<TableDataChange> tableDataChanges;

    private JPanel canEditTableNotePanel;

    private JLabel canEditTableLabel;

    private boolean alwaysShowCanEditNotePanel;

    private InterruptibleProcessPanel cancelPanel;

    private JPanel buttonsEditingPanel;

    private StatementExecutor querySender;

    private List<String> primaryKeyColumns = new ArrayList<String>(0);

    private List<String> foreignKeyColumns = new ArrayList<String>(0);

    private List<org.executequery.databaseobjects.impl.ColumnConstraint> foreigns;
    private Timer timer;

    public TableDataTab(boolean displayRowCount) {

        super(new GridBagLayout());
        this.displayRowCount = displayRowCount;

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() {

        if (displayRowCount) {

            initRowCountPanel();
        }
        createButtonsEditingPanel();

        canEditTableNotePanel = createCanEditTableNotePanel();
        canEditTableNoteConstraints = new GridBagConstraints(1, 1, 1, 1, 1.0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 5), 0, 0);

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

        cancelPanel = new InterruptibleProcessPanel(bundleString("labelExecuting"));

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

    public void loadDataForTable(final DatabaseObject databaseObject) {

        addInProgressPanel();

        if (timer != null) {

            timer.cancel();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                load(databaseObject);
            }
        }, 600);

    }

    private void load(final DatabaseObject databaseObject) {

        ConnectionsTreePanel treePanel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        synchronized (treePanel) {
            treePanel.getTree().setEnabled(false);
            if (worker != null) {

                cancel();
                worker.interrupt();
            }

            worker = new SwingWorker() {

                public Object construct() {
                    try {
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

                    ConnectionsTreePanel treePanel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
                    treePanel.getTree().setEnabled(true);
                }

            };
            worker.start();
        }
    }

    private void addInProgressPanel() {

        ThreadUtils.invokeLater(new Runnable() {

            @Override
            public void run() {

                removeAll();
                add(cancelPanel, scrollerConstraints);

                repaint();
                cancelPanel.start();
            }
        });

    }

    private void cancel() {

        if (executing) {
            try {

                Log.debug("Cancelling open statement for data tab for table - " + databaseObject.getName());
                cancelStatement();

            } finally {
                tableModel.cancelFetch();
                cancelled = true;
            }
        }

    }

    private Object setTableResultsPanel(DatabaseObject databaseObject) {
        querySender = new DefaultStatementExecutor(databaseObject.getHost().getDatabaseConnection(), true);
        tableDataChanges.clear();
        primaryKeyColumns.clear();
        foreignKeyColumns.clear();

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
                    foreigns = databaseTable.getForeignKeys();
                } else {
                    if (foreigns == null)
                        foreigns = new ArrayList<>();
                    else
                        foreigns.clear();
                }


                if (primaryKeyColumns.isEmpty()) {

                    canEditTableLabel.setText("This table has no primary keys defined and is not editable here");
                }

                canEditTableNotePanel.setVisible(alwaysShowCanEditNotePanel);
            }
            List<ColumnData> columnDataList = new ArrayList<>();
            if (!isDatabaseTableObject()) {

                canEditTableNotePanel.setVisible(false);
                buttonsEditingPanel.setVisible(false);
            } else {
                List<DatabaseColumn> list = asDatabaseTableObject().getColumns();
                if (columnDataList == null)
                    columnDataList = new ArrayList<>();
                columnDataList.clear();
                for (DatabaseColumn column : list)
                    columnDataList.add(new ColumnData(databaseObject.getHost().getDatabaseConnection(), column));
            }

            Log.debug("Retrieving data for table - " + databaseObject.getName());
            try {
                ResultSet resultSet = databaseObject.getData();
                tableModel.createTable(resultSet, columnDataList);

            } catch (DataSourceException e) {
                if ((e.getCause() instanceof SQLException)) {
                    SQLException sqlException = (SQLException) e.getCause();
                    if (sqlException.getSQLState().contentEquals("28000"))
                        GUIUtilities.displayExceptionErrorDialog("Data access error", e);
                    else rebuildDataFromMetadata(columnDataList);
                } else rebuildDataFromMetadata(columnDataList);
            } catch (Exception e) {
                rebuildDataFromMetadata(columnDataList);
            }
            createResultSetTable();
            List<String> nonEditableCols = new ArrayList<>();
            //nonEditableCols.addAll(primaryKeyColumns);
            if (isDatabaseTableObject())
                for (DatabaseColumn databaseColumn : asDatabaseTableObject().getColumns()) {
                    if (!nonEditableCols.contains(databaseColumn.getName())) {
                        if (databaseColumn.isGenerated())
                            nonEditableCols.add(databaseColumn.getName());
                    }
                }
            tableModel.setNonEditableColumns(nonEditableCols);

            TableSorter sorter = new TableSorter(tableModel);
            sorter.addSortingListener(new SortingListener() {
                @Override
                public void presorting(SortingEvent e) {
                            tableModel.setFetchAll(true);
                            tableModel.fetchMoreData();
                            if (displayRowCount) {
                                rowCountField.setText(String.valueOf(tableModel.getRowCount()));
                            }
                }

                @Override
                public void postsorting(SortingEvent e) {

                }

                @Override
                public boolean canHandleEvent(ApplicationEvent event) {
                    return false;
                }
            });
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
            if (foreigns != null)
                if (foreigns.size() > 0)
                    for (org.executequery.databaseobjects.impl.ColumnConstraint key : foreigns) {
                        Vector items = itemsForeign(key);
                        table.setComboboxColumn(tableModel.getColumnIndex(key.getColumnName()), items);
                    }


            scroller.getViewport().add(table);
            if (scroller.getVerticalScrollBar().getAdjustmentListeners().length < 1)
                scroller.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
                    @Override
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
                            int extent = scrollBar.getModel().getExtent();
                            int maximum = scrollBar.getModel().getMaximum();
                            if (extent + e.getValue() == maximum) {
                                if (!tableModel.isResultSetClose()) {
                                    tableModel.fetchMoreData();
                                    if (displayRowCount)
                                        rowCountField.setText(String.valueOf(tableModel.getRowCount()));
                                }
                            }
                        }
                    }
                });
            removeAll();

            add(buttonsEditingPanel, canEditTableNoteConstraints);
            add(scroller, scrollerConstraints);

            if (displayRowCount) {

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

            tableModel.addTableModelListener(this);
        }

        setTableProperties();
        validate();
        repaint();

        return "done";
    }

    void rebuildDataFromMetadata(List<ColumnData> columnDataList) {
        Log.error("Error retrieving data for table - " + databaseObject.getName() + ". Try to rebuild table model.");
        databaseObject.releaseResources();
        ResultSet resultSet = databaseObject.getMetaData();
        tableModel.createTableFromMetaData(resultSet, databaseObject.getHost().getDatabaseConnection(), columnDataList);
    }

    Vector itemsForeign(org.executequery.databaseobjects.impl.ColumnConstraint key) {
        String query = "SELECT distinct " + key.getReferencedColumn() + " FROM " + key.getReferencedTable() + " order by 1";
        Vector items = new Vector();
        try {
            ResultSet rs = querySender.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                items.add(rs.getObject(1));
            }
        } catch (Exception e) {
            Log.error("Error get Foreign keys:" + e.getMessage());
        } finally {
            querySender.releaseResources();
        }
        items.add(null);
        return items;
    }

    private void initialiseModel() {

        if (tableModel == null) {

            tableModel = new ResultSetTableModel(SystemProperties.getIntProperty("user", "browser.max.records"), true);
            tableModel.setHoldMetaData(false);
        }

    }

    private boolean isDatabaseTable() {

        return this.databaseObject instanceof DatabaseTable;
    }

    private boolean isDatabaseView() {
        return this.databaseObject instanceof DatabaseView;
    }

    private void addErrorLabel(Throwable e) {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p><center>Error retrieving object data");
        e.printStackTrace();
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
        table.addMouseListener(new ResultSetTablePopupMenu(table, this, asDatabaseTableObject()));
        setTableProperties();
    }

    void insert_record(List<JComponent> components, List<Integer> types, List<ResultSetColumnHeader> rschs, BaseDialog dialog) {
        String query = "INSERT INTO " + databaseObject.getNameForQuery();
        String columns = "(";
        String values = " VALUES (";
        for (int i = 0; i < components.size(); i++) {
            String value = "";
            String component_value;
            JComponent component = components.get(i);
            int sqlType;
            ResultSetColumnHeader rsch = rschs.get(i);
            columns += rsch.getName();
            if (i != components.size() - 1)
                columns += " , ";
            sqlType = rsch.getDataType();
            int type = types.get(i);
            boolean str = false;
            switch (type) {
                case 2017:
                    component_value = String.valueOf(((JComboBox) component).getSelectedItem());
                    break;
                case Types.DATE:
                    component_value = ((DatePicker) component).getDateStringOrEmptyString();
                    break;
                case Types.TIMESTAMP:
                    component_value = ((EQDateTimePicker) component).getStringValue();
                    break;
                case Types.TIME:
                    component_value = ((EQTimePicker) component).getStringValue();//((DateTimePicker) component).timePicker.getTimeStringOrEmptyString();
                    break;
                case Types.BOOLEAN:
                    component_value = ((RDBCheckBox) component).getStringValue();
                    break;
                default:
                    component_value = ((JTextField) component).getText();
                    break;
            }
            switch (sqlType) {

                case Types.LONGVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.CHAR:
                case Types.NCHAR:
                case Types.VARCHAR:
                case Types.NVARCHAR:
                case Types.CLOB:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    value = "'";
                    str = true;
                    break;
                default:
                    break;
            }
            if (MiscUtils.isNull(component_value))
                value = "NULL";
            else {
                value += component_value;
            }

            if (str && value != "NULL")
                value += "'";
            values = values + " " + value;
            if (i < components.size() - 1)
                values += ",";

        }
        columns += ")";
        values += ")";
        query = query + columns + " " + values;
        ExecuteQueryDialog eqd = new ExecuteQueryDialog("Insert record", query, databaseObject.getHost().getDatabaseConnection(), true);
        eqd.display();
        if (eqd.getCommit()) {
            dialog.finished();
            loadDataForTable(databaseObject);
        }
    }

    void add_record(ActionEvent actionEvent) {
        int cols = tableModel.getColumnCount();
        List<RecordDataItem> row = new ArrayList<>();
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbc.gridx = 1;
        gbcLabel.gridheight = 1;
        gbc.gridheight = 1;
        gbcLabel.gridwidth = 1;
        gbc.gridwidth = 1;
        gbcLabel.weightx = 0;
        gbc.weightx = 1.0;
        gbcLabel.weighty = 0;
        gbc.weighty = 0;
        gbcLabel.gridy = -1;
        gbc.gridy = -1;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbc.anchor = GridBagConstraints.WEST;
        gbcLabel.ipadx = 0;
        gbc.ipadx = 0;
        gbcLabel.ipady = 0;
        gbc.ipady = 0;
        gbcLabel.insets = new Insets(5, 5, 5, 5);
        gbc.insets = new Insets(5, 5, 5, 5);
        List<Integer> fgns = new ArrayList<>();
        List<Vector> f_items = new ArrayList<>();
        if (foreigns != null)
            if (foreigns.size() > 0)
                for (org.executequery.databaseobjects.impl.ColumnConstraint key : foreigns) {
                    f_items.add(itemsForeign(key));
                    fgns.add(tableModel.getColumnIndex(key.getColumnName()));
                }
        List<JComponent> components = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        List<ResultSetColumnHeader> rschs = new ArrayList<>();
        for (int i = 0; i < cols; i++) {
            ResultSetColumnHeader rsch = tableModel.getColumnHeaders().get(i);
            if (!databaseObject.getColumns().get(i).isGenerated()) {
                rschs.add(rsch);
                int type = rsch.getDataType();
                String typeName = rsch.getDataTypeName();
                String name = rsch.getName();
                JComponent field;
                JLabel label = new JLabel(name);
                gbcLabel.gridy++;
                gbc.gridy++;
                panel.add(label, gbcLabel);
                if (fgns.contains(i)) {
                    field = new JComboBox(new DefaultComboBoxModel(f_items.get(fgns.indexOf(i))));
                    types.add(2017);
                } else {
                    switch (type) {
                        case Types.DATE:
                            field = new DatePicker();
                            break;
                        case Types.TIMESTAMP:
                            field = new EQDateTimePicker();
                            break;
                        case Types.TIME:
                            field = new EQTimePicker();
                            break;
                        case Types.BOOLEAN:
                            field = new RDBCheckBox();
                            break;
                        default:
                            field = new JTextField(14);
                            break;
                    }
                    types.add(rsch.getDataType());
                }
                panel.add(field, gbc);
                components.add(field);
            }
        }

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(panel);
        JPanel mainPane = new JPanel(new GridBagLayout());
        mainPane.add(scroll, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        BaseDialog dialog = new BaseDialog("Adding record", true, mainPane);
        gbcLabel.gridy++;
        gbc.gridy++;
        gbcLabel.weightx = 0;
        gbcLabel.fill = GridBagConstraints.HORIZONTAL;
        JButton b_cancel = new DefaultButton("Cancel");
        b_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.finished();
            }
        });
        JButton b_ok = new DefaultButton("Ok");
        b_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                insert_record(components, types, rschs, dialog);
            }
        });
        panel.add(b_cancel, gbcLabel);
        panel.add(b_ok, gbc);
        dialog.display();

        //tableModel.AddRow(row);
    }

    void delete_record() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String query = "DELETE FROM " + databaseObject.getNameForQuery() + " WHERE ";
            String order = "";
            boolean first = true;
            for (int i = 0; i < tableModel.getColumnHeaders().size(); i++) {
                if (!databaseObject.getColumns().get(i).isGenerated()) {
                    String value = "";
                    ResultSetColumnHeader rsch = tableModel.getColumnHeaders().get(i);
                    int sqlType = rsch.getDataType();
                    boolean str = false;
                    switch (sqlType) {

                        case Types.LONGVARCHAR:
                        case Types.LONGNVARCHAR:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.CLOB:
                        case Types.DATE:
                        case Types.TIME:
                        case Types.TIMESTAMP:
                            value = "'";
                            str = true;
                            break;
                        default:
                            break;
                    }
                    String temp = String.valueOf(table.getValueAt(row, i));
                    if (temp == null) {
                        value = "NULL";
                    } else
                        value += temp;
                    if (str && value != "NULL")
                        value += "'";
                    if (first) {
                        first = false;
                        order = rsch.getName();
                    } else query += " AND";
                    //if(value=="'null'")
                    if (value == "NULL")
                        query = query + " (" + rsch.getName() + " IS " + value + " )";
                    else
                        query = query + " (" + rsch.getName() + " = " + value + " )";


                }
            }
            query += "\nORDER BY " + order + "\n";
            query += "ROWS 1";
            ExecuteQueryDialog eqd = new ExecuteQueryDialog("Delete record", query, databaseObject.getHost().getDatabaseConnection(), true);
            eqd.display();
            if (eqd.getCommit()) {
                loadDataForTable(databaseObject);
            }
        }
    }

    private void createButtonsEditingPanel() {
        buttonsEditingPanel = new JPanel(new GridBagLayout());
        PanelToolBar bar = new PanelToolBar();
        RolloverButton addRolloverButton = new RolloverButton();
        addRolloverButton.setIcon(GUIUtilities.loadIcon("add_16.png"));
        addRolloverButton.setToolTipText(bundleString("InsertRecord"));
        addRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean useForm = SystemProperties.getBooleanProperty(
                        Constants.USER_PROPERTIES_KEY, "results.table.use.form.adding.deleting");
                if (useForm)
                    add_record(actionEvent);
                else tableModel.AddRow();
            }
        });
        bar.add(addRolloverButton);
        RolloverButton deleteRolloverButton = new RolloverButton();
        deleteRolloverButton.setIcon(GUIUtilities.loadIcon("delete_16.png"));
        deleteRolloverButton.setToolTipText(bundleString("DeleteRecord"));
        deleteRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean useForm = SystemProperties.getBooleanProperty(
                        Constants.USER_PROPERTIES_KEY, "results.table.use.form.adding.deleting");
                if (useForm)
                    delete_record();
                else {
                    int[] rows = table.getSelectedRows();
                    for (int row : rows) {
                        if (row >= 0)
                            tableModel.deleteRow(((TableSorter) table.getModel()).modelIndex(row));
                    }
                }
            }
        });
        bar.add(deleteRolloverButton);
        RolloverButton commitRolloverButton = new RolloverButton();
        commitRolloverButton.setIcon(GUIUtilities.loadIcon("Commit16.png"));
        commitRolloverButton.setToolTipText(bundleString("Commit"));
        commitRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {


                try {
                    stopEditing();
                    DatabaseObjectChangeProvider docp = new DatabaseObjectChangeProvider(asDatabaseTableObject());
                    if (docp.applyDataChanges())
                        loadDataForTable(databaseObject);

                } catch (DataSourceException e) {
                    GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
                }
            }
        });
        bar.add(commitRolloverButton);
        RolloverButton rollbackRolloverButton = new RolloverButton();
        rollbackRolloverButton.setIcon(GUIUtilities.loadIcon("Rollback16.png"));
        rollbackRolloverButton.setToolTipText(bundleString("Rollback"));
        rollbackRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                asDatabaseTableObject().clearDataChanges();
                loadDataForTable(databaseObject);
            }
        });
        bar.add(rollbackRolloverButton);
        RolloverButton fetchAllRolloverButton = new RolloverButton();
        fetchAllRolloverButton.setText(bundleString("FetchAll"));
        fetchAllRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addInProgressPanel();
                if (timer != null) {

                    timer.cancel();
                }

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        tableModel.setFetchAll(true);
                        if (worker != null) {
                            cancel();
                            worker.interrupt();
                        }
                        worker = new SwingWorker() {

                            public Object construct() {
                                try {
                                    executing = true;
                                    tableModel.fetchMoreData();
                                    removeAll();
                                    add(buttonsEditingPanel, canEditTableNoteConstraints);
                                    add(scroller, scrollerConstraints);
                                    if (displayRowCount) {
                                        add(rowCountPanel, rowCountPanelConstraints);
                                        rowCountField.setText(String.valueOf(tableModel.getRowCount()));
                                    }
                                    setTableProperties();
                                    validate();
                                    repaint();
                                    return "done";
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
                }, 600);

            }
        });
        bar.add(fetchAllRolloverButton);
        GridBagConstraints gbc3 = new GridBagConstraints(4, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        buttonsEditingPanel.add(bar, gbc3);
    }

    public void stopEditing() {
        table.stopEditing();
    }

    private void initRowCountPanel() {

        rowCountField = new DisabledField();
        rowCountPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        rowCountPanel.add(new JLabel(Bundles.getCommon("fetched")), gbc);
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

    /**
     * Cancels the currently executing statement.
     */
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

    /**
     * Sets default table display properties.
     */
    public void setTableProperties() {

        if (table == null) {

            return;
        }

        table.applyUserPreferences();
        table.setCellSelectionEnabled(false);

        tableModel.setMaxRecords(SystemProperties.getIntProperty("user", "browser.max.records"));
    }

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

        if (isDatabaseTableObject()) {

            int row = e.getFirstRow();
            if (e.getType() == TableModelEvent.DELETE) {
                List<RecordDataItem> rowDataForRow = ((ResultSetTableModel) e.getSource()).getDeletedRow();
                asDatabaseTableObject().removeTableDataChange(rowDataForRow);
            } else if (row >= 0) {

                List<RecordDataItem> rowDataForRow = tableModel.getRowDataForRow(row);
                for (RecordDataItem recordDataItem : rowDataForRow) {

                    if (recordDataItem.isDeleted()) {
                        Log.debug("Deleting detected in column [ " + recordDataItem.getName() + " ] - value [ " + recordDataItem.getValue() + " ]");

                        asDatabaseTableObject().addTableDataChange(new TableDataChange(rowDataForRow));
                        return;
                    }

                    if (recordDataItem.isNew()) {

                        Log.debug("Adding detected in column [ " + recordDataItem.getName() + " ] - value [ " + recordDataItem.getValue() + " ]");

                        asDatabaseTableObject().addTableDataChange(new TableDataChange(rowDataForRow));
                        return;
                    }

                    if (recordDataItem.isChanged()) {

                        Log.debug("Change detected in column [ " + recordDataItem.getName() + " ] - value [ " + recordDataItem.getValue() + " ]");

                        asDatabaseTableObject().addTableDataChange(new TableDataChange(rowDataForRow));
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

    private boolean isDatabaseTableObject() {
        return this.databaseObject instanceof DatabaseTableObject;
    }

    private DatabaseTableObject asDatabaseTableObject() {
        if (isDatabaseTableObject()) {

            return (DatabaseTableObject) this.databaseObject;
        }
        return null;
    }

    public boolean hasChanges() {

        if (isDatabaseTable()) {

            return asDatabaseTableObject().hasTableDataChanges();
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

    public void closeResultSet() {
        try {
            if (tableModel != null)
                tableModel.closeResultSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String bundleString(String key) {
        return Bundles.get(TableDataTab.class, key);
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






