package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.swing.print.AbstractPrintableTableModel;

import javax.swing.*;
import java.awt.*;
import java.sql.Types;
import java.util.Vector;

public class CreateProcedurePanel extends JPanel {

    public static final int NAME_COLUMN = 0;
    public static final int TYPE_COLUMN = 1;
    public static final int TYPE_OF = 2;
    public static final int DOMAIN_COLUMN = 3;
    public static final int SIZE_COLUMN = 4;
    public static final int SCALE_COLUMN = 5;

    private JPanel upPanel;
    private JPanel inputParametersPanel;
    private JPanel outputParametersPanel;
    private JPanel variablesPanel;
    private JPanel cursorsPanel;
    private JScrollPane sqlScroll;
    private SQLTextPane sqlTextPane;
    private JButton okButton;
    private JButton cancelButton;
    private JTabbedPane tabbedPane;

    DatabaseConnection connection;


    public CreateProcedurePanel ()
    {
        init();
    }

    void init()
    {
        upPanel=new JPanel();
        inputParametersPanel=new JPanel();
        outputParametersPanel=new JPanel();
        variablesPanel=new JPanel();
        cursorsPanel=new JPanel();
        sqlScroll=new JScrollPane();
        sqlTextPane = new SQLTextPane();
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx =0;
        gbc.gridy =0;
        gbc.weightx =0;
        gbc.weighty =0;
        gbc.gridheight=1;
        gbc.gridwidth=1;
        gbc.insets = new Insets(1,1,1,1);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx=0;
        gbc.ipady=0;

        this.setLayout(new GridBagLayout());
        this.add(upPanel,gbc);
        gbc.gridy++;
        this.add(tabbedPane,gbc);
        gbc.gridy++;
        this.add(okButton,gbc);
        gbc.gridx++;
        this.add(cancelButton,gbc);


    protected class ParametersModel extends AbstractPrintableTableModel {

        protected String[] header = { "Name", "Datatype", "Domain",
                "Size", "Scale", "Required",
                "Description",  "Default Value",
                "Encoding"};

        public Vector<ColumnData> tableVector;

        public ParametersModel() {
            tableVector = new Vector<ColumnData>();
            tableVector.addElement(new ColumnData(connection));
        }



        public void setColumnDataArray(ColumnData[] cda) {

            if (cda != null) {
                if (tableVector == null) {
                    tableVector = new Vector<ColumnData>(cda.length);
                } else {
                    tableVector.clear();
                }

                for (int i = 0; i < cda.length; i++) {
                    tableVector.add(cda[i]);
                }
            } else {
                tableVector.clear();
            }

            fireTableDataChanged();
        }

        public int getColumnCount() {
            return header.length;
        }

        public int getRowCount() {
            return tableVector.size();
        }

        /**
         * Returns the printable value at the specified row and column.
         *
         * @param row - the row index
         * @param col - the column index
         * @return the value to print
         */
        public String getPrintValueAt(int row, int col) {
            if (col != PK_COLUMN && col >= 0) {
                Object value = getValueAt(row, col);
                if (value != null) {
                    return value.toString();
                }
                return EMPTY;
            } else if (col == PK_COLUMN) {
                ColumnData cd = tableVector.elementAt(row);
                if (cd.isPrimaryKey()) {
                    if (cd.isForeignKey()) {
                        return "PFK";
                    }
                    return "PK";
                } else if (cd.isForeignKey()) {
                    return "FK";
                }
                return EMPTY;
            } else return EMPTY;
        }

        public Object getValueAt(int row, int col) {

            if (row >= tableVector.size()) {
                return null;
            }
            ColumnData cd = tableVector.elementAt(row);

            switch (col) {

                case PK_COLUMN:
                    return cd;
                case NAME_COLUMN:
                    return cd.getColumnName();

                case TYPE_COLUMN:
                    return cd.getColumnType();

                case DOMAIN_COLUMN:
                    return cd.getDomain();

                case SIZE_COLUMN:
                    return Integer.valueOf(cd.getColumnSize());

                case SCALE_COLUMN:
                    return Integer.valueOf(cd.getColumnScale());

                case REQUIRED_COLUMN:
                    return Boolean.valueOf(cd.isRequired());

                case CHECK_COLUMN:
                    return cd.getCheck();

                case DESCRIPTION_COLUMN:
                    return cd.getDescription();

                case COMPUTED_BY_COLUMN:
                    return cd.getComputedBy();

                case DEFAULT_COLUMN:
                    return cd.getDefaultValue();

                case AUTOINCREMENT_COLUMN:
                    return cd.isAutoincrement();

                case ENCODING_COLUMN:
                    return cd.getCharset();

                default:
                    return null;

            }
        }

        public void setValueAt(Object value, int row, int col) {
            ColumnData cd = tableVector.elementAt(row);

            //Log.debug("setValueAt [row: "+row+" col: "+col+" value: "+value+"]");

            switch (col) {
                case PK_COLUMN:
                    if (cd.isPrimaryKey()) {
                        cd.setKeyType(PRIMARY);
                    } else if (cd.isForeignKey()) {
                        cd.setKeyType(FOREIGN);
                    } else {
                        cd.setKeyType(null);
                    }
                    break;
                case NAME_COLUMN:
                    cd.setColumnName((String) value);
                    break;
                case TYPE_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setColumnType((String) value);
                        if (cd.getSQLType() != cd.getDomainType()) {
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt("0", row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt("0", row, SCALE_COLUMN);
                        } else {
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSize()), row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnScale()), row, SCALE_COLUMN);
                        }
                    } else {
                        cd.setColumnType(dataTypes[(int) value]);
                        cd.setSQLType(intDataTypes[(int) value]);
                        if (cd.getSQLType() != cd.getDomainType()) {
                            _model.setValueAt("", row, DOMAIN_COLUMN);
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt("0", row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt("0", row, SCALE_COLUMN);
                        } else {
                            if (!isEditSize(row))
                                _model.setValueAt("-1", row, SIZE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnSize()), row, SIZE_COLUMN);
                            if (!isEditScale(row))
                                _model.setValueAt("-1", row, SCALE_COLUMN);
                            else
                                _model.setValueAt(String.valueOf(cd.getColumnScale()), row, SCALE_COLUMN);
                        }
                        if (!isEditEncoding(row))
                            cd.setCharset(charsets.get(0));


                    }
                    break;
                case DOMAIN_COLUMN:
                    if (value.getClass() == String.class) {
                        cd.setDomain((String) value);
                    } else {
                        cd.setDatabaseConnection(dc);
                        cd.setDomain(domains[(int) value]);
                        cd.setColumnType(getStringType(cd.getDomainType()));
                        _model.setValueAt(cd.getColumnType(), row, TYPE_COLUMN);
                    }
                    break;
                case SIZE_COLUMN:
                    cd.setColumnSize(Integer.parseInt((String) value));
                    break;
                case SCALE_COLUMN:
                    cd.setColumnScale(Integer.parseInt((String) value));
                    break;
                case REQUIRED_COLUMN:
                    cd.setColumnRequired(((Boolean) value).booleanValue() ? 0 : 1);
                    break;
                case CHECK_COLUMN:
                    cd.setCheck((String) value);
                    break;
                case DESCRIPTION_COLUMN:
                    cd.setDescription((String) value);
                    break;
                case COMPUTED_BY_COLUMN:
                    cd.setComputedBy((String) value);
                    break;
                case DEFAULT_COLUMN:
                    cd.setDefaultValue((String) value);
                case AUTOINCREMENT_COLUMN:
                    break;
                case ENCODING_COLUMN:
                    cd.setCharset((String) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        String getStringType(int x) {
            for (int i = 0; i < intDataTypes.length; i++)
                if (x == intDataTypes[i])
                    return dataTypes[i];
            return "";
        }

        boolean isEditEncoding(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return isEditSize(row) && cd.getSQLType() != Types.NUMERIC && cd.getSQLType() != Types.DECIMAL && cd.getSQLType() != Types.BLOB;
        }

        boolean isEditSize(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getColumnType() != null && (cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.CHAR || cd.getSQLType() == Types.VARCHAR
                    || cd.getSQLType() == Types.DECIMAL || cd.getSQLType() == Types.BLOB
                    || cd.getColumnType().toUpperCase().equals("VARCHAR")
                    || cd.getColumnType().toUpperCase().equals("CHAR"));
        }

        boolean isEditScale(int row) {
            ColumnData cd = tableVector.elementAt(row);
            return cd.getSQLType() == Types.NUMERIC || cd.getSQLType() == Types.DECIMAL;
        }


        public boolean isCellEditable(int row, int col) {
            if (editing)
                switch (col) {
                    case PK_COLUMN:
                        return false;
                    case SIZE_COLUMN:
                        return isEditSize(row);
                    case SCALE_COLUMN:
                        return isEditScale(row);
                    case AUTOINCREMENT_COLUMN:
                        return false;
                    case ENCODING_COLUMN:
                        return isEditEncoding(row);
                    default:
                        return editing;
                }
            else return editing;


        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Class getColumnClass(int col) {
            if (col == REQUIRED_COLUMN || col == AUTOINCREMENT_COLUMN) {
                return Boolean.class;
            } else if (col == SIZE_COLUMN || col == SCALE_COLUMN) {
                return Integer.class;
            } else {
                return String.class;
            }
        }

        public void addNewRow() {
            ColumnData cd = tableVector.lastElement();
            if (!cd.isNewColumn()) {
                tableVector.addElement(new ColumnData(true, dc));
            }

        }

    }

}
