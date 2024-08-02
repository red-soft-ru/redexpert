package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Random;
import java.util.regex.Pattern;

public class GetFromListPanel extends AbstractMethodPanel {

    private JTextField fileField;
    private JTextField delimiterField;

    private JLabel labelFile;
    private SimpleTextArea textArea;
    private JButton browseFileButton;
    private JFileChooser openFileDialog;

    private JComboBox<?> ordersCombo;
    private JComboBox<?> sourcesCombo;

    private int index;
    private String[] dataArray;
    private Object[] listObject;
    private final String dataType;

    public GetFromListPanel(DatabaseColumn col) {
        super(col);
        this.dataType = col.getFormattedDataType();

        init();
        arrange();
    }

    private void init() {

        textArea = new SimpleTextArea();
        openFileDialog = new JFileChooser();

        delimiterField = WidgetFactory.createTextField("delimiterField");
        delimiterField.setDocument(new LimitedDocument(1));
        delimiterField.setText("\\n");

        ordersCombo = WidgetFactory.createComboBox("ordersCombo", bundleString(new String[]{"InOrder", "Random"}));
        sourcesCombo = WidgetFactory.createComboBox("sourcesCombo", bundleString(new String[]{"FromTextArea", "FromFile"}));
        sourcesCombo.addItemListener(this::sourcesComboTriggered);

        fileField = WidgetFactory.createTextField("fileField");
        labelFile = new JLabel(bundleString(isBlob(dataType) ? "ChooseDirectory" : "ChooseFile"));
        browseFileButton = WidgetFactory.createButton("browseFileButton", "...", e -> browseFile());

        if (!isBlob(dataType)) {
            labelFile.setVisible(false);
            fileField.setVisible(false);
            browseFileButton.setVisible(false);
        }
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        if (!isBlob(dataType)) {
            mainPanel.add(new JLabel(bundleString("Source")), gbh.leftGap(3).topGap(3).bottomGap(5).get());
            mainPanel.add(sourcesCombo, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().spanX().get());
            gbh.setWidth(1).setMinWeightX().nextRowFirstCol().bottomGap(0);
        }

        mainPanel.add(new JLabel(bundleString("Method")), gbh.leftGap(3).topGap(3).get());
        mainPanel.add(ordersCombo, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().spanX().get());
        if (!isBlob(dataType)) {
            mainPanel.add(new JLabel(bundleString("Delimiter")), gbh.nextRowFirstCol().setWidth(1).leftGap(3).topGap(8).setMinWeightX().get());
            mainPanel.add(delimiterField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().spanX().get());
        }

        mainPanel.add(labelFile, gbh.nextRowFirstCol().setWidth(1).leftGap(3).topGap(8).setMinWeightX().get());
        mainPanel.add(fileField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        mainPanel.add(browseFileButton, gbh.nextCol().setMinWeightX().get());

        gbh.nextRowFirstCol().fillBoth().spanX().spanY();
        if (!isBlob(dataType))
            mainPanel.add(textArea, gbh.get());
        mainPanel.add(new JPanel(), gbh.get());

        // --- base ---

        setLayout(new GridBagLayout());
        gbh = new GridBagHelper().topGap(5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());
    }

    private void browseFile() {
        openFileDialog.setFileSelectionMode(isBlob(dataType) ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        if (openFileDialog.showOpenDialog(browseFileButton) == JOptionPane.OK_OPTION)
            fileField.setText(openFileDialog.getSelectedFile().getAbsolutePath());
    }

    private void sourcesComboTriggered(ItemEvent e) {

        if (e.getStateChange() != ItemEvent.SELECTED)
            return;

        boolean visible = sourcesCombo.getSelectedIndex() == 0;
        browseFileButton.setVisible(!visible);
        labelFile.setVisible(!visible);
        fileField.setVisible(!visible);
        textArea.setVisible(visible);
    }

    private void initDataList() {
        String fileName = fileField.getText().trim();

        if (isBlob(dataType)) {

            File directory = new File(fileName);
            if (!directory.exists())
                throw new DataSourceException("The selected directory does not exist.");

            File[] files = directory.listFiles();
            if (files != null) {
                dataArray = new String[files.length];
                for (int i = 0; i < dataArray.length; i++)
                    dataArray[i] = files[i].getAbsolutePath();
            }

        } else {

            String delimiter = delimiterField.getText();
            if (!delimiter.startsWith("\\"))
                delimiter = Pattern.quote(delimiter);

            if (sourcesCombo.getSelectedIndex() == 0) {
                dataArray = textArea.getTextAreaComponent().getText().trim().split(delimiter);

            } else {

                File file = new File(fileName);
                if (!file.exists())
                    throw new DataSourceException("The selected file does not exist.");

                try {
                    String fileData = new String(Files.readAllBytes(Paths.get(fileName)));
                    dataArray = fileData.trim().split(delimiter);

                } catch (IOException e) {
                    Log.error(e.getMessage(), e);
                }
            }
        }

        listObject = new Object[dataArray.length];
        for (int i = 0; i < dataArray.length; i++)
            listObject[i] = objectFromString(dataArray[i]);
    }

    private Object objectFromString(String str) {

        if (isBigint(dataType)) {
            return new BigInteger(str);

        } else if (isInteger(dataType)) {
            return Integer.parseInt(str);

        } else if (isSmallint(dataType)) {
            return Short.parseShort(str);

        } else if (isDecimal(dataType)) {
            return Double.valueOf(str);

        } else if (isDecFloat(dataType)) {
            return new BigDecimal(str);

        } else if (isChar(dataType)) {
            return str;

        } else if (isDate(dataType)) {
            return Date.valueOf(str);

        } else if (isTime(dataType)) {
            return Time.valueOf(str);

        } else if (isTimestamp(dataType)) {
            return Timestamp.valueOf(str);

        } else if (isZonedTime(dataType)) {
            return OffsetTime.parse(str);

        } else if (isZonedTimestamp(dataType)) {
            return OffsetDateTime.parse(str);

        } else if (isBlob(dataType)) {
            try {
                return new FileInputStream(str);

            } catch (FileNotFoundException e) {
                Log.error(e.getMessage(), e);
            }

        } else if (isBoolean(dataType))
            return Boolean.valueOf(str);

        return null;
    }

    private static String[] bundleString(String[] keys) {
        return Bundles.get(GeneratorTestDataPanel.class, keys);
    }

    // --- AbstractMethodPanel impl ---

    @Override
    public Object getTestDataObject() {

        if (first) {
            initDataList();
            first = false;
            index = 0;
        }

        if (index >= dataArray.length)
            index = 0;

        if (ordersCombo.getSelectedIndex() == 1)
            index = new Random().nextInt(dataArray.length);

        index++;
        return listObject[index - 1];
    }

    // ---

    private static class LimitedDocument extends PlainDocument {
        private final int limit;

        public LimitedDocument(int limit) {
            super();
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null)
                return;

            if ((getLength() + str.length()) <= limit
                    || getText(0, 1).startsWith("\\")
                    || getLength() == 0 && str.startsWith("\\")
            ) {
                super.insertString(offset, str, attr);
            }
        }

    } // LimitedDocument class

}
