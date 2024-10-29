package org.executequery.actions.databasecommands;

import biz.redsoft.IFBMaintenanceManager;
import org.executequery.GUIUtilities;
import org.executequery.actions.OpenFrameCommand;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.TableValidationPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.actions.BaseCommand;
import org.underworldlabs.util.DynamicLibraryLoader;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Map;

/// @author Alexey Kozlov
public class TableValidationCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        if (isConnected())
            showPanel(new TableValidationPanel());
    }

    public void validateTableAndShowResult(DatabaseConnection dc, String preparedParameter) {
        showPanel(new TableValidationPanel(dc, preparedParameter));
    }

    // --- validating ---

    public OutputStream onlineTableValidation(
            DatabaseConnection dc, String tableIncl, String indexIncl, String tableExcl, String indexExcl) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IFBMaintenanceManager maintenanceManager = getMaintenanceManager(dc);

        if (maintenanceManager != null) {
            try {

                maintenanceManager.setLogger(outputStream);
                maintenanceManager.validateTable(tableIncl, indexIncl, tableExcl, indexExcl);

            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
                GUIUtilities.displayExceptionErrorDialog(bundleString("validationError"), e, this.getClass());
            }
        }

        return outputStream;
    }

    private IFBMaintenanceManager getMaintenanceManager(DatabaseConnection dc) {

        try {
            Driver driver = getDriver(dc);
            Object library = DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(),
                    driver,
                    "FBMaintenanceManagerImpl"
            );

            IFBMaintenanceManager maintenanceManager = (IFBMaintenanceManager) library;
            maintenanceManager.setUser(dc.getUserName());
            maintenanceManager.setPassword(dc.getUnencryptedPassword());
            maintenanceManager.setDatabase(dc.getSourceName());
            maintenanceManager.setHost(dc.getHost());
            maintenanceManager.setPort(dc.getPortInt());

            return maintenanceManager;

        } catch (ClassNotFoundException | SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("initError"), e, this.getClass());
        }

        return null;
    }

    private static Driver getDriver(DatabaseConnection dc) throws SQLException {

        Map<String, Driver> drivers = DefaultDriverLoader.getLoadedDrivers();
        for (Map.Entry<String, Driver> entry : drivers.entrySet()) {

            String driverName = entry.getKey();
            if (driverName.startsWith(String.valueOf(dc.getDriverId())))
                return entry.getValue();
        }

        return DefaultDriverLoader.getDefaultDriver();
    }

    // ---

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

    private static String bundleString(String key) {
        return Bundles.get(TableValidationCommand.class, key);
    }

}
