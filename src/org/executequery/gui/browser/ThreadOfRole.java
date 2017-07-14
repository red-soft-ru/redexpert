package org.executequery.gui.browser;

/**
 * Created by mikhan808 on 05.06.2017.
 */
public class ThreadOfRole implements Runnable{
    public ThreadOfRole(BrowserRolePanel Brp)
    {super();
    brp=Brp;
    }
    BrowserRolePanel brp;
    @Override
    public void run() {

brp.run();
    }
}
