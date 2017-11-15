/*
 * PropertiesLookPlugins.java
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

package org.executequery.gui.prefs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.SimpleValueSelectionDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.plaf.LookAndFeelDefinition;
import org.executequery.repository.LookAndFeelProperties;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 */
public class PropertiesLookPlugins extends JPanel
                                   implements ListSelectionListener,
                                              UserPreferenceFunction,
                                              ActionListener,
                                              KeyListener,
                                              FocusListener {
    
    private JList list;
    
    private JTextField nameField;
    private JTextField libPathField;
    private JTextField classField;
    private JTextField themeField;
    
    private JCheckBox skinCheck;
    private JCheckBox installedCheck;
    
    private Vector lfdv;

    private JButton findClassButton;
    private JButton libBrowseButton;
    private JButton themeBrowseButton;
    
    private JButton newButton;
    private JButton deleteButton;
    
    private JLabel themeLabel;
    private JLabel nameLabel;
    private JLabel libLabel;
    private JLabel classLabel;

    
    public PropertiesLookPlugins() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createLineBorder(
                GUIUtilities.getDefaultBorderColour()));

        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void jbInit() {
        
        LookAndFeelDefinition[] lfda = LookAndFeelProperties.getLookAndFeelArray();
        lfdv = new Vector();
        
        if (lfda != null && lfda.length > 0) {
            
            for (int i = 0; i < lfda.length; i++)
                lfdv.add(lfda[i]);
            
        }
        
        list = new JList(lfdv);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);       
        list.addListSelectionListener(this);
        
        nameField = WidgetFactory.createTextField();
        libPathField = WidgetFactory.createTextField();
        classField = WidgetFactory.createTextField();
        themeField = WidgetFactory.createTextField();
        
        findClassButton = new JButton("Find");
        libBrowseButton = new JButton("Browse");
        themeBrowseButton = new JButton("Browse");

        Insets btnInsets = new Insets(2,2,2,2);
        libBrowseButton.setMargin(btnInsets);
        themeBrowseButton.setMargin(btnInsets);
        findClassButton.setMargin(btnInsets);
        
        skinCheck = new JCheckBox("Skin Look and Feel");
        installedCheck = new JCheckBox("Install");
        
        newButton = new JButton("New");
        deleteButton = new JButton("Delete");
        
        newButton.addActionListener(this);
        deleteButton.addActionListener(this);
        findClassButton.addActionListener(this);
        libBrowseButton.addActionListener(this);
        themeBrowseButton.addActionListener(this);
        skinCheck.addActionListener(this);
        installedCheck.addActionListener(this);
        
        Dimension btnDim = new Dimension(60, 25);
        findClassButton.setPreferredSize(btnDim);
        libBrowseButton.setPreferredSize(btnDim);
        themeBrowseButton.setPreferredSize(btnDim);        
        
        nameField.addFocusListener(this);
        
        nameField.addKeyListener(this);
        libPathField.addKeyListener(this);
        classField.addKeyListener(this);
        themeField.addKeyListener(this);
        
        themeLabel = new JLabel("Theme Pack:");
        nameLabel = new JLabel("Name:");
        classLabel = new JLabel("Class Name:");
        libLabel = new JLabel("Library Path:");

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets.right = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        buttons.add(newButton, gbc);
        gbc.gridx++;
        gbc.insets.right = 0;
        buttons.add(deleteButton, gbc);
        
        JPanel panel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets.bottom = 10;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Look and Feel Plugins"), gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(list), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttons, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(installedCheck, gbc);
        gbc.gridx++;
        gbc.insets.left = 5;
        panel.add(skinCheck, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        panel.add(nameLabel, gbc);
        gbc.gridx++;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(nameField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.right = 5;
        gbc.gridwidth = 1;
        gbc.insets.top = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(libLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(libPathField, gbc);
        gbc.gridx++;
        gbc.insets.right = 0;
        gbc.weightx = 0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(libBrowseButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.right = 5;
        gbc.insets.top = 2;
        panel.add(classLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(classField, gbc);
        gbc.gridx++;
        gbc.insets.right = 0;
        gbc.weightx = 0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(findClassButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 2;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(themeLabel, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(themeField, gbc);
        gbc.gridx++;
        gbc.insets.top = 0;
        gbc.insets.right = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(themeBrowseButton, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(new JLabel("Note: Activate this feature from the 'Display' node."), gbc);

        add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                          GridBagConstraints.NORTHWEST, 
                                          GridBagConstraints.BOTH,
                                          new Insets(5, 5, 0, 5), 0, 0));

        if (lfdv.size() > 0) {
            list.setSelectedIndex(0);
        } else {
            enableAllFields(false);
            enableSkinFields(false);
        }
        
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {}
    
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {}
    
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == newButton)
            addNewLookAndFeel();
        else if (obj == deleteButton)
            deleteLookAndFeel();
        else if (obj == skinCheck)
            skinCheckedAction();
        else if (obj == installedCheck)
            installedCheckedAction();
        else if (obj == findClassButton)
            findClassNames();
        else
            browseButton_actionPerformed(obj);
    }

    private void findClassNames() {
        String paths = libPathField.getText();

        if (MiscUtils.isNull(paths)) {
            GUIUtilities.displayErrorMessage(
                    "A valid path to the look and feel library is required");
            return;
        }

        String[] looks = null;
        try {
            GUIUtilities.showWaitCursor();
            looks = MiscUtils.findImplementingClasses(
                                            "javax.swing.LookAndFeel", paths, false);
        } catch (MalformedURLException urlExc) {
            GUIUtilities.showNormalCursor();
            GUIUtilities.displayErrorMessage(
                    "A valid path to the library is required");
            return;
        } catch (IOException ioExc) {
            GUIUtilities.showNormalCursor();
            StringBuffer sb = new StringBuffer();
            sb.append("An error occured accessing the specified file.").
               append("\n\nThe system returned:\n").
               append(ioExc.getMessage());
            GUIUtilities.displayExceptionErrorDialog(sb.toString(), ioExc);
            return;
        } finally {
            GUIUtilities.showNormalCursor();
        }

        if (looks == null || looks.length == 0) {
            GUIUtilities.displayWarningMessage(
                    "No valid classes of type javax.swing.LookAndFeel\n"+
                    "were found in the specified resource");
            return;
        }

        int result = -1;
        String value = null;
        while (true) {
            SimpleValueSelectionDialog dialog = 
                    new SimpleValueSelectionDialog("Select Look and Feel", looks);
            result = dialog.showDialog();

            if (result == JOptionPane.OK_OPTION) {
                value = dialog.getValue();

                if (value == null) {
                    GUIUtilities.displayErrorMessage(
                            "You must select a look and feel from the list");
                } else {
                    classField.setText(value);
                    break;
                }

            } else {
                break;
            }

        }

        LookAndFeelDefinition lfd = (LookAndFeelDefinition)
                        lfdv.elementAt(list.getSelectedIndex());
        lfd.setClassName(value);
    }
    
    private void skinCheckedAction() {
        enableSkinFields(skinCheck.isSelected());
        keyReleased(null);
    }
    
    private void installedCheckedAction() {
        int index = list.getSelectedIndex();
        int v_size = lfdv.size();
        LookAndFeelDefinition lfd;
        
        boolean check = installedCheck.isSelected();
        
        for (int i = 0; i < v_size; i++) {
            lfd = (LookAndFeelDefinition)lfdv.elementAt(i);
            
            if (i == index)
                lfd.setInstalled(check);
            else
                lfd.setInstalled(!check);
            
        }
        
    }
    
    private void addNewLookAndFeel() {
        enableAllFields(true);
        list.removeListSelectionListener(this);
        
        LookAndFeelDefinition lfd = new LookAndFeelDefinition("New Look and Feel Plugin");
        lfdv.add(lfd);
        list.setListData(lfdv);
        list.setSelectedValue(lfd, true);
        valueChanged(null);
        
        list.addListSelectionListener(this);
        setFocusComponent();
    }
    
    private void enableAllFields(boolean enable) {
        list.setEnabled(enable);
        enableField(nameField, nameLabel, enable);
        enableField(classField, classLabel, enable);
        enableField(libPathField, libLabel, enable);
        findClassButton.setEnabled(enable);
        libBrowseButton.setEnabled(enable);
        installedCheck.setEnabled(enable);
        skinCheck.setEnabled(enable);
    }
    
    private void enableField(JTextField field, JLabel label, boolean enable) {
        field.setText(Constants.EMPTY);
        field.setEnabled(enable);
        label.setEnabled(enable);
        field.setEnabled(enable);
        field.setOpaque(enable);
    }
    
    private String getSkinLibraryPath() {
        int index = list.getSelectedIndex();
        int v_size = lfdv.size();
        LookAndFeelDefinition lfd;
        String path = null;
        
        for (int i = 0; i < v_size; i++) {
            lfd = (LookAndFeelDefinition)lfdv.elementAt(i);
            
            if (i == index)
                continue;
            
            if (lfd.isSkinLookAndFeel()) {
                path = lfd.getLibraryPath();
                break;
            }
            
        }
        
        if (path != null && path.length() > 0)
            return path;
        else
            return Constants.EMPTY;
        
    }
    
    private void deleteLookAndFeel() {
        int v_size = lfdv.size();
        
        if (v_size == 0) {
            return;
        }
        
        int yesNo = GUIUtilities.displayConfirmCancelDialog(
            "Are you sure you want to delete the selected look and feel plugin?");
        
        if (yesNo != JOptionPane.YES_OPTION) {
            return;
        }
        
        int position = list.getSelectedIndex();
        
        lfdv.removeElementAt(position);
        list.removeListSelectionListener(this);
        list.setListData(lfdv);
        list.addListSelectionListener(this);
        
        v_size = lfdv.size();
        
        if (v_size != 0) {
            if (position == v_size)
                list.setSelectedIndex(position - 1);
            else
                list.setSelectedIndex(position);
        } else {
            enableAllFields(false);
            enableSkinFields(false);
        }
        
    }
    
    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        LookAndFeelDefinition lfd = (LookAndFeelDefinition)
                        lfdv.elementAt(list.getSelectedIndex());
        
        if (e == null) {
            lfd.setIsSkinLookAndFeel(skinCheck.isSelected() ? 1 : 0);
            return;
        }
        
        Object obj = e.getSource();
        
        if (obj == nameField)
            lfd.setName(nameField.getText());
        else if (obj == classField)
            lfd.setClassName(classField.getText());
        else if (obj == libPathField)
            lfd.setLibraryPath(libPathField.getText());
        else if (obj == themeField)
            lfd.setThemePack(themeField.getText());
        
    }
    
    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
        list.removeListSelectionListener(this);
        int position = list.getSelectedIndex();
        
        if(lfdv.size() == 0)
            return;
        
        LookAndFeelDefinition lfd = (LookAndFeelDefinition)lfdv.elementAt(position);
        lfd.setName(nameField.getText());
        
        lfdv.setElementAt(lfd, position);
        list.setListData(lfdv);
        
        list.setSelectedIndex(position);
        list.addListSelectionListener(this);
    }
    
    public void valueChanged(ListSelectionEvent e) {
        int index = list.getSelectedIndex();
        
        if (index == -1 || lfdv.size() == 0)
            return;
        
        LookAndFeelDefinition lfd = (LookAndFeelDefinition)lfdv.elementAt(index);
        
        skinCheck.removeActionListener(this);
        
        boolean isSkin = lfd.isSkinLookAndFeel();
        skinCheck.setSelected(isSkin);
        enableSkinFields(isSkin);
        
        nameField.setText(lfd.getName());
        libPathField.setText(lfd.getLibraryPath());
        classField.setText(lfd.getClassName());
        themeField.setText(lfd.getThemePack());
        installedCheck.setSelected(lfd.isInstalled());
        
        libPathField.setCaretPosition(0);
        classField.setCaretPosition(0);
        themeField.setCaretPosition(0);
        
        skinCheck.addActionListener(this);
        
        setFocusComponent();
    }
    
    public void setFocusComponent() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                nameField.requestFocus();
                nameField.selectAll();
            }
        });
    }
    
    private void enableSkinFields(boolean enable) {
        themeField.setEnabled(enable);
        themeLabel.setEnabled(enable);
        themeBrowseButton.setEnabled(enable);
        themeField.setOpaque(enable);
        
        if (enable) {
            String clazz = "com.l2fprod.gui.plaf.skin.SkinLookAndFeel";
            String path = getSkinLibraryPath();
            
            classField.setText(clazz);
            libPathField.setText(path);
            libPathField.setCaretPosition(0);
            classField.setCaretPosition(0);
            
            LookAndFeelDefinition lfd = (LookAndFeelDefinition)
            lfdv.elementAt(list.getSelectedIndex());
            lfd.setClassName(clazz);
            lfd.setLibraryPath(path);
            
        }
        
    }
    
    private void browseButton_actionPerformed(Object obj) {
        FileSelector fs = null;
        JTextField field = null;
        boolean isTheme = false;
        
        if (obj == themeBrowseButton) {
            fs = new FileSelector(new String[] {"zip"}, "ZIP Archive Files");
            field = themeField;
            isTheme = true;
        } else {
            fs = new FileSelector(new String[] {"jar"}, "Java Archive Files");
            field = libPathField;
        }
        
        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(fs);
        
        fileChooser.setDialogTitle("Select Look & Feel Plugin Library...");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        
        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        
        if (result == JFileChooser.CANCEL_OPTION)
            return;
        
        File[] files = fileChooser.getSelectedFiles();
        
        char COLON = ';';
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i].getAbsolutePath());
            if (i != files.length - 1)
                sb.append(COLON);
        }
        
        files = null;
        
        String path = sb.toString();
        field.setText(path);
        sb = null;
        
        field.setCaretPosition(0);
        
        LookAndFeelDefinition lfd = (LookAndFeelDefinition)
        lfdv.elementAt(list.getSelectedIndex());
        
        if (isTheme)
            lfd.setThemePack(path);
        else
            lfd.setLibraryPath(path);
        
    }
    
    public void save() {
        int v_size = lfdv.size();
        LookAndFeelDefinition[] lfda = new LookAndFeelDefinition[v_size];
        
        for (int i = 0; i < v_size; i++) {
            lfda[i] = (LookAndFeelDefinition)lfdv.elementAt(i);
        }
        
        LookAndFeelProperties.saveLookAndFeels(lfda);
    }
    
    public void restoreDefaults() {}
    
}


















