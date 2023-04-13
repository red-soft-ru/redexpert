package org.executequery.gui.browser;


import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.gui.browser.managment.WindowAddUser;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.Printable;


public class BrowserUserPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserUserPanel";
    JTabbedPane tabbedPane;
    JPanel userPanel;
    SimpleSqlTextPanel sqlPanel;
    SimpleSqlTextPanel descriptionPanel;
    int version;
    DefaultDatabaseUser user;
    boolean edit;
    private final BrowserController controller;
    private JTextField firstNameField;
    private NumberTextField groupIDField;
    private JLabel tagLabel;
    private JLabel pluginLabel;
    private JTextField pluginField;
    private JCheckBox adminBox;
    private JTextField lastNameField;
    private JTextField middleNameField;
    private JTextField nameTextField;
    private NumberTextField userIDField;
    private JTable tagTable;
    private JScrollPane tagScroll;
    private JCheckBox activeBox;

    public BrowserUserPanel(BrowserController controller) {
        super();
        this.controller = controller;

//        try {
//            init();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void init() {
        initComponents();
        edit = true;
        nameTextField.setText(user.getName());
        firstNameField.setText(user.getFirstName());
        middleNameField.setText(user.getMiddleName());
        lastNameField.setText(user.getLastName());
        activeBox.setSelected(user.getActive());
        pluginField.setText(user.getPlugin());
        pluginField.setEnabled(false);
        adminBox.setSelected(user.getAdministrator());
        descriptionPanel.setSQLText(user.getComment());
        for (String tag : user.getTags().keySet()) {
            ((DefaultTableModel) tagTable.getModel()).addRow(new Object[]{tag, user.getTag(tag)});
        }
        sqlPanel.setSQLText(SQLUtils.generateCreateUser(user, true));
    }

    public String getLayoutName() {
        return NAME;
    }

    public Printable getPrintable() {
        return null;
    }

    public void cleanup() {
        sqlPanel.cleanup();
        descriptionPanel.cleanup();
    }

    public void setValues(DefaultDatabaseUser user) {
        this.version = user.getDatabaseMajorVersion();
        this.user = user;
        user.loadData();
        init();
    }

    private void initComponents() {

        nameTextField = new JTextField();
        nameTextField.setEditable(false);
        firstNameField = new JTextField();
        firstNameField.setEditable(false);
        middleNameField = new JTextField();
        middleNameField.setEditable(false);
        lastNameField = new JTextField();
        lastNameField.setEditable(false);
        descriptionPanel = new SimpleSqlTextPanel();
        userIDField = new NumberTextField();
        groupIDField = new NumberTextField();
        tagLabel = new JLabel();
        tagTable = new JTable();
        tagScroll = new JScrollPane();
        activeBox = new JCheckBox();
        activeBox.setEnabled(false);
        pluginLabel = new JLabel();
        pluginField = new JTextField();
        adminBox = new JCheckBox();
        adminBox.setEnabled(false);
        tabbedPane = new JTabbedPane();
        userPanel = new JPanel();
        sqlPanel = new SimpleSqlTextPanel();

        tagScroll.setViewportView(tagTable);

        pluginLabel.setText(bundleString("Plugin"));

        adminBox.setText(bundleString("Administrator"));

        tagLabel.setText(bundleString("Tags"));
        tagTable.setModel(new DefaultTableModel(new Object[][]{

        }, bundleStrings(new String[]{
                "tag", "value"
        })));

        activeBox.setText(bundleString("active"));

        userPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbConst = new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(gbConst).defaults();

        gbh.insertEmptyRow(userPanel, 10);
        userPanel.add(editButton, gbh.setLabelDefault().get());
        gbh.nextRowFirstCol();
        gbh.addLabelFieldPair(userPanel, bundleString("UserName"), nameTextField, null, true, false);
        gbh.addLabelFieldPair(userPanel, bundleString("FirstName"), firstNameField, null, true, false);
        gbh.addLabelFieldPair(userPanel, bundleString("MiddleName"), middleNameField, null, true, false);
        gbh.addLabelFieldPair(userPanel, bundleString("LastName"), lastNameField, null, true, false);
        gbh.addLabelFieldPair(userPanel, pluginLabel, pluginField, null, true, false);
        userPanel.add(activeBox, gbh.nextRowFirstCol().setLabelDefault().get());
        userPanel.add(adminBox, gbh.nextCol().get());
        gbh.insertEmptyBigRow(userPanel);
        gbh.setXY(2, 0).setLabelDefault().setHeight(2).setMinWeightY();
        userPanel.add(tagScroll, gbh.setX(2).nextRowWidth().setWidth(2).setMaxWeightY().setMaxWeightX().fillBoth().setHeight(10).get());
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.add(bundleString("properties"), userPanel);
        tabbedPane.add(bundleString("Description"), descriptionPanel);
        tabbedPane.add("SQL", sqlPanel);
        initVersion();
    }

    private void initVersion() {
        if (version >= 3) {
            userIDField.setVisible(false);
            groupIDField.setVisible(false);

        } else {
            activeBox.setVisible(false);
            tagScroll.setVisible(false);
            tagLabel.setVisible(false);
            tagTable.setVisible(false);
            adminBox.setVisible(false);
            pluginField.setVisible(false);
            pluginLabel.setVisible(false);
        }
    }

    protected String bundleString(String key) {
        return Bundles.get(WindowAddUser.class, key);
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(WindowAddUser.class, key, args);
    }

    private String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++)
            key[i] = bundleString(key[i]);
        return key;
    }

}
