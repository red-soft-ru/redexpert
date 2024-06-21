package org.executequery.toolbars;

import org.executequery.gui.WidgetFactory;

import java.awt.event.ActionEvent;

public abstract class AbstractTableIndexesToolBar extends AbstractTableToolBar {

    public AbstractTableIndexesToolBar(String toolTipInsert, String toolTipDelete, String toolTipRefresh, String toolTip) {
        super(toolTipInsert, toolTipDelete, toolTipRefresh);

        toolbar.add(WidgetFactory.createRolloverButton(
                "recalculateSelectivityButton",
                toolTip,
                "icon_db_index_reselectivity",
                this::reselectivity
        ));
    }

    public abstract void reselectivity(ActionEvent e);

}
