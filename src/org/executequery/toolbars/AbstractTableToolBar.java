package org.executequery.toolbars;

import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AbstractTableToolBar extends JPanel {

    protected PanelToolBar toolbar;

    public AbstractTableToolBar(String toolTipInsert, String toolTipDelete, String toolTipRefresh) {
        toolbar = new PanelToolBar();

        toolbar.add(WidgetFactory.createRolloverButton(
                "addColumnButton",
                toolTipInsert,
                "ColumnInsert16.png",
                this::insert
        ));
        toolbar.add(WidgetFactory.createRolloverButton(
                "deleteColumnButton",
                toolTipDelete,
                "ColumnDelete16.png",
                this::delete
        ));
        toolbar.add(WidgetFactory.createRolloverButton(
                "refreshColumnsButton",
                toolTipRefresh,
                "Refresh16.png",
                this::refresh
        ));

        setLayout(new GridBagLayout());
        add(toolbar, new GridBagHelper().setX(4).setMaxWeightX().setMaxWeightY().anchorCenter().fillHorizontally().get());
    }

    public abstract void insert(ActionEvent e);

    public abstract void delete(ActionEvent e);

    public abstract void refresh(ActionEvent e);

}
