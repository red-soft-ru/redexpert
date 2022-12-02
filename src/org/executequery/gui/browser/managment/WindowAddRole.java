package org.executequery.gui.browser.managment;

import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.browser.UserManagerPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by mikhan808 on 15.03.2017.
 */
public class WindowAddRole extends JPanel {

    public static final String TITLE = Bundles.get(WindowAddRole.class,"CreateRole");
    DatabaseConnection dc;
    ActionContainer parent;
    JTextField nameTextField;
    JLabel jLabel1;

    public WindowAddRole(ActionContainer parent, DatabaseConnection dc) {
        this.parent = parent;
        this.dc = dc;
        initComponents();

    }

    private void initComponents() {
        this.setLayout(new BorderLayout());
        nameTextField = new JTextField();
        jLabel1 = new JLabel();
        jLabel1.setText(Bundles.get(UserManagerPanel.class, "RoleName"));

        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(parent.isDialog());
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okAction();
            }
        });
        bottomButtonPanel.setOkButtonText("OK");
        bottomButtonPanel.setHelpButtonVisible(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(jLabel1, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 5),
                0, 0));
        panel.add(nameTextField, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 0, 5),
                0, 0));
        // add empty panel for stretch
        panel.add(new JPanel(), new GridBagConstraints(0, 1,
                2, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));
        this.add(panel, BorderLayout.CENTER);
        this.add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void okAction() {
        String query = "CREATE ROLE " + nameTextField.getText();
        ExecuteQueryDialog eqd = new ExecuteQueryDialog(Bundles.get(WindowAddRole.class,"CreateRole"), query, dc, true);
        eqd.display();
        if (eqd.getCommit())
            parent.finished();
    }
}
