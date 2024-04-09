package org.underworldlabs.swing.celleditor.picker;

import javax.swing.*;

public interface DefaultPicker {

    Object getValue();

    void setValue(Object value);

    JTextField getEditorComponent();
}
