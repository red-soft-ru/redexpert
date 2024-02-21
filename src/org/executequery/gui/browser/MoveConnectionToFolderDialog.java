/*
 * MoveConnectionToFolderDialog.java
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

package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.nodes.ConnectionsFolderNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MoveConnectionToFolderDialog extends BaseDialog {

    public static final String TITLE = bundleString("title");

    private final ConnectionsTreePanel treePanel;
    private final DatabaseConnection databaseConnection;

    // --- GUI components ---

    private JList<ConnectionsFolderNode> foldersList;
    private DefaultListModel<ConnectionsFolderNode> foldersListModel;

    private JButton newFolderButton;
    private JButton okButton;
    private JButton cancelButton;

    // ---

    public MoveConnectionToFolderDialog(DatabaseConnection databaseConnection, ConnectionsTreePanel treePanel) {
        super(TITLE, true);

        this.databaseConnection = databaseConnection;
        this.treePanel = treePanel;

        init();
        arrange();
    }

    private void init() {

        // --- folder list ---

        foldersListModel = new DefaultListModel<>();
        for (ConnectionsFolderNode folder : treePanel.getFolderNodes())
            foldersListModel.addElement(folder);

        foldersList = new JList<>(foldersListModel);
        foldersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        foldersList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                move(e);
            }
        });
        foldersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                move(e);
            }
        });

        // --- buttons ---

        newFolderButton = WidgetFactory.createLinkButton("newFolderButton", bundleString("newFolderButton"));
        newFolderButton.addActionListener(e -> newFolder());

        okButton = WidgetFactory.createButton("okButton", Bundles.get("common.ok.button"));
        okButton.addActionListener(e -> move());

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(e -> dispose());

    }

    private void arrange() {

        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillNone().rightGap(5);
        buttonPanel.add(okButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().setInsets(10, 10, 0, 5);
        mainPanel.add(new JLabel(bundleString("chooseFolderLabel")), gbh.get());
        mainPanel.add(newFolderButton, gbh.nextCol().leftGap(0).rightGap(10).spanX().get());
        mainPanel.add(new JScrollPane(foldersList), gbh.nextRowFirstCol().setMaxWeightY().leftGap(10).topGap(5).fillBoth().spanX().get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().anchorCenter().bottomGap(10).spanX().get());

        // --- base ---

        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        add(mainPanel, new GridBagHelper().fillBoth().spanX().spanY().get());

        setPreferredSize(new Dimension(400, 350));
        setResizable(false);
        pack();

        setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    // --- handlers ---

    public void newFolder() {

        ConnectionsFolder newFolder = treePanel.newFolder();
        if (newFolder != null) {

            ConnectionsFolderNode newFolderNode = treePanel.getFolderNode(newFolder);
            foldersListModel.addElement(newFolderNode);

            foldersList.requestFocus();
            foldersList.setSelectedValue(newFolderNode, true);
        }
    }

    public void move(InputEvent event) {

        if (event instanceof KeyEvent)
            if (((KeyEvent) event).getKeyCode() != KeyEvent.VK_ENTER)
                return;

        if (event instanceof MouseEvent)
            if (((MouseEvent) event).getClickCount() < 2)
                return;

        move();
    }

    public void move() {
        treePanel.moveToFolder(databaseConnection, foldersList.getSelectedValue());
        dispose();
    }

    // ---

    private static String bundleString(String key) {
        return Bundles.get(MoveConnectionToFolderDialog.class, key);
    }

}
