package org.executequery.gui.prefs;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.SimpleValueSelectionDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.plaf.LookAndFeelDefinition;
import org.executequery.repository.LookAndFeelProperties;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author Alexey K.
 */
public class CustomLafSelectionPanel extends JPanel {

    private LookAndFeelDefinition userLaf;

    private JTextField classField;
    private JTextField libraryField;
    private JButton browseClassButton;
    private JButton browseLibraryButton;

    public CustomLafSelectionPanel() {
        super(new GridBagLayout());

        init();
        arrange();
        updateFields();
    }

    private void init() {

        classField = WidgetFactory.createTextField("classField");
        libraryField = WidgetFactory.createTextField("libPathField");

        browseClassButton = WidgetFactory.createButton(
                "browseClassButton",
                e -> browseClass(),
                Bundles.get("common.browse.button")
        );

        browseLibraryButton = WidgetFactory.createButton(
                "browseLibraryButton",
                e -> browseLibrary(),
                Bundles.get("common.browse.button")
        );

        userLaf = LookAndFeelProperties.getLookAndFeel();
        if (userLaf == null)
            restore();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth().bottomGap(5);
        mainPanel.add(new JLabel(bundleString("LibraryPath")), gbh.get());
        mainPanel.add(libraryField, gbh.nextCol().leftGap(5).setMaxWeightX().get());
        mainPanel.add(browseLibraryButton, gbh.nextCol().leftGap(0).setMinWeightX().get());
        mainPanel.add(new JLabel(bundleString("ClassName")), gbh.nextRowFirstCol().bottomGap(0).get());
        mainPanel.add(classField, gbh.nextCol().leftGap(5).setMaxWeightX().get());
        mainPanel.add(browseClassButton, gbh.nextCol().leftGap(0).setMinWeightX().get());

        // --- base ---

        gbh.fillBoth().setInsets(0, 5, 0, 5).spanY().spanX();
        add(mainPanel, gbh.get());
    }

    private void browseLibrary() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setDialogTitle(bundleString("SelectLookFeel"));
        fileChooser.setFileFilter(new FileSelector(new String[]{"jar"}, bundleString("JavaArchiveFiles")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), Bundles.getCommon("select"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        StringBuilder sb = new StringBuilder();
        File[] files = fileChooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
            sb.append(files[i].getAbsolutePath());
            if (i != files.length - 1)
                sb.append(Constants.COLON_CHAR);
        }

        userLaf.setLibraryPath(sb.toString());
        updateFields();
    }

    private void browseClass() {

        String path = libraryField.getText();
        if (MiscUtils.isNull(path)) {
            GUIUtilities.displayErrorMessage(bundleString("LookFeelLibraryIsRequired"));
            return;
        }

        String[] looks;
        try {
            GUIUtilities.showWaitCursor();
            looks = MiscUtils.findImplementingClasses("javax.swing.LookAndFeel", path, false);

        } catch (MalformedURLException e) {
            GUIUtilities.showWaitCursor();
            GUIUtilities.displayErrorMessage(bundleString("LibraryIsRequired"));
            return;

        } catch (IOException e) {
            GUIUtilities.showWaitCursor();
            GUIUtilities.displayExceptionErrorDialog(bundleString("AccessingFileOccurredError"), e);
            return;

        } finally {
            GUIUtilities.showNormalCursor();
        }

        if (looks.length == 0) {
            GUIUtilities.displayWarningMessage(bundleString("NoValidClasses"));
            return;
        }

        SimpleValueSelectionDialog dialog = new SimpleValueSelectionDialog(bundleString("SelectLookFeel"), looks);

        int result = dialog.showDialog();
        if (result == JOptionPane.OK_OPTION) {

            String value = dialog.getValue();
            if (value == null) {
                GUIUtilities.displayErrorMessage(bundleString("SelectLookFeelFromList"));
                return;
            }

            userLaf.setClassName(value);
            updateFields();
        }
    }

    private void updateFields() {
        libraryField.setText(userLaf.getLibraryPath());
        classField.setText(userLaf.getClassName());
    }

    public void save() {
        LookAndFeelProperties.saveLookAndFeels(new LookAndFeelDefinition[]{userLaf});
    }

    public void restore() {
        userLaf = new LookAndFeelDefinition("user-defined-laf");
        updateFields();
    }

    private static String bundleString(String key) {
        return Bundles.get(CustomLafSelectionPanel.class, key);
    }

}
