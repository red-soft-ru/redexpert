package org.executequery.toolbars;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AbstractToolBarForTable extends JPanel {

    protected PanelToolBar bar;
    protected String toolTipInsert;
    protected String toolTipDelete;
    protected String toolTipRefresh;

    public AbstractToolBarForTable(String toolTipInsert, String toolTipDelete, String toolTipRefresh) {
        bar = new PanelToolBar();
        this.toolTipInsert = toolTipInsert;
        this.toolTipDelete = toolTipDelete;
        this.toolTipRefresh = toolTipRefresh;
        init();
    }

    protected void init() {
        setLayout(new GridBagLayout());
        RolloverButton addRolloverButton = new RolloverButton();
        addRolloverButton.setIcon(GUIUtilities.loadIcon("ColumnInsert16.svg"));
        addRolloverButton.setToolTipText(toolTipInsert);
        addRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                insert(actionEvent);
            }
        });
        bar.add(addRolloverButton);
        RolloverButton deleteRolloverButton = new RolloverButton();
        deleteRolloverButton.setIcon(GUIUtilities.loadIcon("ColumnDelete16.svg"));
        deleteRolloverButton.setToolTipText(toolTipDelete);
        deleteRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                delete(actionEvent);
            }
        });
        bar.add(deleteRolloverButton);
        RolloverButton refreshRolloverButton = new RolloverButton();
        refreshRolloverButton.setIcon(GUIUtilities.loadIcon("Refresh16.svg"));
        refreshRolloverButton.setToolTipText(toolTipRefresh);
        refreshRolloverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                refresh(actionEvent);
            }
        });
        bar.add(refreshRolloverButton);
        GridBagConstraints gbc3 = new GridBagConstraints(4, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
        add(bar, gbc3);
    }

    public abstract void insert(ActionEvent e);

    public abstract void delete(ActionEvent e);

    public abstract void refresh(ActionEvent e);

}
