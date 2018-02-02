package org.executequery.gui.browser;

/**
 * Created by mikhan808 on 05.06.2017.
 */
public class ThreadOfRole implements Runnable {

    private BrowserRolePanel browserRolePanel;

    public ThreadOfRole(BrowserRolePanel browserRolePanel) {

        super();
        this.browserRolePanel = browserRolePanel;
    }

    @Override
    public void run() {

        browserRolePanel.run();
    }
}
