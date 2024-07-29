/*
 * SaveOnExitDialog.java
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

package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class SaveOnExitDialog extends AbstractBaseDialog {
    public static final String TITLE = bundleString("title");

    private int result;
    private JList<?> list;

    private JButton saveButton;
    private JButton discardButton;

    public SaveOnExitDialog() {
        super(GUIUtilities.getParentFrame(), TITLE, true);

        init();
        arrange();
    }

    private void init() {
        List<SaveFunction> panels = GUIUtilities.getOpenSaveFunctionPanels();

        saveButton = WidgetFactory.createButton("saveButton", Bundles.get("common.save.button"), e -> save());
        discardButton = WidgetFactory.createButton("discardButton", bundleString("discardButton"), e -> discardSaving());

        list = new DefaultList(panels.toArray());
        list.setSelectionInterval(0, panels.size() - 1);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(saveButton, gbh.nextCol().setMinWeightX().fillNone().get());
        buttonPanel.add(discardButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("label")), gbh.spanX().get());
        mainPanel.add(new JScrollPane(list), gbh.nextRow().topGap(5).setMaxWeightY().fillBoth().get());
        mainPanel.add(buttonPanel, gbh.nextRow().setMinWeightY().get());

        // --- base ---

        setLayout(new GridBagLayout());
        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                discardSaving();
            }
        });

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setResizable(false);
        setVisible(true);
    }

    private void save() {
        result = SaveFunction.SAVE_CANCELLED;

        for (Object selectedFrame : list.getSelectedValuesList()) {
            if (selectedFrame instanceof SaveFunction) {

                SaveFunction saveFunction = (SaveFunction) selectedFrame;
                result = saveFunction.save(false);

                if (result != SaveFunction.SAVE_COMPLETE)
                    break;
            }
        }

        dispose();
    }

    private void discardSaving() {
        result = SaveFunction.SAVE_CANCELLED;
        dispose();
    }

    public int getResult() {
        return result;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(SaveOnExitDialog.class, key, args);
    }

}
