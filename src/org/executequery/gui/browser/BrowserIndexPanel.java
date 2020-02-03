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

        fieldsPanel.add(
                new JScrollPane(table),
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHEAST,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

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

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(10, 10, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        gbc.insets = insets;
        gbc.gridy++;
        base.add(objectNameLabel, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        gbc.insets.right = 10;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(tabPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 5;
        gbc.insets.top = 10;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;

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

        base.add(indexNameField, gbc);
        ++gbc.gridy;

        JPanel panel = new JPanel(new FlowLayout());

        JLabel tableLable = new JLabel(bundleString("TableName"));
        JLabel sortingLable = new JLabel(bundleString("Sorting"));
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(tableLable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tableField, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(sortingLable)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sortingComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(uniqueCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(activeCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(tableLable)
                                        .addComponent(tableField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(sortingLable)
                                        .addComponent(sortingComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(uniqueCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(activeCheckBox)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        --gbc.gridx;
        gbc.gridwidth = 2;
        base.add(panel, gbc);
        gbc.insets.top = 0;

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