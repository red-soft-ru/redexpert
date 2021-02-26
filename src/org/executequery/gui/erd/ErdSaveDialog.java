/*
 * ErdSaveDialog.java
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

package org.executequery.gui.erd;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.event.DefaultFileIOEvent;
import org.executequery.event.FileIOEvent;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.WidgetFactory;
import org.executequery.imageio.*;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Takis Diakoumis
 */
public class ErdSaveDialog extends AbstractBaseDialog
        implements ActionListener,
        KeyListener,
        ChangeListener {

    /**
     * Indicator for JPEG format
     */
    private static final int JPEG_FORMAT = 1;
    /**
     * Indicator for GIF format
     */
    private static final int GIF_FORMAT = 2;
    /**
     * Indicator for SVG format
     */
    private static final int SVG_FORMAT = 4;
    /**
     * Indicator for SVG format
     */
    private static final int PNG_FORMAT = 3;
    /**
     * Indicator for EQ format
     */
    private static final int EQ_FORMAT = 0;
    /**
     * Indicator for transparent background
     */
    private static final int TRANSPARENT_BACKGROUND = 0;
    /**
     * Indicator for white background
     */
    private static final int WHITE_BACKGROUND = 1;

    /**
     * The ERD parent panel
     */
    private ErdViewerPanel parent;

    /**
     * The open file's path - if any
     */
    private String openPath;

    /**
     * Image type combo-box
     */
    private JComboBox imageTypeCombo;

    /**
     * The quality text box
     */
    private NumberTextField qualityTextField;

    /**
     * The quality combo-box
     */
    private JComboBox qualityCombo;

    /**
     * The quality label
     */
    private JLabel qualityLabel;

    /**
     * The background label
     */
    private JLabel backgroundLabel;

    /**
     * The background combo-box
     */
    private JComboBox backgroundCombo;

    /**
     * The quality slider
     */
    private JSlider qualitySlider;

    /**
     * The path field
     */
    private JTextField pathField;

    private JCheckBox svgFontCheckbox;

    /**
     * The default file to save to
     */
    private File defaultFile;
    private int savedResult;

    private ErdSaveDialog() {
        super(GUIUtilities.getParentFrame(), "Save ERD", true);

        savedResult = -1;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ErdSaveDialog(ErdViewerPanel parent) {
        this();
        this.parent = parent;
        display();
    }

    public ErdSaveDialog(ErdViewerPanel parent, File defaultFile) {
        this();
        this.parent = parent;
        this.defaultFile = defaultFile;
        display();
    }

    public ErdSaveDialog(ErdViewerPanel parent, String openPath) {
        this();
        this.parent = parent;
        this.openPath = openPath;
        display();
    }

    private void display() {
        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    private void jbInit() throws Exception {
        qualityLabel = new JLabel("Quality:");
        qualityTextField = new NumberTextField(2);
        qualityTextField.setValue(8);
        qualityTextField.addKeyListener(this);

        qualityCombo = WidgetFactory.createComboBox(new String[]{"Low", "Medium", "High", "Maximum"});
        qualityCombo.setSelectedIndex(2);

        Dimension fieldDim = new Dimension(50, 20);
        qualityTextField.setPreferredSize(fieldDim);

        Dimension comboDim = new Dimension(150, 23);
        qualityCombo.setPreferredSize(comboDim);

        qualitySlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 8);
        qualitySlider.setMajorTickSpacing(5);
        qualitySlider.setMajorTickSpacing(1);
        qualitySlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        qualitySlider.setPreferredSize(new Dimension(300, 30));

        JPanel qualityPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        qualityPanel.add(qualityLabel, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.insets.top = 6;
        qualityPanel.add(qualityTextField, gbc);
        gbc.gridx = 2;
        gbc.insets.top = 5;
        qualityPanel.add(qualityCombo, gbc);
        gbc.insets.left = 5;
        gbc.insets.top = 0;
        gbc.insets.bottom = 0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        qualityPanel.add(qualitySlider, gbc);

        qualityPanel.setBorder(BorderFactory.createTitledBorder("JPEG Options"));

        backgroundLabel = new JLabel("Background:");
        backgroundCombo = WidgetFactory.createComboBox(new String[]{"Transparent", "White"});
        backgroundCombo.setPreferredSize(comboDim);
        backgroundCombo.setSelectedIndex(0);

        JPanel gifPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 3));
        gifPanel.add(backgroundLabel);
        gifPanel.add(backgroundCombo);

        gifPanel.setBorder(BorderFactory.createTitledBorder("GIF/PNG Options"));

        svgFontCheckbox = new JCheckBox("Render fonts as images");
        JPanel svgPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 3));
        svgPanel.add(svgFontCheckbox);
        svgPanel.setBorder(BorderFactory.createTitledBorder("SVG Options"));

        imageTypeCombo = WidgetFactory.createComboBox(new String[]{
                "RedXpert ERD", "JPEG", "GIF", "PNG", "SVG"});
        imageTypeCombo.setPreferredSize(comboDim);

        pathField = WidgetFactory.createTextField();
        pathField.setPreferredSize(fieldDim);

        JButton browseButton = new JButton("Browse");

        JPanel base = new JPanel(new GridBagLayout());
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.top = 7;
        gbc.insets.bottom = 5;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        base.add(new JLabel("Format:"), gbc);
        gbc.insets.top = 5;
        gbc.gridx = 1;
        base.add(imageTypeCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(qualityPanel, gbc);
        gbc.insets.top = 0;
        gbc.gridy++;
        base.add(gifPanel, gbc);
        gbc.gridy++;
        base.add(svgPanel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        base.add(new JLabel("Path:"), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 0;
        gbc.insets.top = 8;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        base.add(pathField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(browseButton, gbc);

        base.setBorder(BorderFactory.createEtchedBorder());
        base.setPreferredSize(new Dimension(400, 300));

        JButton saveButton = new DefaultPanelButton(Bundles.get("common.save.button"));
        saveButton.setActionCommand("Save");
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        cancelButton.setActionCommand("Cancel");

        cancelButton.addActionListener(this);
        saveButton.addActionListener(this);
        browseButton.addActionListener(this);
        imageTypeCombo.addActionListener(this);
        qualityCombo.addActionListener(this);

        qualitySlider.addChangeListener(this);

        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());

        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        c.add(base, gbc);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.insets.top = 0;
        c.add(saveButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        c.add(cancelButton, gbc);

        enableOptionsPanels(0);

        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    /**
     * <p>Displays the file chooser dialog.
     */
    private void showFileChooser() {
        String fileDescription = null;
        String fileExtension = null;

        int imageType = imageTypeCombo.getSelectedIndex();

        if (imageType == EQ_FORMAT) {
            fileDescription = "RedXpert ERD Files";
            fileExtension = "eqd";
        } else if (imageType == JPEG_FORMAT) {
            fileDescription = "JPEG Files";
            fileExtension = "jpeg";
        } else if (imageType == SVG_FORMAT) {
            fileDescription = "SVG Files";
            fileExtension = "svg";
        } else if (imageType == PNG_FORMAT) {
            fileDescription = "PNG Files";
            fileExtension = "png";
        } else {
            fileDescription = "GIF Files";
            fileExtension = "gif";
        }

        FileSelector fs = new FileSelector(new String[]{fileExtension}, fileDescription);

        FileChooserDialog fileChooser = null;

        if (openPath != null) {
            fileChooser = new FileChooserDialog(openPath);
        } else {
            fileChooser = new FileChooserDialog();
        }

        if (defaultFile != null && imageType == EQ_FORMAT) {
            fileChooser.setSelectedFile(defaultFile);
        }

        fileChooser.setDialogTitle("Select File...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileFilter(fs);
        int result = fileChooser.showDialog(GUIUtilities.getParentFrame(), "Select");

        String filePath = null;

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();

        if (file != null) {
            if (file.exists()) {
                int _result = GUIUtilities.displayConfirmCancelDialog(
                        "Overwrite existing file?");

                if (_result == JOptionPane.CANCEL_OPTION) {
                    return;
                } else if (_result == JOptionPane.NO_OPTION) {
                    showFileChooser();
                }

            }
            filePath = fileChooser.getSelectedFile().getAbsolutePath();
        }

        fileExtension = "." + fileExtension;
        if (!filePath.endsWith(fileExtension)) {
            filePath += fileExtension;
        }

        pathField.setText(filePath);
    }

    /**
     * <p>Saves the image in the specified format.
     */
    private String save(String path, int fileFormat) {
        File file = new File(path);

        if (fileFormat == EQ_FORMAT) {
            return saveApplicationFileFormat(file);
        }

        Dimension extents = parent.getMaxImageExtents();
        int width = (int) extents.getWidth();
        int height = (int) extents.getHeight();

        int imageType = 0;
        int bgType = backgroundCombo.getSelectedIndex();

        FileOutputStream fos = null;

        if ((fileFormat == GIF_FORMAT || fileFormat == PNG_FORMAT)
                && bgType == TRANSPARENT_BACKGROUND) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else if (fileFormat == SVG_FORMAT) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            imageType = BufferedImage.TYPE_INT_RGB;
        }

        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D g2d = image.createGraphics();

        if (fileFormat == JPEG_FORMAT || bgType == WHITE_BACKGROUND) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
        }

        paintImage(g2d);

        try {

            ImageWriter imageWriter = null;
            ImageWriterInfo imageWriterInfo = null;

            ImageWriterFactory factory = new DefaultImageWriterFactory();

            if (fileFormat == PNG_FORMAT) {

                imageWriter = factory.createImageWriterForPngImages();
                imageWriterInfo = new PngImageWriterInfo(image, file);

            } else if (fileFormat == SVG_FORMAT) {

                imageWriter = factory.createImageWriterForSvgImages();
                imageWriterInfo = new SvgImageWriterInfo(image, file,
                        svgFontCheckbox.isSelected());

            } else if (fileFormat == JPEG_FORMAT) {

                imageWriter = factory.createImageWriterForJpegImages();
                imageWriterInfo = new JpegImageWriterInfo(image, file,
                        qualitySlider.getValue());

            } else if (fileFormat == GIF_FORMAT) {

                imageWriter = factory.createImageWriterForGifImages();
                imageWriterInfo = new GifImageWriterInfo(image, file);
            }

            imageWriter.write(imageWriterInfo);

            GUIUtilities.scheduleGC();

            savedResult = SaveFunction.SAVE_COMPLETE;
            return "done";

        } catch (Exception e) {
            savedResult = SaveFunction.SAVE_FAILED;
            GUIUtilities.displayExceptionErrorDialog("An error occured saving to file:\n" +
                    e.getMessage(), e);
            return "failed";
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }

    }

    private void paintImage(Graphics2D g) {
        // set the highest quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        parent.resetAllTableJoins();
        parent.getDependenciesPanel().drawDependencies(g);
        ErdTable[] tablesArray = parent.getAllComponentsArray();

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].setSelected(false);
            tablesArray[i].drawTable(g, tablesArray[i].getX(), tablesArray[i].getY());
        }

        ErdTitlePanel title = parent.getTitlePanel();

        if (title != null) {
            title.setSelected(false);
            title.drawTitlePanel(g, title.getX(), title.getY());
        }
    }

    public int getSaved() {
        return savedResult;
    }

    /**
     * <p>Removes the listeners within the JPEG options
     * components so changes do not propagate.
     */
    private void removeListeners() {
        qualityTextField.removeKeyListener(this);
        qualityCombo.removeActionListener(this);
        qualitySlider.removeChangeListener(this);
    }

    /**
     * <p>Adds the listeners within the JPEG options components.
     */
    private void addListeners() {
        qualityTextField.addKeyListener(this);
        qualityCombo.addActionListener(this);
        qualitySlider.addChangeListener(this);
    }

    /**
     * <p>Processes a change in the sliders value.
     *
     * @param the event object
     */
    public void stateChanged(ChangeEvent e) {
        removeListeners();

        int value = qualitySlider.getValue();
        qualityTextField.setText(Integer.toString(value));

        if (value == 10)
            qualityCombo.setSelectedIndex(3);
        else if (value >= 8)
            qualityCombo.setSelectedIndex(2);
        else if (value >= 3)
            qualityCombo.setSelectedIndex(1);
        else
            qualityCombo.setSelectedIndex(0);

        addListeners();

    }

    public void keyReleased(KeyEvent e) {
        removeListeners();
        int value = qualityTextField.getValue();

        if (value > 10) {
            value = 10;
        }

        qualitySlider.setValue(value);

        if (value == 10)
            qualityCombo.setSelectedIndex(3);
        else if (value >= 8)
            qualityCombo.setSelectedIndex(2);
        else if (value >= 3)
            qualityCombo.setSelectedIndex(1);
        else
            qualityCombo.setSelectedIndex(0);

        addListeners();
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    private void enableOptionsPanels(int index) {

        switch (index) {
            case 0:
                enableJpegPanel(false);
                enableGifPanel(false);
                enableSvgPanel(false);
                break;
            case 1:
                enableJpegPanel(true);
                enableGifPanel(false);
                enableSvgPanel(false);
                break;
            case 2:
            case 3:
                enableJpegPanel(false);
                enableGifPanel(true);
                enableSvgPanel(false);
                break;
            case 4:
                enableJpegPanel(false);
                enableGifPanel(false);
                enableSvgPanel(true);
                break;
        }

    }

    private void enableSvgPanel(boolean enable) {
        svgFontCheckbox.setEnabled(enable);
    }

    private void enableJpegPanel(boolean enable) {
        qualityLabel.setEnabled(enable);
        qualityTextField.setEnabled(enable);
        qualityCombo.setEnabled(enable);
        qualitySlider.setEnabled(enable);
    }

    private void enableGifPanel(boolean enable) {
        backgroundCombo.setEnabled(enable);
        backgroundLabel.setEnabled(enable);
    }

    /**
     * <p>Performs the respective action upon selection
     * of a button within this dialog.
     *
     * @param the <code>ActionEvent</code>
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Cancel")) {
            dispose();
        } else if (command.equals("Browse")) {
            showFileChooser();
            return;
        } else if (command.equals("Save")) {
            final String path = pathField.getText();
            if (MiscUtils.isNull(path)) {
                GUIUtilities.displayErrorMessage("You must enter a file name");
                return;
            }

            final int fileFormat = imageTypeCombo.getSelectedIndex();
            if ((fileFormat == EQ_FORMAT && !path.endsWith(".eqd")) ||
                    (fileFormat == JPEG_FORMAT && !path.endsWith(".jpeg")) ||
                    (fileFormat == SVG_FORMAT && !path.endsWith(".svg")) ||
                    (fileFormat == PNG_FORMAT && !path.endsWith(".png")) ||
                    (fileFormat == GIF_FORMAT && !path.endsWith(".gif"))) {
                GUIUtilities.displayErrorMessage("Invalid file extension for selected file type");
                return;
            }

            doSave(path, fileFormat);
            return;
        }

        removeListeners();
        Object object = e.getSource();

        if (object == imageTypeCombo) {
            enableOptionsPanels(imageTypeCombo.getSelectedIndex());
        } else if (object == qualityCombo) {
            int value = -1;
            int index = qualityCombo.getSelectedIndex();

            if (index == 0)
                value = 3;
            else if (index == 1)
                value = 5;
            else if (index == 2)
                value = 8;
            else if (index == 3)
                value = 10;

            qualitySlider.setValue(value);
            qualityTextField.setText(Integer.toString(value));

        }

        addListeners();

    }

    private void doSave(final String path, final int fileFormat) {
        SwingWorker worker = new SwingWorker() {
            public Object construct() {
                try {
                    setVisible(false);
                    GUIUtilities.showWaitCursor();
                    return save(path, fileFormat);
                } finally {
                    GUIUtilities.showNormalCursor();
                }
            }

            public void finished() {
                GUIUtilities.showNormalCursor();
                if (savedResult == SaveFunction.SAVE_COMPLETE) {
                    dispose();
                } else {
                    setVisible(true);
                }
            }
        };
        worker.start();
    }

    private String saveApplicationFileFormat(File file) {

        savedResult = parent.saveApplicationFileFormat(file);

        fireFileOpened(file);

        GUIUtilities.scheduleGC();

        if (savedResult == SaveFunction.SAVE_COMPLETE) {

            GUIUtilities.setTabTitleForComponent(parent, ErdViewerPanel.TITLE +
                    " - " + file.getName());
        }

        return "done";
    }

    private void fireFileOpened(File file) {

        EventMediator.fireEvent(
                new DefaultFileIOEvent(parent, FileIOEvent.OUTPUT_COMPLETE,
                        file.getAbsolutePath()));
    }

}











