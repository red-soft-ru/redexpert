package org.executequery.gui.browser.managment;

import org.executequery.gui.browser.GrantManagerPanel;

public class ThreadOfGrantManager implements Runnable {

    GrantManagerPanel brp;

    public ThreadOfGrantManager(GrantManagerPanel Brp) {
        super();
        brp = Brp;
    }

    @Override
    public void run() {
        brp.run();
    }
}
