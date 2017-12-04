package org.underworldlabs.swing.hexeditor;

import java.util.EventListener;

public interface BinaryEditorListener extends EventListener {
    public void editorUpdated(BinaryEditorEvent e);
}
