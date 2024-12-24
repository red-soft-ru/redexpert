package org.underworldlabs.swing.celleditor;

import org.underworldlabs.swing.celleditor.picker.StringPicker;
import org.underworldlabs.util.validation.Validator;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/// @author Aleksey Kozlov
public class DatePatterCellEditor extends DefaultCellEditor
        implements CellEditorListener {

    private transient Object oldValue;

    private final transient Validator validator;
    private final JTable table;
    private final int rowIndex;

    public DatePatterCellEditor(String key, JTable table, int rowIndex) {
        super(new StringPicker());

        this.validator = Validator.of(key);
        this.rowIndex = rowIndex;
        this.oldValue = null;
        this.table = table;

        addCellEditorListener(this);
    }

    /**
     * Validates the current value in the cell editor.
     * If the validation fails, it reverts the cell value to its previous state.
     */
    public void validate() {
        if (!validator.isValid(((StringPicker) getComponent()).getValue()))
            table.setValueAt(oldValue, rowIndex, 2);
    }

    // --- DefaultCellEditor impl ---

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        this.oldValue = value;
        return component;
    }

    // --- CellEditorListener impl ---

    @Override
    public void editingStopped(ChangeEvent e) {
        validate();
    }

    @Override
    public void editingCanceled(ChangeEvent e) {
        // do nothing
    }

}
