/*
 * ErdSelectionPanel.java
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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.ListSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdSelectionPanel extends JPanel
        implements ItemListener {

    /**
     * The connection combo selection
     */
    protected ConnectionsComboBox connectionsCombo;

    /**
     * The add/remove table selections panel
     */
    private ListSelectionPanel listPanel;

    /**
     * the database connection props object
     */
    private DatabaseConnection databaseConnection;
    private final ErdViewerPanel erdViewer;

    public ErdSelectionPanel() {
        this(null, null);
    }

    public ErdSelectionPanel(DatabaseConnection databaseConnection, ErdViewerPanel erdViewer) {
        super(new GridBagLayout());
        this.databaseConnection = databaseConnection;
        this.erdViewer = erdViewer;
        init();
    }

    private void init() {

        listPanel = new ListSelectionPanel(bundleString("availableTables"), bundleString("selected Tables"));

        connectionsCombo = WidgetFactory.createConnectionComboBox("connectionsCombo", true);
        connectionsCombo.addItemListener(this);

        setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(13, 10, 0, 10);
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel(Bundles.getCommon("connection")), gbc);
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets.top = 10;
        gbc.insets.left = 10;
        gbc.insets.bottom = 10;
        gbc.insets.right = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(listPanel, gbc);

        if (connectionsCombo.getItemCount() > 0)
            connectionChanged();

        setPreferredSize(new Dimension(700, 380));
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     */
    public void itemStateChanged(ItemEvent event) {

        if (event.getStateChange() == ItemEvent.DESELECTED)
            return;
        listPanel.clear();

        final Object source = event.getSource();
        if (source == connectionsCombo)
            GUIUtils.startWorker(this::connectionChanged);
    }

    private void connectionChanged() {
        databaseConnection = getSelectedConnection();

        try {
            List<String> tables = ConnectionsTreePanel.getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(databaseConnection)
                    .getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.TABLE]);

            if (erdViewer != null) {
                for (ErdTable table : erdViewer.getAllTablesArray()) {
                    if (!tables.contains(table.getTableName()))
                        tables.add(table.getTableName());
                }
            }
            populateTableValues(tables);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the tables names for the " +
                            "current connection.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e, this.getClass());
        }
    }

    private void populateTableValues(List<String> tables) {
        GUIUtils.invokeAndWait(() -> listPanel.createAvailableList(tables));
    }

    public Vector getSelectedValues() {
        return listPanel.getSelectedValues();
    }

    public boolean hasSelections() {
        return listPanel.hasSelections();
    }

    public DatabaseConnection getDatabaseConnection() {
        if (databaseConnection == null)
            return getSelectedConnection();
        return databaseConnection;
    }

    private DatabaseConnection getSelectedConnection() {
        return connectionsCombo.getSelectedConnection();
    }

    private String bundleString(String key) {
        return Bundles.get(ErdSelectionPanel.class, key);
    }

}
