package org.executequery.gui.datatype;

import org.executequery.Constants;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.T;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableDefinitionPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

public class SelectTypePanel extends JPanel {

    private final boolean showTypeOf;
    private final KeyListener keyListener;
    private final ActionListener actionListener;

    // --- GUI components ---

    private JLabel sizeLabel;
    private JCheckBox useTypeOfCheck;

    private JComboBox<String> typesCombo;
    private JComboBox<String> collatesCombo;
    private JComboBox<String> encodingsCombo;
    private JComboBox<String> typeOfTableCombo;
    private JComboBox<String> typeOfColumnCombo;

    private DynamicComboBoxModel columnsComboModel;
    private DynamicComboBoxModel collatesComboModel;

    private NumberTextField sizeField;
    private NumberTextField scaleField;
    private NumberTextField subtypeField;

    // ---

    private boolean refreshing = false;
    private boolean disabledCollate = false;
    private boolean refreshingCollate = false;

    private String[] dataTypes;
    private int[] intDataTypes;
    private ColumnData columnData;
    private List<String> charsets;
    private Map<Integer, String> types;

    public SelectTypePanel(String[] types, int[] intTypes, ColumnData columnData, boolean showTypeOf) {
        this(types, intTypes, columnData, showTypeOf, null, null);
    }

    public SelectTypePanel(String[] types, int[] intTypes, ColumnData columnData, boolean showTypeOf,
                           ActionListener actionListener, KeyListener keyListener) {

        this.dataTypes = types;
        this.columnData = columnData;
        this.showTypeOf = showTypeOf;
        this.intDataTypes = intTypes;
        this.actionListener = actionListener;
        this.keyListener = new KeyListenerImpl(keyListener);

        sortTypes();
        removeDuplicates();
        loadCharsets();

        init();
        arrange();

        if (showTypeOf)
            typeOfTableComboValueChanged();
    }

    @SuppressWarnings("unchecked")
    private void init() {

        columnsComboModel = new DynamicComboBoxModel();
        collatesComboModel = new DynamicComboBoxModel();

        sizeLabel = new JLabel(Bundles.getCommon("size"));

        // --- number text fields ---

        sizeField = WidgetFactory.createNumberTextField("sizeField");
        sizeField.addKeyListener(keyListener);

        scaleField = WidgetFactory.createNumberTextField("scaleField");
        scaleField.addKeyListener(keyListener);

        subtypeField = WidgetFactory.createNumberTextField("subtypeField");
        subtypeField.addKeyListener(keyListener);

        // --- combo boxes ---

        encodingsCombo = WidgetFactory.createComboBox("encodingsCombo", charsets.toArray(new String[0]));
        encodingsCombo.addItemListener(e -> encodingsComboValueChanged());

        collatesCombo = WidgetFactory.createComboBox("collatesCombo", collatesComboModel);
        collatesCombo.addItemListener(e -> collatesComboValueChanged());

        typesCombo = WidgetFactory.createComboBox("typesCombo", dataTypes);
        typesCombo.addActionListener(e -> typesComboValueChanged(true));
        typesCombo.setSelectedIndex(0);

        if (!showTypeOf)
            return;

        typeOfTableCombo = WidgetFactory.createComboBox("typeOfTableCombo", columnData.getTableNames().toArray(new String[0]));
        typeOfTableCombo.addActionListener(e -> typeOfTableComboValueChanged());
        typeOfTableCombo.setEnabled(columnData.isTypeOf());

        typeOfColumnCombo = WidgetFactory.createComboBox("typeOfColumnCombo", columnsComboModel);
        typeOfColumnCombo.addActionListener(e -> typeOfColumnComboValueChanged());
        typeOfColumnCombo.setEnabled(columnData.isTypeOf());

        // --- check box ---

        useTypeOfCheck = WidgetFactory.createCheckBox("useTypeOfCheck", bundleString("TypeOf"), e -> useTypeOfCheckChanged());
        useTypeOfCheck.setSelected(columnData.isTypeOf());

        // ---

        if (columnData.isTypeOf()) {
            typeOfTableCombo.setSelectedItem(columnData.getTable());
            typeOfColumnCombo.setSelectedItem(columnData.getColumnTable());
        }
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillHorizontally().anchorNorthWest();
        mainPanel.add(new JLabel(Bundles.getCommon("data-type")), gbh.setWeightX(0).topGap(8).get());
        mainPanel.add(typesCombo, gbh.nextCol().setWeightX(1).rightGap(0).spanX().get());
        mainPanel.add(sizeLabel, gbh.nextRowFirstCol().setWeightX(0).setWidth(1).rightGap(5).topGap(3).get());
        mainPanel.add(sizeField, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
        mainPanel.add(new JLabel(Bundles.getCommon("scale")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).topGap(3).rightGap(5).get());
        mainPanel.add(scaleField, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
        mainPanel.add(new JLabel(Bundles.getCommon("subtype")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).topGap(3).rightGap(5).get());
        mainPanel.add(subtypeField, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
        mainPanel.add(new JLabel(Bundles.getCommon("encoding")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).topGap(3).rightGap(5).get());
        mainPanel.add(encodingsCombo, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
        mainPanel.add(new JLabel(Bundles.getCommon("collate")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).topGap(3).rightGap(5).get());
        mainPanel.add(collatesCombo, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());

        if (showTypeOf) {
            mainPanel.add(useTypeOfCheck, gbh.nextRowFirstCol().leftGap(2).get());
            mainPanel.add(new JLabel(bundleString("Table")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).leftGap(5).topGap(3).rightGap(5).get());
            mainPanel.add(typeOfTableCombo, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
            mainPanel.add(new JLabel(bundleString("Column")), gbh.nextRowFirstCol().setWeightX(0).setWidth(1).topGap(3).rightGap(5).get());
            mainPanel.add(typeOfColumnCombo, gbh.nextCol().setWeightX(1).rightGap(0).topGap(0).spanX().get());
        }

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth().spanX();
        add(mainPanel, gbh.get());
        add(new JPanel(), gbh.nextRow().spanY().get());
    }

    // --- listeners ---

    private void typesComboValueChanged(boolean updateEnabled) {
        int index = typesCombo.getSelectedIndex();
        if (index >= 0) {
            columnData.setTypeName(dataTypes[index]);
            columnData.setSQLType(intDataTypes[index]);

            setSizeEnabled(updateEnabled && (columnData.getSQLType() == Types.NUMERIC
                    || columnData.getSQLType() == Types.CHAR
                    || columnData.getSQLType() == Types.VARCHAR
                    || columnData.getSQLType() == Types.DECIMAL
                    || columnData.getSQLType() == Types.BLOB
                    || columnData.getSQLType() == Types.LONGVARBINARY
                    || columnData.getSQLType() == Types.LONGVARCHAR
                    || columnData.getTypeName().equalsIgnoreCase("VARCHAR")
                    || columnData.getTypeName().equalsIgnoreCase("CHAR")
                    || columnData.getTypeName().equalsIgnoreCase(T.DECFLOAT))
            );

            if (columnData.getSQLType() == Types.NUMERIC || columnData.getSQLType() == Types.DECIMAL)
                sizeLabel.setText(Bundles.getCommon("precision"));
            else
                sizeLabel.setText(Bundles.getCommon("size"));

            setScaleEnabled(updateEnabled && (columnData.getSQLType() == Types.NUMERIC || columnData.getSQLType() == Types.DECIMAL));
            setSubtypeEnabled(updateEnabled && columnData.getSQLType() == Types.BLOB);
            setEncodingEnabled(updateEnabled && (columnData.getSQLType() == Types.CHAR || columnData.getSQLType() == Types.VARCHAR
                    || columnData.getSQLType() == Types.LONGVARCHAR || columnData.getSQLType() == Types.CLOB
                    || columnData.getTypeName().equalsIgnoreCase("VARCHAR")
                    || columnData.getTypeName().equalsIgnoreCase("CHAR")));

            if (!refreshing) {

                if (columnData.getSQLType() == Types.LONGVARBINARY || columnData.getSQLType() == Types.LONGVARCHAR || columnData.getSQLType() == Types.BLOB)
                    sizeField.setText("80");

                if (columnData.getSQLType() == Types.LONGVARBINARY)
                    subtypeField.setText("0");

                if (columnData.getSQLType() == Types.LONGVARCHAR)
                    subtypeField.setText("1");

                if (columnData.getSQLType() == Types.BLOB)
                    subtypeField.setText("0");
            }
        }

        changeActionPerformed();
    }

    private void collatesComboValueChanged() {

        if (!refreshingCollate)
            columnData.setCollate((String) collatesCombo.getSelectedItem());

        changeActionPerformed();
    }

    private void encodingsComboValueChanged() {

        refreshingCollate = true;
        columnData.setCharset((String) encodingsCombo.getSelectedItem());
        collatesComboModel.setElements(loadCollates((String) encodingsCombo.getSelectedItem()));
        refreshingCollate = false;

        if (columnData.getCollate() != null && collatesComboModel.contains(columnData.getCollate())) {
            collatesCombo.setSelectedItem(columnData.getCollate());

        } else {
            collatesCombo.setSelectedIndex(0);
            columnData.setCollate((String) collatesCombo.getSelectedItem());
        }

        changeActionPerformed();
    }

    private void useTypeOfCheckChanged() {
        boolean useTypeOf = useTypeOfCheck.isSelected();

        columnData.setTypeOf(useTypeOf);
        typeOfTableCombo.setEnabled(useTypeOf);
        typeOfColumnCombo.setEnabled(useTypeOf);

        if (columnData.isTypeOf())
            columnData.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);

        changeActionPerformed();
    }

    private void typeOfTableComboValueChanged() {

        columnData.setTable((String) typeOfTableCombo.getSelectedItem());
        columnsComboModel.setElements(columnData.getColumns());
        typeOfColumnCombo.setSelectedIndex(0);

        changeActionPerformed();
    }

    private void typeOfColumnComboValueChanged() {
        columnData.setColumnTable((String) typeOfColumnCombo.getSelectedItem());
        changeActionPerformed();
    }

    private void changeActionPerformed() {
        if (actionListener != null)
            actionListener.actionPerformed(null);
    }

    // ---

    @Override
    public void setEnabled(boolean enabled) {

        sizeField.setEnabled(enabled);
        scaleField.setEnabled(enabled);
        typesCombo.setEnabled(enabled);
        subtypeField.setEnabled(enabled);
        collatesCombo.setEnabled(enabled);
        useTypeOfCheck.setEnabled(enabled);
        encodingsCombo.setEnabled(enabled);

        if (enabled)
            refresh();

        typeOfTableCombo.setEnabled(enabled && useTypeOfCheck.isSelected());
        typeOfColumnCombo.setEnabled(enabled && useTypeOfCheck.isSelected());
    }

    public void refreshColumn() {
        columnData.setSize(sizeField.getValue());
        columnData.setScale(scaleField.getValue());
        columnData.setSubtype(subtypeField.getValue());
    }

    private void setSizeEnabled(boolean enabled) {
        sizeField.setEnabled(enabled);
        sizeField.setValue(enabled ? 1 : 0);

        if (refreshing)
            sizeField.setValue(columnData.getSize());
        columnData.setSize(sizeField.getValue());
    }

    private void setScaleEnabled(boolean enabled) {
        scaleField.setEnabled(enabled);
        scaleField.setValue(enabled ? 1 : 0);

        if (refreshing)
            scaleField.setValue(columnData.getScale());
        columnData.setScale(scaleField.getValue());
    }

    private void setSubtypeEnabled(boolean enabled) {
        subtypeField.setEnabled(enabled);
        subtypeField.setValue(enabled ? 1 : 0);

        if (refreshing)
            subtypeField.setValue(columnData.getSubtype());
        columnData.setSubtype(subtypeField.getValue());
    }

    private void setEncodingEnabled(boolean enabled) {
        encodingsCombo.setEnabled(enabled);
        collatesCombo.setEnabled(enabled && !disabledCollate);

        if (refreshing) {
            collatesCombo.setSelectedItem(columnData.getCollate());
            encodingsCombo.setSelectedItem(columnData.getCharset());
        }

        columnData.setCollate((String) collatesCombo.getSelectedItem());
        columnData.setCharset((String) encodingsCombo.getSelectedItem());
    }

    private void removeDuplicates() {

        if (types == null)
            types = new HashMap<>();
        types.clear();

        String last = "";
        List<String> newTypes = new ArrayList<>();
        List<Integer> newIntTypes = new ArrayList<>();

        for (int i = 0; i < this.dataTypes.length; i++) {
            if (!newTypes.contains(this.dataTypes[i])) {

                newTypes.add(this.dataTypes[i]);
                newIntTypes.add(this.intDataTypes[i]);

                types.put(intDataTypes[i], dataTypes[i]);
                last = dataTypes[i];

            } else
                types.put(intDataTypes[i], last);
        }

        this.dataTypes = newTypes.toArray(new String[0]);
        this.intDataTypes = newIntTypes.stream().mapToInt(Integer::intValue).toArray();
    }

    private void sortTypes() {

        if (dataTypes == null)
            return;

        for (int i = 0; i < dataTypes.length; i++) {
            for (int j = 0; j < dataTypes.length - 1; j++) {

                if (dataTypes[j].compareTo(dataTypes[j + 1]) > 0) {
                    String dataType = dataTypes[j];
                    int intDataType = intDataTypes[j];

                    dataTypes[j] = dataTypes[j + 1];
                    intDataTypes[j] = intDataTypes[j + 1];

                    dataTypes[j + 1] = dataType;
                    intDataTypes[j + 1] = intDataType;
                }
            }
        }
    }

    public void refresh() {
        refresh(true);
    }

    public void refresh(boolean updateEnabled) {
        refreshing = true;
        columnData.setTypeName(getStringType(columnData.getSQLType()));
        typesCombo.setSelectedItem(columnData.getTypeName());
        typesComboValueChanged(updateEnabled);
        refreshing = false;
    }

    private String getStringType(int value) {
        try {
            return types.get(value);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            return Constants.EMPTY;
        }
    }

    private void loadCharsets() {

        if (charsets == null)
            charsets = new ArrayList<>();
        charsets.clear();

        try {

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            for (String string : resource.split("\n")) {
                if (!string.startsWith("#") && !string.isEmpty())
                    charsets.add(string);
            }

            Collections.sort(charsets);
            charsets.add(0, CreateTableSQLSyntax.NONE);
            charsets.add(0, "");

        } catch (Exception e) {
            Log.error("Error getting charsets for SelectTypePanel:", e);
        }
    }

    public void setColumnData(ColumnData columnData) {
        this.columnData = columnData;
    }

    private String[] loadCollates(String charset) {
        String query = "SELECT RDB$COLLATION_NAME\n" +
                "FROM RDB$COLLATIONS CO\n" +
                "LEFT JOIN RDB$CHARACTER_SETS CS\n" +
                "ON CO.RDB$CHARACTER_SET_ID = CS.RDB$CHARACTER_SET_ID\n" +
                "WHERE CS.RDB$CHARACTER_SET_NAME = '" + charset + "'";

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        executor.setDatabaseConnection(columnData.getConnection());

        List<String> collates = new ArrayList<>();
        collates.add(Constants.EMPTY);
        collates.add(CreateTableSQLSyntax.NONE);

        try {
            ResultSet rs = executor.getResultSet(query).getResultSet();
            while (rs.next())
                collates.add(rs.getString(1).trim());

        } catch (Exception e) {
            Log.error(e.getMessage(), e);

        } finally {
            executor.releaseResources();
        }

        return collates.toArray(new String[0]);
    }

    public void setDisabledCollate(boolean disabledCollate) {
        this.disabledCollate = disabledCollate;
        this.collatesCombo.setEnabled(!disabledCollate);
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(TableDefinitionPanel.class, key, args);
    }

    private class KeyListenerImpl implements KeyListener {
        private final KeyListener aditionalKeyListener;

        public KeyListenerImpl(KeyListener aditionalKeyListener) {
            this.aditionalKeyListener = aditionalKeyListener;
        }

        @Override
        public void keyTyped(KeyEvent keyEvent) {
            if (aditionalKeyListener != null)
                aditionalKeyListener.keyTyped(null);
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            if (aditionalKeyListener != null)
                aditionalKeyListener.keyPressed(null);
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {

            Object source = keyEvent.getSource();
            if (source instanceof NumberTextField) {

                NumberTextField field = (NumberTextField) source;
                if (field.getValue() <= 0)
                    field.setValue(1);

                if (Objects.equals(field, sizeField)) {
                    columnData.setSize(field.getValue());

                } else if (Objects.equals(field, scaleField)) {
                    columnData.setScale(field.getValue());

                } else if (Objects.equals(field, subtypeField)) {
                    columnData.setSubtype(field.getValue());
                }
            }

            if (aditionalKeyListener != null)
                aditionalKeyListener.keyReleased(null);
        }

    } // KeyListenerImpl class

}
