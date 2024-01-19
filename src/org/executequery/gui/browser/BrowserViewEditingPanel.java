package org.executequery.gui.browser;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.databaseobjects.impl.DefaultDatabaseView;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.databaseobjects.CreateTriggerPanel;
import org.executequery.gui.databaseobjects.TableTriggersTableModel;
import org.executequery.localization.Bundles;
import org.executequery.toolbars.AbstractToolBarForTable;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BrowserViewEditingPanel extends ObjectDefinitionPanel {

    private JTable triggersTable;
    private TableTriggersTableModel triggersTableModel;
    private JPanel buttonsEditingTriggersPanel;
    private boolean triggersLoaded;

    public BrowserViewEditingPanel(BrowserController controller) {
        super(controller);
        init();
    }

    private void init() {

        triggersLoaded = false;

        buttonsEditingTriggersPanel = new AbstractToolBarForTable(
                "Create Trigger",
                "Delete Trigger",
                "Refresh"
        ) {
            @Override
            public void insert(ActionEvent e) {
                insertTrigger();
            }

            @Override
            public void delete(ActionEvent e) {
                deleteTrigger();
            }

            @Override
            public void refresh(ActionEvent e) {
                loadTriggers();
            }
        };

        triggersTableModel = new TableTriggersTableModel();
        triggersTable = new DefaultTable();
        triggersTable.setModel(new TableSorter(triggersTableModel, triggersTable.getTableHeader()));
        triggersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1)
                    triggerTableClicked();
            }
        });

        addTab(getCreateTriggersPanel(), 1);
    }

    private JPanel getCreateTriggersPanel() {

        JPanel triggersPanel = new JPanel(new GridBagLayout());
        triggersPanel.setName(Bundles.getCommon("triggers"));
        triggersPanel.setBorder(BorderFactory.createTitledBorder(bundleString("table-triggers")));

        GridBagHelper gridBagHelper = new GridBagHelper().setDefaults(GridBagHelper.DEFAULT_CONSTRAINTS).defaults();
        triggersPanel.add(buttonsEditingTriggersPanel, gridBagHelper.get());
        triggersPanel.add(new JScrollPane(triggersTable), gridBagHelper.anchorSouthEast().fillBoth().spanX().spanY().nextRow().get());

        return triggersPanel;
    }

    private void triggerTableClicked() {

        if (triggersTable.getSelectedRow() >= 0) {

            BaseDialog dialog = new BaseDialog(CreateTriggerPanel.EDIT_TITLE, true);
            int row = ((TableSorter) triggersTable.getModel()).modelIndex(triggersTable.getSelectedRow());
            DefaultDatabaseTrigger trigger = ((TableTriggersTableModel) ((TableSorter) triggersTable.getModel()).getTableModel()).getTriggers().get(row);

            CreateTriggerPanel panel = new CreateTriggerPanel(
                    currentObjectView.getHost().getDatabaseConnection(),
                    dialog,
                    trigger,
                    DefaultDatabaseTrigger.TABLE_TRIGGER
            );

            dialog.addDisplayComponent(panel);
            dialog.display();

            currentObjectView.reset();
            setValues(currentObjectView);
            loadTriggers();
        }
    }

    public void insertTrigger() {

        BaseDialog dialog = new BaseDialog(CreateTriggerPanel.CREATE_TITLE, true);
        JPanel panelForDialog = new CreateTriggerPanel(
                currentObjectView.getHost().getDatabaseConnection(),
                dialog,
                DefaultDatabaseTrigger.TABLE_TRIGGER,
                currentObjectView.getName()
        );

        dialog.addDisplayComponent(panelForDialog);
        dialog.display();

        ((DefaultDatabaseHost) currentObjectView.getHost()).reloadMetaTag(NamedObject.TRIGGER);
        currentObjectView.reset();
        setValues(currentObjectView);
        loadTriggers();
    }

    public void deleteTrigger() {

        if (triggersTable.getSelectedRow() >= 0) {

            int row = ((TableSorter) triggersTable.getModel()).modelIndex(triggersTable.getSelectedRow());
            DefaultDatabaseTrigger trigger = ((TableTriggersTableModel) ((TableSorter) triggersTable.getModel()).getTableModel()).getTriggers().get(row);
            String query = "DROP TRIGGER " + MiscUtils.getFormattedObject(trigger.getName(), currentObjectView.getHost().getDatabaseConnection());

            ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog(
                    "Dropping object",
                    query,
                    currentObjectView.getHost().getDatabaseConnection(),
                    true
            );
            executeQueryDialog.display();

            currentObjectView.reset();
            setValues(currentObjectView);
            loadTriggers();
        }
    }

    private synchronized void loadTriggers() {

        if (currentObjectView instanceof DefaultDatabaseView)
            ((DefaultDatabaseView) currentObjectView).clearTriggers();

        try {
            triggersTableModel.setTriggersData(((DefaultDatabaseView) currentObjectView).getTriggers());
            triggersLoaded = true;

        } catch (DataSourceException e) {
            controller.handleException(e);
            triggersTableModel.setTriggersData(null);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabPane.getSelectedIndex();

        if (selectedIndex == 1) {
            if (!triggersLoaded)
                loadTriggers();

        } else if (selectedIndex == 3) {
            if (!dataLoaded)
                loadData();

        } else if (selectedIndex == 5) {
            if (!metaDataLoaded)
                loadMetaData();

        } else if (tableDataPanel.isExecuting())
            tableDataPanel.cancelStatement();
    }

}
