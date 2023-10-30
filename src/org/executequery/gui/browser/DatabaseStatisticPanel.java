package org.executequery.gui.browser;

import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.localization.Bundles;

import javax.swing.*;

public class DatabaseStatisticPanel extends JPanel implements TabView {
    public static final String TITLE = Bundles.get(DatabaseStatisticPanel.class, "title");
    JTabbedPane tabbedPane;
    private JComboBox<DatabaseConnection> databaseBox;

    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }
}
