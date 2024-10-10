package org.executequery.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;

/** Implementation of the <code>DocumentListener</code>.<br>
 * Executes specified <code>Runnable</code> on every type of <code>DocumentEvent</code>.
 *
 * @author Aleksey Kozlov
 */
public class SimpleDocumentListener implements DocumentListener {
    private final Runnable runnable;
    private boolean enabled;

    public SimpleDocumentListener(Runnable runnable) {
        this.runnable = runnable;
        this.enabled = true;
    }

    public static void initialize(Component component, Runnable runnable) {
        SimpleDocumentListener listener = new SimpleDocumentListener(runnable);

        if (component instanceof JTextComponent)
            ((JTextComponent) component).getDocument().addDocumentListener(listener);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void invoke() {
        invoke(false);
    }

    public void invoke(boolean force) {
        if (enabled || force) runnable.run();
    }

    // --- DocumentListener impl ---

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

}
