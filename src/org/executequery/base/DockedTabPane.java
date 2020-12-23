/*
 * DockedTabPane.java
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

package org.executequery.base;

import org.executequery.gui.GUIConstants;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * Left, right and bottom docked tab pane.
 *
 * @author Takis Diakoumis
 */
public class DockedTabPane extends AbstractTabPane {

    /**
     * the currently dragging index
     */
    private int draggingIndex;

    /**
     * the tab panel
     */
    private TabPanel tabPanel;

    /**
     * the tab popup menu
     */
    private TabPopupMenu tabPopupMenu;

    private boolean printTitle = true;


    /**
     * Creates a new instance of DockedTabPane with
     * the specified parent container
     *
     * @param the enclosing tab container
     */
    public DockedTabPane(DockedTabContainer parent, boolean printTitle) {
        this.printTitle = printTitle;
        setLayout(new BorderLayout());
        this.parent = parent;
        init();
    }

    /**
     * Initialises the state of this object
     */
    private void init() {
        super.initComponents();
        // panel where actual tabs are drawn
        tabPanel = new TabPanel(printTitle);
        setTabPanel(tabPanel);
    }

    /**
     * Indicates a top-level focus change.
     */
    protected void focusChanged() {
        if (tabPanel != null)
            tabPanel.repaint();
    }

    /**
     * Adds the specified tab component to the pane.
     *
     * @param the component to be added
     */
    public void addTab(TabComponent tabComponent) {

        components.add(tabComponent);

        Component component = tabComponent.getComponent();
        String layoutName = tabComponent.getLayoutName();

        componentPanel.add(component, layoutName);
        cardLayout.addLayoutComponent(component, layoutName);

        // inform the tab of its position
        tabComponent.setIndex(components.indexOf(tabComponent));
        tabComponent.setPosition(parent.getTabPanePosition(this));

        if (components.size() == 1) {
            setSelectedTab(tabComponent);
        }
    }

    protected void calculateTabRects(int tabCount) {

        // check that we still have the right count
        if (tabRects.length != tabCount) {
            tabRects = new Rectangle[tabCount];
        }

        int tabWidth = getTabWidth(tabCount);
        for (int i = 0; i < tabCount; i++) {
            Rectangle rect = null;
            if (tabRects[i] == null) {
                rect = new Rectangle();
                tabRects[i] = rect;
            } else {
                rect = tabRects[i];
            }

            if (i > 0) {
                rect.x = tabRects[i - 1].x + tabRects[i - 1].width;
            } else {
                rect.x = 0;
            }
            rect.y = 0;
            rect.width = tabWidth;
            rect.height = tabPanel.getTabHeight();
        }

    }

    /**
     * Returns whether the specified x-y coordinates
     * intersect the tab area (tab panel).
     *
     * @param the x coordinate
     * @param the x coordinate
     * @return true if the point intersects, false otherwise
     */
    protected boolean intersectsTabArea(int x, int y) {
        return tabPanel.getBounds().contains(x, y);
    }

    /**
     * Returns the index of the tab currently being dragged.
     *
     * @return the tab index dragged
     */
    protected int getDraggingIndex() {
        return draggingIndex;
    }

    private int getTabWidth(int tabCount) {
        return (getWidth() / tabCount);
    }

    /**
     * Calculates the close icon rectangle for the specified
     * tab bounds.
     *
     * @param the tab bounds
     * @return the close icon bounds
     */
    private Rectangle getCloseIconRectangle(Rectangle tabRect) {
        int y = tabRect.y + ((tabRect.height - TabControlIcon.ICON_HEIGHT) / 2);
        int x = tabRect.x + ((int) (tabRect.width - TabControlIcon.ICON_WIDTH - 6));
        return new Rectangle(x, y, TabControlIcon.ICON_WIDTH, TabControlIcon.ICON_HEIGHT);
    }

    /**
     * Calculates the minimise icon rectangle for the specified
     * tab bounds.
     *
     * @param the tab bounds
     * @return the close icon bounds
     */
    private Rectangle getMinimizeIconRectangle(Rectangle tabRect) {
        int y = tabRect.y + ((tabRect.height - TabControlIcon.ICON_HEIGHT) / 2);
        int x = tabRect.x + ((int) (tabRect.width - (TabControlIcon.ICON_WIDTH * 2) - 10));
        return new Rectangle(x, y, TabControlIcon.ICON_WIDTH, TabControlIcon.ICON_HEIGHT);
    }

    protected Rectangle getTabRectangleAtLocation(int x, int y) {
        int index = getTabAtLocation(x, y);
        if (index == -1) {
            return null;
        }
        return tabRects[index];
    }

    /**
     * Returns the index in the tab pane of the specified
     * tab rectangle. if the specified tab rectangle is
     * not in the tab pane, -1 is returned.
     *
     * @param the tab rectangle
     * @return the index of the tab rectangle or -1 if its
     * not present
     */
    protected int getTabRectangleIndex(Rectangle tabRect) {
        for (int i = 0; i < tabRects.length; i++) {
            if (tabRects[i] == tabRect) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the tab component object at the
     * specified x and y coordinate.
     *
     * @param x coordinate
     * @param y coordinate
     * @return the component index at the x-y coords
     */
    protected int getTabAtLocation(int x, int y) {
        for (int i = 0; i < tabRects.length; i++) {
            if (tabRects[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectedIndex(int index) {
        super.setSelectedIndex(index);
        tabPanel.repaint();
        fireTabSelected(new DockedTabEvent(components.get(index)));
    }

    protected void insertTab(TabComponent tabComponent, int toIndex) {
        if (tabComponent == null || toIndex == -1) {
            return;
        }
        components.add(toIndex, tabComponent);
        componentPanel.add(tabComponent.getComponent(), tabComponent.getTitle());

        // reset the layout
        cardLayout.invalidateLayout(componentPanel);
        int tabCount = components.size();
        for (int i = 0; i < tabCount; i++) {
            TabComponent _tabComponent = components.get(i);
            cardLayout.addLayoutComponent(_tabComponent.getComponent(),
                    _tabComponent.getLayoutName());
        }

        // inform the tab of its position
        tabComponent.setIndex(toIndex);
        tabComponent.setPosition(parent.getTabPanePosition(this));

        setSelectedIndex(toIndex);
    }

    protected void moveTab(int fromIndex, int toIndex) {
        if (fromIndex == toIndex || (fromIndex == -1 || toIndex == -1)) {
            return;
        }

        TabComponent tabComponent = components.get(fromIndex);
        tabComponent.setIndex(toIndex);

        // remove from the component cache
        components.remove(fromIndex);
        components.add(toIndex, tabComponent);
        setSelectedIndex(toIndex);
    }

    /**
     * Returns the height of the tab itself ie. the selection part with
     * the title etc.
     *
     * @return the tab height
     */
    public int getTabHeight() {
        return tabPanel.getHeight();
    }

    /**
     * Minimises all the tabs in the panel
     */
    protected void minimiseAll() {
        for (int i = 0, k = components.size(); i < k; i++) {
            TabComponent tabComponent = components.get(i);
            parent.minimiseComponent(tabComponent);
            fireTabMinimised(new DockedTabEvent(tabComponent));
        }
        removeAllTabs();
    }

    /**
     * Minimises the tab from the panel at the specified index.
     *
     * @param the index to be removed
     */
    protected void minimiseIndex(int index) {
        if (index < 0) {
            return;
        }

        // retrieve the component from the cache
        TabComponent tabComponent = components.get(index);

        // remove from the tab display and minimise
        parent.minimiseComponent(tabComponent);
        removeIndex(index);

        // fire the event
        fireTabMinimised(new DockedTabEvent(tabComponent));
    }

    /**
     * Removes all tab components from this panel
     */
    public void removeAllTabs() {
        cardLayout.invalidateLayout(componentPanel);
        // fire the close event
        for (int i = 0, k = components.size(); i < k; i++) {
            TabComponent tabComponent = components.get(i);
            if (okToClose(tabComponent)) {
                fireTabClosed(new DockedTabEvent(tabComponent));
            } else {
                return;
            }
        }
        allTabsRemoved();
    }

    /**
     * Cleanup method following removal of all tabs.
     */
    private void allTabsRemoved() {
        selectedIndex = -1;
        components.clear();
        components = null;
        cardLayout = null;
        componentPanel = null;
        tabPanel = null;
        parent.removeTabPane(this);
    }

    /**
     * Removes the tab from the panel at the specified index.
     *
     * @param the index to be removed
     */
    public void removeIndex(int index) {
        if (index < 0) {
            return;
        }
        TabComponent removed = components.get(index);
        if (!okToClose(removed)) {
            return;
        }

        // remove from the component cache
        components.remove(index);

        // reset the layout
        cardLayout.invalidateLayout(componentPanel);
        componentPanel.removeAll();

        int tabCount = components.size();
        if (tabCount == 0) {
            allTabsRemoved();
            fireTabClosed(new DockedTabEvent(removed));
            return;
        }

        String layoutName = null;
        for (int i = 0; i < tabCount; i++) {
            TabComponent tabComponent = components.get(i);
            layoutName = tabComponent.getLayoutName();
            componentPanel.add(tabComponent.getComponent(), layoutName);
            cardLayout.addLayoutComponent(tabComponent.getComponent(),
                    layoutName);
        }

        // check if the last panel was removed
        if (index == tabCount) {
            // reset the index
            index--;
        }

        selectedIndex = index;
        TabComponent tabComponent = components.get(index);
        cardLayout.show(componentPanel, tabComponent.getLayoutName());
        tabPanel.repaint();

        // fire the event
        fireTabClosed(new DockedTabEvent(removed));
    }

    protected int getTabPanePosition() {
        return parent.getTabPanePosition(this);
    }

    private Rectangle[] tabRects;

    private class TabPanel extends JPanel {

        protected Font font;

        protected Color foreground;
        protected Color background;
        protected Color activeColor;
        //        protected Color activeNoFocusColor;
        protected Color controlShadow;

        protected int textIconGap;

        protected Insets tabInsets;
        private int height;

        private boolean printTitle;

        protected static final int TEXT_CROP_OFFSET = 10;

        protected static final int TAB_BOTTOM_BORDER_HEIGHT = 3;

        protected TabPanel(boolean printTitle) {
            this.printTitle = printTitle;
            initDefaults();
            tabRects = new Rectangle[0];
            MouseHandler mouseHandler = new MouseHandler();
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        public String getToolTipText(MouseEvent e) {
            // check if we are over a button
            if (currentCloseRolloverIndex != -1 ||
                    currentMinimizeRolloverIndex != -1) {
                return null;
            }

            int x = e.getX();
            int y = e.getY();
            int index = getTabAtLocation(x, y);
            if (index == -1) {
                return null;
            }

            TabComponent tabComponent = components.get(index);
            return tabComponent.getToolTip();
        }

        public int getHeight() {
            if (height == 0) {
                calculateTabHeight();
            }
            return height;
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        protected void calculateTabHeight() {
            if (!printTitle)
                height = 0;
            else {
                FontMetrics metrics = getFontMetrics(font);
                height = metrics.getHeight() + tabInsets.top +
                        tabInsets.bottom + TAB_BOTTOM_BORDER_HEIGHT + 6;
            }
        }

        /*
        protected Rectangle getTabRectangleAt(int x, int y) {
            return null;
        }
        */

        private int getTabHeight() {
            return height - TAB_BOTTOM_BORDER_HEIGHT;
        }

        public void paintComponent(Graphics g) {
            if (!printTitle)
                return;
            if (components == null) {
                return;
            }

            int tabCount = components.size();
            if (tabCount == 0) {
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            Object antialiasHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            UIUtils.antialias(g2d);

            calculateTabHeight();
            calculateTabRects(tabCount);

            int x = getX();
            int y = getY();

            int w = getWidth();
            int h = getHeight();

            // fill the background
            g.setColor(background);
            g.fillRect(x, y, w, h);

            // fill the selected background
            if (selectedIndex != -1) {
                if (isFocusedTabPane) {
                    g.setColor(activeColor);
                    Rectangle selected = tabRects[selectedIndex];
                    g.fillRect(selected.x, selected.y,
                            selected.width + 1, selected.height);

                    // fill the bottom border
                    g.fillRect(x, h - TAB_BOTTOM_BORDER_HEIGHT,
                            w, TAB_BOTTOM_BORDER_HEIGHT);
                }

            }

            // draw the borders
            g.setColor(controlShadow);

            // left and right absolute borders
            g.drawLine(x, y, x, y + h); // left-most
            g.drawLine(x + w - 1, y, x + w - 1, y + h); // right-most

            for (int i = 0; i < tabCount; i++) {
                Rectangle rect = tabRects[i];
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;

                g.drawLine(x, y, x, y + h - 2); // left side
                g.drawLine(x + 1, y, x + w + 2, y); // top side

                /*
                if (i < tabCount - 1) {
                    g.drawLine(x+w-1, y, x+w-1, y+h-2); // right side
                }
                */

                // bottom side
                if (i != selectedIndex) {
                    g.drawLine(x, y + h - 1, x + w, y + h - 1);
                } else {
                    g.drawLine(0, y + h + TAB_BOTTOM_BORDER_HEIGHT - 1,
                            getWidth(), y + h + TAB_BOTTOM_BORDER_HEIGHT - 1);
                }

            }

            // draw the text
            Rectangle iconRect = new Rectangle();
            Rectangle textRect = new Rectangle();

            g.setFont(font);
            FontMetrics metrics = getFontMetrics(font);

            for (int i = 0; i < tabCount; i++) {
                Rectangle tabRect = new Rectangle(tabRects[i]);
                calculateTextRect(tabRect, textRect, i);
                TabComponent tabComponent = components.get(i);

                // if tab selected make crop smaller to
                // account for min and close buttons
                if (i == selectedIndex) {
                    // paint the close button
                    Rectangle buttonRect = getCloseIconRectangle(tabRect);
                    closeIcon.paintIcon(this, g, buttonRect.x, buttonRect.y);

                    if (selectedIndex == currentCloseRolloverIndex) {
                        g.setColor(TabControlIcon.ICON_COLOR);
                        g.drawRect(buttonRect.x - 2,
                                buttonRect.y - 2,
                                buttonRect.width + 3,
                                buttonRect.height + 3);
                    }

                    // paint the minimise button
                    buttonRect = getMinimizeIconRectangle(tabRect);
                    minimizeIcon.paintIcon(this, g, buttonRect.x, buttonRect.y);

                    if (selectedIndex == currentMinimizeRolloverIndex) {
                        g.setColor(TabControlIcon.ICON_COLOR);
                        g.drawRect(buttonRect.x - 2,
                                buttonRect.y - 2,
                                buttonRect.width + 3,
                                buttonRect.height + 3);
                    }

                    // smaller text crop area
                    tabRect.width -= (TabControlIcon.ICON_WIDTH * 2) + 4;
                }

                // text crop offset
                tabRect.width -= TEXT_CROP_OFFSET;

                textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

                String title = SwingUtilities.layoutCompoundLabel(
                        this,
                        metrics,
                        tabComponent.getDisplayName(),
                        tabComponent.getIcon(),
                        SwingUtilities.CENTER,
                        SwingUtilities.LEFT,
                        SwingUtilities.CENTER,
                        SwingUtilities.TRAILING,
                        tabRect,
                        iconRect,
                        textRect,
                        textIconGap);

                g.setColor(foreground);
                if (i != selectedIndex || !isFocusedTabPane) {
                    g.setColor(UIManager.getColor("TabbedPane.foreground"));
                }

                g.drawString(title, textRect.x + 3, textRect.y + textRect.height - 3);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasHint);
            }
        }

        private Rectangle calculateTextRect(Rectangle tabRect,
                                            Rectangle textRect,
                                            int index) {

            textRect.x = tabRect.x + tabInsets.left;
            textRect.y = tabRect.y + tabInsets.top + 2;

            if (index == 0) {
                textRect.height = tabRect.height - tabInsets.top - tabInsets.bottom;
            }

            textRect.width = tabRect.width - tabInsets.left - tabInsets.right;

            if (selectedIndex == index) {
                Rectangle closeIconRect = getCloseIconRectangle(tabRect);
                Rectangle minIconRect = getMinimizeIconRectangle(tabRect);
                textRect.width = tabRect.width -
                        tabInsets.left - tabInsets.right -
                        closeIconRect.width - minIconRect.width;
            }

            return textRect;
        }

        // the close and minimise icon
        private transient Icon closeIcon = new DockedTabCloseIcon();
        private transient Icon minimizeIcon = new DockedTabMinimizeIcon(parent.getOrientation());

        private void initDefaults() {
            Font _font = DockedTabPane.this.getFont();
            if (_font != null) {
                font = _font.deriveFont(Font.PLAIN, GUIConstants.DEFAULT_FONT_SIZE);
            } else {
                font = UIManager.getFont("TabbedPane.font").deriveFont(Font.PLAIN, GUIConstants.DEFAULT_FONT_SIZE);
            }

            background = getTabBackground();
//            activeNoFocusColor = getNofocusTabBackground();

            foreground = getTabForeground();
            activeColor = getSelectedTabBackground();

            /*
            background = DockedTabPane.this.getBackground();
            //activeNoFocusColor = background.brighter();

            //foreground = UIManager.getColor("InternalFrame.activeTitleForeground");
            //activeColor = UIManager.getColor("InternalFrame.activeTitleBackground");
            foreground = UIUtils.getDefaultActiveTextColour();
                    //UIManager.getColor("InternalFrame.activeTitleForeground");
            activeColor = UIUtils.getDefaultActiveBackgroundColour();
            */

//            controlShadow = UIManager.getColor("controlShadow");
            Color color = UIManager.getColor("executequery.TabbedPane.border");
            if (color != null) {

                controlShadow = color;

            } else {

                controlShadow = UIManager.getColor("controlShadow");
            }

            textIconGap = 2;
            tabInsets = tabInsets();
        }

    } // TabPanel

    /**
     * The tab's tool tip
     */
    protected DockedTabToolTip toolTip;

    /**
     * Indicates the current rollover index for the min button
     */
    protected int currentMinimizeRolloverIndex = -1;

    /**
     * Indicates the current rollover index for the close button
     */
    protected int currentCloseRolloverIndex = -1;

    private class MouseHandler implements MouseInputListener {

        private boolean dragging;

        public void mouseMoved(MouseEvent e) {
            boolean doRepaint = false;

            try {
                if (currentCloseRolloverIndex != -1 || currentMinimizeRolloverIndex != -1) {
                    doRepaint = true;
                    currentCloseRolloverIndex = -1;
                    currentMinimizeRolloverIndex = -1;
                }

                int x = e.getX();
                int y = e.getY();
                int index = -1;

                for (int i = 0, k = components.size(); i < k; i++) {
                    Rectangle tabRect = tabRects[i];
                    if (tabRect.contains(x, y)) {
                        index = i;
                        Rectangle iconRect = getCloseIconRectangle(tabRect);
                        if (iconRect.contains(x, y)) {
                            currentCloseRolloverIndex = i;
                            doRepaint = true;
                            return;
                        }
                        iconRect = getMinimizeIconRectangle(tabRect);
                        if (iconRect.contains(x, y)) {
                            currentMinimizeRolloverIndex = i;
                            doRepaint = true;
                            return;
                        }
                        break;
                    }
                }

                // --------------------------------------------
                // tool tip display
                // --------------------------------------------

                if (index == -1) {
                    if (toolTip != null && toolTip.isVisible()) {
                        toolTip.setVisible(false);
                    }
                    return;
                }

                TabComponent tabComponent = components.get(index);
                if (tabComponent.hasToolTipText()) {
                    if (toolTip == null) {
                        toolTip = new DockedTabToolTip();
                        ToolTipManager.sharedInstance().registerComponent(tabPanel);
                    }
                    toolTip.setVisible(true);
                }

                // --------------------------------------------
            } finally {
                if (doRepaint) {
                    tabPanel.repaint();
                }
            }

        }

        private boolean maybeShowPopup(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();

            int index = getTabAtLocation(x, y);
            if (index == -1) {
                return false;
            }

            if (e.isPopupTrigger()) {

                if (tabPopupMenu == null) {
                    tabPopupMenu = new TabPopupMenu();
                }
                tabPopupMenu.showPopup(index, x, y);
                return true;
            }

            return false;
        }

        public void mouseDragged(MouseEvent e) {
            if (!dragging) {
                dragging = true;
                draggingIndex = getTabAtLocation(e.getX(), e.getY());
                if (draggingIndex == -1) {
                    return;
                }
            }
            parent.dockedTabDragged(new DockedDragEvent(
                    DockedTabPane.this, e, getTabComponentAt(draggingIndex)));
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            if (currentCloseRolloverIndex != -1 || currentMinimizeRolloverIndex != -1) {
                currentCloseRolloverIndex = -1;
                currentMinimizeRolloverIndex = -1;
                tabPanel.repaint();
            }
            if (toolTip != null && toolTip.isVisible()) {
                toolTip.setVisible(false);
            }
        }

        public void mouseClicked(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (maybeShowPopup(e) ||
                    (tabPopupMenu != null && tabPopupMenu.isVisible())) {
                return;
            }

            try {
                int x = e.getX();
                int y = e.getY();

                int index = getTabAtLocation(x, y);

                if (!dragging) {

                    // if nothing bail
                    if (index < 0) {
                        return;
                    }

                    // if the index is not the current index, select the tab
                    if (index != selectedIndex) {
                        setSelectedIndex(index);
                        return;
                    }

                    // check if a close button was pushed on the selected index
                    Rectangle tabRect = tabRects[index];
                    Rectangle iconRect = getCloseIconRectangle(tabRect);
                    if (iconRect.contains(x, y)) {
                        removeIndex(index);
                        return;
                    }

                    // check if a minimise button was pushed on the selected index
                    iconRect = getMinimizeIconRectangle(tabRect);
                    if (iconRect.contains(x, y)) {
                        minimiseIndex(index);
                        return;
                    }

                } else {
                    parent.dockedTabReleased(new DockedDragEvent(
                            DockedTabPane.this, e, getTabComponentAt(draggingIndex)));
                }
                dragging = false;
                draggingIndex = -1;
            } finally {
                currentCloseRolloverIndex = -1;
                currentMinimizeRolloverIndex = -1;
            }
        }

    }

    /**
     * Popup menu for tab components accessible through
     * mouse right-click action.
     */
    private class TabPopupMenu extends JPopupMenu
            implements ActionListener {

        private int popupTabIndex;
        private JMenuItem minimise;
        private JMenuItem close;
        private JMenuItem minimiseAll;

        public TabPopupMenu() {
            Font font = UIManager.getFont("PopupMenu.font").
                    deriveFont(Font.PLAIN, 10);
            setFont(font);

            close = MenuItemFactory.createMenuItem(bundledString("menuItem.close"));
            minimise = MenuItemFactory.createMenuItem(bundledString("menuItem.minimize"));
            minimiseAll = MenuItemFactory.createMenuItem(bundledString("menuItem.minimizeAll"));

            close.addActionListener(this);
            minimiseAll.addActionListener(this);
            minimise.addActionListener(this);

            add(minimise);
            add(minimiseAll);
            addSeparator();
            add(close);

            popupTabIndex = -1;
        }

        public void showPopup(int index, int x, int y) {
            popupTabIndex = index;
            show(tabPanel, x, y);
        }

        public void actionPerformed(ActionEvent e) {
            if (popupTabIndex == -1) {
                return;
            }

            try {
                Object source = e.getSource();
                if (source == close) {
                    removeIndex(popupTabIndex);
                } else if (source == minimiseAll) {
                    minimiseAll();
                } else if (source == minimise) {
                    minimiseIndex(popupTabIndex);
                }
            } finally {
                popupTabIndex = -1;
            }

        }

    }

}

