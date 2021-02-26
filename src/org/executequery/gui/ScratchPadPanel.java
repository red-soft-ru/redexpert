/*
 * ScratchPadPanel.java
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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.text.DefaultTextEditorContainer;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The Scratch Pad simple text editor.
 *
 * @author Takis Diakoumis
 */
public class ScratchPadPanel extends DefaultTextEditorContainer
        implements FocusablePanel,
        TabView,
        NamedView,
        ActionListener {

    public static final String TITLE = "Scratch Pad";
    public static final String FRAME_ICON = "ScratchPad16.png";

    private JTextArea textArea;

    private RolloverButton editorButton;
    private RolloverButton newButton;
    private RolloverButton trashButton;

    private static int count = 1;

    private PanelToolBar tools;

    /**
     * Constructs a new instance.
     */
    public ScratchPadPanel() {

        this(null);
    }

    public ScratchPadPanel(String text) {

        super(new BorderLayout());
        init();

        if (text != null) {

            textArea.setText(text);
            textArea.setCaretPosition(0);
        }

    }

    private void init() {

        editorButton = new RolloverButton("/org/executequery/icons/ScratchToEditor16.png",
                "Paste to Query Editor");
        newButton = new RolloverButton("/org/executequery/icons/NewScratchPad16.png",
                "New Scratch Pad");
        trashButton = new RolloverButton("/org/executequery/icons/Delete16.png",
                "Clear");

        editorButton.addActionListener(this);
        trashButton.addActionListener(this);
        newButton.addActionListener(this);

        SimpleTextArea simpleTextArea = new SimpleTextArea();
        textArea = simpleTextArea.getTextAreaComponent();
        textArea.setMargin(new Insets(2, 2, 2, 2));
        textComponent = textArea;

        tools = new PanelToolBar();
        tools.addButton(newButton);
        tools.addButton(editorButton);
        tools.addButton(trashButton);

        JPanel base = new JPanel(new BorderLayout());
        base.add(tools, BorderLayout.NORTH);
        base.add(simpleTextArea, BorderLayout.CENTER);

        simpleTextArea.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        add(base, BorderLayout.CENTER);

    }

    public PanelToolBar getPanelToolBar() {
        return tools;
    }

    public Component getDefaultFocusComponent() {
        return textArea;
    }

    public void focusGained() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea.requestFocusInWindow();
            }
        });
    }

    public void focusLost() {
    }

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == editorButton) {

            if (GUIUtilities.isPanelOpen(QueryEditor.TITLE)) {
                QueryEditor queryEditor =
                        (QueryEditor) GUIUtilities.getOpenFrame(QueryEditor.TITLE);
                queryEditor.setEditorText(textArea.getText());
            } else {
                GUIUtilities.addCentralPane(QueryEditor.TITLE,
                        QueryEditor.FRAME_ICON,
                        new QueryEditor(textArea.getText()),
                        null,
                        true);
            }
        } else if (obj == trashButton) {
            textArea.setText(Constants.EMPTY);
        } else if (obj == newButton) {
            GUIUtilities.addCentralPane(ScratchPadPanel.TITLE,
                    ScratchPadPanel.FRAME_ICON,
                    new ScratchPadPanel(),
                    null,
                    true);
        }

    }

    public String getPrintJobName() {
        return "RedXpert - scratch pad";
    }

    public String getDisplayName() {
        return toString();
    }

    public String toString() {
        return TITLE + " - " + (count++);
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        focusGained();
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    // --------------------------------------------

}






