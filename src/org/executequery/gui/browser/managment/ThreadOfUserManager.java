package org.executequery.gui.browser.managment;

import org.executequery.gui.browser.UserManagerPanel;

public class ThreadOfUserManager implements Runnable {
    public ThreadOfUserManager(UserManagerPanel ump) {
        super();
        brp = ump;
    }

    UserManagerPanel brp;

    @Override
    public void run() {

        brp.run();
    }
}
