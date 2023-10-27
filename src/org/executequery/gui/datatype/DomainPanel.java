package org.executequery.gui.datatype;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseDomain;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.CreateDomainPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class DomainPanel extends JPanel {
    private JComboBox domainBox;
    private JButton editDomainButton;
    private JButton newDomainButton;
    private final ColumnData columnData;
    private final boolean editing;
    private final String currentDomain;

    public DomainPanel(ColumnData cd, String currentDomain) {
        columnData = cd;
        this.currentDomain = currentDomain;
        editing = currentDomain != null;
        init();
    }

    private void init() {
        domainBox = new JComboBox();
        addDomainComboBoxActionListener(actionEvent -> {
            DefaultDatabaseDomain defaultDatabaseDomain = (DefaultDatabaseDomain) domainBox.getSelectedItem();
            if (defaultDatabaseDomain == null)
                columnData.setDomain(null);
            else
                columnData.setDomain(defaultDatabaseDomain.getName());
            if (!editing) {
                editDomainButton.setEnabled(columnData.getDomain() != null && !columnData.getDomain().equals(""));
            }
        });
        editDomainButton = new JButton(bundleString("EditDomain"));
        newDomainButton = new JButton(bundleString("NewDomain"));
        newDomainButton.addActionListener(actionEvent -> {
            BaseDialog dialog = new BaseDialog(CreateDomainPanel.CREATE_TITLE, true);
            CreateDomainPanel panel = new CreateDomainPanel(columnData.getDatabaseConnection(), dialog);
            dialog.addDisplayComponent(panel);
            dialog.display();
        });
        editDomainButton.addActionListener(actionEvent -> {
            BaseDialog dialog = new BaseDialog(CreateDomainPanel.EDIT_TITLE, true);
            CreateDomainPanel panel = new CreateDomainPanel(
                    columnData.getDatabaseConnection(), dialog,
                    ((DefaultDatabaseDomain) Objects.requireNonNull(domainBox.getSelectedItem())).getName());
            dialog.addDisplayComponent(panel);
            dialog.display();
        });
        Vector<NamedObject> domains = new Vector(getDomains());
        domains.add(0, null);
        domainBox.setModel(new DefaultComboBoxModel(domains));
        if (editing) {
            boolean finded = false;
            for (NamedObject namedObject : domains) {
                if (namedObject != null) {
                    if (namedObject.getName().trim().contentEquals(currentDomain)) {
                        domainBox.setSelectedItem(namedObject);
                        finded = true;
                    }
                }
            }
            if (!finded) {
                NamedObject namedObject = ConnectionsTreePanel.getNamedObjectFromHost(columnData.getDatabaseConnection(), NamedObject.SYSTEM_DOMAIN, currentDomain);
                domainBox.addItem(namedObject);
                domainBox.setSelectedItem(namedObject);
            }
        }
        //else domainBox.setModel(new DefaultComboBoxModel(new Vector(getDomains())));
        this.setLayout(new GridBagLayout());
        this.add(new Label(bundleString("Domain")), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(domainBox, new GridBagConstraints(1, 0, 1, 1, 0.5, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(new Panel(), new GridBagConstraints(2, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(editDomainButton, new GridBagConstraints(3, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        this.add(newDomainButton, new GridBagConstraints(3, 1, 1, 1, 0, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        editDomainButton.setEnabled(editing);
    }

    public void addDomainComboBoxActionListener(ActionListener listener) {
        domainBox.addActionListener(listener);
    }

    public int getDomainComboBoxSelectedIndex() {
        return domainBox.getSelectedIndex();
    }

    public Object getDomainComboBoxSelectedItem() {
        return domainBox.getSelectedItem();
    }

    /*private String[] getEditingDomains() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(columnData.getDatabaseConnection(), true);
        List<String> domains = new ArrayList<>();
        domains.add(currentDomain);
        try {
            String query = "select " +
                    "RDB$FIELD_NAME FROM RDB$FIELDS " +
                    "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                    "and RDB$FIELD_NAME not like 'MON$%'\n" +
                    "order by RDB$FIELD_NAME";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading domains:" + e.getMessage());
            return null;
        }
    }*/

    List<NamedObject> getDomains() {
        return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(columnData.getDatabaseConnection()).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.DOMAIN]);
    }

    private String bundleString(String key) {
        return Bundles.get(DomainPanel.class, key);
    }
}
