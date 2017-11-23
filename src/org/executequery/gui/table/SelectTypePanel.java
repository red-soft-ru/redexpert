package org.executequery.gui.table;

import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Types;
import java.util.*;
import java.util.List;

public class SelectTypePanel extends JPanel {
    private JLabel typeLabel;
    private JLabel sizeLabel;
    private JLabel scaleLabel;
    private JLabel encodingLabel;
    private JComboBox typeBox;
    private JComboBox encodingBox;
    private NumberTextField sizeField;
    private NumberTextField scaleField;

    String[] dataTypes;
    int[] intDataTypes;
    KeyListener keyListener;
    ColumnData cd;
    boolean refreshing = false;
    List<String> charsets;
    Map<Integer, String> types;

    public SelectTypePanel(String[] types, int[] intTypes, ColumnData cd) {
        this.dataTypes = types;
        this.intDataTypes = intTypes;
        sortTypes();
        removeDuplicates();
        this.cd = cd;
        loadCharsets();
        init();
    }

    void init() {
        typeLabel = new JLabel("Type");
        sizeLabel = new JLabel("Size");
        scaleLabel = new JLabel("Scale");
        encodingLabel = new JLabel("Encoding");
        typeBox = new JComboBox();
        encodingBox = new JComboBox();
        sizeField = new NumberTextField();
        scaleField = new NumberTextField();

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
                }
            }
        };


        typeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                refreshType();
            }
        });

        typeBox.setModel(new DefaultComboBoxModel(dataTypes));
        typeBox.setSelectedIndex(0);

        encodingBox.setModel(new DefaultComboBoxModel(charsets.toArray(new String[charsets.size()])));
        encodingBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                cd.setCharset((String) encodingBox.getSelectedItem());
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0.01, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1), 0, 0);
        this.add(typeLabel, gbc);
        gbc.gridy++;
        gbc.weighty = 0.01;
        this.add(sizeLabel, gbc);
        gbc.gridy++;
        this.add(scaleLabel, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        this.add(encodingLabel, gbc);
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.weightx = 0.3;
        gbc.weighty = 0;
        this.add(typeBox, gbc);
        gbc.gridy++;
        this.add(sizeField, gbc);
        gbc.gridy++;
        this.add(scaleField, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        this.add(encodingBox, gbc);
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.gridheight = 4;
        this.add(new JPanel(), gbc);
    }

    void refreshType() {
        int index = typeBox.getSelectedIndex();
        cd.setColumnType(dataTypes[index]);
        cd.setSQLType(intDataTypes[index]);
        setSizeVisible(cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                || cd.getSQLType() == Types.DECIMAL || cd.getSQLType() == Types.BLOB
                || cd.getColumnType().toUpperCase().equals("VARCHAR")
                || cd.getColumnType().toUpperCase().equals("CHAR"));
        setScaleVisible(cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL);
        setEncodingVisible(cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                ||cd.getSQLType()==Types.LONGVARCHAR || cd.getSQLType() == Types.CLOB
                || cd.getColumnType().toUpperCase().equals("VARCHAR")
                || cd.getColumnType().toUpperCase().equals("CHAR"));
    }

    public void refreshColumn() {
        cd.setColumnSize(sizeField.getValue());
        cd.setColumnScale(scaleField.getValue());
    }

    void setSizeVisible(boolean flag) {
        sizeField.setEnabled(flag);
        //sizeLabel.setEnabled(flag);
        if (flag)
            sizeField.setValue(1);
        else sizeField.setValue(0);
        if (refreshing)
            sizeField.setValue(cd.getColumnSize());
        cd.setColumnSize(sizeField.getValue());
    }

    void setScaleVisible(boolean flag) {
        scaleField.setEnabled(flag);
        //scaleLabel.setVisible(flag);
        if (flag) {
            scaleField.setValue(1);
        } else scaleField.setValue(0);
        if (refreshing)
            scaleField.setValue(cd.getColumnScale());
        cd.setColumnScale(scaleField.getValue());
    }

    void setEncodingVisible(boolean flag) {
        encodingBox.setEnabled(flag);
        if (refreshing)
            encodingBox.setSelectedItem(cd.getCharset());
        cd.setCharset((String) encodingBox.getSelectedItem());
    }

    void removeDuplicates() {
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

    void sortTypes() {
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

    String getStringType(int x) {
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
                charsets = new ArrayList<String>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n"/*System.getProperty("line.separator")*/);
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    charsets.add(s);
            }
            java.util.Collections.sort(charsets);
            charsets.add(0, CreateTableSQLSyntax.NONE);

        } catch (Exception e) {
            Log.error("Error getting charsets for SelectTypePanel:",e);
            return;
        }
    }
}
