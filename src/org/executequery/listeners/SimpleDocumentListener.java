package org.executequery.listeners;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

/**
 * @author Aleksey Kozlov
 */
public class SimpleDocumentListener implements DocumentListener {
    private final ActionListener listener;

    public SimpleDocumentListener(ActionListener listener) {
        this.listener = listener;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        listener.actionPerformed(null);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        listener.actionPerformed(null);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        listener.actionPerformed(null);
    }

}
