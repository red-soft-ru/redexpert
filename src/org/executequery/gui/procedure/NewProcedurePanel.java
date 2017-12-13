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

    private StringBuffer sqlText;

    public List<String> descriptions;

    boolean primary;

    public NewProcedurePanel(int typeParameter) {
        super(typeParameter);
        sqlText = new StringBuffer(100);
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

    }

    @Override
    public void addColumnLines(int row) {

    }
}
