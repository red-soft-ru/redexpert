package org.underworldlabs.swing.hexeditor.textgrid;

import java.util.EventListener;

public interface TextGridModelListener extends EventListener {
    public void textGridUpdated(TextGridModelEvent e);
}
