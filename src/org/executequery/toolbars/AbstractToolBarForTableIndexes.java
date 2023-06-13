package org.executequery.toolbars;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractToolBarForTableIndexes extends AbstractToolBarForTable {
    private final String toolTipReselectivityAllIndexes;

    public AbstractToolBarForTableIndexes(String toolTipInsert, String toolTipDelete, String toolTipRefresh, String toolTipReselectivityAllIndexes) {
        super(toolTipInsert, toolTipDelete, toolTipRefresh);
        this.toolTipReselectivityAllIndexes = toolTipReselectivityAllIndexes;
        RolloverButton reselectivityAllIndexes = new RolloverButton();
        reselectivityAllIndexes.setIcon(GUIUtilities.loadIcon("reselectivityAllIndicies16.svg"));
        reselectivityAllIndexes.setToolTipText(toolTipReselectivityAllIndexes);
        reselectivityAllIndexes.addActionListener(this::reselectivity);
        bar.add(reselectivityAllIndexes);
    }

    public abstract void reselectivity(ActionEvent e);

}
