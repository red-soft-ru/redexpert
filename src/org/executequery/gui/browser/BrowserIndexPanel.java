package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DefaultComboBox;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.StyledLogPane;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vasiliy on 15.02.17.
 */
public class BrowserIndexPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserIndexPanel";

    private DependenciesPanel dependenciesPanel;

    private DisabledField indexNameField;

    private JLabel objectNameLabel;

    /**
     * the current database object in view
     */
    private DatabaseObject currentObjectView;

    /**
     * The tabbed description pane
     */
    private JTabbedPane tabPane;

    private JTextPane descriptionPane;

    private Map cache;

    List<DefaultDatabaseIndex.DatabaseIndexColumn> columns = new ArrayList<DefaultDatabaseIndex.DatabaseIndexColumn>();

    private JTable table;

    private DefaultDatabaseIndex.IndexColumnsModel model;

    private JCheckBox uniqueCheckBox;
    private JTextField tableField;
    private JComboBox sortingComboBox;
    private JCheckBox activeCheckBox;
    private SimpleSqlTextPanel expressionText;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserIndexPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        dependenciesPanel = new DependenciesPanel();
        model = new DefaultDatabaseIndex.IndexColumnsModel(columns);
        table = new JTable(model);
        GridBagConstraints gbc_def = new GridBagConstraints();
        gbc_def.fill = GridBagConstraints.HORIZONTAL;
        gbc_def.anchor = GridBagConstraints.NORTHWEST;
        gbc_def.insets = new Insets(10, 10, 10, 10);
        gbc_def.gridy = -1;
        gbc_def.gridx = 0;
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(gbc_def).defaults();
        fieldsPanel.add(
                new JScrollPane(table), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.add(bundleString("IncludedFields"), fieldsPanel);
        JPanel descriptionPanel = new JPanel(new BorderLayout());

        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabPane.add(Bundles.getCommon("description"), descriptionPanel);
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        objectNameLabel = new JLabel();
        indexNameField = new DisabledField();

        tableField = new DisabledField();
        uniqueCheckBox = new JCheckBox(bundleString("Unique"));
        uniqueCheckBox.setEnabled(false);
        uniqueCheckBox.setSelected(false);
        activeCheckBox = new JCheckBox(bundleString("Active"));
        activeCheckBox.setSelected(false);
        activeCheckBox.setEnabled(false);
        sortingComboBox = new DefaultComboBox();
        List<String> sorting = new ArrayList<>();
        sorting.add(bundleString("Ascending"));
        sorting.add(bundleString("Descending"));
        sortingComboBox.setModel(new DefaultComboBoxModel(sorting.toArray()));

        JPanel base = new JPanel(new GridBagLayout());
        gbh.defaults();
        gbh.addLabelFieldPair(base, objectNameLabel, indexNameField, null);
        gbh.addLabelFieldPair(base, bundleString("TableName"), tableField, null);
        gbh.addLabelFieldPair(base, bundleString("Sorting"), sortingComboBox, null);
        base.add(activeCheckBox, gbh.nextRowFirstCol().setLabelDefault().get());
        base.add(uniqueCheckBox, gbh.nextCol().get());
        base.add(tabPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());


        setHeaderText("Database UDF");
        setHeaderIcon(GUIUtilities.loadIcon("TableIndex16.png", true));
        setContentPanel(base);
        cache = new HashMap();

    }

    public String getLayoutName() {
        return NAME;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    public void setValues(DefaultDatabaseIndex index) {
        dependenciesPanel.setDatabaseObject(index);
        if (index.getParent().getMetaDataKey() != NamedObject.META_TYPES[NamedObject.SYSTEM_INDEX]) {
            DefaultDatabaseMetaTag metaTag = (DefaultDatabaseMetaTag) index.getParent();
            currentObjectView = metaTag.getIndexFromName(index.getName());
            index = (DefaultDatabaseIndex) currentObjectView;
        } else {
            currentObjectView = index;
        }

        columns.clear();
        index.loadColumns();
        for (DefaultDatabaseIndex.DatabaseIndexColumn column : index.getIndexColumns())
            columns.add(column);

        model = new DefaultDatabaseIndex.IndexColumnsModel(columns);

        tableField.setText(index.getTableName());
        sortingComboBox.setSelectedIndex(index.getIndexType());
        uniqueCheckBox.setSelected(index.isUnique());
        activeCheckBox.setSelected(index.isActive());
        descriptionPane.setText(index.getRemarks());
        if (index.getExpression() != null) {
            expressionText = new SimpleSqlTextPanel();
            expressionText.setSQLText(index.getExpression());
            tabPane.remove(0);
            tabPane.insertTab("Expression", null, expressionText, null, 0);
            tabPane.setSelectedIndex(0);
        }

        objectNameLabel.setText(bundleString("IndexName"));
        setHeaderText(bundleString("DatabaseIndex"));
        setHeaderIcon(GUIUtilities.loadIcon("TableIndex16.png", true));

        try {
            indexNameField.setText(index.getName());
            descriptionPane.setText(index.getRemarks());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseIndex index = (DefaultDatabaseIndex) cache.get(metaObject);
        setValues(metaObject, index);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseIndex index) {

        objectNameLabel.setText(bundleString("IndexName"));
        setHeaderText(bundleString("DatabaseIndex"));
        setHeaderIcon("TableIndex16.png");

        if (index != null) {
            indexNameField.setText(index.getName());
            descriptionPane.setText(index.getRemarks());

        } else {
            indexNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }
}