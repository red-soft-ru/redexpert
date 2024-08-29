package org.executequery.gui.browser.managment.tracemanager;

import biz.redsoft.IFBTraceManager;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.managment.tracemanager.net.SessionInfo;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SessionManagerPanel extends JPanel {

    private boolean refreshFlag;
    private IFBTraceManager fbTraceManager;

    // --- GUI components ---

    private JList<SessionInfo> sessionsList;
    private DynamicComboBoxModel sessionModel;

    private JTextField sessionIdField;
    private JTextArea flagsField;
    private JTextField nameField;
    private JTextField userField;
    private JTextField timestampField;
    private final JTextField sessionField;

    private JButton stopButton;
    private JButton refreshButton;

    // ---

    public SessionManagerPanel(IFBTraceManager fbTraceManager, JTextField sessionField) {
        this.fbTraceManager = fbTraceManager;
        this.sessionField = sessionField;

        setRefreshFlag(true);
        init();
        arrange();
    }

    private void init() {

        sessionModel = new DynamicComboBoxModel();
        sessionsList = new JList<>(sessionModel);
        sessionsList.setBorder(BorderFactory.createTitledBorder(bundleString("Sessions")));
        sessionsList.addListSelectionListener(e -> sessionChanged());

        flagsField = new JTextArea();
        flagsField.setBorder(BorderFactory.createTitledBorder(bundleString("Flags")));
        flagsField.setFont(UIManager.getDefaults().getFont("Label.font"));
        flagsField.setEditable(false);

        userField = WidgetFactory.createTextField("userField", false);
        nameField = WidgetFactory.createTextField("nameField", false);
        sessionIdField = WidgetFactory.createTextField("sessionIdField", false);
        timestampField = WidgetFactory.createTextField("timestampField", false);

        stopButton = WidgetFactory.createButton("stopButton", bundleString("Stop"), e -> stop());
        refreshButton = WidgetFactory.createButton("refreshButton", bundleString("Refresh"), e -> refresh());
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- buttons panel ---

        JPanel buttonsPanel = WidgetFactory.createPanel("buttonsPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonsPanel.add(refreshButton, gbh.nextCol().get());
        buttonsPanel.add(stopButton, gbh.nextCol().leftGap(5).get());

        // --- info panel ---

        JPanel infoPanel = WidgetFactory.createPanel("infoPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        infoPanel.add(new JLabel(bundleString("SessionId")), gbh.setMinWeightX().topGap(3).get());
        infoPanel.add(sessionIdField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(0).get());
        infoPanel.add(new JLabel(bundleString("Name")), gbh.nextCol().setMinWeightX().leftGap(10).topGap(3).get());
        infoPanel.add(nameField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(0).get());
        infoPanel.add(new JLabel(bundleString("User")), gbh.nextRowFirstCol().setMinWeightX().leftGap(0).topGap(8).get());
        infoPanel.add(userField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).get());
        infoPanel.add(new JLabel(bundleString("Date")), gbh.nextCol().setMinWeightX().leftGap(10).topGap(8).get());
        infoPanel.add(timestampField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).get());
        infoPanel.add(flagsField, gbh.nextRowFirstCol().leftGap(0).setMaxWeightY().fillBoth().spanX().spanY().get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillBoth();
        mainPanel.add(sessionsList, gbh.setWeightX(0.15).setMaxWeightY().get());
        mainPanel.add(buttonsPanel, gbh.nextRow().setMinWeightY().topGap(5).fillHorizontally().get());
        mainPanel.add(infoPanel, gbh.nextCol().previousRow().leftGap(5).topGap(0).fillBoth().spanY().spanX().get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().topGap(5).anchorNorthWest().fillBoth();
        add(mainPanel, gbh.spanX().spanY().get());
    }

    private void stop() {

        if (sessionsList.getSelectedValue() == null)
            return;

        try {

            SessionInfo sessionInfo = sessionsList.getSelectedValue();
            if (sessionField.getText().contentEquals(sessionInfo.getName())) {
                int confirmResult = GUIUtilities.displayConfirmDialog(bundleString("SessionManagerPanel.SessionsEquals"));
                if (confirmResult == JOptionPane.YES_OPTION)
                    stopAndRefresh(sessionInfo);

            } else
                stopAndRefresh(sessionInfo);

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorStopping", e.getMessage()), e, this.getClass());
        }
    }

    private void stopAndRefresh(SessionInfo sessionInfo) throws SQLException {
        fbTraceManager.stopTraceSession(Integer.parseInt(sessionInfo.getId()));
        refresh();
    }

    private void sessionChanged() {

        if (sessionsList.getSelectedValue() == null)
            return;

        SessionInfo sessionInfo = sessionsList.getSelectedValue();
        sessionIdField.setText(sessionInfo.getId());
        nameField.setText(sessionInfo.getName());
        userField.setText(sessionInfo.getUser());
        timestampField.setText(sessionInfo.getDatetime());
        flagsField.setText(sessionInfo.getFlags());
    }

    public void setSessions(List<SessionInfo> sessions) {
        sessionModel.setElements(sessions);
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
        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());
        }
    }

    public void setFbTraceManager(IFBTraceManager fbTraceManager) {
        this.fbTraceManager = fbTraceManager;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(SessionManagerPanel.class, key, args);
    }

}
