/*
 * ErdDependanciesPanel.java
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

import javax.swing.*;
import java.awt.*;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdDependanciesPanel extends JComponent {

    /**
     * The controller for the ERD viewer
     */
    private final ErdViewerPanel parent;
    /**
     * The table dependencies to draw
     */
    private ErdTableDependency[] dependencies;

    /**
     * The line colour
     */
    private int lineColour;
    /**
     * The dashed stroke
     */
    private static BasicStroke dashedStroke;
    /**
     * Solid line stroke
     */
    private static BasicStroke solidStroke;

    /**
     * Whether a dashed line stroke is selected
     */
    private boolean isDashed;
    /**
     * The line weight
     */
    private float lineWeight;
    /**
     * Whether the arrow connection is filled
     */
    private boolean filledArrow;
    /**
     * The line style
     */
    private int lineStyle;
    /**
     * The colour index
     */
    private final int colourIndex;

    /**
     * A solid line
     */
    public static final int LINE_STYLE_ONE = 0;
    /**
     * Dashed style 1
     */
    public static final int LINE_STYLE_TWO = 1;
    /**
     * Dashed style 2
     */
    public static final int LINE_STYLE_THREE = 2;

    /**
     * <p>Constructs a new instance with the specified
     * <code>ErdViewerPanel</code> as the parent or controller
     * object.
     *
     * @param the parent controller object
     */
    public ErdDependanciesPanel(ErdViewerPanel parent) {
        super();

        lineColour = 0;
        colourIndex = 0;
        lineStyle = 0;
        lineWeight = 2.0f;
        isDashed = false;
        filledArrow = true;
        solidStroke = new BasicStroke(lineWeight);

        setDoubleBuffered(true);
        this.parent = parent;

    }

    /**
     * <p>Constructs a new instance with the specified
     * <code>ErdViewerPanel</code> as the parent or controller
     * object.
     *
     * @param the parent controller object
     */
    public ErdDependanciesPanel(ErdViewerPanel parent, Vector t_dependencies) {
        this(parent);

        if (t_dependencies != null)
            setTableDependencies(t_dependencies);

    }

    public ErdTableDependency[] getTableDependencies() {
        return dependencies;
    }

    public void setArrowStyle(boolean filledArrow) {
        this.filledArrow = filledArrow;
    }

    public int getArrowStyleIndex() {
        return filledArrow ? 0 : 1;
    }

    public void setLineWeight(float lineWeight) {
        this.lineWeight = lineWeight;
    }

    public float getLineWeight() {
        return lineWeight;
    }

    public int getColourIndex() {
        return colourIndex;
    }

    public int getLineStyleIndex() {
        return lineStyle;
    }

    public void setLineStyle(int style) {
        lineStyle = style;
        solidStroke = new BasicStroke(lineWeight);

        switch (style) {
            case LINE_STYLE_ONE:
                isDashed = false;
                break;
            case LINE_STYLE_TWO:
                isDashed = true;
                float[] dash1 = {2.0f};
                dashedStroke = new BasicStroke(lineWeight, 0, 0, 10f, dash1, 0.0f);
                break;
            case LINE_STYLE_THREE:
                isDashed = true;
                float[] dash2 = {5f, 2.0f};
                dashedStroke = new BasicStroke(lineWeight, 0, 0, 10f, dash2, 0.0f);
                break;
        }

    }

    public int getLineColour() {
        return lineColour;
    }

    public void setLineColour(int lineColour) {
        this.lineColour = lineColour;
    }

    protected void setTableDependencies(Vector v) {
        int v_size = v.size();
        dependencies = new ErdTableDependency[v_size];

        for (int i = 0; i < v_size; i++) {
            dependencies[i] = (ErdTableDependency) v.elementAt(i);
        }

    }

    public Dimension getSize() {
        return getPreferredSize();
    }

    /**
     * <p>Overrides to return <code>false</code>.
     *
     * @return <code>false</code>
     */
    public boolean isOpaque() {
        return false;
    }

    /**
     * <p>Overrides this class's <code>paintComponent</code>
     * method to draw the relationship lines between tables.
     *
     * @param the <code>Graphics</code> object
     */
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawDependencies(g2d);
    }

    protected void drawDependencies(Graphics2D g2d) {
        drawDependencies(g2d, 0, 0);
    }

    TreeSet<Integer> verticalLines;

    protected void drawDependencies(Graphics2D g2d, int xOffset, int yOffset) {

        if (dependencies == null) {
            return;
        }
        parent.resetAllTableJoins();
        if (verticalLines == null)
            verticalLines = new TreeSet<>();
        verticalLines.clear();

        for (int i = 0; i < dependencies.length; i++) {
            if (getLineColour() == 0)
                g2d.setColor(ErdViewerPanel.LINE_COLORS[dependencies[i].getTable_1().getTitleBarBgColor()]);
            else g2d.setColor(Color.BLACK);
            determinePositions(dependencies[i], 20);
            drawLines(g2d, dependencies[i], xOffset, yOffset);
        }
    }

    private void determinePositions(ErdTableDependency dependency, int indent) {

        dependency.reset();

        ErdTable table1 = dependency.getTable_1();
        ErdTable table2 = dependency.getTable_2();

        Rectangle rec1 = table1.getColumnBounds(dependency.getColumn1());
        Rectangle rec2 = table2.getColumnBounds(dependency.getColumn2());

        if (rec2.x < (rec1.x + rec1.width + indent) && (rec2.x + rec2.width + indent) > rec1.x) {

            dependency.setPosition(ErdTableDependency.POSITION_1);


            dependency.setXPosn_1(rec1.x + rec1.width);
            dependency.setYPosn_1(rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1()));

            dependency.setXPosn_3(rec2.x + rec2.width);
            dependency.setYPosn_3(rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2()));

            int x = dependency.getXPosn_1();
            if (dependency.getXPosn_3() > x)
                x = dependency.getXPosn_3();
            while (verticalLines.contains(Integer.valueOf(x + 20)))
                x += 10;
            dependency.setXPosn_2(x + 20);
            verticalLines.add(Integer.valueOf(x + 20));


        } else if (rec2.x > (rec1.x + rec1.width + indent)) {

            dependency.setPosition(ErdTableDependency.POSITION_2);

            dependency.setXPosn_1(rec1.x + rec1.width);


            int x = dependency.getXPosn_1() +
                    ((rec2.x - dependency.getXPosn_1()) / 2);
            while (verticalLines.contains(Integer.valueOf(x))) {
                x += 10;
            }
            if (x < rec2.x - 20) {
                dependency.setXPosn_2(x);
                verticalLines.add(Integer.valueOf(x));
                dependency.setYPosn_1(rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1()));

                dependency.setXPosn_3(rec2.x);
                dependency.setYPosn_3(rec2.y + table2.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn2()));

            } else determinePositions(dependency, indent + 10);


        } else {
            dependency.setPosition(ErdTableDependency.POSITION_3);
            dependency.setXPosn_1(rec1.x);


            dependency.setXPosn_3(rec2.x + rec2.width);


            int x = dependency.getXPosn_3() +
                    ((rec1.x - dependency.getXPosn_3()) / 2);
            while (verticalLines.contains(Integer.valueOf(x))) {
                x -= 10;
            }
            if (x > rec2.x + rec2.width + 20) {
                dependency.setXPosn_2(x);
                verticalLines.add(Integer.valueOf(x));
                dependency.setYPosn_1(rec1.y + table1.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn1()));
                dependency.setYPosn_3(rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2()));
            } else determinePositions(dependency, indent + 10);
        }

    }

    private void drawLines(Graphics2D g,
                           ErdTableDependency dependency,
                           int xOffset,
                           int yOffset) {

        int poliSize = 10;
        poliSize += 2 * lineWeight;
        int poliSize2 = poliSize / 2;
        int xPosn_1 = dependency.getXPosn_1() + xOffset;
        int xPosn_2 = dependency.getXPosn_2() + xOffset;
        int xPosn_3 = dependency.getXPosn_3() + xOffset;
        int yPosn_1 = dependency.getYPosn_1() + yOffset;
        int yPosn_2 = dependency.getYPosn_2() + yOffset;
        int yPosn_3 = dependency.getYPosn_3() + yOffset;

        if (isDashed) {
            g.setStroke(dashedStroke);
        } else {
            g.setStroke(solidStroke);
        }

        if (dependency.getPosition() == ErdTableDependency.POSITION_1) {

            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_3);
            g.drawLine(xPosn_2, yPosn_3, xPosn_3 + (int) lineWeight, yPosn_3);

            int[] polyXs = {xPosn_3 + poliSize, xPosn_3, xPosn_3 + poliSize};
            int[] polyYs = {yPosn_3 - poliSize2, yPosn_3, yPosn_3 + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        } else if (dependency.getPosition() == ErdTableDependency.POSITION_2) {

            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_3);
            g.drawLine(xPosn_2, yPosn_3, xPosn_3 - (int) lineWeight, yPosn_3);

            int[] polyXs = {xPosn_3 - poliSize, xPosn_3, xPosn_3 - poliSize};
            int[] polyYs = {yPosn_3 - poliSize2, yPosn_3, yPosn_3 + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        } else if (dependency.getPosition() == ErdTableDependency.POSITION_3) {

            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_3);
            g.drawLine(xPosn_2, yPosn_3, xPosn_3 + (int) lineWeight, yPosn_3);

            int[] polyXs = {xPosn_3 + poliSize, xPosn_3, xPosn_3 + poliSize};
            int[] polyYs = {yPosn_3 - poliSize2, yPosn_3, yPosn_3 + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        }
    }
}















