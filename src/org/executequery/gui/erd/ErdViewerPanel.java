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
import org.executequery.print.PrintFunction;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.Printable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
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

    /**
     * The panel's title
     */
    public static final String TITLE = "Entity Relationship Diagram";

    /**
     * The panel's icon
     */
    public static final String FRAME_ICON = "ErdPanel16.png";

    /**
     * Whether this instance has a tool bar palatte
     */
    private boolean showTools;

    /**
     * Whether this is a static diagram
     */
    private boolean editable;

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
    private Vector tables;

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

    /**
     * the connection props object
     */
    private DatabaseConnection databaseConnection;

    /**
     * flag whether to display reference keys only
     */
    private boolean displayKeysOnly = false;

    private static final int INITIAl_VIEW_HEIGHT = 450;

    private double defaultScaledView;

    /**
     * The scale values
     */
    protected static final String[] scaleValues = {"25%", "50%", "75%", "100%",
            "125%", "150%", "175%", "200%"};

    private static int openCount = 1;

    private List tableNames;

    private List columnData;

    public ErdViewerPanel(boolean showTools, boolean editable) {
        this(null, null, true, showTools, editable);
    }

    public ErdViewerPanel(Vector tableNames, Vector columnData, boolean isNew) {
        this(tableNames, columnData, isNew, true, true);
    }

    public ErdViewerPanel(Vector tableNames, Vector columnData,
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
            setTables(tableNames, columnData);
        } else {
            tables = new Vector();
        }

        if (tableNames != null && columnData != null) {
            dependsPanel.setTableDependencies(buildTableRelationships());
            resizeCanvas();
            layeredPane.validate();
        }

        fileName = "erd" + (openCount++) + ".eqd";
        setScaledView(0.75);
    }

    public ErdViewerPanel(ErdSaveFileFormat savedErd, String absolutePath) {
        this(null, null, true, true, true);
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
        tableFontSize = 10;
        tableNameFontStyle = Font.PLAIN;
        columnNameFontStyle = Font.PLAIN;

        tableNameFont = new Font(tableFontName, tableNameFontStyle, tableFontSize + 1);
        columnNameFont = new Font(tableFontName, columnNameFontStyle, tableFontSize);

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
        
        /*
        ActionMap actionMap = getActionMap();
        actionMap.put("zoom-in", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom(true);
            }
        });
        
        actionMap.put("zoom-out", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom(false);
            }
        });
        
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("control EQUALS"), "zoom-in");
        inputMap.put(KeyStroke.getKeyStroke("control MINUS"), "zoom-out");
        */
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

    public void resetTableValues(List tableNames, List columnData) {
        removeAllTables();
        setTables(tableNames, columnData);
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
    public void setTables(List tableNames, List columnData) {

        this.tableNames = tableNames;
        this.columnData = columnData;

        // next position of component added
        int next_x = 20;
        int next_y = 20;

        // height and width of current table
        int height = -1;
        int width = -1;

        // width of last table
        int lastWidth = 0;

        // vertical and horizontal differences
        int vertDiff = 50;
        int horizDiff = 50;

        int size = tableNames.size();
        tables = new Vector(size);

        ErdTable table = null;
        for (int i = 0; i < size; i++) {

            // create the ERD display component
            table = new ErdTable((String) tableNames.get(i),
                    (ColumnData[]) columnData.get(i), this);

            table.setEditable(editable);
            height = table.getHeight();
            width = table.getWidth();

            // if it doesn't fit vertically within the
            // initial size of the view, move to a new
            // column within the grid display
            if (next_y + height + 20 > INITIAl_VIEW_HEIGHT) {
                next_y = 20;

                if (i > 0)
                    next_x += lastWidth + horizDiff;

                lastWidth = 0;

            }

            // position within the layered pane
            table.setBounds(next_x, next_y, width, height);
            layeredPane.add(table);

            table.toFront();

            next_y += height + vertDiff;

            if (lastWidth < width)
                lastWidth = width;

            // add to the vector
            tables.add(table);

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

        ErdTable[] tables_array = getAllComponentsArray();

        Vector tableDependencies = new Vector();
        ErdTableDependency dependency = null;

        ErdTable table = null;
        HashMap tempHash = new HashMap();

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

                            table = (ErdTable) tables.elementAt(j);

                            // check to see that the combination
                            // does not already exist
                            if ((tempHash.containsKey(tables_array[k]) &&
                                    tempHash.get(tables_array[k]) == table) ||
                                    (tempHash.containsKey(table) &&
                                            tempHash.get(table) == tables_array[k])) {
                                break;
                            }

                            dependency = new ErdTableDependency(tables_array[k], table);

                            // place the tables in the temp HashMap so
                            // the combination is not added a second time
                            tempHash.put(tables_array[k], table);
                            tempHash.put(table, tables_array[k]);

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
            ((ErdTable) tables.elementAt(i)).resetAllJoins();
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
        ErdTable[] tablesArray = getAllComponentsArray();

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
            return ((ErdTable) tables.elementAt(0)).getTableBackground();
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
                                    int columnNameStyle, int size) {

        tableFontSize = size;
        tableFontName = fontName;
        tableNameFontStyle = tableNameStyle;
        columnNameFontStyle = columnNameStyle;

        tableNameFont = new Font(fontName, tableNameStyle, size + 1);
        columnNameFont = new Font(fontName, columnNameStyle, size);

        ErdTable[] tablesArray = getAllComponentsArray();

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].tableColumnsChanged();
        }

        layeredPane.repaint();
    }

    public boolean canPrint() {

        return true;
    }

    public String getPrintJobName() {
        return "RedXpert - ERD";
    }

    public Printable getPrintable() {
        return new ErdPrintable(this);
    }

    public boolean isDisplayKeysOnly() {
        return displayKeysOnly;
    }

    public void setDisplayKeysOnly(boolean displayKeysOnly) {
        this.displayKeysOnly = displayKeysOnly;
        ErdTable[] allTables = getAllComponentsArray();
        for (int i = 0; i < allTables.length; i++) {
            allTables[i].setDisplayReferencedKeysOnly(displayKeysOnly);
            allTables[i].tableColumnsChanged();
        }
        layeredPane.repaint();
    }

    /**
     * <p>Adds a new table to the canvas.
     */
    protected void addNewTable(ErdTable newTable) {

        if (tables == null) {
            tables = new Vector();
        }

        tables.add(newTable);

        // place the new table in the center of the canvas
        newTable.setBounds((layeredPane.getWidth() - newTable.getWidth()) / 2,
                (layeredPane.getHeight() - newTable.getHeight()) / 2,
                newTable.getWidth(), newTable.getHeight());

        layeredPane.add(newTable, JLayeredPane.DEFAULT_LAYER, tables.size());
        newTable.toFront();
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
            erdTable = (ErdTable) tables.elementAt(i);
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

    protected void removeSelectedTables() {
        boolean tablesRemoved = false;
        ErdTable[] allTables = getAllComponentsArray();

        for (int i = 0; i < allTables.length; i++) {

            if (allTables[i].isSelected()) {
                allTables[i].clean();
                layeredPane.remove(allTables[i]);
                tables.remove(allTables[i]);
                allTables[i] = null;
                tablesRemoved = true;
            }

        }

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

    public Vector getAllComponentsVector() {
        return tables;
    }

    public Vector getTableColumnsVector(String tableName) {

        if (tables == null)
            tables = new Vector();

        int v_size = tables.size();
        ErdTable erdTable = null;
        Vector columns = null;
        Vector _columns = null;

        for (int i = 0; i < v_size; i++) {
            erdTable = (ErdTable) tables.elementAt(i);

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

    public ErdTable[] getAllComponentsArray() {

        if (tables == null)
            tables = new Vector();

        int v_size = tables.size();
        ErdTable[] tablesArray = new ErdTable[v_size];

        for (int i = 0; i < v_size; i++) {
            tablesArray[i] = (ErdTable) tables.elementAt(i);
        }

        return tablesArray;
    }

    protected Dimension getMaxImageExtents() {
        int width = 0;
        int height = 0;
        int tableExtentX = 0;
        int tableExtentY = 0;

        ErdTable[] tablesArray = getAllComponentsArray();

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
        ErdTable[] allTables = getAllComponentsArray();

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
            int confirm = GUIUtilities.displayConfirmDialog("Do you want to save your changes?");

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

        Font columnNameFont = _savedErd.getColumnNameFont();
        Font tableNameFont = _savedErd.getTableNameFont();

        ErdTableFileData[] fileData = _savedErd.getTables();
        ErdTable table = null;

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

            layeredPane.add(table);
            tables.add(table);
            table.toFront();
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

        this.savedErd = _savedErd;
        savedErd.setAbsolutePath(absolutePath);

        tableFontName = tableNameFont.getName();
        tableFontSize = columnNameFont.getSize();
        tableNameFontStyle = tableNameFont.getStyle();
        columnNameFontStyle = columnNameFont.getStyle();

        if (savedErd.hasCanvasBackground())
            setCanvasBackground(savedErd.getCanvasBackground());

        fileName = savedErd.getFileName();
        GUIUtilities.setTabTitleForComponent(this, TITLE + " - " + fileName);

        dependsPanel.setTableDependencies(buildTableRelationships());
        resizeCanvas();
        layeredPane.validate();
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

        ErdTable[] tables = getAllComponentsArray();
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
        }

        ErdSaveFileFormat eqFormat = new ErdSaveFileFormat(fileData, file.getName());
        eqFormat.setColumnNameFont(columnNameFont);
        eqFormat.setTableNameFont(tableNameFont);
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
            GUIUtilities.displayErrorMessage("An error occured saving to file:\n" +
                    e.getMessage());
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

        ErdTable[] tablesArray = getAllComponentsArray();

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
        resetTableValues(tableNames, columnData);
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

            ErdTable table = (ErdTable) tables.elementAt(i);
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

    public String getAllSQLText() {
        char newLine = '\n';
        StringBuffer sb = new StringBuffer();
        ErdTable[] allTables = getAllComponentsArray();

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

}















