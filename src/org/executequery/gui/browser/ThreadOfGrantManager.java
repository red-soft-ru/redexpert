package org.executequery.gui.browser;

public class ThreadOfGrantManager implements Runnable{
    public ThreadOfGrantManager(GrantManagerPanel Brp)
    {super();
        brp=Brp;
    }
    GrantManagerPanel brp;
    @Override
    public void run() {

        brp.run();
    }
}
