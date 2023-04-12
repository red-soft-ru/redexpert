package org.underworldlabs.swing.util;

public class InterruptibleThread extends Thread {

    private boolean canceled;

    public InterruptibleThread(Runnable target) {
        super(target);
        canceled = false;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
