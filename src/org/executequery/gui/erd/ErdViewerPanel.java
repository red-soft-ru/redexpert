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

    private static final int VERT_DIFF = 50;
    private static final int HORIZ_DIFF = 80;

    /**
     * The panel's title
     */
    public static final String TITLE = bundleString("title");

    /**
     * The panel's icon
     */
    public static final String FRAME_ICON = "ErdPanel16.png";

    /**
     * Whether this instance has a tool bar palatte
     */
    private final boolean showTools;

    /**
     * Whether this is a static diagram
     */
    private final boolean editable;

    /**
     * The base panel
     */
    private JPanel base;

    /**
     * The background panel
     */
    private ErdBackgroundPanel bgPanel;

    /**
     * The pane containing the tables
     */
    private ErdLayeredPane layeredPane;

    /**
     * The customised scroll pane
     */
    private ErdScrollPane scroll;

    /**
     * The panel to draw dependencies
     */
    private ErdDependanciesPanel dependsPanel;

    /**
     * The status bar containing zoom controls
     */
    private ErdToolBarPalette tools;

    /**
     * The title panel
     */
    private ErdTitlePanel erdTitlePanel;

    /** The ERD tools palette */
    //private InternalFramePalette toolPalette;

    /**
     * An open saved erd file
     */
    private ErdSaveFileFormat savedErd;

    /**
     * A <code>Vector</code> containing all tables
     */
    private Vector<ErdTable> tables;

    private Vector<ErdTextPanel> textPanels;

    /**
     * The font name displayed
     */
    private String tableFontName;

    /**
     * The font size displayed
     */
    private int tableFontSize;

    /**
     * The font style displayed for a table name
     */
    private int tableNameFontStyle;

    /**
     * The font style displayed for a column name
     */
    private int columnNameFontStyle;
    private int textBlockFontStyle;

    /**
     * The default file name
     */
    private String fileName;

    /**
     * The font for the column names
     */
    private Font columnNameFont;
    /**
     * The font for the table name
     */
    private Font tableNameFont;

    private Font textBlockFont;

    /**
     * the connection props object
     */
    private DatabaseConnection databaseConnection;

    /**
     * flag whether to display reference keys only
     */
    private boolean displayKeysOnly = false;

    private static final int INITIAl_VIEW_HEIGHT = 800;

    private double defaultScaledView;

    /**
     * The scale values
     */
    protected static final String[] scaleValues = {"25%", "50%", "75%", "100%",
            "125%", "150%", "175%", "200%"};

    private static int openCount = 1;

    private List tableNames;

    private List<ErdTableInfo> tableInfos;

    private int next_x = 20;
    private int next_y = 20;
    private int lastWidth = 0;

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

    public static final int DELETE = 0;
    public static final int NEW_OBJECT = DELETE + 1;
    public static final int CHANGE_BG_COLOR = NEW_OBJECT + 1;
    public static final int CHANGE_LOCATION = CHANGE_BG_COLOR + 1;

    public ErdViewerPanel(boolean showTools, boolean editable) {
        this(null, true, showTools, editable);
    }

    public ErdViewerPanel(Vector<ErdTableInfo> tableInfos, boolean isNew) {
        this(tableInfos, isNew, true, true);
    }

    public ErdViewerPanel(Vector<ErdTableInfo> tableInfos,
                          boolean isNew, boolean showTools, boolean editable) {

        super(new GridBagLayout());

        this.showTools = showTools;
        this.editable = editable;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //setCanvasBackground(Color.WHITE);

        // build all the tables to display
        if (!isNew) {
            setTables(tableInfos);
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

    public ErdViewerPanel(ErdSaveFileFormat savedErd, String absolutePath) {
        this(null, true, true, true);
        //setSavedErd(savedErd, absolutePath);
        fileName = savedErd.getFileName();

    }

    private void jbInit() throws Exception {
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

    public void resetTableValues(List<ErdTableInfo> tableInfos) {
        removeAllTables();
        setTables(tableInfos);
        dependsPanel.setTableDependencies(buildTableRelationships());
        resizeCanvas();
        layeredPane.validate();
    }

    /**
     * <p>Builds the ERD table views on feature startup.
     *
     * @param a   <code>Vector</code> of table names
     * @param the column meta data for the tables
     */
    public void setTables(List<ErdTableInfo> tableInfos) {

        if (tableInfos != null) {
            tableNames = new Vector();
            for (ErdTableInfo etf : tableInfos) {
                tableNames.add(etf.getName());
            }
        }
        this.tableInfos = tableInfos;

        // next position of component added


        // height and width of current table
        int height = -1;
        int width = -1;

        // width of last table

        // vertical and horizontal differences

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
            if (next_y + height + 20 > INITIAl_VIEW_HEIGHT) {
                next_y = 20;

                if (i > 0)
                    next_x += lastWidth + HORIZ_DIFF;

                lastWidth = 0;

            }

            // position within the layered pane
            table.setBounds(next_x, next_y, width, height);
            layeredPane.add(table);

            table.toFront();

            next_y += height + VERT_DIFF;

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

            for (int i = 0; i < cda.length; i++) {

                if (!cda[i].isForeignKey())
                    continue;

                cca = cda[i].getColumnConstraintsArray();

                for (int n = 0; n < cca.length; n++) {

                    if (cca[n].getType() == PRIMARY_KEY)
                        continue;

                    referencedTable = cca[n].getRefTable();

                    for (int j = 0; j < m; j++) {

                        if (referencedTable.equalsIgnoreCase(
                                tables.elementAt(j).toString())) {

                            table = tables.elementAt(j);

                            ColumnData refCol = null;
                            for (ColumnData col : table.getTableColumns()) {
                                if (col.getColumnName().contentEquals(cca[n].getRefColumn())) {
                                    refCol = col;
                                    break;
                                }
                            }
                            dependency = new ErdTableDependency(tables_array[k], table, cda[i], refCol);

                            // place the tables in the temp HashMap so
                            // the combination is not added a second time

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

    public void setDisplayMargin(boolean displayMargin) {
        bgPanel.setDisplayMargin(displayMargin);
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
     * <p>Returns a <code>ErdTableDependency</code> array of
     * all recorded/manufactured table dependencies within the
     * schema ERD displayed.
     *
     * @return the <code>ErdTableDependency</code> array of
     * the open ERD
     */
    public ErdTableDependency[] getTableDependencies() {
        return dependsPanel.getTableDependencies();
    }

    /**
     * <p>Adds the outline panel of a selected table to the
     * layered pane when a drag operation occurs.
     *
     * @param the dragging outline panel to be added
     */
    protected void addOutlinePanel(JPanel panel) {
        layeredPane.add(panel, JLayeredPane.DRAG_LAYER);
    }

    /**
     * <p>Removes the specified outline panel when dragging
     * has completed (mouse released).
     *
     * @param the outline drag panel to remove
     */
    protected void removeOutlinePanel(JPanel panel) {
        layeredPane.remove(panel);
    }

    /**
     * <p>Resets the ERD table joins for all tables.
     */
    protected void resetAllTableJoins() {

        for (int i = 0, k = tables.size(); i < k; i++) {
            tables.elementAt(i).resetAllJoins();
        }
        for (ErdMoveableComponent comp : getAllComponentsVector()) {
            comp.resetInsets();
        }

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
     * @param a Dimension object representing the desired
     *          preferred size for the canvas
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

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].setTableBackground(c);
        }

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
        if (tables.size() == 0) {
            return UIUtils.getColour("executequery.Erd.tableBackground", Color.WHITE);
        } else {
            return tables.elementAt(0).getTableBackground();
        }
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

    /**
     * <p>Removes the specified <code>ErdTable</code> from
     * the <code>Vector</code>.
     *
     * @param the table to remove
     */
    public void removeTableComponent(ErdTable table) {
        tables.removeElement(table);
    }

    public void setTableDisplayFont(String fontName, int tableNameStyle,
                                    int columnNameStyle, int textBlockStyle, int size) {

        tableFontSize = size;
        tableFontName = fontName;
        tableNameFontStyle = tableNameStyle;
        columnNameFontStyle = columnNameStyle;
        textBlockFontStyle = textBlockStyle;

        tableNameFont = new Font(fontName, tableNameStyle, size + 1);
        columnNameFont = new Font(fontName, columnNameStyle, size);
        textBlockFont = new Font(fontName, textBlockStyle, size);

        ErdTable[] tablesArray = getAllTablesArray();

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].tableColumnsChanged(true);
        }

        layeredPane.repaint();
    }

    public boolean canPrint() {

        return true;
    }

    public String getPrintJobName() {
        return "Red Expert - ERD";
    }

    public Printable getPrintable() {
        return new ErdPrintable(this);
    }

    public boolean isDisplayKeysOnly() {
        return displayKeysOnly;
    }

    public void setDisplayKeysOnly(boolean displayKeysOnly) {
        this.displayKeysOnly = displayKeysOnly;
        ErdTable[] allTables = getAllTablesArray();
        for (int i = 0; i < allTables.length; i++) {
            allTables[i].setDisplayReferencedKeysOnly(displayKeysOnly);
            allTables[i].tableColumnsChanged(true);
        }
        layeredPane.repaint();
    }

    public void setDisplayCommentOnFields(boolean displayCommentOnFields) {
        ErdTable[] allTables = getAllTablesArray();
        for (int i = 0; i < allTables.length; i++) {
            allTables[i].setShowCommentOnFields(displayCommentOnFields);
            allTables[i].tableColumnsChanged(true);
        }
        layeredPane.repaint();
    }

    public void setDisplayCommentOnTable(boolean displayCommentOnTable) {
        ErdTable[] allTables = getAllTablesArray();
        for (int i = 0; i < allTables.length; i++) {
            allTables[i].setShowCommentOnTable(displayCommentOnTable);
            allTables[i].tableColumnsChanged(true);
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
        boolean tablesRemoved = false;
        table.clean();
        layeredPane.remove(table);
        tables.remove(table);
        tablesRemoved = true;

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

    public void setColumnNameFont(Font font) {
        columnNameFont = font;
    }

    public Font getColumnNameFont() {
        return columnNameFont;
    }

    public void setTableNameFont(Font font) {
        tableNameFont = font;
    }

    public Font getTableNameFont() {
        return tableNameFont;
    }

    public Font getTextBlockFont() {
        return textBlockFont;
    }

    public void setTextBlockFont(Font textBlockFont) {
        this.textBlockFont = textBlockFont;
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

    public void setTableFontSize(int tableFontSize) {
        this.tableFontSize = tableFontSize;
    }

    public void setTableFontName(String tableFontName) {
        this.tableFontName = tableFontName;
    }

    public JLayeredPane getCanvas() {
        return layeredPane;
    }

    public void resizeCanvas() {
        scroll.resizeCanvas();
    }

    public Vector<ErdTable> getAllTablesVector() {
        return tables;
    }

    public Vector<ErdMoveableComponent> getAllComponentsVector() {
        Vector<ErdMoveableComponent> vector = new Vector<>();
        if (tables != null)
            vector.addAll(tables);
        if (textPanels != null)
            vector.addAll(textPanels);
        return vector;
    }

    public Vector getTableColumnsVector(String tableName) {

        if (tables == null)
            tables = new Vector();

        int v_size = tables.size();
        ErdTable erdTable = null;
        Vector columns = null;
        Vector _columns = null;

        for (int i = 0; i < v_size; i++) {
            erdTable = tables.elementAt(i);

            if (erdTable.getTableName().equalsIgnoreCase(tableName)) {
                _columns = erdTable.getTableColumnsVector();

                int size = _columns.size();
                columns = new Vector(size);

                for (int j = 0; j < size; j++) {
                    columns.add(_columns.elementAt(j).toString());
                }

                break;
            }

        }

        return columns;
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

    public Vector<ErdTextPanel> getTextPanels() {
        return textPanels;
    }

    protected Dimension getMaxImageExtents() {
        int width = 0;
        int height = 0;
        int tableExtentX = 0;
        int tableExtentY = 0;

        ErdTable[] tablesArray = getAllTablesArray();

        for (int i = 0; i < tablesArray.length; i++) {
            tableExtentX = tablesArray[i].getX() + tablesArray[i].getWidth();
            tableExtentY = tablesArray[i].getY() + tablesArray[i].getHeight();

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

    public void setSavedErd(ErdSaveFileFormat savedErd) {
        this.savedErd = savedErd;
    }

    public void setSavedErd(ErdSaveFileFormat _savedErd, String absolutePath) {

        if (tables != null && tables.size() > 0) {
            int confirm = GUIUtilities.displayConfirmDialog(bundleString("setSavedErd"));

            if (confirm == JOptionPane.YES_OPTION) {

                if (savedErd != null) {
                    saveApplicationFileFormat(new File(savedErd.getAbsolutePath()));
                } else {
                    new ErdSaveDialog(this);
                }

            }

            removeAllTables();

        }

        tables = new Vector();
        textPanels = new Vector<>();

        Font columnNameFont = _savedErd.getColumnNameFont();
        Font tableNameFont = _savedErd.getTableNameFont();
        Font textBlockFont = _savedErd.getTextBlockFont();

        ErdTableFileData[] fileData = _savedErd.getTables();
        ErdTable table = null;

        if (fileData != null) {
            for (int i = 0; i < fileData.length; i++) {
                table = new ErdTable(fileData[i].getTableName(),
                        fileData[i].getColumnData(), this);

                table.setCreateTableScript(fileData[i].getCreateTableScript());
                table.setAlterTableHash(fileData[i].getAlterTableHash());
                table.setAlterTableScript(fileData[i].getAlterTableScript());
                table.setAddConstraintsScript(fileData[i].getAddConstraintScript());
                table.setBounds(fileData[i].getTableBounds());
                table.setEditable(true);
                table.setTableBackground(fileData[i].getTableBackground());
                table.setDescriptionTable(fileData[i].getTableDescription());
                table.setShowCommentOnTable(fileData[i].isShowCommentOnTable());
                table.setShowCommentOnFields(fileData[i].isShowCommentsOnfields());

                layeredPane.add(table);
                addTableToList(table);
                table.toFront();
                table.tableColumnsChanged(false);
            }
        }

        ErdTitlePanelData titlePanelData = _savedErd.getTitlePanel();

        if (titlePanelData != null) {
            ErdTitlePanel _erdTitlePanel = new ErdTitlePanel(this,
                    titlePanelData.getErdName(),
                    titlePanelData.getErdDate(),
                    titlePanelData.getErdDescription(),
                    titlePanelData.getErdDatabase(),
                    titlePanelData.getErdAuthor(),
                    titlePanelData.getErdRevision(),
                    titlePanelData.getErdFileName());
            _erdTitlePanel.setBounds(titlePanelData.getTitleBounds());
            layeredPane.add(_erdTitlePanel);
            _erdTitlePanel.toFront();
            this.erdTitlePanel = _erdTitlePanel;
        }
        ErdTextPanelData[] textFileData = _savedErd.getTextBlocks();
        ErdTextPanel textPanel = null;
        if (textFileData != null) {
            for (int i = 0; i < textFileData.length; i++) {
                textPanel = new ErdTextPanel(this,
                        textFileData[i].getErdDescription());

                textPanel.setBounds(textFileData[i].getTableBounds());
                textPanel.setTableBackground(textFileData[i].getTableBackground());
                textPanels.add(textPanel);
                layeredPane.add(textPanel);
                textPanel.toFront();
            }
        }

        this.savedErd = _savedErd;
        savedErd.setAbsolutePath(absolutePath);

        tableFontName = tableNameFont.getName();
        tableFontSize = columnNameFont.getSize();
        tableNameFontStyle = tableNameFont.getStyle();
        columnNameFontStyle = columnNameFont.getStyle();
        textBlockFontStyle = textBlockFont.getStyle();

        if (savedErd.hasCanvasBackground())
            setCanvasBackground(savedErd.getCanvasBackground());

        fileName = savedErd.getFileName();
        GUIUtilities.setTabTitleForComponent(this, TITLE + " - " + fileName);

        dependsPanel.setTableDependencies(buildTableRelationships());
        resizeCanvas();
        layeredPane.validate();
    }

    void addTableToList(ErdTable table) {
        if (table == null)
            tables = new Vector<>();
        tables.add(table);
        table.setTitleBarBgColor((tables.size() - 1) % TITLE_COLORS.length);
    }

    public boolean hasOpenFile() {
        return savedErd != null;
    }

    public boolean contentCanBeSaved() {

        return tables.size() > 0;

    }

    public int save(boolean saveAs) {

        ErdSaveDialog saveDialog = null;

        if (savedErd != null) {

            if (saveAs) {
                saveDialog = new ErdSaveDialog(this, savedErd.getAbsolutePath());
            } else {
                return saveApplicationFileFormat(new File(savedErd.getAbsolutePath()));
            }

        } else {
            saveDialog = new ErdSaveDialog(this, new File(fileName));
        }

        int saved = saveDialog.getSaved();
        saveDialog = null;

        return saved;
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
        if (textFileData != null)
            eqFormat.setTextBlocks(textFileData);
        eqFormat.setColumnNameFont(columnNameFont);
        eqFormat.setTableNameFont(tableNameFont);
        eqFormat.setTextBlockFont(textBlockFont);
        if (tables != null && tables.length > 0)
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
            e.printStackTrace();
            GUIUtilities.displayErrorMessage(bundleString("errorFileSaving") + e.getMessage());
            return SaveFunction.SAVE_FAILED;
        }

    }

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

        tools.setScaleComboValue(value);
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
        resetTableValues(tableInfos);
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
                    tools.incrementScaleCombo(1);
                }
            } else
                return;

        } else {

            if (scale > 0.25) {
                scale -= 0.25;
                if (tools != null) {
                    tools.incrementScaleCombo(-1);
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

    public void setTextBlockFontStyle(int textBlockFontStyle) {
        this.textBlockFontStyle = textBlockFontStyle;
    }

    public String getAllSQLText() {
        char newLine = '\n';
        StringBuffer sb = new StringBuffer();
        ErdTable[] allTables = getAllTablesArray();

        for (int i = 0; i < allTables.length; i++) {

            if (allTables[i].hasSQLScripts()) {
                sb.append(allTables[i].getAllSQLScripts()).
                        append(newLine);
            }

        }

        return sb.toString();
    }

    public String getErdFileName() {
        return fileName;
    }

    public String getDisplayName() {
        return toString();
    }

    public String toString() {
        return TITLE + " - " + fileName;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
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


    // --------------------------------------------

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

        int width = newTable.getWidth();
        int height = newTable.getHeight();

        if (setCentered) {
            newTable.setBounds((layeredPane.getWidth() - newTable.getWidth()) / 2,
                    (layeredPane.getHeight() - newTable.getHeight()) / 2,
                    width, height);
            fireSaveUndoAction(new UndoRedoAction(NEW_OBJECT, newTable));
        } else {

            if (next_y + height + 20 > INITIAl_VIEW_HEIGHT) {
                next_y = 20;
                if (tables.size() > 0)
                    next_x += lastWidth + HORIZ_DIFF;
                lastWidth = 0;
            }

            newTable.setBounds(next_x, next_y, width, height);

            next_y += height + VERT_DIFF;
            if (lastWidth < width)
                lastWidth = width;
        }
        addTableToList(newTable);
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
