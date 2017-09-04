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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.util.Vector;

import javax.swing.JComponent;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
@SuppressWarnings({"rawtypes"})
public class ErdDependanciesPanel extends JComponent {
    
    /** The controller for the ERD viewer */
    private ErdViewerPanel parent;
    /** The table dependencies to draw */
    private ErdTableDependency[] dependencies;
    
    /** The line colour */
    private Color lineColour;
    /** The dashed stroke */
    private static BasicStroke dashedStroke;
    /** Solid line stroke */
    private static BasicStroke solidStroke;
    
    /** Whether a dashed line stroke is selected */
    private boolean isDashed;
    /** The line weight */
    private float lineWeight;
    /** Whether the arrow connection is filled */
    private boolean filledArrow;
    /** The line style */
    private int lineStyle;
    /** The colour index */
    private int colourIndex;
    
    /** A solid line */
    public static final int LINE_STYLE_ONE = 0;
    /** Dashed style 1 */
    public static final int LINE_STYLE_TWO = 1;
    /** Dashed style 2 */
    public static final int LINE_STYLE_THREE = 2;
    
    /** <p>Constructs a new instance with the specified
     *  <code>ErdViewerPanel</code> as the parent or controller
     *  object.
     *
     *  @param the parent controller object
     */
    public ErdDependanciesPanel(ErdViewerPanel parent) {
        super();
        
        lineColour = Color.BLACK;
        colourIndex = 0;
        lineStyle = 0;
        lineWeight = 1.0f;
        isDashed = false;
        filledArrow = true;
        solidStroke = new BasicStroke(lineWeight);
        
        setDoubleBuffered(true);
        this.parent = parent;
        
    }
    
    /** <p>Constructs a new instance with the specified
     *  <code>ErdViewerPanel</code> as the parent or controller
     *  object.
     *
     *  @param the parent controller object
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
                float dash1[] = {2.0f};
                dashedStroke = new BasicStroke(lineWeight, 0, 0, 10f, dash1, 0.0f);
                break;
            case LINE_STYLE_THREE:
                isDashed = true;
                float dash2[] = {5f, 2.0f};
                dashedStroke = new BasicStroke(lineWeight, 0, 0, 10f, dash2, 0.0f);
                break;
        }
        
    }
    
    public void setLineColour(Color lineColour) {
        this.lineColour = lineColour;
    }
    
    public Color getLineColour() {
        return lineColour;
    }
    
    protected void setTableDependencies(Vector v) {
        int v_size = v.size();
        dependencies = new ErdTableDependency[v_size];
        
        for (int i = 0; i < v_size; i++) {
            dependencies[i] = (ErdTableDependency)v.elementAt(i);
        }
        
    }
    
    public Dimension getSize() {
        return getPreferredSize();
    }
    
    /** <p>Overrides to return <code>false</code>.
     *
     *  @return <code>false</code>
     */
    public boolean isOpaque() {
        return false;
    }
    
    /** <p>Overrides this class's <code>paintComponent</code>
     *  method to draw the relationship lines between tables.
     *
     *  @param the <code>Graphics</code> object
     */
    protected void paintComponent(Graphics g) {
        parent.resetAllTableJoins();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawDependencies(g2d);
    }
    
    protected void drawDependencies(Graphics2D g2d) {
        drawDependencies(g2d, 0, 0);
    }

    protected void drawDependencies(Graphics2D g2d, int xOffset, int yOffset) {
        
        if (dependencies == null) {
            return;
        }

        g2d.setColor(lineColour);        
        for (int i = 0; i < dependencies.length; i++) {
            determinePositions(dependencies[i]);
            drawLines(g2d, dependencies[i], xOffset, yOffset);
        }
    }
    
    private void determinePositions(ErdTableDependency dependency) {
        
        dependency.reset();
        
        ErdTable table1 = dependency.getTable_1();
        ErdTable table2 = dependency.getTable_2();
        
        Rectangle rec1 = table1.getBounds();
        Rectangle rec2 = table2.getBounds();
        
        if ( ( (rec2.y + rec2.height + 20) < rec1.y ) &&
                ( rec2.x < (rec1.x + (rec1.width / 2)) ) ) {
            
            dependency.setPosition(ErdTableDependency.POSITION_1);
            
            dependency.setXPosn_3(table2.getNextJoin(ErdTable.BOTTOM_JOIN) + rec2.x);
            dependency.setXPosn_1(table1.getNextJoin(ErdTable.TOP_JOIN) + rec1.x);
            
            dependency.setYPosn_1(rec1.y);
            dependency.setYPosn_2((rec2.y + rec2.height) +
                    ((rec1.y - (rec2.y + rec2.height)) / 2));
            dependency.setYPosn_3(rec2.y + rec2.height);
            
        }
        
        else if ( ( (rec1.y + rec1.height + 20) < rec2.y ) &&
                ( rec1.x < (rec2.x + (rec2.width / 2)) ) ) {
            
            dependency.setPosition(ErdTableDependency.POSITION_2);
            
            dependency.setXPosn_1(table1.getNextJoin(ErdTable.BOTTOM_JOIN) + rec1.x);
            
            dependency.setXPosn_3(table2.getNextJoin(ErdTable.TOP_JOIN) + rec2.x);
            
            dependency.setYPosn_1(rec1.y + rec1.height);
            dependency.setYPosn_2(dependency.getYPosn_1() +
                    ((rec2.y - dependency.getYPosn_1()) / 2));
            dependency.setYPosn_3(rec2.y);
            
        }
        
        else if (rec1.y > rec2.y + (0.75 * rec2.height) + 20) {
            
            dependency.setPosition(ErdTableDependency.POSITION_3);
            
            dependency.setXPosn_2(table2.getNextJoin(ErdTable.BOTTOM_JOIN) + rec2.x);
            
            if (rec1.x > rec2.x) {
                dependency.setYPosn_1(table1.getNextJoin(ErdTable.LEFT_JOIN) + rec1.y);
                dependency.setXPosn_1(rec1.x);
            } else {
                dependency.setYPosn_1(table1.getNextJoin(ErdTable.RIGHT_JOIN) + rec1.y);
                dependency.setXPosn_1(rec1.x + rec1.width);
            }
            
            dependency.setYPosn_2(rec2.y + rec2.height);
            
        }
        
        else if (rec2.y > rec1.y + (0.75 * rec1.height) + 20) {
            
            dependency.setPosition(ErdTableDependency.POSITION_4);
            
            dependency.setXPosn_1(table1.getNextJoin(ErdTable.BOTTOM_JOIN) + rec1.x);
            
            if (rec1.x > rec2.x) {
                dependency.setYPosn_2(table2.getNextJoin(ErdTable.RIGHT_JOIN) + rec2.y);
                dependency.setXPosn_2(rec2.x + rec2.width);
            } else {
                dependency.setYPosn_2(table2.getNextJoin(ErdTable.LEFT_JOIN) + rec2.y);
                dependency.setXPosn_2(rec2.x);
            }
            
            dependency.setYPosn_1(rec1.y + rec1.height);
            
        }
        
        else if (rec2.x < rec1.x) {
            
            dependency.setPosition(ErdTableDependency.POSITION_5);
            
            dependency.setYPosn_1(table2.getNextJoin(ErdTable.RIGHT_JOIN) + rec2.y);
            
            dependency.setYPosn_2(table1.getNextJoin(ErdTable.LEFT_JOIN) + rec1.y);
            
            dependency.setXPosn_1(rec2.x + rec2.width);
            dependency.setXPosn_2(dependency.getXPosn_1() +
                    ((rec1.x - dependency.getXPosn_1()) / 2));
            dependency.setXPosn_3(rec1.x);
            
        }
        
        else {
            
            dependency.setPosition(ErdTableDependency.POSITION_6);
            
            dependency.setYPosn_2(table1.getNextJoin(ErdTable.RIGHT_JOIN) + rec1.y);
            dependency.setYPosn_1(table2.getNextJoin(ErdTable.LEFT_JOIN) + rec2.y);
            
            dependency.setXPosn_1(rec2.x);
            dependency.setXPosn_2(rec1.x + rec1.width +
                    ((rec2.x - (rec1.x + rec1.width)) / 2));
            dependency.setXPosn_3(rec1.x + rec1.width);
            dependency.setYPosn_3(rec1.y);
            
        }
        
    }
    
    private void drawLines(Graphics2D g, 
                           ErdTableDependency dependency, 
                           int xOffset,
                           int yOffset) {
        
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
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_1, yPosn_2);
            g.drawLine(xPosn_1, yPosn_2, xPosn_3, yPosn_2);
            g.drawLine(xPosn_3, yPosn_2, xPosn_3, yPosn_3);
            
            int[] polyXs = {xPosn_3 + 5, xPosn_3, xPosn_3 - 5};
            int[] polyYs = {yPosn_3 + 10, yPosn_3, yPosn_3 + 10};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
        else if (dependency.getPosition() == ErdTableDependency.POSITION_2) {
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_1, yPosn_2);
            g.drawLine(xPosn_1, yPosn_2, xPosn_3, yPosn_2);
            g.drawLine(xPosn_3, yPosn_2, xPosn_3, yPosn_3);
            
            int[] polyXs = {xPosn_3 - 5, xPosn_3, xPosn_3 + 5};
            int[] polyYs = {yPosn_3 - 10, yPosn_3, yPosn_3 - 10};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
        else if (dependency.getPosition() == ErdTableDependency.POSITION_3) {
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_2);
            
            int[] polyXs = {xPosn_2 + 5, xPosn_2, xPosn_2 - 5};
            int[] polyYs = {yPosn_2 + 10, yPosn_2, yPosn_2 + 10};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
        else if (dependency.getPosition() == ErdTableDependency.POSITION_4) {
            
            Rectangle rec1 = dependency.getTable_1().getBounds();
            Rectangle rec2 = dependency.getTable_2().getBounds();
            
            int[] polyXs = new int[3];
            
            if (rec1.x > rec2.x) {
                polyXs[0] = xPosn_2 + 10;
                polyXs[2] = xPosn_2 + 10;
            } else {
                polyXs[0] = xPosn_2 - 10;
                polyXs[2] = xPosn_2 - 10;
            }
            
            polyXs[1] = xPosn_2;
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_1, yPosn_2);
            g.drawLine(xPosn_1, yPosn_2, xPosn_2, yPosn_2);
            
            int[] polyYs = {yPosn_2 - 5, yPosn_2, yPosn_2 + 5};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
        else if (dependency.getPosition() == ErdTableDependency.POSITION_5) {
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_2);
            g.drawLine(xPosn_2, yPosn_2, xPosn_3, yPosn_2);
            
            int[] polyXs = {xPosn_1 + 10, xPosn_1, xPosn_1 + 10};
            int[] polyYs = {yPosn_1 - 5, yPosn_1, yPosn_1 + 5};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
        else if (dependency.getPosition() == ErdTableDependency.POSITION_6) {
            
            g.drawLine(xPosn_1, yPosn_1, xPosn_2, yPosn_1);
            g.drawLine(xPosn_2, yPosn_1, xPosn_2, yPosn_2);
            g.drawLine(xPosn_2, yPosn_2, xPosn_3, yPosn_2);
            
            int[] polyXs = {xPosn_1 - 10, xPosn_1, xPosn_1 - 10};
            int[] polyYs = {yPosn_1 - 5, yPosn_1, yPosn_1 + 5};
            
            if (isDashed)
                g.setStroke(solidStroke);
            
            if (filledArrow)
                g.fillPolygon(polyXs, polyYs, 3);
            else
                g.drawPolyline(polyXs, polyYs, 3);
            
        }
        
    }
    
}















