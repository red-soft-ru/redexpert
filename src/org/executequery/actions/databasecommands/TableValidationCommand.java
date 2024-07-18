package org.executequery.actions.databasecommands;

import biz.redsoft.IFBMaintenanceManager;
import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.TableValidationPanel;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.util.DynamicLibraryLoader;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Alexey Kozlov
 */
public class TableValidationCommand extends OpenFrameCommand
        implements BaseCommand {

    public void validateTableAndShowResult(DatabaseConnection dc, String preparedParameter) {
        showPanel(new TableValidationPanel(dc, preparedParameter));
    }

    public OutputStream onlineTableValidation(
            DatabaseConnection dc, String tableIncl, String indexIncl, String tableExcl, String indexExcl) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IFBMaintenanceManager maintenanceManager = getMaintenanceManager(dc);

        if (maintenanceManager != null) {
            try {

                maintenanceManager.setLogger(outputStream);
                maintenanceManager.validateTable(tableIncl, indexIncl, tableExcl, indexExcl);

            } catch (SQLException e) {
                e.printStackTrace(System.out);
                GUIUtilities.displayExceptionErrorDialog("Unable run database validation", e, this.getClass());
            }
        }

        return outputStream;
    }

    private IFBMaintenanceManager getMaintenanceManager(DatabaseConnection dc) {

        try {

            Driver driver = null;
            Map<String, Driver> drivers = DefaultDriverLoader.getLoadedDrivers();
            for (String driverName : drivers.keySet()) {
                if (driverName.startsWith(String.valueOf(dc.getDriverId()))) {
                    driver = drivers.get(driverName);
                    break;
                }
            }
            if (driver == null)
                driver = DefaultDriverLoader.getDefaultDriver();

            IFBMaintenanceManager maintenanceManager = (IFBMaintenanceManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(), driver, "FBMaintenanceManagerImpl");

            maintenanceManager.setUser(dc.getUserName());
            maintenanceManager.setPassword(dc.getUnencryptedPassword());
            maintenanceManager.setDatabase(dc.getSourceName());
            maintenanceManager.setHost(dc.getHost());
            maintenanceManager.setPort(dc.getPortInt());

            return maintenanceManager;

        } catch (ClassNotFoundException | SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Unable to init IFBMaintenanceManager instance", e, this.getClass());
        }

        return null;
    }

    private void showPanel(TableValidationPanel tableValidationPanel) {

        String title = TableValidationPanel.TITLE;
        if (isCentralPaneOpen(title))
            return;

        try {
            GUIUtilities.showWaitCursor();

            GUIUtilities.addCentralPane(
                    title,
                    TableValidationPanel.FRAME_ICON,
                    tableValidationPanel,
                    null, true
            );

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    @Override
    public void execute(ActionEvent e) {
        if (isConnected())
            showPanel(new TableValidationPanel());
    }

}
