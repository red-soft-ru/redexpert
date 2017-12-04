package org.underworldlabs.swing.hexeditor.textgrid;

import java.util.EventListener;

public interface TextGridCursorListener extends EventListener {
    public void cursorUpdated(TextGridCursorEvent e);
}
