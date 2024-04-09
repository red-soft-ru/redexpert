package org.underworldlabs.swing.celleditor;

import org.underworldlabs.swing.celleditor.picker.DefaultPicker;

import javax.swing.*;

public class CustomCellEditor extends DefaultCellEditor {
    private final DefaultPicker picker;

    public CustomCellEditor(DefaultPicker picker) {
        super(picker.getEditorComponent());
        this.picker = picker;
    }

    @Override
    public Object getCellEditorValue() {
        return picker.getValue();
    }
}
