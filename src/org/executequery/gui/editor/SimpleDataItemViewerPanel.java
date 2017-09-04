/*
 * SimpleDataItemViewerPanel.java
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

package org.executequery.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultActionButtonsPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.SimpleRecordDataItem;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.util.FileUtils;

public class SimpleDataItemViewerPanel extends DefaultActionButtonsPanel {

    private JTextArea textArea;

    private final SimpleRecordDataItem recordDataItem;

    private final ActionContainer parent;

    public SimpleDataItemViewerPanel(ActionContainer parent, SimpleRecordDataItem recordDataItem) {

        this.parent = parent;
        this.recordDataItem = recordDataItem;
    
        try {
            
            init();
        
        } catch (Exception e) {
            
            e.printStackTrace();
        
        } finally {
            
            textArea.requestFocus();
        }
        
    }

    private void init() {

        Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(emptyBorder);
        
        textArea = createTextArea();
        textPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Text", textPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(400, 300));
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JLabel descriptionLabel = new JLabel(formatDescriptionString());
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 0));

        contentPanel.add(descriptionLabel, BorderLayout.SOUTH);
        
        JButton closeButton = create(Bundles.get("common.close.button"), "close");
        JButton copyButton = create("Copy to Clipboard", "copy");
        JButton saveButton = create(Bundles.get("common.save-as.button"), "save");
        
        addActionButton(copyButton);        
        addActionButton(saveButton);
        addActionButton(closeButton);
        
        setPreferredSize(new Dimension(500, 400));

        addContentPanel(contentPanel);
    }

    private JButton create(String text, String actionCommand) {
        
        JButton button = WidgetFactory.createButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        
        return button;
    }
    
    private String formatDescriptionString() {
        
        StringBuilder sb = new StringBuilder();

        sb.append("Data Type: ").append(recordDataItem.getDataTypeName());
        sb.append("   Size: ").append(recordDataItem.length());
        
        return sb.toString();
    }
    
    private JTextArea createTextArea() {
        
        JTextArea textArea = new JTextArea(recordDataItem.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        textArea.setMargin(new Insets(4, 4, 4, 4));
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        
        return textArea;
    }

    public void save() {
        
        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showSaveDialog((JDialog) parent);
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }
        
        if (fileChooser.getSelectedFile() != null) {
            
            try {

                GUIUtilities.showWaitCursor();
                
                FileUtils.writeFile(fileChooser.getSelectedFile(), 
                        recordDataItem.toString());
                
            } catch (IOException e) {
                
                if (Log.isDebugEnabled()) {
                    
                    Log.debug("Error writing record data item value to file", e);
                }

                GUIUtilities.displayErrorMessage(
                        "Error writing record data item to file:\n" + e.getMessage());
                return;
                
            } finally {
                
                GUIUtilities.showNormalCursor();
            }

        }
        
        close();
    }
    
    public void copy() {

        GUIUtilities.copyToClipBoard(recordDataItem.toString());
        close();
    }
    
    public void close() {
        
        parent.finished();
    }

}






