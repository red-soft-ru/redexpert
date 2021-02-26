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

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.gui.text.DefaultTextEditorContainer;
import org.executequery.gui.text.SimpleTextArea;
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

    public static final String TITLE = "System Log Viewer";

    public static final String FRAME_ICON = "SystemOutput.png";

    private JTextArea textArea;

    private JComboBox logCombo;

    private RolloverButton reloadButton;
    private RolloverButton trashButton;

    public SystemLogsViewer(int type) {

        super(new BorderLayout());

        try {

            init(type);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void init(final int type) throws Exception {

        String[] logs = {"System Log: ~/.redexpert/logs/eq.output.log",
                "Export Log: ~/.redexpert/logs/eq.export.log",
                "Import Log: ~/.redexpert/logs/eq.import.log"};

        logCombo = WidgetFactory.createComboBox(logs);
        logCombo.addItemListener(this);

        SimpleTextArea simpleTextArea = new SimpleTextArea();
        textArea = simpleTextArea.getTextAreaComponent();
        textComponent = textArea;

        reloadButton = new RolloverButton("/org/executequery/icons/Refresh16.png",
                "Reload this log file");

        trashButton = new RolloverButton("/org/executequery/icons/Delete16.png",
                "Reset this log file");

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

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                load(type);
            }

        });

    }

    public void itemStateChanged(ItemEvent e) {

        // interested in selections only
        if (e.getStateChange() == ItemEvent.DESELECTED) {

            return;
        }

        load(logCombo.getSelectedIndex());
    }

    public void setSelectedLog(int type) {

        logCombo.setSelectedIndex(type);
    }

    private void load(final int type) {

        SwingWorker worker = new SwingWorker() {

            public Object construct() {

                GUIUtilities.showWaitCursor();
                GUIUtilities.showWaitCursor(textArea);

                return logRepository().load(type);
            }

            public void finished() {

                String content = (String) get();

                setLogText(content);
            }
        };

        worker.start();
    }

    private void setLogText(final String text) {

        try {

            GUIUtils.invokeAndWait(new Runnable() {

                public void run() {

                    if (!MiscUtils.isNull(text)) {

                        textArea.setText(text);

                    } else {

                        textArea.setText("");
                    }

                    textArea.setCaretPosition(0);
                }
            });

        } catch (OutOfMemoryError e) {

            GUIUtils.scheduleGC();

            GUIUtilities.showNormalCursor();

            GUIUtilities.displayErrorMessage(
                    "Out of Memory.\nThe file is too large to open for viewing.");

        } finally {

            GUIUtilities.showNormalCursor();
            GUIUtilities.showNormalCursor(textArea);
        }

    }

    private LogRepository logRepository() {

        return (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
    }

    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        if (source == reloadButton) {

            load(logCombo.getSelectedIndex());

        } else if (source == trashButton) {

            if (resetConfirmed()) {

                int type = logCombo.getSelectedIndex();

                logRepository().reset(type);

                textArea.setText("");

            }

        }

    }

    private boolean resetConfirmed() {

        String message = "Are you sure you want to reset the selected log file?";

        return GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION;
    }

    // --------------------------------------------
    // TabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        textArea = null;
        textComponent = null;
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    // --------------------------------------------

    public String getPrintJobName() {
        return "RedXpert - system log";
    }

    public String toString() {
        return TITLE;
    }

}




