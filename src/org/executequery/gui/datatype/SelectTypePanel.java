package org.executequery.gui.datatype;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectTypePanel extends JPanel {
    private JLabel typeLabel;
    private JLabel sizeLabel;
    private JLabel scaleLabel;
    private JLabel subtypeLabel;
    private JLabel encodingLabel;
    private JLabel collateLabel;
    private JComboBox typeBox;
    private JComboBox encodingBox;
    private JComboBox collateBox;
    private NumberTextField sizeField;
    private NumberTextField scaleField;
    private NumberTextField subtypeField;
    private boolean displayTypeOf;

    private String[] dataTypes;
    private int[] intDataTypes;
    private KeyListener keyListener;
    private ColumnData cd;
    private boolean refreshing = false;
    private List<String> charsets;
    private Map<Integer, String> types;
    private TypeOfPanel typeOfPanel;
    private DynamicComboBoxModel collateModel;
    private boolean refreshingCollate = false;

    public SelectTypePanel(String[] types, int[] intTypes, ColumnData cd, boolean displayTypeOf) {
        this.dataTypes = types;
        this.intDataTypes = intTypes;
        this.displayTypeOf = displayTypeOf;
        sortTypes();
        removeDuplicates();
        this.cd = cd;
        loadCharsets();
        init();
    }

    private void init() {
        typeLabel = new JLabel(Bundles.getCommon("data-type"));
        sizeLabel = new JLabel(Bundles.getCommon("size"));
        scaleLabel = new JLabel(Bundles.getCommon("scale"));
        subtypeLabel = new JLabel(Bundles.getCommon("subtype"));
        encodingLabel = new JLabel(Bundles.getCommon("encoding"));
        collateLabel = new JLabel(Bundles.getCommon("collate"));
        typeBox = new JComboBox();
        encodingBox = new JComboBox();
        collateBox = new JComboBox();
        sizeField = new NumberTextField();
        scaleField = new NumberTextField();
        subtypeField = new NumberTextField();
        typeOfPanel = new TypeOfPanel(cd);
        typeOfPanel.setVisible(displayTypeOf);
        keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                NumberTextField field = (NumberTextField) keyEvent.getSource();
                if (field.getValue() <= 0)
                    field.setValue(1);
                if (field == sizeField) {
                    cd.setColumnSize(field.getValue());
                } else if (field == scaleField) {
                    cd.setColumnScale(field.getValue());
                } else if (field == subtypeField) {
                    cd.setColumnSubtype(field.getValue());
                }
            }
        };


        typeBox.addActionListener(actionEvent -> refreshType());

        typeBox.setModel(new DefaultComboBoxModel(dataTypes));
        typeBox.setSelectedIndex(0);

        encodingBox.setModel(new DefaultComboBoxModel(charsets.toArray(new String[charsets.size()])));
        encodingBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    refreshingCollate = true;
                    cd.setCharset((String) encodingBox.getSelectedItem());
                    collateModel.setElements(loadCollates((String) encodingBox.getSelectedItem()));
                    refreshingCollate = false;
                    if (cd.getCollate() != null && collateModel.contains(cd.getCollate()))
                        collateBox.setSelectedItem(cd.getCollate());
                    else {
                        collateBox.setSelectedIndex(0);
                        cd.setCollate((String) collateBox.getSelectedItem());
                    }
                }
            }
        }); //
        collateModel = new DynamicComboBoxModel();
        collateBox.setModel(collateModel);
        collateBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (!refreshingCollate)
                        cd.setCollate((String) collateBox.getSelectedItem());
                }
            }
        });

        sizeField.addKeyListener(keyListener);
        scaleField.addKeyListener(keyListener);
        subtypeField.addKeyListener(keyListener);

        this.setLayout(new GridBagLayout());
        this.add(typeLabel, new GridBagConstraints(0, 0, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(sizeLabel, new GridBagConstraints(0, 1, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(scaleLabel, new GridBagConstraints(0, 2, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(subtypeLabel, new GridBagConstraints(0, 3, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(encodingLabel, new GridBagConstraints(0, 4, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(collateLabel, new GridBagConstraints(0, 5, 1, 1,
                0, 0, GridBagConstraints.NORTH,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        this.add(typeBox, new GridBagConstraints(1, 0, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(sizeField, new GridBagConstraints(1, 1, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(scaleField, new GridBagConstraints(1, 2, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(subtypeField, new GridBagConstraints(1, 3, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(encodingBox, new GridBagConstraints(1, 4, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(collateBox, new GridBagConstraints(1, 5, 1, 1,
                1, 0, GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0));
        this.add(typeOfPanel, new GridBagConstraints(0, 4, 2, 1,
                1, 1, GridBagConstraints.NORTH,
                GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
        // empty panel for stretch
        this.add(new JPanel(), new GridBagConstraints(0, 6, 2, 1,
                1, 1, GridBagConstraints.NORTH,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    private void refreshType() {
        int index = typeBox.getSelectedIndex();
        cd.setColumnType(dataTypes[index]);
        cd.setSQLType(intDataTypes[index]);
        setSizeVisible(cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                || cd.getSQLType() == Types.DECIMAL || cd.getSQLType() == Types.BLOB
                || cd.getSQLType() == Types.LONGVARBINARY || cd.getSQLType() == Types.LONGVARCHAR
                || cd.getColumnType().toUpperCase().equals("VARCHAR")
                || cd.getColumnType().toUpperCase().equals("CHAR"));
        setScaleVisible(cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL);
        setScaleVisible(cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL);
        setSubtypeVisible(cd.getSQLType() == Types.BLOB);
        setEncodingVisible(cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                || cd.getSQLType() == Types.LONGVARCHAR || cd.getSQLType() == Types.CLOB
                || cd.getColumnType().toUpperCase().equals("VARCHAR")
                || cd.getColumnType().toUpperCase().equals("CHAR"));

        if (cd.getSQLType() == Types.LONGVARBINARY || cd.getSQLType() == Types.LONGVARCHAR || cd.getSQLType() == Types.BLOB) {
            sizeField.setText("80");
        }
        if (cd.getSQLType() == Types.LONGVARBINARY)
            subtypeField.setText("0");
        if (cd.getSQLType() == Types.LONGVARCHAR)
            subtypeField.setText("1");
        if (cd.getSQLType() == Types.BLOB)
            subtypeField.setText("0");
    }

    public void refreshColumn() {
        cd.setColumnSize(sizeField.getValue());
        cd.setColumnScale(scaleField.getValue());
        cd.setColumnSubtype(subtypeField.getValue());
    }

    private void setSizeVisible(boolean flag) {
        sizeField.setEnabled(flag);
        //sizeLabel.setEnabled(flag);
        if (flag)
            sizeField.setValue(1);
        else sizeField.setValue(0);
        if (refreshing)
            sizeField.setValue(cd.getColumnSize());
        cd.setColumnSize(sizeField.getValue());
    }

    private void setScaleVisible(boolean flag) {
        scaleField.setEnabled(flag);
        //scaleLabel.setVisible(flag);
        if (flag) {
            scaleField.setValue(1);
        } else scaleField.setValue(0);
        if (refreshing)
            scaleField.setValue(cd.getColumnScale());
        cd.setColumnScale(scaleField.getValue());
    }

    private void setSubtypeVisible(boolean flag) {
        subtypeField.setEnabled(flag);
        if (flag) {
            subtypeField.setValue(1);
        } else subtypeField.setValue(0);
        if (refreshing)
            subtypeField.setValue(cd.getColumnSubtype());
        cd.setColumnSubtype(subtypeField.getValue());
    }

    private void setEncodingVisible(boolean flag) {
        encodingBox.setEnabled(flag);
        collateBox.setEnabled(flag);
        if (refreshing) {
            encodingBox.setSelectedItem(cd.getCharset());
            collateBox.setSelectedItem(cd.getCollate());
        }
        cd.setCharset((String) encodingBox.getSelectedItem());
        cd.setCollate((String) collateBox.getSelectedItem());
    }

    private void removeDuplicates() {
        if (types == null)
            types = new HashMap<>();
        else types.clear();
        java.util.List<String> newTypes = new ArrayList<>();
        List<Integer> newIntTypes = new ArrayList<>();
        String last = "";
        for (int i = 0; i < this.dataTypes.length; i++) {
            if (!newTypes.contains(this.dataTypes[i])) {
                newTypes.add(this.dataTypes[i]);
                newIntTypes.add(this.intDataTypes[i]);
                types.put(intDataTypes[i], dataTypes[i]);
                last = dataTypes[i];
            } else {
                types.put(intDataTypes[i], last);
            }
        }
        this.dataTypes = newTypes.toArray(new String[0]);
        this.intDataTypes = newIntTypes.stream().mapToInt(Integer::intValue).toArray();
    }

    private void sortTypes() {
        if (dataTypes != null) {
            for (int i = 0; i < dataTypes.length; i++) {
                for (int g = 0; g < dataTypes.length - 1; g++) {
                    int compare = dataTypes[g].compareTo(dataTypes[g + 1]);
                    if (compare > 0) {
                        int temp1 = intDataTypes[g];
                        String temp2 = dataTypes[g];
                        intDataTypes[g] = intDataTypes[g + 1];
                        dataTypes[g] = dataTypes[g + 1];
                        intDataTypes[g + 1] = temp1;
                        dataTypes[g + 1] = temp2;
                    }
                }
            }
        }
    }

    public void refresh() {
        refreshing = true;
        cd.setColumnType(getStringType(cd.getSQLType()));
        typeBox.setSelectedItem(cd.getColumnType());
        refreshType();
        refreshing = false;
    }

    private String getStringType(int x) {
        try {
            return types.get(x);
        } catch (Exception e) {
            Log.error(e.getMessage());
            return "";
        }
    }

    private void loadCharsets() {
        try {
            if (charsets == null)
                charsets = new ArrayList<>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n");
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    charsets.add(s);
            }
            java.util.Collections.sort(charsets);
            charsets.add(0, CreateTableSQLSyntax.NONE);

        } catch (Exception e) {
            Log.error("Error getting charsets for SelectTypePanel:", e);
        }
    }

    public void setColumnData(ColumnData columnData) {
        this.cd = columnData;
    }

    protected String[] loadCollates(String charset) {
        DefaultStatementExecutor sender = new DefaultStatementExecutor();
        sender.setDatabaseConnection(cd.getDatabaseConnection());
        List<String> collates = new ArrayList<>();
        collates.add(CreateTableSQLSyntax.NONE);
        String query = "SELECT RDB$COLLATION_NAME\n" +
                "FROM RDB$COLLATIONS CO LEFT JOIN RDB$CHARACTER_SETS CS ON CO.RDB$CHARACTER_SET_ID = CS.RDB$CHARACTER_SET_ID\n" +
                "WHERE CS.RDB$CHARACTER_SET_NAME='" + charset + "'";
        try {
            ResultSet rs = sender.getResultSet(query).getResultSet();
            while (rs.next()) {
                collates.add(rs.getString(1).trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }
        return collates.toArray(new String[collates.size()]);
    }
}
