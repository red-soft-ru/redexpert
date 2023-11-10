/*
 * ParseDateSelectionPanel.java
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

package org.executequery.gui.importexport;

import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.help.HelpPopupBase;
import org.executequery.log.Log;
import org.underworldlabs.swing.ComponentTitledPanel;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * @author Takis Diakoumis
 */
public class ParseDateSelectionPanel extends ComponentTitledPanel
        implements ItemListener,
        ActionListener {

    /**
     * the parse check box
     */
    private JCheckBox parseDatesCheck;

    /**
     * the formats editable combo
     */
    private JComboBox dateFormats;

    /**
     * the help popup button
     */
    private JButton helpButton;

    /**
     * the date format label
     */
    private JLabel dateFormatLabel;

    /**
     * the parent process
     */
    private ImportExportDataProcess importExportProcess;

    /**
     * Creates a new instance of ParseDateSelectionPanel
     */
    public ParseDateSelectionPanel(ImportExportDataProcess importExportProcess) {

        String checkBoxLabel = "Parse Date/Time Values";

        if (importExportProcess.isExport()) {

            checkBoxLabel = "Date/Time Value Pattern";
        }

        parseDatesCheck = new JCheckBox(checkBoxLabel);
        setTitleComponent(parseDatesCheck);
        parseDatesCheck.addItemListener(this);

        this.importExportProcess = importExportProcess;

        dateFormats = WidgetFactory.createComboBox("dateFormats", loadDatePatterns());
        dateFormats.setEditable(true);

        JPanel panel = getContentPane();
        panel.setLayout(new GridBagLayout());

        dateFormatLabel = new JLabel("Date format:");

        // disable all on startup
        dateFormats.setEnabled(false);
        dateFormats.setOpaque(false);
        dateFormatLabel.setEnabled(false);

        helpButton = new RolloverButton(
                "/org/executequery/icons/TipOfTheDay16.svg", "Date/time format masks");
        helpButton.addActionListener(this);
        helpButton.setEnabled(false);
        helpButton.addMouseListener(new HelpPopupAdapter());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(10, 5, 10, 5);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy = 0;
        gbc.gridx = 0;
        panel.add(dateFormatLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.insets.right = 2;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(dateFormats, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(helpButton, gbc);
    }

    private String[] loadDatePatterns() {

        try {

            String datePatterns = FileUtils.loadResource(
                    "org/executequery/gui/importexport/date.formats");

            return datePatterns.split("\n|\r");

        } catch (IOException e) {

            Log.error("Error loading predefined date format masks.", e);
        }
        return new String[0];
    }

    /**
     * Override to enable/disable the check box and associated fields.
     */
    public void setEnabled(boolean enabled) {

        parseDatesCheck.setEnabled(enabled);

        if (parseDatesCheck.isSelected()) {

            dateFormats.setOpaque(enabled);
            dateFormats.setEnabled(enabled);
            dateFormatLabel.setEnabled(enabled);
            helpButton.setEnabled(enabled);
        }

        super.setEnabled(enabled);
    }

    /**
     * Returns whether to parse date values using the format
     * selected/specified.
     *
     * @return true | false
     */
    public boolean parseDates() {
        return parseDatesCheck.isSelected();
    }

    /**
     * Returns the selected/specified date format to use for parsing
     * and formatting date values or NULL if the parse dates check
     * box has not been selected.
     *
     * @return the date format
     */
    public String getDateFormat() {

        if (parseDates()) {

            return dateFormats.getEditor().getItem().toString();
        }

        return null;
    }

    public void actionPerformed(ActionEvent e) {
        //new DateFormatDialog();
    }

    /**
     * Invoked when the parse dates checkbox has been selected/deselected
     * to enable/disable the date format combo box.
     */
    public void itemStateChanged(ItemEvent e) {
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        dateFormats.setEnabled(selected);
        dateFormats.setOpaque(selected);
        dateFormatLabel.setEnabled(selected);
        helpButton.setEnabled(selected);
    }

    class HelpPopupAdapter extends MouseAdapter {

        public void mouseReleased(MouseEvent e) {

            new HelpPopupBase(
                    "Date Format Masks",
                    new DateFormatDialog(),
                    importExportProcess.getDialog(),
                    e);

        }
    }


    class DateFormatDialog extends JPanel {

        public DateFormatDialog() {

            super(new BorderLayout());

            try {

                String path = "/org/executequery/gui/importexport/date-masks.html";
                JEditorPane textPane = new JEditorPane(getClass().getResource(path));
                textPane.setEditable(false);
                setPreferredSize(new Dimension(480, 300));
                add(new JScrollPane(textPane), BorderLayout.CENTER);

            } catch (IOException e) {

                String message = "Error loading date mask help file.";
                Log.error(message, e);
                GUIUtilities.displayExceptionErrorDialog(
                        message + "\n" + e.getMessage(), e);
            }
        }

    }

}











