package org.executequery.components.table;

//import com.sun.istack.internal.Nullable;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * Created by mikhan808 on 26.04.2017.
 */
public class RoleTableModel extends DefaultTableModel{
    public RoleTableModel ()
    {
        super();
    }
    public RoleTableModel (int rowCount,int columnCount)
    {
        super(rowCount, columnCount);
    }
    public RoleTableModel (Vector columnNames,int rowCount)
    {
        super(columnNames,rowCount);
    }
    public RoleTableModel (Vector data,Vector columnNames)
    {
        super(data,columnNames);
    }
    public RoleTableModel (Object[] columnNames, int rowCount)
    {
        super(columnNames, rowCount);
    }
    public RoleTableModel ( Object[][] data, Object [] columnNames)
    {
        super(data,columnNames);
    }
    public boolean isCellEditable(int rowIndex,
                                  int columnIndex)
    {
        return false;
    }
}
