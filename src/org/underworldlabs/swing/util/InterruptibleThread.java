package org.underworldlabs.swing.util;

public class InterruptibleThread extends Thread {

    private boolean canceled;
    private Object userObject;

    public InterruptibleThread(Runnable target) {
        this(target, null);
    }

    public InterruptibleThread(Runnable target, Object userObject) {
        super(target);
        this.userObject = userObject;
        canceled = false;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public Object getUserObject() {
        return userObject;
    }

}
