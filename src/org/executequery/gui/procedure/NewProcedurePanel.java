package org.executequery.gui.procedure;

import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.table.TableFunction;
import org.executequery.gui.table.TableModifier;
import org.underworldlabs.util.MiscUtils;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vasiliy
 */
public class NewProcedurePanel extends ProcedureDefinitionPanel
        implements CreateTableSQLSyntax {

    /**
     * The table creator object - parent to this
     */
    private CreateProcedureFunctionPanel creator;

    /**
     * The buffer for the current line
     */
    private StringBuffer line;

    /**
     * The buffer off all SQL generated
     */
    private StringBuffer sqlText;

    private StringBuffer primaryText;

    public List<String> descriptions;

    boolean primary;

    public NewProcedurePanel(CreateProcedureFunctionPanel creator) {
        super();
        this.creator = creator;

        line = new StringBuffer(50);
        sqlText = new StringBuffer(100);
        primaryText = new StringBuffer(50);
    }

    /**
     * Returns the SQL scriptlet text.
     *
     * @return the SQL text
     */
    public String getSQLText() {
        return sqlText.toString();
    }

    /**
     * Resets the SQL text.
     */
    public void resetSQLText() {
        addColumnLines(-1);
    }

    /**
     * Indicates that the table value for the specified row and
     * column has changed to the value specified.
     *
     * @param col   - the last updated col
     * @param row   - the last updated row
     * @param value - the new value
     */
    public void tableChanged(int col, int row, String value) {

        //Log.debug("tableChanged [row: "+row+" col: "+col+" value: "+value + "]");

        if (value == null) {
            //updateScript(row, col);
            return;
        }

        //if (row == -1 || (col == 1 && value == null)) {
        if (row == -1) {// || (col == 1 && value == null)) {
            return;
        }

        ColumnData cd = (ColumnData) tableVector.get(row);
        switch (col) {
            case NAME_COLUMN:
                cd.setColumnName(value);
                break;
            case TYPE_COLUMN:
                cd.setColumnType(value);
                break;
            case DOMAIN_COLUMN:
                cd.setDomain(value);
                break;
            case SIZE_COLUMN:
                if (!MiscUtils.isNull(value)) {
                    int _value = Integer.parseInt(value);
                    cd.setColumnSize(_value);
                }
                break;
            case SCALE_COLUMN:
                if (!MiscUtils.isNull(value)) {
                    int _value = Integer.parseInt(value);
                    cd.setColumnScale(_value);
                }
                break;
        }
        //updateScript(row, col);
    }

    /**
     * Updates the generated scriptlet using the specified
     * row and col as the last upfdaed/modified value.
     *
     * @param row - the last updated row
     * @param col - the last updated col
     */
    private void updateScript(int row, int col) {
        line.setLength(0);
        ColumnData cd = (ColumnData) tableVector.get(row);
        line.setLength(0);
        line.append(NEW_LINE_2).
                append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getColumnName()).
                append(SPACE);
        if (MiscUtils.isNull(cd.getComputedBy())) {
            if (MiscUtils.isNull(cd.getDomain())) {
                if (cd.getColumnType() != null) {
                    line.append(cd.getFormattedDataType());
                }
            } else {
                line.append(cd.getDomain());
            }
            if (!MiscUtils.isNull(cd.getDefaultValue())) {
                String value = "";
                boolean str = false;
                int sqlType = cd.getSQLType();
                switch (sqlType) {

                    case Types.LONGVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.VARCHAR:
                    case Types.NVARCHAR:
                    case Types.CLOB:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        value = "'";
                        str = true;
                        break;
                    default:
                        break;
                }
                value += cd.getDefaultValue();
                if (str) {
                    value += "'";
                }
                line.append(" DEFAULT " + value);
            }
            line.append(cd.isRequired() ? NOT_NULL : CreateTableSQLSyntax.EMPTY);
            if (!MiscUtils.isNull(cd.getCheck())) {
                line.append(" CHECK ( " + cd.getCheck() + ")");
            }
        } else {
            line.append("COMPUTED BY ( " + cd.getComputedBy() + ")");
        }
        if (row < tableVector.size() - 1) {
            line.append(COMMA);
        }

        if (cd.isNewColumn()) {
            cd.setNewColumn(false);
        }

        addColumnLines(row);
    }

    /**
     * Adds all the column definition lines to
     * the SQL text buffer for display.
     *
     * @param row current row being edited
     */
    public void addColumnLines(int row) {

        sqlText.setLength(0);
        primary = false;
        primaryText.setLength(0);
        descriptions = new ArrayList<>();
        for (int i = 0, k = tableVector.size(); i < k; i++) {
            ColumnData cd = (ColumnData) tableVector.elementAt(i);
            if (cd.isPrimaryKey()) {
                if (primary)
                    primaryText.append(", ");
                else primaryText.append(" ");
                primaryText.append(cd.getColumnName());
                primary = true;
            }
            if (!MiscUtils.isNull(cd.getDescription())) {
                descriptions.add(cd.getColumnName() + " is '" + cd.getDescription() + "'");
            }
            if (i == row) {
                sqlText.append(line);
            } else if (!cd.isNewColumn()) {

                sqlText.append(NEW_LINE_2).append(
                        cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getColumnName()).
                        append(SPACE);
                if (MiscUtils.isNull(cd.getComputedBy())) {
                    if (MiscUtils.isNull(cd.getDomain())) {
                        if (cd.getColumnType() != null) {
                            sqlText.append(cd.getColumnType().toUpperCase());

                            if (cd.getColumnSize() != -1) {
                                sqlText.append(B_OPEN).append(cd.getColumnSize());

                                if (cd.getColumnScale() != -1) {
                                    sqlText.append(COMMA).append(cd.getColumnScale());
                                }

                                sqlText.append(B_CLOSE);
                            }

                        }
                    } else {
                        sqlText.append(cd.getDomain());
                    }
                    if (!MiscUtils.isNull(cd.getDefaultValue())) {
                        String value = "";
                        boolean str = false;
                        int sqlType = cd.getSQLType();
                        switch (sqlType) {

                            case Types.LONGVARCHAR:
                            case Types.LONGNVARCHAR:
                            case Types.CHAR:
                            case Types.NCHAR:
                            case Types.VARCHAR:
                            case Types.NVARCHAR:
                            case Types.CLOB:
                            case Types.DATE:
                            case Types.TIME:
                            case Types.TIMESTAMP:
                                value = "'";
                                str = true;
                                break;
                            default:
                                break;
                        }
                        value += cd.getDefaultValue();
                        if (str) {
                            value += "'";
                        }
                        sqlText.append(" DEFAULT " + value);
                    }
                    sqlText.append(cd.isRequired() ? NOT_NULL : CreateTableSQLSyntax.EMPTY);
                    if (!MiscUtils.isNull(cd.getCheck())) {
                        sqlText.append(" CHECK ( " + cd.getCheck() + ")");
                    }
                } else {
                    sqlText.append("COMPUTED BY ( " + cd.getComputedBy() + ")");
                }
                if (i != k - 1) {
                    sqlText.append(COMMA);
                }

            }


        }

        creator.setSQLText(sqlText.toString(), TableModifier.COLUMN_VALUES);

    }

    public String getPrimaryText() {
        if (primary)
            return primaryText.toString();
        else return "";
    }

//    // -----------------------------------------------
//    // -------- TableFunction implementations --------
//    // -----------------------------------------------
//
//    public void moveColumnUp() {
////        NewProcedurePanel panel = (NewProcedurePanel)parametersTabs.getSelectedComponent();
////        panel.get
////        int index = ().getSelectedIndex();
////        if (index == 0) {
//        this.moveColumnUp();
////        }
//    }
//
//    public void moveColumnDown() {
////        int index = parametersTabs.getSelectedIndex();
////        if (index == 0) {
////            inputParametersPanel.moveColumnDown();
////        }
//        this.moveColumnDown();
////        panel.moveColumnDown();
//    }
//
//    public void deleteRow() {
////        if (parametersTabs.getSelectedIndex() == 0) {
////            inputParametersPanel.deleteRow();
////        }
////        NewProcedurePanel panel = (NewProcedurePanel)parametersTabs.getSelectedComponent();
//        deleteRow();
//    }
//
//    public void insertBefore() {
////        inputParametersPanel.insertBefore();
//        NewProcedurePanel panel = (NewProcedurePanel)parametersTabs.getSelectedComponent();
//        panel.insertBefore();
//    }
//
//    public void insertAfter() {
////        if (parametersTabs.getSelectedIndex() == 0) {
////            inputParametersPanel.insertAfter();
////        }
//        NewProcedurePanel panel = (NewProcedurePanel)parametersTabs.getSelectedComponent();
//        panel.insertAfter();
//    }
}
