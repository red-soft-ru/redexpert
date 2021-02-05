package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseDomain;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.databaseobjects.CreateDomainPanel;
import org.executequery.gui.databaseobjects.DefaultDatabaseObjectTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.localization.Bundles;
import org.executequery.print.TablePrinter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.StyledLogPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vasiliy on 02.02.17.
 */
public class BrowserDomainPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserDomainPanel";

    private DisabledField domainNameField;

    private JLabel objectNameLabel;

    /**
     * the current database object in view
     */
    private DatabaseObject currentObjectView;

    /**
     * The tabbed description pane
     */
    private JTabbedPane tabPane;

    /**
     * the table description table
     */
    private DefaultDatabaseObjectTable tableDescriptionTable;

    private JTextPane descriptionPane;

    private JTextPane sqlPane;

    private DependenciesPanel dependenciesPanel;

    private Map cache;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserDomainPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        tableDescriptionTable = new DefaultDatabaseObjectTable();
        JPanel descPanel = new JPanel(new GridBagLayout());
        descPanel.add(
                new JScrollPane(tableDescriptionTable),
                new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                        GridBagConstraints.SOUTHEAST,
                        GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        dependenciesPanel = new DependenciesPanel();
        tabPane = new JTabbedPane(JTabbedPane.TOP);
        tabPane.add(bundleString("Domain"), descPanel);
        JPanel descriptionPanel = new JPanel(new BorderLayout());

        descriptionPanel.setBorder(BorderFactory.createEtchedBorder());

        descriptionPane = new StyledLogPane();

        descriptionPanel.add(descriptionPane, BorderLayout.CENTER);

        tabPane.add(Bundles.getCommon("description"), descriptionPanel);

        JPanel sqlPanel = new JPanel(new BorderLayout());

        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        sqlPane = new SQLTextPane();

        sqlPanel.add(sqlPane, BorderLayout.CENTER);

        tabPane.add("Sql", sqlPanel);

        objectNameLabel = new JLabel();
        domainNameField = new DisabledField();
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);

        tableDescriptionTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    int row = tableDescriptionTable.getSelectedRow();
                    if (row >= 0) {
                        BaseDialog dialog = new BaseDialog(CreateDomainPanel.EDIT_TITLE, true);
                        CreateDomainPanel panel = new CreateDomainPanel(currentObjectView.getHost().getDatabaseConnection(), dialog, currentObjectView.getName().trim());
                        dialog.addDisplayComponent(panel);
                        dialog.display();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

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
        base.add(domainNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;

        setHeaderText(Bundles.get(BrowserDomainPanel.class, "DatabaseDomain"));
        setHeaderIcon(GUIUtilities.loadIcon("domain16.png", true));
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
        int tabIndex = tabPane.getSelectedIndex();
        switch (tabIndex) {

            case 0:
                return new TablePrinter(tableDescriptionTable,
                        bundleString("table") + currentObjectView.getName());
            default:
                return null;
        }
    }

    public void setValues(DefaultDatabaseDomain domain) {

        currentObjectView = domain;
        currentObjectView.setHost(((DatabaseMetaTag) domain.getParent()).getHost());
        dependenciesPanel.setDatabaseObject(currentObjectView);
        objectNameLabel.setText(bundleString("DomainName"));
        setHeaderText(bundleString("DatabaseDomain"));
        setHeaderIcon(GUIUtilities.loadIcon("domain16.png", true));

        try {
            domainNameField.setText(domain.getName());
            tableDescriptionTable.setColumnData(domain.getDomainCols());
            descriptionPane.setText(domain.getRemarks());
            sqlPane.setText(domain.getCreateSQLText());
        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseDomain domain = (DefaultDatabaseDomain) cache.get(metaObject);
        setValues(metaObject, domain);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseDomain domain) {

        objectNameLabel.setText("Domain Name:");
        setHeaderText("Database Domain");
        setHeaderIcon("domain16.png");

        if (domain != null) {
            domainNameField.setText(domain.getName());
            tableDescriptionTable.setColumnData(domain.getDomainCols());
            descriptionPane.setText(domain.getRemarks());
            sqlPane.setText(domain.getCreateSQLText());

        } else {
            domainNameField.setText(metaObject.getName());
        }

    }

    private void setHeaderIcon(String icon) {

        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }


}