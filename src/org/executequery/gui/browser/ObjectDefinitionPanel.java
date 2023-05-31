/*
 * ObjectDefinitionPanel.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.databaseobjects.impl.TransactionAgnosticResultSet;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.databaseobjects.CreateViewPanel;
import org.executequery.gui.databaseobjects.DefaultDatabaseObjectTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleCommentPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.print.TablePrinter;
import org.executequery.sql.TokenizingFormatter;
import org.underworldlabs.Constants;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ObjectDefinitionPanel extends AbstractFormObjectViewPanel
        implements ChangeListener,
        KeywordListener {

    public static final String NAME = "ObjectDefinitionPanel";

    private DependenciesPanel dependenciesPanel;

    /**
     * The table data display
     */
    private TableDataTab tableDataPanel;

    /**
     * The tabbed description pane
     */
    private JTabbedPane tabPane;

    /**
     * Contains the view name
     */
    private DisabledField tableNameField;

    private DatabaseObjectMetaDataPanel metaDataPanel;

    /**
     * table description base panel
     */
    private JPanel tableDescriptionPanel;

    /**
     * the table description table
     */
    private DefaultDatabaseObjectTable tableDescriptionTable;


    /**
     * the current database object in view
     */
    private DatabaseObject currentObjectView;

    /**
     * panel base
     */
    private JPanel descBottomPanel;

    /**
     * no results label
     */
    private JLabel noResultsLabel;

    private SimpleCommentPanel simpleCommentPanel;
    private boolean hasResults;

    /**
     * whether we have privilege data loaded
     */
    private boolean privilegesLoaded;

    /**
     * whether we have data loaded
     */
    private boolean dataLoaded;

    /**
     * the browser's control object
     */
    private final BrowserController controller;

    private boolean metaDataLoaded;

    private SimpleSqlTextPanel sqlTextPanel;

    public ObjectDefinitionPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() {

        GridBagHelper gridBagHelper = new GridBagHelper().
                anchorNorthWest().fillBoth().setInsets(5,5,5,5);

        dependenciesPanel = new DependenciesPanel();
        noResultsLabel = new JLabel("No information for this object is available.",
                JLabel.CENTER);

        tableNameField = new DisabledField();
        //schemaNameField = new DisabledField();

        // configure the table column descriptions panel
        descBottomPanel = new JPanel(new BorderLayout());
        descBottomPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("columns")));

        tableDataPanel = new TableDataTab(true);

        metaDataPanel = new DatabaseObjectMetaDataPanel();

        sqlTextPanel = new SimpleSqlTextPanel();
        JButton formatSqlButton = WidgetFactory.createButton(Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(e -> formatSql());

        //sql panel
        JPanel sqlPanel = new JPanel(new GridBagLayout());

        sqlPanel.add(formatSqlButton, gridBagHelper.setLabelDefault().get());
        sqlPanel.add(sqlTextPanel, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        //tabbed panel
        tabPane = new JTabbedPane();
        tabPane.add(Bundles.getCommon("columns"), descBottomPanel);
        addPrivilegesTab(tabPane, null);
        tabPane.add(Bundles.getCommon("data"), tableDataPanel);
        tabPane.add(Bundles.getCommon("SQL"), sqlPanel);
        tabPane.add(Bundles.getCommon("metadata"), metaDataPanel);
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);
        tabPane.add(Bundles.getCommon("comment-field-label"), null);

        //components arranging
        JPanel descPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().
                anchorNorthWest().fillBoth().setInsets(5,5,5,5);

        gridBagHelper.addLabelFieldPair(descPanel,
                new JLabel(Bundles.getCommon("name")), tableNameField,
                null, true, true);
//        gridBagHelper.addLabelFieldPair(descPanel,
//                new JLabel("Schema:"), schemaNameField,
//                null, true, true);
        descPanel.add(tabPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().setMaxWeightY().get());

        tabPane.addChangeListener(this);
        //tableDescPanel = new SimpleTableDescriptionPanel();

        setHeader("Database Object", GUIUtilities.loadIcon(BrowserConstants.DATABASE_OBJECT_IMAGE));
        setContentPanel(descPanel);
        //cache = new HashMap();
    }

    public DatabaseConnection getSelectedConnection() {
        return currentObjectView.getHost().getDatabaseConnection();
    }

    public String getLayoutName() {
        return NAME;
    }

    public Printable getPrintable() {

        int tabIndex = tabPane.getSelectedIndex();
        switch (tabIndex) {

            case 0:
                return new TablePrinter(tableDescriptionTable,
                        "Table: " + currentObjectView.getName());


            case 2:
                return new TablePrinter(tableDataPanel.getTable(),
                        "Table Data: " + currentObjectView.getName());

            case 4:
                return new TablePrinter(metaDataPanel.getTable(),
                        "Meta Data: " + currentObjectView.getName());

            default:
                return null;

        }

    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        sqlTextPanel.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        sqlTextPanel.setSQLKeywords(true);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent);
    }

    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabPane.getSelectedIndex();
        if (selectedIndex == 2) {

            if (!dataLoaded) {

                loadData();
            }

        } else if (selectedIndex == 4) {

            if (!metaDataLoaded) {

                loadMetaData();
            }

        } else if (tableDataPanel.isExecuting()) {

            tableDataPanel.cancelStatement();
        }

    }

    private void loadMetaData() {

        try {

            metaDataPanel.setData(currentObjectView.getMetaData());

        } catch (DataSourceException e) {

            controller.handleException(e);
            metaDataPanel.setData(null);

        } finally {

            metaDataLoaded = true;
        }

    }

    private void loadData() {

        try {

            tableDataPanel.loadDataForTable(currentObjectView);

        } finally {

            dataLoaded = true;
        }
    }



    /**
     * Create the table description panel if not yet initialised.
     */
    private void createTablePanel() {
        if (tableDescriptionPanel == null) {
            tableDescriptionTable = new DefaultDatabaseObjectTable();
            tableDescriptionTable.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        if (currentObjectView instanceof DefaultDatabaseView) {
                            BaseDialog dialog = new BaseDialog("Edit View", true);
                            DatabaseConnection databaseConnection = controller.getDatabaseConnection();
                            if (databaseConnection == null) {
                                databaseConnection = currentObjectView.getHost().getDatabaseConnection();
                            }
                            CreateViewPanel panel = new CreateViewPanel(databaseConnection,
                                    dialog, (DefaultDatabaseView) currentObjectView);
                            dialog.addDisplayComponent(panel);
                            dialog.display();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseReleased(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseEntered(MouseEvent mouseEvent) {

                }

                @Override
                public void mouseExited(MouseEvent mouseEvent) {

                }
            });
            tableDescriptionPanel = new JPanel(new GridBagLayout());
            tableDescriptionPanel.add(
                    new JScrollPane(tableDescriptionTable),
                    new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2), 0, 0));
        }
    }

    public void setValues(DatabaseObject object) {

        // reset to the first tab
//        tabPane.setSelectedIndex(0);

        // reset the current object values
        currentObjectView = object;
        privilegesLoaded = false;
        dataLoaded = false;
        metaDataLoaded = false;

        sqlTextPanel.getTextPane().setDatabaseConnection(object.getHost().getDatabaseConnection());
        sqlTextPanel.setSQLText(Constants.EMPTY);

        simpleCommentPanel = new SimpleCommentPanel(currentObjectView);
        simpleCommentPanel.addActionForCommentUpdateButton(e -> {
            sqlTextPanel.setSQLText(currentObjectView.getCreateSQLText());
        });
        tabPane.setComponentAt(6, simpleCommentPanel.getCommentPanel());

        // header values
        if (object.getType() == NamedObject.VIEW) {
            setHeaderText(bundleString("DatabaseView"));
            dependenciesPanel.setDatabaseObject(object);
        } else {
            setHeaderText("Database " + MiscUtils.firstLetterToUpper(object.getMetaDataKey()));
            tabPane.remove(dependenciesPanel);
        }
        tableNameField.setText(object.getName());
        //schemaNameField.setText(object.getSchemaName());

        object.getType();

        setHeaderIcon(GUIUtilities.loadIcon(BrowserConstants.TABLES_IMAGE));


        descBottomPanel.removeAll();

        try {

            // retrieve the description info
            List<DatabaseColumn> columns = object.getColumns();
            if (columns == null || columns.size() == 0) {

                descBottomPanel.add(noResultsLabel, BorderLayout.CENTER);

            } else {

                hasResults = true;
                createTablePanel();
                tableDescriptionTable.setColumnData(columns);
                descBottomPanel.add(tableDescriptionPanel, BorderLayout.CENTER);
            }

            sqlTextPanel.setSQLText(currentObjectView.getCreateSQLText());
            sqlTextPanel.getTextPane().setEditable(false);

        } catch (DataSourceException e) {

            controller.handleException(e);
            descBottomPanel.add(noResultsLabel, BorderLayout.CENTER);
        }

        stateChanged(null);
        repaint();
    }

    private void formatSql() {
        String sqlText = sqlTextPanel.getSQLText();
        if (StringUtils.isNotEmpty(sqlText)) {
            sqlTextPanel.setSQLText(new TokenizingFormatter().format(sqlText));
        }
    }

    public void refresh() {
        super.refresh();
        privilegesLoaded = false;
        dataLoaded = false;
    }

    public void cleanup() {
        sqlTextPanel.cleanup();
    }

    public JTable getTable() {
        if (!hasResults)
            return null;

        return (tabPane.getSelectedIndex() == 0) ? tableDescriptionTable : null;
    }

    public boolean commitResultSet() {
        try {
            if (tableDataPanel.resultSet != null) {
                if (tableDataPanel.resultSet instanceof TransactionAgnosticResultSet) {
                    Connection con = ((TransactionAgnosticResultSet) tableDataPanel.resultSet).getConnection();
                    if (con != null && !con.isClosed()) {
                        con.commit();
                        con.close();
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

}






