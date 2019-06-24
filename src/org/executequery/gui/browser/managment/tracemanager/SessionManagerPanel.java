package org.executequery.gui.browser.managment.tracemanager;

import biz.redsoft.IFBTraceManager;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.TraceManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.net.SessionInfo;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class SessionManagerPanel extends JPanel {
    private DynamicComboBoxModel sessionBoxModel;
    private JList sessionBox;
    private JTextField nameField;
    private JTextField idField;
    private JTextArea flagsField;
    private JTextField userField;
    private JTextField datetimeField;
    private JButton stopButton;
    private JButton refreshButton;
    private boolean refreshFlag;
    private IFBTraceManager fbTraceManager;
    private JTextField sessionField;

    public SessionManagerPanel(IFBTraceManager fbTraceManager, JTextField sessionField) {
        this.fbTraceManager = fbTraceManager;
        this.sessionField = sessionField;
        init();
    }

    private void init() {
        setLayout(new GridBagLayout());
        setRefreshFlag(true);
        sessionBoxModel = new DynamicComboBoxModel();
        sessionBox = new JList(sessionBoxModel);
        sessionBox.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (sessionBox.getSelectedValue() != null) {
                    SessionInfo sessionInfo = (SessionInfo) sessionBox.getSelectedValue();
                    idField.setText(sessionInfo.getId());
                    nameField.setText(sessionInfo.getName());
                    userField.setText(sessionInfo.getUser());
                    datetimeField.setText(sessionInfo.getDatetime());
                    flagsField.setText(sessionInfo.getFlags());
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(sessionBox);
        nameField = new JTextField();
        nameField.setEditable(false);
        idField = new JTextField();
        idField.setEditable(false);
        flagsField = new JTextArea();
        flagsField.setFont(UIManager.getDefaults().getFont("Label.font"));
        flagsField.setEditable(false);
        userField = new JTextField();
        userField.setEditable(false);
        datetimeField = new JTextField();
        datetimeField.setEditable(false);
        stopButton = new DefaultButton(TraceManagerPanel.bundleString("Stop"));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sessionBox.getSelectedValue() != null)
                    try {
                        SessionInfo sessionInfo = (SessionInfo) sessionBox.getSelectedValue();
                        if (sessionField.getText().contentEquals(sessionInfo.getName())) {
                            int res = GUIUtilities.displayConfirmDialog("this session may be current session.Do you want continue?");
                            if (res == JOptionPane.YES_OPTION) {
                                fbTraceManager.stopTraceSession(Integer.parseInt(sessionInfo.getId()));
                                refresh();
                            }
                        } else {
                            fbTraceManager.stopTraceSession(Integer.parseInt(sessionInfo.getId()));
                            refresh();
                        }
                    } catch (SQLException e1) {
                        GUIUtilities.displayExceptionErrorDialog("Error stop session", e1);
                    }
            }
        });

        refreshButton = new DefaultButton(TraceManagerPanel.bundleString("Refresh"));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });

        JLabel label = new JLabel(TraceManagerPanel.bundleString("Sessions"));
        add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(scrollPane, new GridBagConstraints(0, 1,
                1, 4, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("ID"));
        add(label, new GridBagConstraints(1, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(idField, new GridBagConstraints(2, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("Name"));
        add(label, new GridBagConstraints(3, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(nameField, new GridBagConstraints(4, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("User"));
        add(label, new GridBagConstraints(1, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(userField, new GridBagConstraints(2, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("Date"));
        add(label, new GridBagConstraints(3, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(datetimeField, new GridBagConstraints(4, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("Flags"));
        add(label, new GridBagConstraints(1, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(flagsField, new GridBagConstraints(2, 3,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));

        add(stopButton, new GridBagConstraints(1, 4,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(refreshButton, new GridBagConstraints(2, 4,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));


    }

    public void setSessions(List<SessionInfo> sessions) {
        sessionBoxModel.setElements(sessions);
    }

    public boolean isRefreshFlag() {
        return refreshFlag;
    }

    public void setRefreshFlag(boolean refreshFlag) {
        this.refreshFlag = refreshFlag;
    }

    private void refresh() {
        setRefreshFlag(true);
        try {
            fbTraceManager.listTraceSessions();
        } catch (SQLException e1) {
            GUIUtilities.displayExceptionErrorDialog("Error refresh", e1);
        }
    }
}
