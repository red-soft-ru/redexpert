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
import org.executequery.databaseobjects.TablePrivilege;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.databaseobjects.CreateViewPanel;
import org.executequery.gui.databaseobjects.DefaultDatabaseObjectTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.print.TablePrinter;
import org.executequery.sql.SQLFormatter;
import org.underworldlabs.Constants;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ObjectDefinitionPanel extends AbstractFormObjectViewPanel
        implements ChangeListener,
        KeywordListener {

    public static final String NAME = "ObjectDefinitionPanel";

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

    private TablePrivilegeTab tablePrivilegePanel;

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

    private boolean hasResults;

    /**
     * header icons
     */
    private ImageIcon[] icons;

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
    private BrowserController controller;

    private boolean metaDataLoaded;

    private SimpleSqlTextPanel sqlTextPanel;

    private JButton formatSqlButton;

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

        noResultsLabel = new JLabel("No information for this object is available.",
                JLabel.CENTER);

        JPanel descPanel = new JPanel(new GridBagLayout());

        tableNameField = new DisabledField();
        //schemaNameField = new DisabledField();

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(10, 5, 5, 5);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        descPanel.add(new JLabel(Bundles.getCommon("name")), gbc);
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        descPanel.add(tableNameField, gbc);
        gbc.insets.top = 0;
        gbc.gridy++;
        //descPanel.add(schemaNameField, gbc);
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.gridx = 0;
        gbc.weightx = 0;
        //descPanel.add(new JLabel("Schema:"), gbc);

        // configure the table column descriptions panel
        descBottomPanel = new JPanel(new BorderLayout());
        descBottomPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("columns")));

        tableDataPanel = new TableDataTab(true);
        tablePrivilegePanel = new TablePrivilegeTab();

        metaDataPanel = new DatabaseObjectMetaDataPanel();

        sqlTextPanel = new SimpleSqlTextPanel();
        formatSqlButton = WidgetFactory.createButton(Bundles.getCommon("FormatSQL"));
        formatSqlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatSql();
            }
        });

        JPanel sqlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints sqlGbc = new GridBagConstraints();
        Insets sqlIns = new Insets(10, 5, 5, 5);
        sqlGbc.insets = sqlIns;
        sqlGbc.anchor = GridBagConstraints.NORTHWEST;
        sqlGbc.fill = GridBagConstraints.NONE;
        sqlGbc.gridx = 0;
        sqlGbc.gridy = 0;
        sqlPanel.add(formatSqlButton, sqlGbc);
        sqlGbc.gridy++;
        sqlGbc.fill = GridBagConstraints.BOTH;
        sqlGbc.weighty = 1;
        sqlGbc.weightx = 1;
        sqlPanel.add(sqlTextPanel, sqlGbc);


        tabPane = new JTabbedPane();
        tabPane.add(Bundles.getCommon("description"), descBottomPanel);
        tabPane.add(Bundles.getCommon("privileges"), tablePrivilegePanel);
        tabPane.add(Bundles.getCommon("data"), tableDataPanel);
        tabPane.add(Bundles.getCommon("SQL"), sqlPanel);
        tabPane.add(Bundles.getCommon("metadata"), metaDataPanel);

        // add the tab pane
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.bottom = 5;
        gbc.insets.top = 5;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        descPanel.add(tabPane, gbc);

        tabPane.addChangeListener(this);
        //tableDescPanel = new SimpleTableDescriptionPanel();

        icons = new ImageIcon[BrowserConstants.META_TYPE_ICONS.length];

        for (int i = 0; i < BrowserConstants.META_TYPE_ICONS.length; i++) {
            icons[i] = GUIUtilities.loadIcon(BrowserConstants.META_TYPE_ICONS[i]);
        }

        setHeader("Database Object", icons[0]);
        setContentPanel(descPanel);
        //cache = new HashMap();
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

            case 1:
                return new TablePrinter(tablePrivilegePanel.getTable(),
                        "Access rights for table: " +
                                currentObjectView.getName());

            case 2:
                return new TablePrinter(tableDataPanel.getTable(),
                        "Table Data: " + currentObjectView.getName());

            case 3:
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

        } else if (selectedIndex == 1) {

            if (!privilegesLoaded) {

                loadPrivileges();
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

    private void loadPrivileges() {

        try {

            tablePrivilegePanel.setValues(currentObjectView.getPrivileges());

        } catch (DataSourceException e) {

            controller.handleException(e);
            tablePrivilegePanel.setValues(new TablePrivilege[0]);

        } finally {

            privilegesLoaded = true;
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

        sqlTextPanel.setSQLText(Constants.EMPTY);

        // header values
        if (object.getType() == NamedObject.VIEW) {
            setHeaderText(bundleString("DatabaseView"));
        } else
            setHeaderText("Database " + MiscUtils.firstLetterToUpper(object.getMetaDataKey()));
        tableNameField.setText(object.getName());
        //schemaNameField.setText(object.getSchemaName());

        int type = object.getType();
        if (type < icons.length) {

            setHeaderIcon(icons[type]);

        } else {

            setHeaderIcon(icons[BrowserConstants.TABLE_NODE]);
        }

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

        } catch (DataSourceException e) {

            controller.handleException(e);
            descBottomPanel.add(noResultsLabel, BorderLayout.CENTER);
        }

        stateChanged(null);
        repaint();
    }

    private void formatSql() {
        if (StringUtils.isNotEmpty(sqlTextPanel.getSQLText())) {
            String sqlText = sqlTextPanel.getSQLText();
            sqlTextPanel.setSQLText(new SQLFormatter(sqlText).format());
        }
    }

    public void refresh() {
        super.refresh();
        privilegesLoaded = false;
        dataLoaded = false;
    }

    public void cleanup() {
    }

    public JTable getTable() {
        if (!hasResults) {
            return null;
        }

        int tabIndex = tabPane.getSelectedIndex();
        switch (tabIndex) {
            case 0:
                return tableDescriptionTable;
            case 1:
                return tablePrivilegePanel.getTable();
            default:
                return null;
        }

    }

}






