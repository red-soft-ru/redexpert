package org.executequery.gui.browser.managment;

import org.executequery.gui.browser.UserManagerPanel;

public class ThreadOfUserManager implements Runnable {

    UserManagerPanel brp;

    public ThreadOfUserManager(UserManagerPanel ump) {
        super();
        brp = ump;
    }

    @Override
    public void run() {
        brp.run();
    }
}
