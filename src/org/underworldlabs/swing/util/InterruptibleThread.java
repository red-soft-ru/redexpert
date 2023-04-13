package org.underworldlabs.swing.util;

import org.executequery.gui.browser.ComparerDBPanel;

public class InterruptibleThread extends Thread {

    private boolean canceled;
    private Object userObject;

    public InterruptibleThread(Runnable target) {
        super(target);
        canceled = false;
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

    public void recreateProgressBar(String label, String metaTag, int maxValue) {
        if (userObjectNotNull() && userObject instanceof ComparerDBPanel)
            ((ComparerDBPanel) userObject).recreateProgressBar(label, metaTag, maxValue);
    }

    public void incrementProgressBarValue() {
        if (userObjectNotNull() && userObject instanceof ComparerDBPanel)
            ((ComparerDBPanel) userObject).incrementProgressBarValue();
    }

    public boolean userObjectNotNull() {
        return userObject != null;
    }

}
