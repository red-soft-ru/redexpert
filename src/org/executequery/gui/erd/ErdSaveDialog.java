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
import org.executequery.gui.SaveFunction;
import org.executequery.gui.WidgetFactory;
import org.executequery.imageio.*;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
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

/**
 * @author Takis Diakoumis
 */
public class ErdSaveDialog extends AbstractBaseDialog
        implements ActionListener,
        KeyListener,
        ChangeListener {

    private static final String TITLE = bundleString("title");

    private static final int EQ_FORMAT = 0;
    private static final int JPG_FORMAT = EQ_FORMAT + 1;
    private static final int GIF_FORMAT = JPG_FORMAT + 1;
    private static final int SVG_FORMAT = GIF_FORMAT + 1;
    private static final int PNG_FORMAT = SVG_FORMAT + 1;

    private static final int TRANSPARENT_BACKGROUND = 0;
    private static final int WHITE_BACKGROUND = TRANSPARENT_BACKGROUND + 1;

    // --- GUI components ---

    private JComboBox<?> qualityCombo;
    private JComboBox<?> imageTypeCombo;
    private JComboBox<?> backgroundCombo;

    private JLabel qualityLabel;
    private JLabel backgroundLabel;

    private JButton saveButton;
    private JButton browseButton;
    private JButton cancelButton;

    private JTextField filePathField;
    private JSlider qualitySlider;
    private JCheckBox renderTextAsImageCheck;
    private NumberTextField qualityTextField;

    // ---

    private int savedResult;
    private String openPath;
    private File defaultFile;
    private ErdViewerPanel parent;

    private ErdSaveDialog() {
        super(GUIUtilities.getParentFrame(), TITLE, true);
        savedResult = -1;

        init();
        arrange();
        enablePanel(0);
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

    private void init() {

        String[] backgroundTypesArray = {
                bundleString("background.transparent"),
                bundleString("background.white")
        };

        String[] qualitiesArray = {
                bundleString("quality.low"),
                bundleString("quality.medium"),
                bundleString("quality.high"),
                bundleString("quality.maximum")
        };

        String[] imageTypesArray = {
                "Red Expert ERD",
                "JPEG",
                "GIF",
                "PNG",
                "SVG"
        };

        // ---

        qualityLabel = new JLabel(bundleString("quality"));
        backgroundLabel = new JLabel(bundleString("background"));

        qualityTextField = WidgetFactory.createNumberTextField("qualityTextField", "8");
        qualityTextField.setDigits(2);

        qualityCombo = WidgetFactory.createComboBox("qualityCombo", qualitiesArray);
        qualityCombo.setSelectedIndex(2);

        backgroundCombo = WidgetFactory.createComboBox("backgroundCombo", backgroundTypesArray);
        backgroundCombo.setSelectedIndex(0);

        qualitySlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 8);
        qualitySlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        qualitySlider.setMajorTickSpacing(5);
        qualitySlider.setMajorTickSpacing(1);

        saveButton = WidgetFactory.createButton("saveButton", Bundles.get("common.save.button"), e -> save());
        cancelButton = WidgetFactory.createButton("saveButton", Bundles.get("common.cancel.button"), e -> dispose());
        browseButton = WidgetFactory.createButton("browseButton", Bundles.get("common.browse.button"), e -> browse());

        filePathField = WidgetFactory.createTextField("filePathField");
        imageTypeCombo = WidgetFactory.createComboBox("imageTypeCombo", imageTypesArray);
        renderTextAsImageCheck = WidgetFactory.createCheckBox("renderTextAsImageCheck", bundleString("renderTextAsImageCheck"));

        // --- add listeners

        qualityCombo.addActionListener(this);
        qualitySlider.addChangeListener(this);
        qualityTextField.addKeyListener(this);
        imageTypeCombo.addActionListener(this);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- quality panel ---

        JPanel qualityPanel = new JPanel(new GridBagLayout());
        qualityPanel.setBorder(BorderFactory.createTitledBorder(bundleString("options.jpeg")));

        gbh = new GridBagHelper().setInsets(5, 8, 5, 5).anchorNorthWest().fillHorizontally();
        qualityPanel.add(qualityLabel, gbh.setMinWeightX().get());
        qualityPanel.add(qualityTextField, gbh.nextCol().topGap(5).leftGap(0).get());
        qualityPanel.add(qualityCombo, gbh.nextCol().setMaxWeightX().get());
        qualityPanel.add(qualitySlider, gbh.nextRowFirstCol().topGap(0).leftGap(5).spanX().get());

        // --- png panel ---

        JPanel pngPanel = new JPanel(new GridBagLayout());
        pngPanel.setBorder(BorderFactory.createTitledBorder(bundleString("options.png")));

        gbh = new GridBagHelper().setInsets(5, 8, 5, 5).anchorNorthWest().fillHorizontally();
        pngPanel.add(backgroundLabel, gbh.setMinWeightX().get());
        pngPanel.add(backgroundCombo, gbh.nextCol().setMaxWeightX().topGap(5).leftGap(0).get());

        // --- svg panel ---

        JPanel svgPanel = new JPanel(new GridBagLayout());
        svgPanel.setBorder(BorderFactory.createTitledBorder(bundleString("options.svg")));

        gbh = new GridBagHelper().setInsets(3, 5, 5, 5).anchorNorthWest().fillHorizontally();
        svgPanel.add(renderTextAsImageCheck, gbh.spanX().get());

        // --- path panel ---

        JPanel pathPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        pathPanel.add(new JLabel(bundleString("path")), gbh.leftGap(3).topGap(3).setMinWeightX().get());
        pathPanel.add(filePathField, gbh.nextCol().leftGap(5).topGap(0).setMaxWeightX().get());
        pathPanel.add(browseButton, gbh.nextCol().setMinWeightX().rightGap(3).get());

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorCenter().fillHorizontally();
        buttonPanel.add(saveButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(400, 300));

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("format")), gbh.setMinWeightX().leftGap(3).topGap(3).get());
        mainPanel.add(imageTypeCombo, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(0).rightGap(5).get());
        mainPanel.add(qualityPanel, gbh.nextRowFirstCol().setMaxWeightY().leftGap(0).topGap(5).rightGap(3).spanX().get());
        mainPanel.add(pngPanel, gbh.nextRowFirstCol().get());
        mainPanel.add(svgPanel, gbh.nextRowFirstCol().get());
        mainPanel.add(pathPanel, gbh.nextRowFirstCol().setMinWeightY().get());

        // --- base ---

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        add(mainPanel, gbh.setMaxWeightY().spanX().get());
        add(buttonPanel, gbh.nextRow().topGap(10).setMinWeightY().get());
    }

    private void enablePanel(int index) {
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
        renderTextAsImageCheck.setEnabled(enable);
    }

    private void enableJpegPanel(boolean enable) {
        qualityTextField.setEnabled(enable);
        qualitySlider.setEnabled(enable);
        qualityLabel.setEnabled(enable);
        qualityCombo.setEnabled(enable);
    }

    private void enableGifPanel(boolean enable) {
        backgroundCombo.setEnabled(enable);
        backgroundLabel.setEnabled(enable);
    }

    private void display() {
        pack();
        setResizable(false);
        setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    // ---

    private void save() {

        final String path = filePathField.getText();
        if (MiscUtils.isNull(path)) {
            GUIUtilities.displayErrorMessage(bundleString("noFileName"));
            return;
        }

        final int fileFormat = imageTypeCombo.getSelectedIndex();
        if ((fileFormat == EQ_FORMAT && !path.endsWith(".eqd"))
                || (fileFormat == JPG_FORMAT && !path.endsWith(".jpeg"))
                || (fileFormat == SVG_FORMAT && !path.endsWith(".svg"))
                || (fileFormat == PNG_FORMAT && !path.endsWith(".png"))
                || (fileFormat == GIF_FORMAT && !path.endsWith(".gif"))) {

            GUIUtilities.displayErrorMessage(bundleString("invalidFileExtension"));
            return;
        }

        SwingWorker worker = new SwingWorker("saving ER-diagram") {

            @Override
            public Object construct() {
                try {
                    setVisible(false);
                    GUIUtilities.showWaitCursor();
                    return saveFile(path, fileFormat);

                } finally {
                    GUIUtilities.showNormalCursor();
                }
            }

            @Override
            public void finished() {
                if (savedResult == SaveFunction.SAVE_COMPLETE)
                    dispose();
                else
                    setVisible(true);
            }
        };

        worker.start();
    }

    private String saveFile(String path, int fileFormat) {

        File file = new File(path);
        if (fileFormat == EQ_FORMAT)
            return saveApplicationFileFormat(file);

        Dimension extents = parent.getMaxImageExtents();
        int width = (int) extents.getWidth();
        int height = (int) extents.getHeight();

        int imageType;
        int bgType = backgroundCombo.getSelectedIndex();

        if ((fileFormat == GIF_FORMAT || fileFormat == PNG_FORMAT) && bgType == TRANSPARENT_BACKGROUND) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else if (fileFormat == SVG_FORMAT) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else
            imageType = BufferedImage.TYPE_INT_RGB;

        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        if (fileFormat == JPG_FORMAT || bgType == WHITE_BACKGROUND) {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
        }

        paintImage(graphics);

        try {

            ImageWriter imageWriter = null;
            ImageWriterInfo imageWriterInfo = null;
            ImageWriterFactory factory = new DefaultImageWriterFactory();

            if (fileFormat == PNG_FORMAT) {
                imageWriter = factory.createImageWriterForPngImages();
                imageWriterInfo = new PngImageWriterInfo(image, file);

            } else if (fileFormat == SVG_FORMAT) {
                imageWriter = factory.createImageWriterForSvgImages();
                imageWriterInfo = new SvgImageWriterInfo(image, file, renderTextAsImageCheck.isSelected());

            } else if (fileFormat == JPG_FORMAT) {
                imageWriter = factory.createImageWriterForJpegImages();
                imageWriterInfo = new JpegImageWriterInfo(image, file, qualitySlider.getValue());

            } else if (fileFormat == GIF_FORMAT) {
                imageWriter = factory.createImageWriterForGifImages();
                imageWriterInfo = new GifImageWriterInfo(image, file);
            }

            if (imageWriter != null)
                imageWriter.write(imageWriterInfo);

            GUIUtilities.scheduleGC();

            savedResult = SaveFunction.SAVE_COMPLETE;
            return "done";

        } catch (Exception e) {
            savedResult = SaveFunction.SAVE_FAILED;
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());
            return "failed";
        }
    }

    private void paintImage(Graphics2D graphics) {

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        parent.getDependenciesPanel().drawDependencies(graphics);

        ErdTable[] tablesArray = parent.getAllTablesArray();
        for (ErdTable erdTable : tablesArray) {
            erdTable.setSelected(false);
            erdTable.drawTable(graphics, erdTable.getX(), erdTable.getY());
        }

        ErdTitlePanel title = parent.getTitlePanel();
        if (title != null) {
            title.setSelected(false);
            title.drawTitlePanel(graphics, title.getX(), title.getY());
        }
    }

    private void browse() {

        String fileExtension;
        String fileDescription;

        int imageType = imageTypeCombo.getSelectedIndex();
        if (imageType == EQ_FORMAT) {
            fileDescription = bundleString("files", "Red Expert ERD");
            fileExtension = "eqd";

        } else if (imageType == JPG_FORMAT) {
            fileDescription = bundleString("files", "JPEG");
            fileExtension = "jpeg";

        } else if (imageType == SVG_FORMAT) {
            fileDescription = bundleString("files", "SVG");
            fileExtension = "svg";

        } else if (imageType == PNG_FORMAT) {
            fileDescription = bundleString("files", "PNG");
            fileExtension = "png";

        } else {
            fileDescription = bundleString("files", "GIF");
            fileExtension = "gif";
        }

        FileChooserDialog fileChooser = getFileChooserDialog(imageType, fileExtension, fileDescription);
        int result = fileChooser.showDialog(GUIUtilities.getParentFrame(), Bundles.get("common.select.button"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        String filePath = null;
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            if (file.exists()) {

                int overwriteResult = GUIUtilities.displayConfirmCancelDialog(bundleString("overwriteFile"));
                if (overwriteResult == JOptionPane.CANCEL_OPTION)
                    return;

                if (overwriteResult == JOptionPane.NO_OPTION) {
                    browse();
                    return;
                }
            }

            filePath = fileChooser.getSelectedFile().getAbsolutePath();
        }

        if (filePath != null) {
            fileExtension = "." + fileExtension;
            if (!filePath.endsWith(fileExtension))
                filePath += fileExtension;
        }

        filePathField.setText(filePath);
    }

    private FileChooserDialog getFileChooserDialog(int imageType, String fileExtension, String fileDescription) {

        FileChooserDialog fileChooser = openPath != null ?
                new FileChooserDialog(openPath) :
                new FileChooserDialog();

        if (defaultFile != null && imageType == EQ_FORMAT)
            fileChooser.setSelectedFile(defaultFile);

        fileChooser.setDialogTitle(bundleString("selectFile"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileSelector(new String[]{fileExtension}, fileDescription));

        return fileChooser;
    }

    private String saveApplicationFileFormat(File file) {
        savedResult = parent.saveApplicationFileFormat(file);

        fireFileOpened(file);
        GUIUtilities.scheduleGC();

        if (savedResult == SaveFunction.SAVE_COMPLETE)
            GUIUtilities.setTabTitleForComponent(parent, ErdViewerPanel.TITLE + " - " + file.getName());

        return "done";
    }

    private void addListeners() {
        qualityCombo.addActionListener(this);
        qualityTextField.addKeyListener(this);
        qualitySlider.addChangeListener(this);
    }

    private void removeListeners() {
        qualityTextField.removeKeyListener(this);
        qualityCombo.removeActionListener(this);
        qualitySlider.removeChangeListener(this);
    }

    public int getSaved() {
        return savedResult;
    }

    // --- ActionListener impl ---

    @Override
    public void actionPerformed(ActionEvent e) {
        removeListeners();

        Object object = e.getSource();
        if (object == imageTypeCombo) {
            enablePanel(imageTypeCombo.getSelectedIndex());

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

    // --- ChangeListener impl ---

    @Override
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

    // --- KeyListener impl ---

    @Override
    public void keyReleased(KeyEvent e) {
        removeListeners();

        int value = qualityTextField.getValue();
        if (value > 10)
            value = 10;

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

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // ---

    private void fireFileOpened(File file) {
        EventMediator.fireEvent(new DefaultFileIOEvent(parent, FileIOEvent.OUTPUT_COMPLETE, file.getAbsolutePath()));
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ErdSaveDialog.class, key, args);
    }

}
