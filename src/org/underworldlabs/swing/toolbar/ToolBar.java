/*
 * ToolBar.java
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

package org.underworldlabs.swing.toolbar;

import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ToolBar extends AbstractToolBarPanel
        implements MouseListener,
        MouseMotionListener {

    /**
     * The tool bar separator width
     */
    public static final int SEPARATOR_WIDTH = 4;

    // ---------------------------------------------
    // --- Bound properties for tool bar changes ---
    // ---------------------------------------------

    /**
     * Selected property
     */
    public static final String TOOL_BAR_BEGIN_MOVING = "toolBarBeginMoving";
    /**
     * Moving property
     */
    public static final String TOOL_BAR_MOVING = "toolBarMoving";
    /**
     * Moved property
     */
    public static final String TOOL_BAR_MOVED = "toolBarMoved";
    /**
     * Selected property
     */
    public static final String TOOL_BAR_SELECTED = "toolBarSelected";
    /**
     * Deselected property
     */
    public static final String TOOL_BAR_DESELECTED = "toolBarDeselected";
    /**
     * Resize property
     */
    public static final String TOOL_BAR_RESIZING = "toolBarResizing";

    /**
     * The tool bar's border
     */
    protected static Border toolBarBorder;

    /**
     * The top parent container
     */
    protected ToolBarBase parent;

    /**
     * The button panel
     */
    protected JPanel buttonPanel;

    /**
     * This tool bar's name
     */
    protected String name;

    /**
     * Button group for toggle buttons
     */
    protected ButtonGroup buttonGroup;

    /**
     * Component constraints
     */
    protected GridBagConstraints gbc;

    /**
     * The tool buttons added
     */
    protected List toolButtons;

    /**
     * Dummy object for separator
     */
    private static final Object SEPARATOR = new Object();

    // --------------------------------
    // --- Tracking mouse movements ---
    // --------------------------------

    /**
     * The initial X position
     */
    private int m_XDifference;

    /**
     * The initial Y position
     */
    private int m_YDifference;

    /**
     * Whether the tool bar is being dragged
     */
    private boolean dragging;

    /**
     * The tool bar width + the x position
     */
    private int rightX;

    /**
     * Whether the tool bar is being resized
     */
    private boolean resizing;

    /**
     * The mouse region where resizing will begin
     */
    private static final int resizeRegionX = 2;

    public ToolBar(ToolBarBase parent) {
        super(new BorderLayout());
        this.parent = parent;

        toolButtons = new ArrayList();

        ToolBarSelectionWidget moveWidget = new ToolBarSelectionWidget();
        moveWidget.addMouseListener(this);
        moveWidget.addMouseMotionListener(this);

        buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);

        add(moveWidget, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.CENTER);

        if (toolBarBorder == null) {
            toolBarBorder = BorderFactory.createEtchedBorder();
        }

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = 2;
        gbc.insets.top = 1;
        gbc.insets.bottom = 1;

        setBorder(toolBarBorder);
    }

    public ToolBar(ToolBarBase parent, String name) {
        this(parent);
        this.name = name;
    }

    public void enableButtonsSelection(boolean enable) {
        for (int i = 0, k = toolButtons.size(); i < k; i++) {
            Object obj = toolButtons.get(i);
            if (obj instanceof RolloverButton) {
                ((RolloverButton) obj).enableSelectionRollover(enable);
            }
        }
    }

    public void addSeparator() {
        toolButtons.add(SEPARATOR);
    }

    public void addToggleButton(JToggleButton button) {
        if (buttonGroup == null) {
            buttonGroup = new ButtonGroup();
        }

        buttonGroup.add(button);
        gbc.gridx++;
        buttonPanel.add(button, gbc);
        button.setSelected(true);
    }

    public void removeToggleButton(JToggleButton button) {
        buttonGroup.remove(button);
        buttonPanel.remove(button);
        repaint();
    }

    public void addButton(JComponent button) {
        toolButtons.add(button);
    }

    public void buildToolBar() {
        Object obj = null;
        int buttonCount = toolButtons.size();
        gbc.weightx = 0;

        for (int i = 0; i < buttonCount; i++) {
            gbc.gridx++;

            if (i + 1 == buttonCount - 1) {
                obj = toolButtons.get(i + 1);

                if (obj == SEPARATOR) {
                    gbc.weightx = 1.0;
                }

            }

            obj = toolButtons.get(i);

            if (obj == SEPARATOR) {
                JSeparator sep = new JSeparator();
                sep.setOrientation(JSeparator.VERTICAL);
                sep.setMinimumSize(new Dimension(SEPARATOR_WIDTH, 16));
                sep.setPreferredSize(new Dimension(SEPARATOR_WIDTH, 16));
                sep.setMaximumSize(new Dimension(SEPARATOR_WIDTH, 16));
                obj = sep;
            } else if (obj instanceof JComboBox) {
                gbc.insets.top = 1;
            }

            if (i == buttonCount - 1) {
                gbc.weightx = 1.0;
            }

            buttonPanel.add((JComponent) obj, gbc);
            gbc.insets.left = 0;
            gbc.insets.top = 1;
        }

    }

    public void removeAllButtons() {
        int size = toolButtons.size();

        for (int i = 0; i < size; i++) {

            Object obj = toolButtons.get(i);
            if (obj instanceof Component) {
                buttonPanel.remove((Component) obj);
            }

        }

        toolButtons.clear();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void mouseDragged(MouseEvent e) {
        int ex = e.getX();
        int ey = e.getY();
        int x = getX();
        int y = getY();
        int w = getParent().getWidth();
        int h = getParent().getHeight();

        int locX = -1;
        int locY = -1;

        if (resizing) {

            if (getWidth() - ex - m_XDifference >= resizeRegionX) {
                locX = getX() + ex - m_XDifference;
                setBounds(locX, getY(), getWidth() - ex - m_XDifference, getHeight());
            } else {

                if (ex + resizeRegionX < rightX) {
                    locX = rightX - resizeRegionX;
                    setBounds(locX, getY(), resizeRegionX, getHeight());
                } else {
                    locX = ex;
                    setBounds(locX, getY(), resizeRegionX, getHeight());
                }

            }

            validate();
            firePropertyChange(TOOL_BAR_RESIZING, x, locX);
        } else if (dragging) {

            if ((ey + y > 0 && ey + y < h) && (ex + x > 0 && ex + x < w)) {
                locX = ex - m_XDifference + x;
                locY = ey - m_YDifference + y;
            } else if (!(ey + y > 0 && ey + y < h) && (ex + x > 0 && ex + x < w)) {

                if (!(ey + y > 0) && ey + y < h) {
                    locX = ex - m_XDifference + x;
                    locY = 0 - m_YDifference;
                } else if (ey + y > 0 && !(ey + y < h)) {
                    locX = ex - m_XDifference + x;
                    locY = h - m_YDifference;
                }

            } else if ((ey + y > 0 && ey + y < h) && !(ex + x > 0 && ex + x < w)) {

                if (!(ex + x > 0) && ex + x < w) {
                    locX = 0 - m_XDifference;
                    locY = ey - m_YDifference + y;
                } else if (ex + x > 0 && !(ex + x < w)) {
                    locX = w - m_XDifference;
                    locY = ey - m_YDifference + y;
                }

            } else if (!(ey + y > 0) && ey + y < h && !(ex + x > 0) && ex + x < w) {
                locX = 0 - m_XDifference;
                locY = 0 - m_YDifference;
            } else if (!(ey + y > 0) && ey + y < h && ex + x > 0 && !(ex + x < w)) {
                locX = w - m_XDifference;
                locY = 0 - m_YDifference;
            } else if (ey + y > 0 && !(ey + y < h) && !(ex + x > 0) && ex + x < w) {
                locX = 0 - m_XDifference;
                locY = h - m_YDifference;
            } else if (ey + y > 0 && !(ey + y < h) && ex + x > 0 && !(ex + x < w)) {
                locX = w - m_XDifference;
                locY = h - m_YDifference;
            }

            if (locX < 0) {
                locX = 0;
            }

            int parentHeight = parent.getHeight();
            //int maxY = parentHeight - (ToolBarLayout.getRowHeight() / 2);
            int maxY = parentHeight - (getHeight() / 2);

            if (locY < 0) {
                locY = 0;
            } else if (locY > maxY) {
                firePropertyChange(TOOL_BAR_MOVING, m_YDifference, locY);
                parentHeight = parent.getHeight();

                if (locY > parentHeight) {
                    locY = parentHeight;
                }

            }

            setLocation(locX, locY);

        }

    }

    public void mousePressed(MouseEvent e) {
        int xPos = getX();
        rightX = xPos + getWidth();
        m_XDifference = e.getX();
        m_YDifference = e.getY();

        if (m_XDifference <= resizeRegionX && xPos != 0) {
            resizing = true;
        } else {
            firePropertyChange(TOOL_BAR_BEGIN_MOVING, 0, 1);
            dragging = true;
        }

    }

    public void mouseReleased(MouseEvent e) {
        if (dragging) {
            firePropertyChange(TOOL_BAR_MOVED, 0, 1);
        }

        firePropertyChange(TOOL_BAR_DESELECTED, 0, 1);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        dragging = false;
        resizing = false;
    }

    public void mouseExited(MouseEvent e) {
        if (!resizing) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (e.getX() > resizeRegionX && !resizing) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (!dragging && !resizing && e.getX() <= resizeRegionX && getX() != 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public String toString() {
        return "Tool Bar: " + name;
    }


    private static Color middleground;
    private static Color background;
    private static Color foreground;

    /**
     * <p>Small widget placed on the left edge of the tool bar.
     */
    class ToolBarSelectionWidget extends JPanel {

        /**
         * fixed width - 9px
         */
        protected static final int DEFAULT_WIDTH = 9;

        /**
         * indicates whether the Java L&F (or a derivative) is used
         */
        private boolean isJavaLookAndFeel;

        private boolean isMac;

        public ToolBarSelectionWidget() {

            isMac = UIUtils.isMac();
            isJavaLookAndFeel = GUIUtils.isMetalLookAndFeel();

            if (background == null) {
                background = UIManager.getDefaults().getColor("controlDkShadow");
            }

            if (foreground == null) {
                foreground = UIManager.getDefaults().getColor("controlLtHighlight");
            }

        }

        public int getWidth() {
            return isJavaLookAndFeel ? DEFAULT_WIDTH : super.getWidth();
        }

        public boolean isOpaque() {
            return false;
        }

        public void paintComponent(Graphics g) {
            int height = getHeight();
            if (!isJavaLookAndFeel) {

                if (isMac) {

                    background = UIManager.getDefaults().getColor("controlShadow");
                }

                if (middleground == null) {

                    middleground = UIManager.getDefaults().getColor("control");
                }

                int start = (height - 13) / 2;

                start = 7;
                int x = 3;
                int heightOffset = 8;

                g.setColor(background);
                g.drawLine(x, start, x, height - heightOffset);

                x++;
                heightOffset--;
                g.setColor(middleground);
                g.drawLine(x, start + 1, x, height - heightOffset);

                x++;
                heightOffset--;
                g.setColor(foreground);
                g.drawLine(x, start + 1, x, height - heightOffset);


                x++;
                heightOffset = 8;
                g.setColor(background);
                g.drawLine(x, start, x, height - heightOffset);

                x++;
                heightOffset--;
                g.setColor(middleground);
                g.drawLine(x, start + 1, x, height - heightOffset);

                x++;
                heightOffset--;
                g.setColor(foreground);
                g.drawLine(x, start + 1, x, height - heightOffset);
                
                /*
                for (int i = 0; i < 3; i++) {

                    g.setColor(background);
                    g.drawLine(2, start, 3, start);
                    g.drawLine(2, start, 2, start + 2);

                    g.setColor(middleground);
                    g.drawLine(4, start, 4, start);
                    g.drawLine(3, start + 1, 3, start + 1);
                    g.drawLine(2, start + 2, 2, start + 2);

                    g.setColor(foreground);
                    g.drawLine(3, start + 2, 4, start + 2);
                    g.drawLine(4, start + 1, 4, start + 1);
                    start += 6;
                }
                */

            } else {
                Shape clip = g.getClip();
                g.setClip(1, 1, getWidth() - 3, height - 3);

                g.setColor(foreground);
                for (int x = 1; x <= height; x += 4) {

                    for (int y = 1; y <= height; y += 4) {
                        g.drawLine(x, y, x, y);
                        g.drawLine(x + 2, y + 2, x + 2, y + 2);
                    }

                }

                g.setColor(background);
                for (int x = 1; x <= height; x += 4) {

                    for (int y = 1; y <= height; y += 4) {
                        g.drawLine(x + 1, y + 1, x + 1, y + 1);
                        g.drawLine(x + 3, y + 3, x + 3, y + 3);
                    }

                }
                g.setClip(clip);
            }

        }

    } // ToolBarSelectionWidget

}







