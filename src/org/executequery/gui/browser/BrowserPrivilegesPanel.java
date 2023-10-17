package org.executequery.gui.browser;

import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.browser.managment.grantmanager.PrivilegesTablePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public class BrowserPrivilegesPanel extends JPanel {

    private JTabbedPane tabbedPane;
    private PrivilegesTablePanel userObjectsPanel;
    private PrivilegesTablePanel objectUsersPanel;

    public BrowserPrivilegesPanel() {
        tabbedPane = new JTabbedPane();
        userObjectsPanel = new PrivilegesTablePanel(PrivilegesTablePanel.USER_OBJECTS, null);
        objectUsersPanel = new PrivilegesTablePanel(PrivilegesTablePanel.OBJECT_USERS, null);
    }

    public void setValues(AbstractDatabaseObject ddo) {
        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        if (ddo instanceof DefaultDatabaseUser
                || ddo instanceof DefaultDatabaseRole
                || ddo instanceof DefaultDatabaseTrigger) {
            add(userObjectsPanel, gbh.fillBoth().spanX().spanY().get());
            //tabbedPane.addTab(bundleString("userObjects"),userObjectsPanel);
            userObjectsPanel.setDatabaseObject(ddo);
        } else if (ddo instanceof DefaultDatabaseView
                || ddo instanceof DefaultDatabaseExecutable) {
            add(tabbedPane, gbh.fillBoth().spanX().spanY().get());
            tabbedPane.addTab(bundleString("userObjects"), userObjectsPanel);
            tabbedPane.addTab(bundleString("objectUsers"), objectUsersPanel);
            userObjectsPanel.setDatabaseObject(ddo);
            objectUsersPanel.setDatabaseObject(ddo);
        } else {
            add(objectUsersPanel, gbh.fillBoth().spanX().spanY().get());
            //tabbedPane.addTab(bundleString("objectUsers"),objectUsersPanel);
            objectUsersPanel.setDatabaseObject(ddo);
        }

    }



    public String bundleString(String key) {
        return Bundles.get(GrantManagerPanel.class, key);
    }

    public String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++) {
            if (key.length > 0)
                key[i] = bundleString(key[i]);
        }
        return key;
    }

    public void cleanup() {
        if (userObjectsPanel != null)
            userObjectsPanel.cleanup();
        userObjectsPanel = null;
        if (objectUsersPanel != null)
            objectUsersPanel.cleanup();
        objectUsersPanel = null;
        if (tabbedPane != null)
            tabbedPane.removeAll();
        tabbedPane = null;
    }
}
