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
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.toolbar.PanelToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

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

    public static final String TITLE = bundleString("title");
    public static final String FRAME_ICON = "ScratchPad16.png";

    private static int count = 1;

    private PanelToolBar toolbar;
    private SimpleTextArea textArea;

    private RolloverButton newScratchPadButton;
    private RolloverButton pasteToEditorButton;
    private RolloverButton clearButton;
    private RolloverButton openButton;
    private RolloverButton saveButton;
    private RolloverButton printButton;

    public ScratchPadPanel() {
        this(null);
    }

    public ScratchPadPanel(String text) {
        super(new BorderLayout());

        init();
        arrange();
        setText(text);
    }

    private void init() {

        newScratchPadButton = WidgetFactory.createRolloverButton(
                "newScratchPadButton",
                bundleString("new"),
                "NewScratchPad16.png",
                this
        );

        pasteToEditorButton = WidgetFactory.createRolloverButton(
                "pasteToEditorButton",
                bundleString("paste"),
                "ScratchToEditor16.png",
                this
        );

        clearButton = WidgetFactory.createRolloverButton(
                "clearButton",
                bundleString("clear"),
                "Delete16.png",
                this
        );

        openButton = WidgetFactory.createRolloverButton(
                "openButton",
                ActionBuilder.get("open-command"),
                Bundles.get("action.open-command")
        );

        saveButton = WidgetFactory.createRolloverButton(
                "saveButton",
                ActionBuilder.get("save-command"),
                Bundles.get("action.save-command")
        );

        printButton = WidgetFactory.createRolloverButton(
                "printButton",
                ActionBuilder.get("print-command"),
                Bundles.get("action.print-command")
        );

        textArea = new SimpleTextArea();
        textArea.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));
        textArea.getTextAreaComponent().setMargin(new Insets(2, 2, 2, 2));

        textComponent = getTextArea();
    }

    private void arrange() {

        // --- toolbar ---

        toolbar = new PanelToolBar();
        toolbar.addButton(newScratchPadButton);
        toolbar.addButton(pasteToEditorButton);
        toolbar.addButton(clearButton);
        toolbar.addButton(openButton);
        toolbar.addButton(saveButton);
        toolbar.addButton(printButton);

        // --- main panel ---

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(textArea, BorderLayout.CENTER);

        // --- base ---

        add(mainPanel, BorderLayout.CENTER);
    }

    public PanelToolBar getToolbar() {
        return toolbar;
    }

    private JTextArea getTextArea() {
        return (JTextArea) getDefaultFocusComponent();
    }

    private void setText(String text) {
        if (text != null) {
            getTextArea().setText(text);
            getTextArea().setCaretPosition(0);
        }
    }

    // --- ActionListener impl ---

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (Objects.equals(source, newScratchPadButton)) {
            GUIUtilities.addCentralPane(
                    ScratchPadPanel.TITLE,
                    ScratchPadPanel.FRAME_ICON,
                    new ScratchPadPanel(),
                    null,
                    true
            );

        } else if (Objects.equals(source, pasteToEditorButton)) {

            if (GUIUtilities.isPanelOpen(QueryEditor.TITLE)) {
                QueryEditor queryEditor = (QueryEditor) GUIUtilities.getOpenFrame(QueryEditor.TITLE);
                queryEditor.setEditorText(getTextArea().getText());

            } else {
                GUIUtilities.addCentralPane(
                        QueryEditor.TITLE,
                        QueryEditor.FRAME_ICON,
                        new QueryEditor(getTextArea().getText()),
                        null,
                        true
                );
            }

        } else if (Objects.equals(source, clearButton)) {
            getTextArea().setText(Constants.EMPTY);
        }
    }

    // --- TabView impl ---

    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        focusGained();
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    // --- FocusablePanel impl ---

    @Override
    public Component getDefaultFocusComponent() {
        return textArea.getTextAreaComponent();
    }

    @Override
    public void focusGained() {
        SwingUtilities.invokeLater(() -> getDefaultFocusComponent().requestFocusInWindow());
    }

    @Override
    public void focusLost() {
    }

    // ---

    @Override
    public String getPrintJobName() {
        return bundleString("JobName");
    }

    @Override
    public String getDisplayName() {
        return toString();
    }

    @Override
    public String toString() {
        return TITLE + " - " + (count++);
    }

    private static String bundleString(String key) {
        return Bundles.get(ScratchPadPanel.class, key);
    }

}
