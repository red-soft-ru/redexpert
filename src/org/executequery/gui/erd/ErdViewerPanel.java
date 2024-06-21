/*
 * ErdViewerPanel.java
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

import org.executequery.ActiveComponent;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.SaveFunction;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.print.PrintFunction;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.print.Printable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdViewerPanel extends DefaultTabView
        implements PrintFunction,
        SaveFunction,
        ActiveComponent {

    public static final String TITLE = bundleString("title");
    public static final String FRAME_ICON = "ErdPanel16";

    private static final int VERTICAL_DIFF = 50;
    private static final int HORIZONTAL_DIFF = 80;
    private static final int INITIAL_VIEW_HEIGHT = 450;

    public static final int DELETE = 0;
    public static final int NEW_OBJECT = DELETE + 1;
    public static final int CHANGE_BG_COLOR = NEW_OBJECT + 1;
    public static final int CHANGE_LOCATION = CHANGE_BG_COLOR + 1;

    protected static final String[] SCALE_VALUES = {
            "25%", "50%", "75%", "100%",
            "125%", "150%", "175%", "200%"
    };

    public final static Color[] TITLE_COLORS = new Color[]{
            new Color(255, 173, 173),
            new Color(255, 214, 165),
            new Color(202, 255, 191),
            new Color(155, 246, 255),
            new Color(0, 150, 255),
            new Color(189, 178, 255),
            new Color(255, 198, 255),
            new Color(192, 192, 192),
            new Color(248, 211, 201),
            new Color(253, 255, 182),
    };

    public final static Color[] LINE_COLORS = new Color[]{
            new Color(255, 0, 0),
            new Color(255, 128, 0),
            new Color(0, 255, 0),
            new Color(0, 255, 255),
            new Color(0, 0, 255),
            new Color(128, 128, 255),
            new Color(255, 0, 255),
            new Color(85, 85, 85),
            new Color(237, 146, 119),
            new Color(255, 255, 0),
    };

    private static int openCount = 1;
    private boolean displayKeysOnly = false;

    private final boolean showTools;
    private final boolean editable;

    private JPanel base;
    private ErdScrollPane scroll;
    private ErdToolBarPalette tools;
    private ErdSaveFileFormat savedErd;
    private ErdLayeredPane layeredPane;
    private ErdBackgroundPanel bgPanel;
    private ErdTitlePanel erdTitlePanel;
    private ErdDependanciesPanel dependsPanel;

    private List tableNames;
    private String fileName;
    private String tableFontName;

    private Font tableNameFont;
    private Font textBlockFont;
    private Font columnNameFont;

    private Vector<ErdTable> tables;
    private List<ErdTableInfo> tableInfos;
    private Vector<ErdTextPanel> textPanels;

    private int next_x = 20;
    private int next_y = 20;
    private int lastWidth = 0;
    private int tableFontSize;
    private int tableNameFontStyle;
    private int textBlockFontStyle;
    private int columnNameFontStyle;
    private double defaultScaledView;

    public ErdViewerPanel(boolean showTools, boolean editable) {
        this(null, null, true, showTools, editable);
    }

    public ErdViewerPanel(Vector tableNames, Vector<ErdTableInfo> tableInfos, boolean isNew) {
        this(tableNames, tableInfos, isNew, true, true);
    }

    public ErdViewerPanel(Vector tableNames, Vector<ErdTableInfo> tableInfos,
                          boolean isNew, boolean showTools, boolean editable) {

        super(new GridBagLayout());

        this.showTools = showTools;
        this.editable = editable;

        jbInit();

        // build all the tables to display
        if (!isNew) {
            setTables(tableNames, tableInfos);
        } else {
            tables = new Vector();
        }

        if (tableNames != null && tableInfos != null) {
            dependsPanel.setTableDependencies(buildTableRelationships());
            resizeCanvas();
            layeredPane.validate();
        }

        fileName = "erd" + (openCount++) + ".eqd";
        setScaledView(0.75);
    }

    public ErdViewerPanel(ErdSaveFileFormat savedErd) {
        this(null, null, true, true, true);
        fileName = savedErd.getFileName();
    }

    private void jbInit() {
        // set the background panel
        bgPanel = new ErdBackgroundPanel(true);
        // set the layered pane
        layeredPane = new ErdLayeredPane(this);

        // add the dependencies line panel
        dependsPanel = new ErdDependanciesPanel(this);
        layeredPane.add(dependsPanel, Integer.MIN_VALUE + 1);

        // initialise the fonts
        tableFontName = "Dialog";
        tableFontSize = 14;
        tableNameFontStyle = Font.PLAIN;
        columnNameFontStyle = Font.PLAIN;
        textBlockFontStyle = Font.PLAIN;

        tableNameFont = new Font(tableFontName, tableNameFontStyle, tableFontSize + 1);
        columnNameFont = new Font(tableFontName, columnNameFontStyle, tableFontSize);
        textBlockFont = new Font(tableFontName, textBlockFontStyle, tableFontSize);

        // add the background component
        layeredPane.add(bgPanel, Integer.MIN_VALUE);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resizeCanvas();
            }
        });

        // setup the base panel and add the layered pane
        base = new JPanel(new BorderLayout());
        base.add(layeredPane, BorderLayout.CENTER);

        // set the view's scroller
        scroll = new ErdScrollPane(this);
        scroll.setViewportView(base);

        scroll.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, GUIUtilities.getDefaultBorderColour()));
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.add(scroll, BorderLayout.CENTER);
        scrollPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 3));

        // add all components to a main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // add the tool bar
        if (showTools) {
            tools = new ErdToolBarPalette(this);
            mainPanel.add(tools, BorderLayout.NORTH);
        }
        mainPanel.add(scrollPanel, BorderLayout.CENTER);

        add(mainPanel, new GridBagConstraints(
                1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST,
                GridBagConstraints.BOTH,
                Constants.EMPTY_INSETS, 0, 0));

        if (!editable && !showTools) {

            layeredPane.displayPopupMenuViewItemsOnly();
        }
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    if (isEditable())
                        removeSelectedTables();
                }
                if (e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) {
                    if (!undoActions.isEmpty()) {
                        UndoRedoAction undoRedoAction = undoActions.pop();
                        undoRedoAction.undoExecute();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_Y && e.isControlDown()) {
                    if (!redoActions.isEmpty()) {
                        UndoRedoAction undoRedoAction = redoActions.pop();
                        undoRedoAction.undoExecute();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
                    for (ErdMoveableComponent emc : getAllComponentsArray()) {
                        emc.setSelected(true);
                    }
                    layeredPane.repaint();
                }
            }
        });
        undoActions = new Stack<>();
        redoActions = new Stack<>();

    }

    public void addTextPanel(ErdTextPanel erdTextPanel) {
        if (textPanels == null)
            textPanels = new Vector<>();
        layeredPane.add(erdTextPanel);
        erdTextPanel.setBounds(50, 50,
                erdTextPanel.getWidth(), erdTextPanel.getHeight());
        layeredPane.moveToFront(erdTextPanel);
        textPanels.add(erdTextPanel);
        layeredPane.repaint();
        fireSaveUndoAction(new UndoRedoAction(NEW_OBJECT, erdTextPanel));
    }

    public void addTitlePanel(ErdTitlePanel erdTitlePanel) {
        layeredPane.add(erdTitlePanel);
        erdTitlePanel.setBounds(50, 50,
                erdTitlePanel.getWidth(), erdTitlePanel.getHeight());
        layeredPane.moveToFront(erdTitlePanel);
        this.erdTitlePanel = erdTitlePanel;
        layeredPane.repaint();
    }

    public boolean isEditable() {
        return editable;
    }

    public void resetTableValues(List tableNames, List<ErdTableInfo> tableInfos) {
        removeAllTables();
        setTables(tableNames, tableInfos);
        dependsPanel.setTableDependencies(buildTableRelationships());
        resizeCanvas();
        layeredPane.validate();
    }

    public void setTables(List tableNames, List<ErdTableInfo> tableInfos) {

        this.tableNames = tableNames;
        this.tableInfos = tableInfos;

        // height and width of current table
        int height = -1;
        int width = -1;

        int size = tableNames.size();
        tables = new Vector(size);

        ErdTable table = null;
        for (int i = 0; i < size; i++) {

            // create the ERD display component
            table = new ErdTable((String) tableNames.get(i), tableInfos.get(i).getColumns(), this);

            table.setEditable(editable);
            table.setDescriptionTable(tableInfos.get(i).getComment());
            height = table.getHeight();
            width = table.getWidth();

            // if it doesn't fit vertically within the
            // initial size of the view, move to a new
            // column within the grid display
            if (next_y + height + 20 > INITIAL_VIEW_HEIGHT) {
                next_y = 20;

                if (i > 0)
                    next_x += lastWidth + HORIZONTAL_DIFF;

                lastWidth = 0;

            }

            // position within the layered pane
            table.setBounds(next_x, next_y, width, height);
            layeredPane.add(table);

            table.toFront();

            next_y += height + VERTICAL_DIFF;

            if (lastWidth < width)
                lastWidth = width;

            // add to the vector
            addTableToList(table);
        }
    }

    /**
     * <p>Sets the relationships between each table.
     *
     * @return the <code>Vector</code> of
     * <code>ErdTableDependency</code> objects
     */
    public Vector buildTableRelationships() {

        String referencedTable = null;
        ColumnData[] cda = null;
        ColumnConstraint[] cca = null;

        ErdTable[] tables_array = getAllTablesArray();

        Vector tableDependencies = new Vector();
        ErdTableDependency dependency = null;

        ErdTable table = null;

        for (int k = 0, m = tables.size(); k < m; k++) {

            cda = tables_array[k].getTableColumns();

            if (cda == null) {
                continue;
            }

            for (ColumnData columnData : cda) {

                if (!columnData.isForeignKey())
                    continue;

                cca = columnData.getColumnConstraintsArray();

                for (ColumnConstraint columnConstraint : cca) {

                    if (columnConstraint.getType() == PRIMARY_KEY)
                        continue;

                    referencedTable = columnConstraint.getRefTable();

                    for (int j = 0; j < m; j++) {

                        if (referencedTable.equalsIgnoreCase(
                                tables.elementAt(j).toString())) {

                            table = tables.elementAt(j);

                            ColumnData refCol = null;
                            for (ColumnData col : table.getTableColumns()) {
                                if (col.getColumnName().contentEquals(columnConstraint.getRefColumn())) {
                                    refCol = col;
                                    break;
                                }
                            }

                            dependency = new ErdTableDependency(tables_array[k], table, columnData, refCol);
                            tableDependencies.add(dependency);
                            break;
                        }
                    }
                }
            }
        }

        return tableDependencies;
    }

    /**
     * <p>Swaps the canvas background from the grid
     * display to the white background and vice-versa.
     */
    public void swapCanvasBackground() {
        bgPanel.swapBackground();
        layeredPane.repaint();
    }

    /**
     * <p>Returns whether the grid is set to be displayed.
     *
     * @return whether the grid is displayed
     */
    public boolean shouldDisplayGrid() {
        return bgPanel.shouldDisplayGrid();
    }

    public void swapPageMargin() {
        bgPanel.swapPageMargin();
        layeredPane.repaint();
    }

    public ErdTitlePanel getTitlePanel() {
        return erdTitlePanel;
    }

    public boolean shouldDisplayMargin() {
        return bgPanel.shouldDisplayMargin();
    }

    /**
     * <p>Adds the outline panel of a selected table to the
     * layered pane when a drag operation occurs.
     *
     * @param panel dragging outline panel to be added
     */
    protected void addOutlinePanel(JPanel panel) {
        layeredPane.add(panel, JLayeredPane.DRAG_LAYER);
    }

    /**
     * <p>Removes the specified outline panel when dragging
     * has completed (mouse released).
     *
     * @param panel outline drag panel to remove
     */
    protected void removeOutlinePanel(JPanel panel) {
        layeredPane.remove(panel);
    }

    /**
     * <p>Resets the ERD table joins for all tables.
     */
    protected void resetAllTableJoins() {

        for (int i = 0, k = tables.size(); i < k; i++)
            tables.elementAt(i).resetAllJoins();

        for (ErdMoveableComponent comp : getAllComponentsVector())
            comp.resetInsets();
    }

    /**
     * <p>Returns the preferred size of the canvas.
     *
     * @return a Dimension object representing the current
     * preferred size of the canvas
     */
    public Dimension getCanvasSize() {
        return layeredPane.getPreferredSize();
    }

    /**
     * <p>Sets the preferred size of the canvas and all
     * background components - grid panel, dependencies panel.
     *
     * @param dim Dimension object representing the desired
     *            preferred size for the canvas
     */
    protected void setCanvasSize(Dimension dim) {

        double scale = layeredPane.getScale();
        int panelWidth = (int) (dim.width / scale);
        int panelHeight = (int) (dim.height / scale);

        Dimension scaleDim = new Dimension(panelWidth, panelHeight);

        base.setPreferredSize(dim);

        layeredPane.setPreferredSize(dim);//scale < 1.0 ? scaleDim : dim);
        dependsPanel.setPreferredSize(scaleDim);
        bgPanel.setPreferredSize(scaleDim);

        dependsPanel.setBounds(bgPanel.getBounds());
        //    dependsPanel.setBounds(0, 0, panelWidth, panelWidth);
        layeredPane.setBounds(0, 0, dim.width, dim.height);

        layeredPane.repaint();
    }

    protected void setTableBackground(Color c) {
        ErdMoveableComponent[] tablesArray = getAllComponentsArray();

        for (ErdMoveableComponent erdMoveableComponent : tablesArray)
            erdMoveableComponent.setTableBackground(c);

        layeredPane.repaint();
    }

    public void setDisplayGrid(boolean displayGrid) {
        bgPanel.setDisplayGrid(displayGrid);
    }

    /**
     * Sets the canvas background to the specified colour.
     */
    public void setCanvasBackground(Color c) {
        bgPanel.setBackground(c);
        layeredPane.setGridDisplayed(false);
        layeredPane.repaint();
    }

    protected Color getTableBackground() {
        return tables.isEmpty() ?
                UIUtils.getColour("executequery.Erd.tableBackground", Color.WHITE) :
                tables.elementAt(0).getTableBackground();
    }

    protected Color getCanvasBackground() {
        return bgPanel.getBackground();
    }

    /**
     * <p>Repaints the layered pane during
     * table component movement and reapplication
     * of the relationship joins
     */
    protected void repaintLayeredPane() {
        layeredPane.repaint();
    }

    public void setTableDisplayFont(String fontName, int tableNameStyle, int columnNameStyle, int textBlockStyle, int size) {

        tableFontSize = size;
        tableFontName = fontName;
        tableNameFontStyle = tableNameStyle;
        columnNameFontStyle = columnNameStyle;
        textBlockFontStyle = textBlockStyle;

        tableNameFont = new Font(fontName, tableNameStyle, size + 1);
        columnNameFont = new Font(fontName, columnNameStyle, size);
        textBlockFont = new Font(fontName, textBlockStyle, size);

        ErdTable[] tablesArray = getAllTablesArray();
        for (ErdTable erdTable : tablesArray)
            erdTable.tableColumnsChanged(true);

        layeredPane.repaint();
    }

    @Override
    public boolean canPrint() {
        return true;
    }

    @Override
    public String getPrintJobName() {
        return "Red Expert - ERD";
    }

    @Override
    public Printable getPrintable() {
        return new ErdPrintable(this);
    }

    public boolean isDisplayKeysOnly() {
        return displayKeysOnly;
    }

    public void setDisplayKeysOnly(boolean displayKeysOnly) {
        this.displayKeysOnly = displayKeysOnly;

        ErdTable[] allTables = getAllTablesArray();
        for (ErdTable allTable : allTables) {
            allTable.setDisplayReferencedKeysOnly(displayKeysOnly);
            allTable.tableColumnsChanged(true);
        }

        layeredPane.repaint();
    }

    public void setDisplayCommentOnFields(boolean displayCommentOnFields) {

        ErdTable[] allTables = getAllTablesArray();
        for (ErdTable allTable : allTables) {
            allTable.setShowCommentOnFields(displayCommentOnFields);
            allTable.tableColumnsChanged(true);
        }

        layeredPane.repaint();
    }

    public void setDisplayCommentOnTable(boolean displayCommentOnTable) {

        ErdTable[] allTables = getAllTablesArray();
        for (ErdTable allTable : allTables) {
            allTable.setShowCommentOnTable(displayCommentOnTable);
            allTable.tableColumnsChanged(true);
        }

        layeredPane.repaint();
    }

    protected ErdDependanciesPanel getDependenciesPanel() {
        return dependsPanel;
    }

    protected void updateTableRelationships() {
        dependsPanel.setTableDependencies(buildTableRelationships());
        layeredPane.repaint();
    }

    protected ErdTable[] getSelectedTablesArray() {
        Vector selected = new Vector();
        int size = tables.size();

        ErdTable erdTable = null;

        for (int i = 0; i < size; i++) {
            erdTable = tables.elementAt(i);
            if (erdTable.isSelected()) {
                selected.add(erdTable);
            }
        }

        size = selected.size();
        ErdTable[] selectedTables = new ErdTable[size];

        for (int i = 0; i < size; i++) {
            selectedTables[i] = (ErdTable) selected.elementAt(i);
        }

        return selectedTables;
    }

    protected ErdMoveableComponent[] getSelectedComponentsArray() {

        Vector selected = new Vector();
        Vector vector = getAllComponentsVector();
        int size = vector.size();

        ErdMoveableComponent erdTable = null;

        for (int i = 0; i < size; i++) {
            erdTable = (ErdMoveableComponent) vector.elementAt(i);
            if (erdTable.isSelected()) {
                selected.add(erdTable);
            }
        }

        size = selected.size();
        ErdMoveableComponent[] selectedTables = new ErdMoveableComponent[size];

        for (int i = 0; i < size; i++) {
            selectedTables[i] = (ErdMoveableComponent) selected.elementAt(i);
        }

        return selectedTables;
    }

    Stack<UndoRedoAction> undoActions;
    Stack<UndoRedoAction> redoActions;

    static List<ErdMoveableComponent> listFromSingleComponent(ErdMoveableComponent component) {
        List<ErdMoveableComponent> list = new ArrayList<>();
        list.add(component);
        return list;
    }

    public void removeTable(ErdTable table) {
        table.clean();
        layeredPane.remove(table);
        tables.remove(table);

        dependsPanel.setTableDependencies(buildTableRelationships());

        if (erdTitlePanel != null && erdTitlePanel.isSelected()) {
            layeredPane.remove(erdTitlePanel);
            erdTitlePanel = null;
        }

        layeredPane.repaint();
    }

    public Font getColumnNameFont() {
        return columnNameFont;
    }

    public Font getTableNameFont() {
        return tableNameFont;
    }

    public Font getTextBlockFont() {
        return textBlockFont;
    }

    public int getColumnNameFontStyle() {
        return columnNameFontStyle;
    }

    public int getTableNameFontStyle() {
        return tableNameFontStyle;
    }

    public int getTableFontSize() {
        return tableFontSize;
    }

    public String getTableFontName() {
        return tableFontName;
    }

    public void resizeCanvas() {
        scroll.resizeCanvas();
    }

    public Vector<ErdTable> getAllTablesVector() {
        return tables;
    }

    public Vector<ErdMoveableComponent> getAllComponentsVector() {

        Vector<ErdMoveableComponent> vector = new Vector<>();
        vector.addAll(tables != null ? tables : new Vector<>());
        vector.addAll(textPanels != null ? textPanels : new Vector<>());

        return vector;
    }

    public ErdTable[] getAllTablesArray() {

        if (tables == null)
            tables = new Vector();

        int v_size = tables.size();
        ErdTable[] tablesArray = new ErdTable[v_size];

        for (int i = 0; i < v_size; i++) {
            tablesArray[i] = tables.elementAt(i);
        }

        return tablesArray;
    }

    public ErdTextPanel[] getTextPanelsArray() {

        if (textPanels == null)
            textPanels = new Vector();

        int v_size = textPanels.size();
        ErdTextPanel[] tablesArray = new ErdTextPanel[v_size];

        for (int i = 0; i < v_size; i++) {
            tablesArray[i] = textPanels.elementAt(i);
        }

        return tablesArray;
    }

    public ErdMoveableComponent[] getAllComponentsArray() {

        Vector vector = getAllComponentsVector();

        int v_size = vector.size();
        ErdMoveableComponent[] tablesArray = new ErdMoveableComponent[v_size];

        for (int i = 0; i < v_size; i++) {
            tablesArray[i] = (ErdMoveableComponent) vector.elementAt(i);
        }

        return tablesArray;
    }

    protected Dimension getMaxImageExtents() {
        int width = 0;
        int height = 0;
        int tableExtentX = 0;
        int tableExtentY = 0;

        ErdTable[] tablesArray = getAllTablesArray();

        for (ErdTable erdTable : tablesArray) {

            tableExtentX = erdTable.getX() + erdTable.getWidth();
            tableExtentY = erdTable.getY() + erdTable.getHeight();

            if (tableExtentX > width)
                width = tableExtentX;

            if (tableExtentY > height)
                height = tableExtentY;
        }

        if (erdTitlePanel != null) {
            tableExtentX = erdTitlePanel.getX() + erdTitlePanel.getWidth();
            tableExtentY = erdTitlePanel.getY() + erdTitlePanel.getHeight();

            if (tableExtentX > width)
                width = tableExtentX;

            if (tableExtentY > height)
                height = tableExtentY;
        }

        return new Dimension(width + 20, height + 20);
    }

    public void removeAllTables() {
        ErdTable[] allTables = getAllTablesArray();

        for (int i = 0; i < allTables.length; i++) {
            allTables[i].clean();
            layeredPane.remove(allTables[i]);
            tables.remove(allTables[i]);
            allTables[i] = null;
        }

        layeredPane.repaint();

    }

    void addTableToList(ErdTable table) {

        if (tables == null)
            tables = new Vector<>();
        tables.add(table);

        table.setTitleBarBgColor((tables.size() - 1) % TITLE_COLORS.length);
    }

    @Override
    public boolean contentCanBeSaved() {
        return !tables.isEmpty();
    }

    @Override
    public int save(boolean saveAs) {
        ErdSaveDialog saveDialog;

        if (savedErd != null) {
            if (saveAs) {
                saveDialog = new ErdSaveDialog(this, savedErd.getAbsolutePath());

            } else
                return saveApplicationFileFormat(new File(savedErd.getAbsolutePath()));

        } else
            saveDialog = new ErdSaveDialog(this, new File(fileName));

        return saveDialog.getSaved();
    }

    protected int saveApplicationFileFormat(File file) {

        ErdTable[] tables = getAllTablesArray();
        ErdTableFileData[] fileData = new ErdTableFileData[tables.length];

        for (int i = 0; i < tables.length; i++) {
            fileData[i] = new ErdTableFileData();
            fileData[i].setTableBounds(tables[i].getBounds());
            fileData[i].setTableName(tables[i].getTableName());
            fileData[i].setCreateTableScript(tables[i].getCreateTableScript());
            fileData[i].setColumnData(tables[i].getTableColumns());
            fileData[i].setAlterTableHash(tables[i].getAlterTableHash());
            fileData[i].setAlterTableScript(tables[i].getAlterTableScript());
            fileData[i].setAddConstraintScript(tables[i].getAddConstraintsScript());
            fileData[i].setDropConstraintScript(tables[i].getDropConstraintsScript());
            fileData[i].setTableBackground(tables[i].getTableBackground());
            fileData[i].setTableDescription(tables[i].getDescriptionTable());
            fileData[i].setShowCommentOnTable(tables[i].isShowCommentOnTable());
            fileData[i].setShowCommentsOnfields(tables[i].isShowCommentOnFields());
        }
        ErdTextPanel[] erdTextPanels = getTextPanelsArray();
        ErdTextPanelData[] textFileData = new ErdTextPanelData[erdTextPanels.length];

        for (int i = 0; i < erdTextPanels.length; i++) {
            textFileData[i] = new ErdTextPanelData();
            textFileData[i].setTableBounds(erdTextPanels[i].getBounds());
            textFileData[i].setErdDescription(erdTextPanels[i].getErdDescription());
            textFileData[i].setTableBackground(erdTextPanels[i].getTableBackground());
        }

        ErdSaveFileFormat eqFormat = new ErdSaveFileFormat(fileData, file.getName());
        eqFormat.setTextBlocks(textFileData);
        eqFormat.setColumnNameFont(columnNameFont);
        eqFormat.setTableNameFont(tableNameFont);
        eqFormat.setTextBlockFont(textBlockFont);
        if (tables.length > 0)
            eqFormat.setTableBackground(tables[0].getTableBackground());
        eqFormat.setAbsolutePath(file.getAbsolutePath());

        if (erdTitlePanel != null) {
            ErdTitlePanelData titlePanelData = new ErdTitlePanelData();
            titlePanelData.setErdAuthor(erdTitlePanel.getErdAuthor());
            titlePanelData.setErdDatabase(erdTitlePanel.getErdDatabase());
            titlePanelData.setErdDate(erdTitlePanel.getErdDate());
            titlePanelData.setErdDescription(erdTitlePanel.getErdDescription());
            titlePanelData.setErdFileName(erdTitlePanel.getErdFileName());
            titlePanelData.setErdName(erdTitlePanel.getErdName());
            titlePanelData.setErdRevision(erdTitlePanel.getErdRevision());
            titlePanelData.setTitleBounds(erdTitlePanel.getBounds());
            eqFormat.setTitlePanel(titlePanelData);
        }

        if (!shouldDisplayGrid()) {
            eqFormat.setCanvasBackground(getCanvasBackground());
        } else {
            eqFormat.setCanvasBackground(null);
        }

        try {

            FileOutputStream fileOut = new FileOutputStream(file);
            BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut);
            ObjectOutputStream obOut = new ObjectOutputStream(bufferedOut);
            obOut.writeObject(eqFormat);

            bufferedOut.close();
            obOut.close();
            fileOut.close();

            savedErd = eqFormat;
            return SaveFunction.SAVE_COMPLETE;

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage(bundleString("errorFileSaving") + e.getMessage());
            return SaveFunction.SAVE_FAILED;
        }

    }

    @Override
    public void cleanup() {

        // -------------------------------------------------
        // memory leak noticed so 'shutdown hook' added
        // to selected components used within this feature
        // unitl fix is found.
        // -------------------------------------------------

        layeredPane.clean();

        ErdTable[] tablesArray = getAllTablesArray();

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].clean();
            tablesArray[i] = null;
        }

    }

    public void showFontStyleDialog() {
        new ErdFontStyleDialog(this);
    }

    public void showLineStyleDialog() {
        new ErdLineStyleDialog(dependsPanel);
    }

    protected void setScaleComboValue(String value) {

        if (!editable)
            return;

        tools.setScale(value);
    }

    protected void setPopupMenuScaleValue(int index) {

        if (!editable)
            return;

        layeredPane.setMenuScaleSelection(index);
    }

    protected double getScaleIndex() {
        return layeredPane.getScale();
    }

    public void setDefaultScaledView(double defaultScaledView) {
        this.defaultScaledView = defaultScaledView;
        setScaledView(defaultScaledView);
    }

    public void reset() {
        resetTableValues(tableNames, tableInfos);
        setScaledView(defaultScaledView);
    }

    public void setScaledView(double scale) {

        if (defaultScaledView == 0) {

            defaultScaledView = scale;
        }

        if (tables == null) {

            tables = new Vector();
        }

        for (int i = 0, k = tables.size(); i < k; i++) {

            ErdTable table = tables.elementAt(i);
            table.setScale(scale);
        }

        if (erdTitlePanel != null) {

            erdTitlePanel.setScale(scale);
        }

        layeredPane.setScale(scale);
        scroll.setScale(scale);
        layeredPane.repaint();
        resizeCanvas();
    }

    protected void zoom(boolean zoomIn) {

        double scale = layeredPane.getScale();

        if (zoomIn) {

            if (scale < 2.0) {
                scale += 0.25;
                if (tools != null) {
                    tools.incrementScale(1);
                }
            } else
                return;

        } else {

            if (scale > 0.25) {
                scale -= 0.25;
                if (tools != null) {
                    tools.incrementScale(-1);
                }
            } else {
                return;
            }
        }

        setScaledView(scale);
    }

    public int getTextBlockFontStyle() {
        return textBlockFontStyle;
    }

    public String getAllSQLText() {

        char newLine = '\n';
        ErdTable[] allTables = getAllTablesArray();

        StringBuilder sb = new StringBuilder();
        for (ErdTable allTable : allTables)
            if (allTable.hasSQLScripts())
                sb.append(allTable.getAllSQLScripts()).append(newLine);

        return sb.toString();
    }

    public String getErdFileName() {
        return fileName;
    }

    @Override
    public String getDisplayName() {
        return toString();
    }

    @Override
    public String toString() {
        return TITLE + " - " + fileName;
    }

    public DatabaseConnection getDatabaseConnection() {
        return tools.getSelectedConnection();
    }

    // --------------------------------------------
    // TabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {

        UserProperties properties = UserProperties.getInstance();

        if (properties.getBooleanProperty("general.save.prompt")) {

            if (!GUIUtilities.saveOpenChanges(this)) {

                return false;
            }

        }

        cleanup();

        return true;
    }

    private static String bundleString(String key) {
        return Bundles.get(ErdViewerPanel.class, key);
    }

    public ErdScrollPane getScroll() {
        return scroll;
    }

    /**
     * <p>Adds a new table to the canvas.
     */
    protected boolean addNewTable(ErdTable newTable, boolean setCentered) {

        if (tables == null) {
            tables = new Vector();
        }
        for (ErdTable table : tables) {
            if (table.getTableName().contentEquals(newTable.getTableName())) {
                table.setTableColumns(newTable.getTableColumns());
                table.setDescriptionTable(newTable.getDescriptionTable());
                table.tableColumnsChanged(true);
                return false;
            }
        }
        addTableToList(newTable);

        int width = newTable.getWidth();
        int height = newTable.getHeight();

        if (setCentered) {
            newTable.setBounds((layeredPane.getWidth() - newTable.getWidth()) / 2,
                    (layeredPane.getHeight() - newTable.getHeight()) / 2,
                    width, height);
            fireSaveUndoAction(new UndoRedoAction(NEW_OBJECT, newTable));
        } else {

            if (next_y + height + 20 > INITIAL_VIEW_HEIGHT) {
                next_y = 20;
                next_x += width + HORIZONTAL_DIFF;
                lastWidth = 0;
            }

            newTable.setBounds(next_x, next_y, width, height);

            next_y += height + VERTICAL_DIFF;
        }

        layeredPane.add(newTable, JLayeredPane.DEFAULT_LAYER, tables.size());
        newTable.toFront();
        return true;
    }

    protected List<ErdMoveableComponent> getSelectedComponents() {
        Vector selected = new Vector();
        Vector vector = getAllComponentsVector();
        int size = vector.size();

        ErdMoveableComponent erdTable = null;

        for (int i = 0; i < size; i++) {
            erdTable = (ErdMoveableComponent) vector.elementAt(i);
            if (erdTable.isSelected()) {
                selected.add(erdTable);
            }
        }
        return selected;
    }

    protected void removeSelectedTables() {
        boolean tablesRemoved = false;
        ErdTable[] allTables = getAllTablesArray();
        List<ErdMoveableComponent> removedComponents = new ArrayList<>();
        for (int i = 0; i < allTables.length; i++) {

            if (allTables[i].isSelected()) {
                removedComponents.add(allTables[i]);
                //allTables[i].clean();
                layeredPane.remove(allTables[i]);
                tables.remove(allTables[i]);
                allTables[i] = null;
                tablesRemoved = true;
            }

        }

        ErdTextPanel[] allTexts = getTextPanelsArray();

        for (int i = 0; i < allTexts.length; i++) {

            if (allTexts[i].isSelected()) {
                removedComponents.add(allTexts[i]);
                //allTexts[i].clean();
                layeredPane.remove(allTexts[i]);
                textPanels.remove(allTexts[i]);
                allTexts[i] = null;
            }

        }
        fireSaveUndoAction(new UndoRedoAction(DELETE, removedComponents));
        if (tablesRemoved)
            dependsPanel.setTableDependencies(buildTableRelationships());

        if (erdTitlePanel != null) {

            if (erdTitlePanel.isSelected()) {
                layeredPane.remove(erdTitlePanel);
                erdTitlePanel = null;
            }

        }

        layeredPane.repaint();
    }

    public void fireDragging() {
        fireSaveUndoAction(new UndoRedoAction(ErdViewerPanel.CHANGE_LOCATION, getSelectedComponents()));
    }

    public void fireChangedBgColor() {
        fireSaveUndoAction(new UndoRedoAction(ErdViewerPanel.CHANGE_BG_COLOR, getSelectedComponents()));
    }

    void fireSaveUndoAction(UndoRedoAction undoRedoAction) {
        undoActions.push(undoRedoAction);
        redoActions.clear();
    }

    class UndoRedoAction {
        int typeAction;
        List<ErdMoveableComponent> listComponents;
        List<Color> bgColors;
        List<Rectangle> boundsComponents;
        boolean undoAction;

        public UndoRedoAction(int typeAction, ErdMoveableComponent component) {
            this(typeAction, listFromSingleComponent(component));
        }

        public UndoRedoAction(int typeAction, List<ErdMoveableComponent> listComponents) {
            this(typeAction, listComponents, true);
        }

        public UndoRedoAction(int typeAction, List<ErdMoveableComponent> listComponents, boolean undoAction) {
            this.undoAction = undoAction;
            this.typeAction = typeAction;
            this.listComponents = listComponents;
            bgColors = new ArrayList<>();
            boundsComponents = new ArrayList<>();
            for (ErdMoveableComponent emc : listComponents) {
                bgColors.add(emc.getTableBackground());
                boundsComponents.add(emc.getBounds());
            }
        }

        void undoExecute() {
            switch (typeAction) {
                case DELETE: {
                    for (ErdMoveableComponent emc : listComponents) {
                        if (emc instanceof ErdTable) {
                            tables.add((ErdTable) emc);
                            layeredPane.add(emc);
                            layeredPane.moveToFront(emc);
                            dependsPanel.setTableDependencies(buildTableRelationships());
                        } else if (emc instanceof ErdTextPanel) {
                            textPanels.add((ErdTextPanel) emc);
                            layeredPane.add(emc);
                            layeredPane.moveToFront(emc);
                        }
                    }
                    if (undoAction)
                        redoActions.push(new UndoRedoAction(NEW_OBJECT, listComponents, false));
                    else undoActions.push(new UndoRedoAction(NEW_OBJECT, listComponents, true));
                }
                break;
                case NEW_OBJECT: {
                    for (ErdMoveableComponent emc : listComponents) {
                        if (emc instanceof ErdTable) {
                            tables.remove((ErdTable) emc);
                            layeredPane.remove(emc);
                            dependsPanel.setTableDependencies(buildTableRelationships());
                        } else if (emc instanceof ErdTextPanel) {
                            textPanels.remove((ErdTextPanel) emc);
                            layeredPane.remove(emc);
                        }
                    }
                    if (undoAction)
                        redoActions.push(new UndoRedoAction(DELETE, listComponents, false));
                    else undoActions.push(new UndoRedoAction(DELETE, listComponents, true));
                }
                break;
                case CHANGE_BG_COLOR: {
                    if (undoAction)
                        redoActions.push(new UndoRedoAction(CHANGE_BG_COLOR, listComponents, false));
                    else undoActions.push(new UndoRedoAction(CHANGE_BG_COLOR, listComponents, true));
                    for (int i = 0; i < listComponents.size(); i++) {
                        listComponents.get(i).setTableBackground(bgColors.get(i));
                    }
                }
                break;
                case CHANGE_LOCATION: {
                    if (undoAction)
                        redoActions.push(new UndoRedoAction(CHANGE_LOCATION, listComponents, false));
                    else undoActions.push(new UndoRedoAction(CHANGE_LOCATION, listComponents, true));
                    for (int i = 0; i < listComponents.size(); i++) {
                        listComponents.get(i).setBounds(boundsComponents.get(i));
                    }
                    dependsPanel.setTableDependencies(buildTableRelationships());
                }
                break;
            }
            layeredPane.repaint();
        }
    }

}
