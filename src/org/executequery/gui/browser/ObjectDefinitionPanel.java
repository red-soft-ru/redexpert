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
import org.executequery.log.Log;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // --- GUI objects ---

    private JPanel sqlPanel;
    private JPanel descBottomPanel;
    private JPanel tableDescriptionPanel;
    private SimpleSqlTextPanel sqlTextPanel;
    private DependenciesPanel dependenciesPanel;
    private DatabaseObjectMetaDataPanel metaDataPanel;

    protected JTabbedPane tabPane;
    protected TableDataTab tableDataPanel;

    private JLabel noResultsLabel;
    private JButton formatSqlButton;
    private DisabledField tableNameField;

    // ---

    protected final BrowserController controller;

    protected DatabaseObject currentObjectView;
    private DefaultDatabaseObjectTable tableDescriptionTable;

    private boolean hasResults;
    protected boolean dataLoaded;
    protected boolean metaDataLoaded;

    public ObjectDefinitionPanel(BrowserController controller) {
        super();
        this.controller = controller;

        init();
        arrange();
    }

    private void init() {

        // --- panels ---

        descBottomPanel = new JPanel(new BorderLayout());
        descBottomPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("columns")));

        sqlPanel = new JPanel(new GridBagLayout());
        sqlTextPanel = new SimpleSqlTextPanel();
        dependenciesPanel = new DependenciesPanel();
        metaDataPanel = new DatabaseObjectMetaDataPanel();
        tableDataPanel = new TableDataTab(true);

        // --- others ---

        formatSqlButton = WidgetFactory.createButton("formatSQLButton", Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(e -> formatSql());

        noResultsLabel = new JLabel("No information for this object is available.", JLabel.CENTER);
        tableNameField = new DisabledField();

        // --- tabbed pane ---

        tabPane = new JTabbedPane();
        tabPane.add(Bundles.getCommon("columns"), descBottomPanel);
        addPrivilegesTab(tabPane, null);
        tabPane.add(Bundles.getCommon("data"), tableDataPanel);
        tabPane.add(Bundles.getCommon("SQL"), sqlPanel);
        tabPane.add(Bundles.getCommon("metadata"), metaDataPanel);
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);
        tabPane.add(Bundles.getCommon("comment-field-label"), null);
        tabPane.addChangeListener(this);
    }

    private void arrange() {

        GridBagHelper gridBagHelper;

        // --- sql panel ---

        gridBagHelper = new GridBagHelper().anchorNorthWest().fillBoth().setInsets(5, 5, 5, 5);
        sqlPanel.add(formatSqlButton, gridBagHelper.setLabelDefault().get());
        sqlPanel.add(sqlTextPanel, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        // --- desc panel ---

        JPanel descPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper().anchorNorthWest().fillBoth().setInsets(5, 5, 5, 5);
        descPanel.add(new JLabel(Bundles.getCommon("name")), gridBagHelper.fillHorizontally().get());
        descPanel.add(tableNameField, gridBagHelper.nextCol().spanX().get());
        descPanel.add(tabPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().setMaxWeightY().get());

        // --- base ---

        setHeader("Database Object", GUIUtilities.loadIcon(BrowserConstants.DATABASE_OBJECT_IMAGE));
        setContentPanel(descPanel);
    }

    /**
     * Create the table description panel if not yet initialised.
     */
    private void createTablePanel() {

        if (tableDescriptionPanel == null) {

            tableDescriptionTable = new DefaultDatabaseObjectTable();
            tableDescriptionTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {

                    if (e.getClickCount() > 1) {
                        if (currentObjectView instanceof DefaultDatabaseView) {

                            DatabaseConnection databaseConnection = controller.getDatabaseConnection();
                            if (databaseConnection == null)
                                databaseConnection = currentObjectView.getHost().getDatabaseConnection();

                            BaseDialog dialog = new BaseDialog("Edit View", true);
                            CreateViewPanel panel = new CreateViewPanel(databaseConnection, dialog, (DefaultDatabaseView) currentObjectView);
                            dialog.addDisplayComponent(panel);
                            dialog.display();
                        }
                    }
                }
            });

            tableDescriptionPanel = new JPanel(new GridBagLayout());
            tableDescriptionPanel.add(
                    new JScrollPane(tableDescriptionTable),
                    new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST,
                            GridBagConstraints.BOTH,
                            new Insets(2, 2, 2, 2), 0, 0)
            );
        }
    }

    public void setValues(DatabaseObject object) {

        // --- reset the current object values ---

        currentObjectView = object;
        dataLoaded = false;
        metaDataLoaded = false;

        sqlTextPanel.getTextPane().setDatabaseConnection(object.getHost().getDatabaseConnection());
        sqlTextPanel.setSQLText(Constants.EMPTY);

        SimpleCommentPanel simpleCommentPanel = new SimpleCommentPanel(currentObjectView);
        simpleCommentPanel.addActionForCommentUpdateButton(e -> sqlTextPanel.setSQLText(currentObjectView.getCreateSQLText()));
        tabPane.setComponentAt(tabPane.getTabCount() - 1, simpleCommentPanel.getCommentPanel());

        // --- header values ---

        if (object.getType() == NamedObject.VIEW) {
            setHeaderText(bundleString("DatabaseView"));
            dependenciesPanel.setDatabaseObject(object);

        } else {
            setHeaderText("Database " + MiscUtils.firstLetterToUpper(object.getMetaDataKey()));
            tabPane.remove(dependenciesPanel);
        }

        // ---

        tableNameField.setText(object.getName());
        setHeaderIcon(GUIUtilities.loadIcon(BrowserConstants.TABLES_IMAGE));
        descBottomPanel.removeAll();

        try {

            List<DatabaseColumn> columns = object.getColumns();
            if (columns == null || columns.isEmpty()) {
                descBottomPanel.add(noResultsLabel, BorderLayout.CENTER);

            } else {
                hasResults = true;
                createTablePanel();
                tableDescriptionTable.setColumnData(columns);
                descBottomPanel.add(tableDescriptionPanel, BorderLayout.CENTER);
            }

            setSQLText();

        } catch (DataSourceException e) {
            controller.handleException(e);
            descBottomPanel.add(noResultsLabel, BorderLayout.CENTER);
        }

        stateChanged(null);
        repaint();
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
            Log.error(e.getMessage(), e);
        }

        return true;
    }

    protected void loadMetaData() {
        try {
            metaDataPanel.setData(currentObjectView.getMetaData());

        } catch (DataSourceException e) {
            controller.handleException(e);
            metaDataPanel.setData(null);

        } finally {
            metaDataLoaded = true;
        }
    }

    protected void loadData() {
        try {
            tableDataPanel.loadDataForTable(currentObjectView);
        } finally {
            dataLoaded = true;
        }
    }

    private void formatSql() {
        if (StringUtils.isNotEmpty(sqlTextPanel.getSQLText()))
            sqlTextPanel.setSQLText(new TokenizingFormatter().format(sqlTextPanel.getSQLText()));
    }

    public JTable getTable() {
        return (hasResults && tabPane.getSelectedIndex() == 0) ? tableDescriptionTable : null;
    }

    public DatabaseConnection getSelectedConnection() {
        return currentObjectView.getHost().getDatabaseConnection();
    }

    protected void addTab(JPanel panel, int index) {
        tabPane.add(panel, index);
    }

    protected void setSQLText() {
        sqlTextPanel.setSQLText(currentObjectView.getCreateSQLText());
        sqlTextPanel.getTextPane().setEditable(false);
    }

    @Override
    public String getLayoutName() {
        return NAME;
    }

    @Override
    public Printable getPrintable() {

        switch (tabPane.getSelectedIndex()) {

            case 0:
                return new TablePrinter(tableDescriptionTable, "Table: " + currentObjectView.getName());

            case 2:
                return new TablePrinter(tableDataPanel.getTable(), "Table Data: " + currentObjectView.getName());

            case 4:
                return new TablePrinter(metaDataPanel.getTable(), "Meta Data: " + currentObjectView.getName());

            default:
                return null;
        }
    }

    @Override
    public void keywordsAdded(KeywordEvent e) {
        sqlTextPanel.setSQLKeywords(true);
    }

    @Override
    public void keywordsRemoved(KeywordEvent e) {
        sqlTextPanel.setSQLKeywords(true);
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabPane.getSelectedIndex();
        if (selectedIndex == 2) {
            if (!dataLoaded)
                loadData();

        } else if (selectedIndex == 4) {
            if (!metaDataLoaded)
                loadMetaData();

        } else if (tableDataPanel.isExecuting())
            tableDataPanel.cancelStatement();
    }

    @Override
    public void refresh() {
        super.refresh();
        dataLoaded = false;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        sqlTextPanel.cleanup();
    }

}
