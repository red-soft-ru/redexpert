/*
 * SystemLogsViewer.java
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

package org.executequery.gui;

import org.executequery.ApplicationException;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.gui.text.DefaultTextEditorContainer;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Takis Diakoumis
 */
public class SystemLogsViewer extends DefaultTextEditorContainer
        implements ItemListener,
        TabView,
        ActionListener {

    public static final String TITLE = bundleString("title");
    public static final String FRAME_ICON = "SystemOutput.svg";

    private JTextArea textArea;
    private JComboBox<?> logCombo;
    private RolloverButton reloadButton;
    private RolloverButton trashButton;

    public SystemLogsViewer(int type) {

        super(new BorderLayout());

        try {
            init(type);

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void init(final int type) {

        LogRepository logRepository = ((LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID));
        String[] logs = {
                "System Log: " + logRepository.getLogFilePath(LogRepository.ACTIVITY),
                "Export Log: " + logRepository.getLogFilePath(LogRepository.EXPORT),
                "Import Log: " + logRepository.getLogFilePath(LogRepository.IMPORT)
        };

        logCombo = WidgetFactory.createComboBox("logCombo", logs);
        logCombo.addItemListener(this);

        SimpleTextArea simpleTextArea = new SimpleTextArea();
        textArea = simpleTextArea.getTextAreaComponent();
        textComponent = textArea;

        reloadButton = new RolloverButton("/org/executequery/icons/Refresh16.svg", bundleString("reload"));
        trashButton = new RolloverButton("/org/executequery/icons/Delete16.svg", bundleString("reset"));

        reloadButton.addActionListener(this);
        trashButton.addActionListener(this);

        // build the tools area
        PanelToolBar tools = new PanelToolBar();
        tools.addButton(reloadButton);
        tools.addButton(trashButton);
        tools.addSeparator();
        tools.addComboBox(logCombo);

        simpleTextArea.setBorder(BorderFactory.createEmptyBorder(1, 3, 3, 3));

        JPanel base = new JPanel(new BorderLayout());
        base.add(tools, BorderLayout.NORTH);
        base.add(simpleTextArea, BorderLayout.CENTER);

        add(base, BorderLayout.CENTER);
        setFocusable(true);

        SwingUtilities.invokeLater(() -> load(type));
    }

    public void setSelectedLog(int type) {
        logCombo.setSelectedIndex(type);
    }

    private void load(final int index) {

        SwingWorker worker = new SwingWorker("LoadSystemLogs") {

            @Override
            public Object construct() {

                GUIUtilities.showWaitCursor();
                GUIUtilities.showWaitCursor(textArea);

                try {
                    return logRepository().load(index);

                } catch (ApplicationException e) {
                    GUIUtilities.displayWarningMessage(bundleString("LogFileNotFound"));
                    return "";
                }
            }

            @Override
            public void finished() {
                setLogText((String) get());
            }
        };

        worker.start();
    }

    private void setLogText(final String text) {

        try {
            GUIUtils.invokeAndWait(() -> {
                textArea.setText(!MiscUtils.isNull(text) ? text : "");
                textArea.setCaretPosition(0);
            });

        } catch (OutOfMemoryError e) {
            GUIUtils.scheduleGC();
            GUIUtilities.showNormalCursor();
            GUIUtilities.displayErrorMessage(bundleString("outOfMemory"));

        } finally {
            GUIUtilities.showNormalCursor();
            GUIUtilities.showNormalCursor(textArea);
        }
    }

    private LogRepository logRepository() {
        return (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
    }

    private boolean resetConfirmed() {
        return GUIUtilities.displayConfirmDialog(bundleString("resetConfirmed")) == JOptionPane.YES_OPTION;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        if (source == reloadButton) {
            load(logCombo.getSelectedIndex());

        } else if (source == trashButton && resetConfirmed()) {
            logRepository().reset(logCombo.getSelectedIndex());
            textArea.setText("");
        }

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != ItemEvent.DESELECTED)
            load(logCombo.getSelectedIndex());
    }

    // --- TabView implementation ---

    /**
     * Indicates the panel is being removed from the pane
     */
    @Override
    public boolean tabViewClosing() {
        textArea = null;
        textComponent = null;
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    @Override
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    // ---

    @Override
    public String getPrintJobName() {
        return "Red Expert - system log";
    }

    @Override
    public String toString() {
        return TITLE;
    }

    private static String bundleString(String key) {
        return Bundles.get(SystemLogsViewer.class, key);
    }

}
