package org.underworldlabs.swing.celleditor;

import org.executequery.gui.resultset.RecordDataItem;
import org.underworldlabs.swing.celleditor.picker.DefaultPicker;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class CustomCellEditor extends AbstractCellEditor
        implements TableCellEditor {

    private final DefaultPicker picker;

    public CustomCellEditor(DefaultPicker picker) {
        this.picker = picker;
    }

    @Override
    public Object getCellEditorValue() {
        return picker.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

        if (value instanceof RecordDataItem)
            picker.setValue(((RecordDataItem) value).getNewValue());

        return picker.getEditorComponent();
    }

}
