/*
 * ErdTable.java
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

import org.apache.batik.ext.awt.g2d.DefaultGraphics2D;
import org.apache.commons.lang.ArrayUtils;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdTable extends ErdMoveableComponent
        implements Serializable {

    /**
     * The table name displayed
     */
    private String tableName;
    private static final String UNIQUE = "(UQ) ";
    /**
     * The table comment
     */
    private String descriptionTable;
    /**
     * The table's original columns
     */
    private ColumnData[] originalData;

    /**
     * The CREATE TABLE script for a new table
     */
    private String createTableScript;
    /**
     * The ALTER TABLE script for a definition change
     */
    private String alterTableScript;
    /**
     * The ALTER TABLE script for a constraint change
     */
    private String addConstraintScript;
    /**
     * The ALTER TABLE script for a constraint drop
     */
    private String dropConstraintScript;
    /**
     * <code>Hashtable</code> containing table modifications
     */
    private Hashtable alterTableHash;

    /**
     * Whether this is a new table
     */
    private boolean newTable;

    private boolean editable;
    /**
     * The table's columns
     */
    private ColumnData[] columns;
    private boolean showCommentOnTable;
    private String[] descriptionLines;


    /**
     * This components calculated width
     */
    private int FINAL_WIDTH = -1;
    /**
     * This components calculated height
     */
    private int FINAL_HEIGHT = -1;
    /**
     * The height of the title bar
     */
    private static int TITLE_BAR_HEIGHT = 20;

    private boolean displayReferencedKeysOnly;


    private static final String PRIMARY = "(PK) ";
    private static final String FOREIGN = "(FK)";
    private boolean showCommentOnFields;

    private int titleBarBgColor = 0;


    private Map<ColumnData, ColumnJoins> joins;

    protected static final int LEFT_JOIN = 0;
    protected static final int RIGHT_JOIN = 1;
    private int keyWidth;

    private int dataTypeOffset;
    private int keyLabelOffset;
    /**
     * <p>Constructs a new instance with the specified
     * table name and <code>ErdViewerPanel</code> as the
     * paent controller object.
     *
     * @param the table name displayed
     * @param the <code>ErdViewerPanel</code> controller object
     */
    public ErdTable(String tableName, ColumnData[] columns,
                    ErdViewerPanel parent) {
        super(parent);
        this.columns = columns;
        this.tableName = tableName.toUpperCase();

        newTable = false;
        editable = false;
        showCommentOnTable = false;
        showCommentOnFields = false;
        displayReferencedKeysOnly = parent.isDisplayKeysOnly();
        tableBackground = UIUtils.getColour("executequery.Erd.tableBackground", Color.WHITE);

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>Initialises the state of this instance.
     */
    private void jbInit() throws Exception {
        dataTypeOffset = 0;
        keyLabelOffset = 0;
        Font tableNameFont = parent.getTableNameFont();
        Font columnNameFont = parent.getColumnNameFont();

        FontMetrics fmColumns = getFontMetrics(columnNameFont);
        FontMetrics fmTitle = getFontMetrics(tableNameFont);
        int commentWidth = 0;
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                ColumnData column = columns[i];
                int valueWidth = 10;
                if (column.getColumnName() != null)
                    valueWidth = fmColumns.stringWidth(column.getColumnName());
                dataTypeOffset = Math.max(dataTypeOffset, valueWidth);
                if (column.getFormattedColumnName() != null)
                    valueWidth = fmColumns.stringWidth(column.getFormattedDataType());
                keyLabelOffset = Math.max(keyLabelOffset, valueWidth);
                if (showCommentOnFields && column.getRemarks() != null) {
                    valueWidth = fmColumns.stringWidth("/*" + column.getRemarks() + "*/");
                    commentWidth = Math.max(commentWidth, valueWidth);
                }
            }
        }

        // add a further offset to the data type and key label offsets
        dataTypeOffset += 10;
        keyLabelOffset += 2;

        keyWidth = fmColumns.stringWidth(PRIMARY + FOREIGN);
        int maxWordLength = dataTypeOffset + keyLabelOffset + keyWidth + commentWidth + 10;

        // compare to the title length
        maxWordLength = Math.max(fmTitle.stringWidth(tableName), maxWordLength);

        // add 20px to the final width
        if (FINAL_WIDTH < 0) {
            FINAL_WIDTH = maxWordLength;// + 20;

            if (ArrayUtils.isEmpty(columns)) {

                FINAL_WIDTH += 80;
            }
        }

        // minimum width is 130px
        //      if (FINAL_WIDTH < 130)
        //        FINAL_WIDTH = 130;

        TITLE_BAR_HEIGHT = fmTitle.getHeight() + 5;

        int keysCount = 0;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].isKey()) {
                keysCount++;
            }
        }
        int commentLines = 0;
        if (FINAL_HEIGHT < 0) {
            if (showCommentOnTable && !MiscUtils.isNull(getDescriptionTable())) {
                partitionDescription(getGraphics(), FINAL_WIDTH - 10);
                commentLines = descriptionLines.length + 2;
            }
            int commentHeight = fmColumns.getHeight() * commentLines;
            if (columns.length > 0) {
                if (displayReferencedKeysOnly) {
                    if (keysCount > 0) {
                        FINAL_HEIGHT = (fmColumns.getHeight() * keysCount) +
                                TITLE_BAR_HEIGHT + 10;
                    } else {
                        FINAL_HEIGHT = fmColumns.getHeight() + TITLE_BAR_HEIGHT + 8;
                    }
                } else {
                    FINAL_HEIGHT = (fmColumns.getHeight() * columns.length) +
                            TITLE_BAR_HEIGHT + 10;
                }
            } else {
                // have one blank row (column) on the table
                FINAL_HEIGHT = fmColumns.getHeight() + TITLE_BAR_HEIGHT + 10;
            }
            FINAL_HEIGHT += commentHeight;
        }
        joins = new HashMap<>();


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                originalData = new ColumnData[columns.length];

                for (int i = 0; i < columns.length; i++) {
                    originalData[i] = new ColumnData(columns[i].getConnection());
                    originalData[i].setValues(columns[i]);
                }

            }
        });

    }

    private void partitionDescription(Graphics g, int textWidth) {

        FontMetrics fm = null;
        if (g == null) {

            fm = getFontMetrics(parent.getColumnNameFont());

        } else {

            fm = g.getFontMetrics(parent.getColumnNameFont());
        }

        List<String> description = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(getDescriptionTable(), " ", true);
        StringBuilder sb = new StringBuilder();

        int length = 0;
        int currentLength = 0;

        while (st.hasMoreTokens()) {

            String _text = st.nextToken();
            length = fm.stringWidth(_text);

            if (currentLength + length <= textWidth) {

                sb.append(_text);
                currentLength += length;

            } else {

                description.add(sb.toString().trim());

                sb.setLength(0);
                if (length >= textWidth) {
                    List<String> texts = partitionToken(g, _text, textWidth);
                    description.addAll(texts);
                    currentLength = 0;
                } else {

                    currentLength = length;
                    sb.append(_text);
                }
            }

            _text = null;

        }

        if (sb.length() > 0) {

            description.add(sb.toString().trim());
        }

        descriptionLines = description.toArray(new String[description.size()]);

    }

    protected List<String> partitionToken(Graphics g, String text, int width) {
        FontMetrics fm = null;
        if (g == null) {

            fm = getFontMetrics(parent.getColumnNameFont());

        } else {

            fm = g.getFontMetrics(parent.getColumnNameFont());
        }
        List<String> list = new ArrayList<>();
        int ind = text.length() / 2;
        String text1 = text.substring(0, ind);
        String text2 = text.substring(ind);
        int length = fm.stringWidth(text1);
        if (length >= width)
            list.addAll(partitionToken(g, text1, width));
        else list.add(text1);
        length = fm.stringWidth(text2);
        if (length >= width)
            list.addAll(partitionToken(g, text2, width));
        else list.add(text2);
        return list;
    }


    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets whether this table is editable or not.
     *
     * @param editable <code>true</code> | <code>false</coe>
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setDisplayReferencedKeysOnly(boolean display) {
        displayReferencedKeysOnly = display;
    }

    public void tableColumnsChanged(boolean resetBounds) {
        resetAllJoins();
        if (resetBounds) {
            resetBounds();
        }
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaint();
        revalidate();
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        FINAL_HEIGHT = height;
        FINAL_WIDTH = width;
    }

    /**
     * <p>Returns the <code>Hashtable</code> containing
     * ALTER TABLE SQL script changes for this table.
     *
     * @return the ALTER TABLE <code>Hashtable</code>
     */
    public Hashtable getAlterTableHash() {
        return alterTableHash;
    }

    /**
     * <p>Sets the <code>Hashtable</code> containing
     * ALTER TABLE SQL script changes for this table.
     *
     * @return the ALTER TABLE <code>Hashtable</code>
     */
    public void setAlterTableHash(Hashtable alterTableHash) {
        this.alterTableHash = alterTableHash;
    }

    /**
     * <p>Returns a concatenation of all SQL scipts
     * generated for this table, if any. The order of
     * the scripts returned is CREATE TABLE, ALTER TABLE
     * (table definition), ALTER TABLE (table constraints).
     *
     * @return all this table's generated SQL scripts
     */
    public String getAllSQLScripts() {
        String EMPTY = "";

        return (createTableScript == null ? EMPTY : createTableScript) +
                (alterTableScript == null ? EMPTY : alterTableScript) +
                (addConstraintScript == null ? EMPTY : addConstraintScript) +
                (dropConstraintScript == null ? EMPTY : dropConstraintScript);
    }

    /**
     * <p>Returns whether this table having changes
     * made to its definition has an SQL script.
     *
     * @return <code>true</code> if a script is available |
     * <code>false</code> otherwise
     */
    public boolean hasSQLScripts() {
        return createTableScript != null ||
                alterTableScript != null ||
                addConstraintScript != null ||
                dropConstraintScript != null;
    }

    /**
     * <p>Returns the ALTER TABLE script for this table
     * for table definition changes only - ie. column name,
     * datatype changes and so forth.
     *
     * @return the ALTER TABLE script
     */
    public String getAlterTableScript() {
        return alterTableScript;
    }

    /**
     * <p>Sets the ALTER TABLE script for this table
     * for table definition changes only - ie. column name,
     * datatype changes and so forth.
     *
     * @return the ALTER TABLE script
     */
    public void setAlterTableScript(String alterTableScript) {
        this.alterTableScript = alterTableScript;
    }

    /**
     * <p>Returns the ALTER TABLE script for this table
     * for relationship/constraint changes only.
     *
     * @return the ALTER TABLE script
     */
    public String getAddConstraintsScript() {
        return addConstraintScript;
    }

    /**
     * <p>Sets the ALTER TABLE script for this table
     * for relationship/constraint changes only.
     *
     * @return the ALTER TABLE script
     */
    public void setAddConstraintsScript(String addConstraintScript) {

        if (this.addConstraintScript == null)
            this.addConstraintScript = addConstraintScript;

        else
            this.addConstraintScript += addConstraintScript;

    }

    /**
     * <p>Returns the ALTER TABLE script for this table
     * for relationship/constraint drops only.
     *
     * @return the ALTER TABLE script
     */
    public String getDropConstraintsScript() {
        return dropConstraintScript;
    }

    /**
     * <p>Sets the ALTER TABLE script for this table
     * for relationship/constraint drop only.
     *
     * @return the ALTER TABLE script
     */
    public void setDropConstraintsScript(String dropConstraintScript) {

        if (this.dropConstraintScript == null) {
            this.dropConstraintScript = dropConstraintScript;
        } else {
            this.dropConstraintScript += dropConstraintScript;
        }

    }

    /**
     * <p>Returns the CREATE TABLE script for this table.
     *
     * @return the CREATE TABLE script
     */
    public String getCreateTableScript() {
        return createTableScript;
    }

    /**
     * <p>Sets the CREATE TABLE script for this table.
     *
     * @return the CREATE TABLE script
     */
    public void setCreateTableScript(String createTableScript) {
        this.createTableScript = createTableScript;
    }

    /**
     * <p>Notifies this table that all changes have been
     * commited to the database and all SQL script values
     * can be reset.
     */
    public void changesCommited() {
        alterTableHash = null;
        createTableScript = null;
        alterTableScript = null;
        dropConstraintScript = null;
        addConstraintScript = null;
        newTable = false;
    }

    public void resetBounds() {
        FINAL_WIDTH = -1;
        FINAL_HEIGHT = -1;
    }

    /**
     * <p>Sets this table as a newly created table - ie.
     * this table does not exist as yet in the database
     * and is only a part of the ERD or sets this table as
     * an existing table within the database after the CREATE
     * TABLE script has been successfully executed.
     *
     * @param <code>true</code> to set this as a new table |
     *                          <code>false</code> otherwise
     */
    public void setNewTable(boolean newTable) {
        this.newTable = newTable;
    }

    /**
     * <p>Returns whether this table is a new table - ie.
     * this table does not exist as yet in the database
     * and is only a part of the ERD.
     */
    public boolean isNewTable() {
        return newTable;
    }

    public void setParentContainer(ErdViewerPanel parent) {
        this.parent = parent;
    }

    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        drawTable(g2d, 0, 0);
    }

    protected void drawTable(Graphics2D g, int offsetX, int offsetY) {

        if (parent == null) {

            return;
        }

        Font tableNameFont = parent.getTableNameFont();
        Font columnNameFont = parent.getColumnNameFont();

        // set the table value background
        g.setColor(ErdViewerPanel.TITLE_COLORS[titleBarBgColor]);
        g.fillRect(offsetX, offsetY, FINAL_WIDTH - 1, TITLE_BAR_HEIGHT);

        // set the table value
        FontMetrics fm = g.getFontMetrics(tableNameFont);
        int lineHeight = fm.getHeight();
        int titleXPosn = (FINAL_WIDTH / 2) - (fm.stringWidth(tableName) / 2) + offsetX;

        g.setColor(Color.BLACK);
        g.setFont(tableNameFont);
        g.drawString(tableName, titleXPosn, lineHeight + offsetY);

        // draw the line separator
        lineHeight = TITLE_BAR_HEIGHT + offsetY - 1;
        g.drawLine(offsetX + 1, lineHeight, offsetX + FINAL_WIDTH - 1, lineHeight);

        // fill the white background
        g.setColor(tableBackground);
        g.fillRect(offsetX,
                TITLE_BAR_HEIGHT + offsetY,
                FINAL_WIDTH - 1,
                FINAL_HEIGHT - TITLE_BAR_HEIGHT - 1);

        // add the column names
        fm = g.getFontMetrics(columnNameFont);
        int heightPlusSep = 1 + TITLE_BAR_HEIGHT + offsetY;
        int leftMargin = 5 + offsetX;

        lineHeight = fm.getHeight();
        g.setColor(Color.BLACK);
        g.setFont(columnNameFont);

        int drawCount = 0;
        String value = null;
        if (ArrayUtils.isNotEmpty(columns)) {
            for (int i = 0; i < columns.length; i++) {
                ColumnData column = columns[i];
                if (displayReferencedKeysOnly && !column.isKey()) {
                    continue;
                }

                int y = (((drawCount++) + 1) * lineHeight) + heightPlusSep;
                int x = leftMargin;
                // draw the column value string
                value = column.getColumnName();
                if (value != null)
                    g.drawString(value, x, y);

                // draw the data type and size string
                x = leftMargin + dataTypeOffset;
                value = column.getFormattedDataType();
                if (value != null)
                    g.drawString(value, x, y);

                // draw the key label
                if (column.isKey()) {

                    if (column.isPrimaryKey() && column.isForeignKey()) {

                        value = PRIMARY + FOREIGN;

                    } else if (column.isUniqueKey() && column.isForeignKey()) {

                        value = UNIQUE + FOREIGN;

                    } else if (column.isPrimaryKey()) {

                        value = PRIMARY;

                    } else if (column.isForeignKey()) {

                        value = FOREIGN;
                    } else if (column.isUniqueKey()) {

                        value = UNIQUE;
                    }

                    x = leftMargin + dataTypeOffset + keyLabelOffset;
                    g.drawString(value, x, y);
                }
                if (isShowCommentOnFields() && column.getRemarks() != null) {
                    x = leftMargin + dataTypeOffset + keyLabelOffset + keyWidth;
                    g.drawString("/*" + column.getRemarks() + "*/", x, y);
                }

            }

        }
        if (showCommentOnTable && descriptionLines != null) {
            int x = leftMargin;
            int y = (((drawCount++) + 1) * lineHeight) + heightPlusSep;
            g.drawString("/*", x, y);
            for (int i = 0; i < descriptionLines.length; i++) {
                y = (((drawCount++) + 1) * lineHeight) + heightPlusSep;
                g.drawString(descriptionLines[i], x, y);
            }
            y = (((drawCount++) + 1) * lineHeight) + heightPlusSep;
            g.drawString("*/", x, y);
        }

        // draw the rectangle border
        double scale = g.getTransform().getScaleX();

        if (selected && scale != ErdPrintable.PRINT_SCALE) {
            g.setStroke(focusBorderStroke);
            g.setColor(Color.BLUE);
        } else {
            g.setColor(Color.BLACK);
        }

        g.drawRect(offsetX + 1, offsetY + 1, FINAL_WIDTH - 2, FINAL_HEIGHT - 2);
        //    g.setColor(Color.DARK_GRAY);
        //    g.draw3DRect(offsetX, offsetY, FINAL_WIDTH - 2, FINAL_HEIGHT - 2, true);
    }

    /**
     * <p>Resets all of this component's joins.
     */
    public void resetAllJoins() {

        if (joins != null) {

            for (ColumnJoins cj : joins.values()) {
                if (ArrayUtils.isNotEmpty(cj.verticalLeftJoins))
                    for (int i = 0; i < cj.verticalLeftJoins.length; i++) {
                        cj.verticalLeftJoins[i].reset();
                        cj.verticalRightJoins[i].reset();
                    }
            }

        }
    }

    /**
     * <p>Retrieves the next available join point on the
     * specified axis for this component.
     *
     * @return the next join position
     */
    public int getNextJoin(int axis, ColumnData column) {
        ColumnJoins cj = joins.get(column);
        if (cj == null) {
            cj = new ColumnJoins();
            joins.put(column, cj);
        }
        switch (axis) {
            case LEFT_JOIN:
                return getNextJoin(cj.verticalLeftJoins);

            case RIGHT_JOIN:
                return getNextJoin(cj.verticalRightJoins);
        }
        return 0;
    }

    private int getNextJoin(ErdTableConnectionPoint[] points) {

        if (points == null) {
            return 0;
        }

        int connectionCount = 0;
        int lastConnectionCount = 0;
        for (int i = 0; i < points.length; i++) {
            ErdTableConnectionPoint point = points[i];

            connectionCount = point.getConnectionCount();
            if (connectionCount == 0 || connectionCount < lastConnectionCount) {
                point.addConnection();
                return point.getPosition();
            }
            lastConnectionCount = connectionCount;
        }

        // default to the first connection point
        if (points.length > 0) {
            return points[0].getPosition();
        }
        return 0;
    }

    /**
     * <p>Retrieves the table column meta data for
     * this table as a <code>ColumnData</code> array.
     *
     * @return this table's column meta data
     */
    public ColumnData[] getTableColumns() {
        return columns;
    }

    /**
     * <p>Retrieves the table column meta data for
     * this table as a <code>Vector</code> of
     * <code>ColumnData</code> objects.
     *
     * @return this table's column meta data
     */
    public Vector getTableColumnsVector() {
        Vector columnsVector = new Vector(columns.length);

        Collections.addAll(columnsVector, columns);

        return columnsVector;
    }

    /**
     * <p>Retrieves the original table column meta data for
     * this table as a <code>ColumnData</code> array before
     * changes (if (any) were or have been applied.
     *
     * @return this table's original column meta data
     */
    public ColumnData[] getOriginalTableColumns() {
        return originalData;
    }

    /**
     * <p>Sets this table's name to the specified value.
     *
     * @param the table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName.toUpperCase();
    }

    /**
     * <p>Sets this table's column metadata to the specified
     * values as a <code>ColumnData</code> array.
     *
     * @param the column metadata values
     */
    public void setTableColumns(ColumnData[] columns) {
        this.columns = columns;
    }

    /**
     * <p>Returns this table's name.
     *
     * @return this table's name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * <p>Retrieves this component's height.
     *
     * @return the component's height
     */
    public int getHeight() {
        return FINAL_HEIGHT;
    }

    public void setHeight(int FINAL_HEIGHT) {
        this.FINAL_HEIGHT = FINAL_HEIGHT;
    }

    public void setWidth(int FINAL_WIDTH) {
        this.FINAL_WIDTH = FINAL_WIDTH;
    }

    /**
     * <p>Retrieves this component's width.
     *
     * @return the component's width
     */
    public int getWidth() {
        return FINAL_WIDTH;
    }

    /**
     * <p>Gets the bounds of this component as a <code>Rectangle</code>
     * object. The bounds specify this component's width, height, and
     * location relative to its parent.
     *
     * @return a rectangle indicating this component's bounds
     */
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), FINAL_WIDTH, FINAL_HEIGHT);
    }

    protected int getLineHeght() {
        Graphics2D g = (Graphics2D) getGraphics();
        if (g == null)
            g = new DefaultGraphics2D(true);
        Font columnNameFont = parent.getColumnNameFont();
        FontMetrics fm = g.getFontMetrics(columnNameFont);
        return fm.getHeight();
    }

    public Rectangle getColumnBounds(ColumnData columnData) {
        int x = getX();
        int offsetY = getY();
        int heightPlusSep = 1 /*+ TITLE_BAR_HEIGHT*/ + offsetY;

        int lineHeight = getLineHeght();

        int drawCount = 0;
        String value = null;
        if (ArrayUtils.isNotEmpty(columns)) {
            for (int i = 0; i < columns.length; i++) {
                ColumnData column = columns[i];
                if (displayReferencedKeysOnly && !column.isKey()) {
                    continue;
                }
                drawCount++;
                if (column == columnData) {
                    int y = (((drawCount) + 1) * lineHeight) + heightPlusSep;
                    if (y > offsetY + FINAL_HEIGHT)
                        y = offsetY + FINAL_HEIGHT - lineHeight;
                    return new Rectangle(x, y, FINAL_WIDTH, lineHeight);
                }
            }
        }
        return null;
    }

    public void doubleClicked(MouseEvent e) {
        
/*      if (!newTable)
        new ErdEditTableDialog(parent, this);
 
      else*/

        if (editable) {
            new ErdNewTableDialog(parent, this);
        }

    }

    /**
     * <p>Returns a string representation of this
     * component - the table name.
     *
     * @return the table name
     */
    public String toString() {
        return tableName;
    }

    public void clean() {
        parent = null;
        columns = null;
        joins = null;
    }

    public int getTitleBarBgColor() {
        return titleBarBgColor;
    }

    public void setTitleBarBgColor(int titleBarBgColor) {
        this.titleBarBgColor = titleBarBgColor;
    }

    public String getDescriptionTable() {
        return descriptionTable;
    }

    public void setDescriptionTable(String descriptionTable) {
        this.descriptionTable = descriptionTable;
    }

    public boolean isShowCommentOnTable() {
        return showCommentOnTable;
    }

    public void setShowCommentOnTable(boolean showCommentOnTable) {
        this.showCommentOnTable = showCommentOnTable;
    }

    public boolean isShowCommentOnFields() {
        return showCommentOnFields;
    }

    public void setShowCommentOnFields(boolean showCommentOnFields) {
        this.showCommentOnFields = showCommentOnFields;
    }

    public Vector<ColumnConstraint> getColumnConstraints() {

        if (columns == null)
            return new Vector<>();

        Vector<ColumnConstraint> columnConstraintVector = new Vector<>();
        for (ColumnData columnData : columns) {
            Vector<ColumnConstraint> tempConstraintsVector = columnData.getColumnConstraintsVector();
            if (tempConstraintsVector != null)
                columnConstraintVector.addAll(tempConstraintsVector);
        }

        return columnConstraintVector;
    }

    static class ErdTableConnectionPoint {

        private final int axisType;

        private int position;

        private int tablesConnected;

        public ErdTableConnectionPoint(int axisType, int position) {
            this.axisType = axisType;
            this.position = position;
            tablesConnected = 0;
        }

        public ErdTableConnectionPoint(int axisType) {
            this.axisType = axisType;
            tablesConnected = 0;
        }

        public void addConnection() {
            tablesConnected++;
        }

        public int getConnectionCount() {
            return tablesConnected;
        }

        public void reset() {
            tablesConnected = 0;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getAxisType() {
            return axisType;
        }

        public int getPosition() {
            return position;
        }

        public boolean hasConnection() {
            return tablesConnected > 0;
        }

    }

    class ColumnJoins {
        private final transient ErdTableConnectionPoint[] verticalLeftJoins;
        private final transient ErdTableConnectionPoint[] verticalRightJoins;

        public ColumnJoins() {
            int joinSpacing = 6;
            int vertSize = (getLineHeght() * 2 / joinSpacing) - 1;
            verticalLeftJoins = new ErdTableConnectionPoint[vertSize];
            verticalRightJoins = new ErdTableConnectionPoint[vertSize];
            int midPointVert = vertSize / 2;
            //int midPointHoriz = FINAL_WIDTH / 2;

            int aboveMidPoint = midPointVert;
            int belowMidPoint = midPointVert;

            for (int i = 0; i < verticalLeftJoins.length; i++) {
                verticalLeftJoins[i] = new ErdTableConnectionPoint(LEFT_JOIN);
                verticalRightJoins[i] = new ErdTableConnectionPoint(RIGHT_JOIN);

                if (i == 0) {
                    verticalLeftJoins[i].setPosition(midPointVert);
                    verticalRightJoins[i].setPosition(midPointVert);
                } else if (i % 2 == 0) {
                    belowMidPoint -= joinSpacing;

                    if (belowMidPoint < vertSize) {
                        verticalLeftJoins[i].setPosition(belowMidPoint);
                        verticalRightJoins[i].setPosition(belowMidPoint);
                    }

                } else {
                    aboveMidPoint += joinSpacing;

                    if (aboveMidPoint > 0) {
                        verticalLeftJoins[i].setPosition(aboveMidPoint);
                        verticalRightJoins[i].setPosition(aboveMidPoint);
                    }

                }

            }
        }

        public ErdTableConnectionPoint[] getVerticalLeftJoins() {
            return verticalLeftJoins;
        }

        public ErdTableConnectionPoint[] getVerticalRightJoins() {
            return verticalRightJoins;
        }
    }

}











