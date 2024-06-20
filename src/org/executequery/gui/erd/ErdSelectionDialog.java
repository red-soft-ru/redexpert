/*
 * ErdSelectionDialog.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.erd;

import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class ErdSelectionDialog extends AbstractBaseDialog {

    private final ErdViewerPanel parent;

    private JButton addButton;
    private JButton cancelButton;
    private ErdSelectionPanel selectionPanel;

    public ErdSelectionDialog(ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), "Add Table", true);
        this.parent = parent;

        init();
        arrange();
    }

    private void init() {
        selectionPanel = new ErdSelectionPanel();

        addButton = WidgetFactory.createButton(
                "addButton",
                Bundles.get("common.add.button"),
                e -> add()
        );
        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> dispose()
        );

    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().rightGap(5).anchorEast();
        buttonPanel.add(addButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(selectionPanel, gbh.setMaxWeightY().spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().fillNone().get());

        // --- base ---

        add(mainPanel, BorderLayout.CENTER);

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    private void add() {

        if (!selectionPanel.hasSelections()) {
            GUIUtilities.displayErrorMessage("You must select at least one table.");
            return;
        }

        new ErdGenerateProgressDialog(
                selectionPanel.getSelectedValues(),
                parent
        );

        setVisible(false);
        dispose();
    }

}
