package org.executequery.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Aleksey Kozlov
 */
public class SimpleDocumentListener implements DocumentListener {
    private final Runnable runnable;
    private boolean enabled;

    public SimpleDocumentListener(Runnable runnable) {
        this.runnable = runnable;
        this.enabled = true;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        invoke();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        invoke();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        invoke();
    }

    public void invoke() {
        if (enabled)
            runnable.run();
    }

}
