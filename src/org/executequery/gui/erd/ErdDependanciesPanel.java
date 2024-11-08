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
import java.util.ArrayList;
import java.util.List;
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

    public static final int RIGHT_ANGLE = 0;
    public static final int Z_LINE = RIGHT_ANGLE + 1;
    private int lineBend;

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
        lineBend = RIGHT_ANGLE;
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

    public int getLineBend() {
        return lineBend;
    }

    public void setLineBend(int lineBend) {
        this.lineBend = lineBend;
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
            if (lineBend == RIGHT_ANGLE)
                determinePositionsRightAngle(dependencies[i], 30);
            else
                determinePositions(dependencies[i], 30);
            drawLines(g2d, dependencies[i], xOffset, yOffset);
        }
    }

    int MAX_VALUE = 100000;

    String strFromIntLocation(int location) {
        switch (location) {
            case GridBagConstraints.NORTH:
                return "NORTH";
            case GridBagConstraints.EAST:
                return "EAST";
            case GridBagConstraints.SOUTH:
                return "SOUTH";
            case GridBagConstraints.WEST:
                return "WEST";
        }
        return "UNKNOWN";
    }

    private List<Point> buildRoute(Point startPoint, Point finishPoint, List<Point> previousPoints) {
        previousPoints.add(startPoint);
        Vector<ErdMoveableComponent> components = parent.getAllComponentsVector();
        List<ErdMoveableComponent> intersects = new ArrayList<>();
        for (ErdMoveableComponent comp : components) {
            if (comp.getBounds().intersectsLine(startPoint.x, startPoint.y, finishPoint.x, finishPoint.y))
                intersects.add(comp);
        }
        if (!intersects.isEmpty()) {
            ErdMoveableComponent nearComp = intersects.get(0);
            NearBoundsInfo nbi = nearPointOnBounds(startPoint, finishPoint, nearComp.getBounds());
            if (intersects.size() > 1) {
                for (int i = 1; i < intersects.size(); i++) {
                    NearBoundsInfo nbiX = nearPointOnBounds(startPoint, finishPoint, intersects.get(i).getBounds());
                    if (nbiX.distanceToStartPoint < nbi.distanceToStartPoint) {
                        nearComp = intersects.get(i);
                        nbi = nbiX;
                    }
                }
            }
            Point nextPoint = null;
            Point nextNextPoint = null;
            int inset = nearComp.getInsetFromLocation(nbi.location);
            Point insetPoint = calculateInsetCoordinates(inset, nbi.location, nearComp.getBounds());
            nextPoint = findIntersection(startPoint, finishPoint, checkPoint(insetPoint, 0), checkPoint(insetPoint, MAX_VALUE));
            if (nextPoint == null) {
                if (nearComp.getBounds().contains(startPoint) || nearComp.getBounds().contains(finishPoint)) {
                    previousPoints.add(finishPoint);
                    return previousPoints;
                }
                nearComp.revertInsetFromLocation(nbi.location);
                int x = startPoint.x, y = startPoint.y;
                switch (nbi.location) {
                    case GridBagConstraints.NORTH:
                        y -= inset;
                        break;
                    case GridBagConstraints.EAST:
                        x += inset;
                        break;
                    case GridBagConstraints.SOUTH:
                        y += inset;
                        break;
                    case GridBagConstraints.WEST:
                        x -= inset;
                        break;

                }
                return buildRoute(new Point(x, y), finishPoint, previousPoints);
            }
            Rectangle r = nearComp.getBounds();
            Point rXY = new Point(r.x + r.width, r.y + r.height);
            switch (nbi.location) {
                case GridBagConstraints.NORTH:
                case GridBagConstraints.SOUTH:
                    if (nextPoint.x > startPoint.x)
                        nextNextPoint = new Point(rXY.x + inset, insetPoint.y);
                    else nextNextPoint = new Point(r.x - inset, insetPoint.y);
                    break;
                case GridBagConstraints.EAST:
                case GridBagConstraints.WEST:
                    if (nextPoint.y > startPoint.y)
                        nextNextPoint = new Point(insetPoint.x, rXY.y + inset);
                    else nextNextPoint = new Point(insetPoint.x, r.y - inset);
                    break;
            }
            previousPoints.add(nextPoint);
            return buildRoute(nextNextPoint, finishPoint, previousPoints);
        }
        previousPoints.add(finishPoint);
        return previousPoints;
    }

    Point checkPoint(Point p, int value) {
        return new Point(checkCoordinate(p.x, value), checkCoordinate(p.y, value));
    }

    int checkCoordinate(int coord, int value) {
        if (coord == -1)
            return value;
        return coord;
    }

    Point calculateInsetCoordinates(int inset, int location, Rectangle r) {
        int x = -1, y = -1;
        switch (location) {
            case GridBagConstraints.NORTH:
                y = r.y - inset;
                break;
            case GridBagConstraints.EAST:
                x = r.getBounds().x + r.getBounds().width + inset;
                break;
            case GridBagConstraints.SOUTH:
                y = r.y + r.height + inset;
                break;
            case GridBagConstraints.WEST:
                x = r.x - inset;
        }
        return new Point(x, y);
    }

    private NearBoundsInfo nearPointOnBounds(Point sPoint, Point fPoint, Rectangle r) {
        Point[][] recLines = new Point[][]{
                new Point[]{new Point(r.x, r.y), new Point(r.x + r.width, r.y)},
                new Point[]{new Point(r.x + r.width, r.y), new Point(r.x + r.width, r.y + r.height)},
                new Point[]{new Point(r.x, r.y + r.height), new Point(r.x + r.width, r.y + r.height)},
                new Point[]{new Point(r.x, r.y), new Point(r.x, r.y + r.height)},
        };
        NearBoundsInfo nbi = new NearBoundsInfo();
        nbi.distanceToStartPoint = Integer.MAX_VALUE;
        for (int i = 0; i < recLines.length; i++) {
            Point inters = findIntersection(sPoint, fPoint, recLines[i][0], recLines[i][1]);
            if (inters != null) {
                int distance = (int) sPoint.distance(inters.x, inters.y);
                if (distance < nbi.distanceToStartPoint) {
                    nbi.distanceToStartPoint = distance;
                    nbi.intersectsPoint = inters;
                    switch (i) {
                        case 0:
                            nbi.location = GridBagConstraints.NORTH;
                            break;
                        case 1:
                            nbi.location = GridBagConstraints.EAST;
                            break;
                        case 2:
                            nbi.location = GridBagConstraints.SOUTH;
                            break;
                        case 3:
                            nbi.location = GridBagConstraints.WEST;
                    }
                }
            }
        }
        return nbi;
    }

    private Point findIntersection(Point sp1, Point fp1, Point sp2, Point fp2) {
        int x1 = sp1.x;
        int y1 = sp1.y;
        int x2 = fp1.x;
        int y2 = fp1.y;

        int x3 = sp2.x;
        int y3 = sp2.y;
        int x4 = fp2.x;
        int y4 = fp2.y;

        double denominator = (y4 - y3) * (x1 - x2) - (x4 - x3) * (y1 - y2);

        if (denominator == 0) {
            return null; // Отрезки параллельны или совпадают
        } else {
            int numerator_a = (x4 - x2) * (y4 - y3) - (x4 - x3) * (y4 - y2);
            int numerator_b = (x1 - x2) * (y4 - y2) - (x4 - x2) * (y1 - y2);
            double Ua = numerator_a / denominator;
            double Ub = numerator_b / denominator;
            if (Ua >= 0 && Ua <= 1 && Ub >= 0 && Ub <= 1) {
                int x = (int) (x1 * Ua + x2 * (1 - Ua));
                int y = (int) (y1 * Ua + y2 * (1 - Ua));
                return new Point(x, y);
            } else return null;
            /*int intersectX = (((x1 * y2 - y1 * x2) * (x3 - x4)) - ((x1 - x2) * (x3 * y4 - y3 * x4))) / denominator;
            int intersectY = (((x1 * y2 - y1 * x2) * (y3 - y4)) - ((y1 - y2) * (x3 * y4 - y3 * x4))) / denominator;

            // Проверяем, лежит ли точка пересечения внутри обоих отрезков
            if (intersectX >= Math.min(x1, x2) && intersectX <= Math.max(x1, x2) &&
                    intersectX >= Math.min(x3, x4) && intersectX <= Math.max(x3, x4) &&
                    intersectY >= Math.min(y1, y2) && intersectY <= Math.max(y1, y2) &&
                    intersectY >= Math.min(y3, y4) && intersectY <= Math.max(y3, y4)) {
                return new Point(intersectX, intersectY);*/
        }
    }

    private void determinePositionsRightAngle(ErdTableDependency dependency, int indent) {

        dependency.reset();

        ErdTable table1 = dependency.getTable_1();
        ErdTable table2 = dependency.getTable_2();

        Rectangle rec1 = table1.getColumnBounds(dependency.getColumn1());
        Rectangle rec2 = table2.getColumnBounds(dependency.getColumn2());
        if(rec1==null||rec2==null)
            return;

        if (rec2.x < (rec1.x + rec1.width + indent) && (rec2.x + rec2.width + indent) > rec1.x) {

            dependency.setPosition(ErdTableDependency.POSITION_1);


            dependency.setPosition(ErdTableDependency.POSITION_1);
            Point startPoint = new Point(rec1.x + rec1.width, rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1()));
            dependency.getPoints().add(startPoint);

            Point finishPoint = new Point(rec2.x + rec2.width, rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2()));

            int x = startPoint.x;
            if (finishPoint.x > x)
                x = finishPoint.x;
            while (verticalLines.contains(Integer.valueOf(x + 20)))
                x += 10;
            dependency.getPoints().add(new Point(x + 20, startPoint.y));
            dependency.getPoints().add(new Point(x + 20, finishPoint.y));
            dependency.getPoints().add(finishPoint);
            verticalLines.add(Integer.valueOf(x + 20));


        } else if (rec2.x > (rec1.x + rec1.width + indent)) {

            dependency.setPosition(ErdTableDependency.POSITION_2);
            Point startPoint = new Point(rec1.x + rec1.width, -1);
            int x = startPoint.x +
                    ((rec2.x - startPoint.x) / 2);
            while (verticalLines.contains(Integer.valueOf(x))) {
                x += 10;
            }
            if (x < rec2.x - 20) {
                verticalLines.add(Integer.valueOf(x));
                startPoint.y = rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1());
                dependency.getPoints().add(startPoint);
                dependency.getPoints().add(new Point(x, startPoint.y));
                Point finishPoint = new Point(rec2.x, rec2.y + table2.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn2()));
                dependency.getPoints().add(new Point(x, finishPoint.y));
                dependency.getPoints().add(finishPoint);

            } else determinePositionsRightAngle(dependency, indent + 10);


        } else {
            dependency.setPosition(ErdTableDependency.POSITION_3);
            Point startPoint = new Point(rec1.x, -1);
            Point finishPoint = new Point(rec2.x + rec2.width, -1);
            int x = finishPoint.x +
                    ((rec1.x - finishPoint.x) / 2);
            while (verticalLines.contains(Integer.valueOf(x))) {
                x -= 10;
            }
            if (x > rec2.x + rec2.width + 20) {
                verticalLines.add(Integer.valueOf(x));
                startPoint.y = rec1.y + table1.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn1());
                finishPoint.y = rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2());
                dependency.getPoints().add(startPoint);
                dependency.getPoints().add(new Point(x, startPoint.y));
                dependency.getPoints().add(new Point(x, finishPoint.y));
                dependency.getPoints().add(finishPoint);
            } else determinePositionsRightAngle(dependency, indent + 10);
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
            Point startPoint = new Point(rec1.x + rec1.width, rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1()));
            dependency.getPoints().add(startPoint);
            Point finishPoint = new Point(rec2.x + rec2.width, rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2()));

            Point secondPoint = new Point(startPoint.x + 20, startPoint.y);
            Point preFinishPoint = new Point(finishPoint.x + 20, finishPoint.y);
            List<Point> points = buildRoute(secondPoint, preFinishPoint, dependency.getPoints());
            points.add(finishPoint);

        } else if (rec2.x > (rec1.x + rec1.width + indent)) {

            dependency.setPosition(ErdTableDependency.POSITION_2);
            Point startPoint = new Point(rec1.x + rec1.width, rec1.y + table1.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn1()));
            dependency.getPoints().add(startPoint);
            Point finishPoint = new Point(rec2.x, rec2.y + table2.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn2()));

            Point secondPoint = new Point(startPoint.x + 20, startPoint.y);
            Point preFinishPoint = new Point(finishPoint.x - 20, finishPoint.y);
            List<Point> points = buildRoute(secondPoint, preFinishPoint, dependency.getPoints());
            points.add(finishPoint);


        } else {
            dependency.setPosition(ErdTableDependency.POSITION_3);
            Point startPoint = new Point(rec1.x, rec1.y + table1.getNextJoin(ErdTable.LEFT_JOIN, dependency.getColumn1()));
            dependency.getPoints().add(startPoint);
            Point finishPoint = new Point(rec2.x + rec2.width, rec2.y + table2.getNextJoin(ErdTable.RIGHT_JOIN, dependency.getColumn2()));

            Point secondPoint = new Point(startPoint.x - 20, startPoint.y);
            Point preFinishPoint = new Point(finishPoint.x + 20, finishPoint.y);
            List<Point> points = buildRoute(secondPoint, preFinishPoint, dependency.getPoints());
            points.add(finishPoint);
        }

    }

    private void drawLines(Graphics2D g,
                           ErdTableDependency dependency,
                           int xOffset,
                           int yOffset) {

        int poliSize = 10;
        poliSize += 2 * lineWeight;
        int poliSize2 = poliSize / 2;

        if (isDashed) {
            g.setStroke(dashedStroke);
        } else {
            g.setStroke(solidStroke);
        }
        Point startPoint = dependency.getPoints().get(0);
        for (int i = 1; i < dependency.getPoints().size() - 1; i++) {
            g.drawLine(startPoint.x + xOffset, startPoint.y + yOffset, dependency.getPoints().get(i).x + xOffset, dependency.getPoints().get(i).y + yOffset);
            startPoint = dependency.getPoints().get(i);
        }
        Point lastPoint = dependency.getPoints().get(dependency.getPoints().size() - 1);
        lastPoint = new Point(lastPoint.x + xOffset, lastPoint.y + yOffset);
        if (dependency.getPosition() == ErdTableDependency.POSITION_1) {
            g.drawLine(startPoint.x + xOffset, startPoint.y + yOffset, lastPoint.x + (int) lineWeight, lastPoint.y);

            int[] polyXs = {lastPoint.x + poliSize, lastPoint.x, lastPoint.x + poliSize};
            int[] polyYs = {lastPoint.y - poliSize2, lastPoint.y, lastPoint.y + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        } else if (dependency.getPosition() == ErdTableDependency.POSITION_2) {

            g.drawLine(startPoint.x + xOffset, startPoint.y + yOffset, lastPoint.x - (int) lineWeight, lastPoint.y);

            int[] polyXs = {lastPoint.x - poliSize, lastPoint.x, lastPoint.x - poliSize};
            int[] polyYs = {lastPoint.y - poliSize2, lastPoint.y, lastPoint.y + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        } else if (dependency.getPosition() == ErdTableDependency.POSITION_3) {

            g.drawLine(startPoint.x + xOffset, startPoint.y + yOffset, lastPoint.x + (int) lineWeight, lastPoint.y);

            int[] polyXs = {lastPoint.x + poliSize, lastPoint.x, lastPoint.x + poliSize};
            int[] polyYs = {lastPoint.y - poliSize2, lastPoint.y, lastPoint.y + poliSize2};

            if (isDashed)
                g.setStroke(solidStroke);

            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);

        }
    }

    class NearBoundsInfo {
        Point intersectsPoint;
        int location;

        int distanceToStartPoint;
    }
}















