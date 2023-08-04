package org.underworldlabs.swing.treetable;

import java.awt.*;

public interface UIConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    /** Color used to draw vertical gridlines in JTables */
    public static final Color TABLE_VERTICAL_GRID_COLOR = /*!UIUtils.isDarkResultsBackground() ?
            new Color(214, 223, 247) :*/ new Color(84, 93, 117);

    /** if true, results tables display the horizontal grid lines */
    public static final boolean SHOW_TABLE_HORIZONTAL_GRID = false;

    /** if true, results tables display the vertical grid lines */
    public static final boolean SHOW_TABLE_VERTICAL_GRID = true;

    /** Color used for painting selected cell background in JTables */
    public static final Color TABLE_SELECTION_BACKGROUND_COLOR = new Color(193, 210, 238); //(253, 249, 237)

    /** Color used for painting selected cell foreground in JTables */
    public static final Color TABLE_SELECTION_FOREGROUND_COLOR = Color.BLACK;
    public static final int TABLE_ROW_MARGIN = 0;

    public static final String PROFILER_PANELS_BACKGROUND = "ProfilerPanels.background"; // NOI18N
}

