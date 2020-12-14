/*
 * ScrollingTabPane.java
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

import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Central tab pane with scroll and menu buttons.
 *
 * @author Takis Diakoumis
 */
public class ScrollingTabPane extends AbstractTabPane
        implements SwingConstants {

    /**
     * the tab panel
     */
    private TabPanel tabPanel;

    /**
     * the viewport for tab scrolling
     */
    private ScrollableTabViewport viewport;

    /**
     * the tab scrolling panel
     */
    private JPanel scrollingPanel;

    /**
     * the scroll button panel
     */
    private ScrollButtonPanel scrollButtonPanel;

    /**
     * the tab popup menu
     */
    private TabPopupMenu tabPopupMenu;

    /**
     * the tab selection popup menu
     */
    private TabSelectionPopupMenu tabSelectionPopupMenu;

    /**
     * Creates a new instance of DockedTabPane with
     * the specified parent container
     *
     * @param the enclosing tab container
     */
    public ScrollingTabPane(DockedTabContainer parent) {
        setLayout(new BorderLayout());
        this.parent = parent;
        isFocusedTabPane = true;
        init();
        // change the focus on all other panes so this one has focus
        parent.tabPaneFocusChange(this);
    }

    /**
     * Initialises the state of this object
     */
    private void init() {
        super.initComponents();
        // panel where actual tabs are drawn
        tabPanel = new TabPanel();

        viewport = new ScrollableTabViewport();
        viewport.setView(tabPanel);

        scrollButtonPanel = new ScrollButtonPanel();

        scrollingPanel = new JPanel(new BorderLayout());
        scrollingPanel.add(viewport, BorderLayout.CENTER);
        scrollingPanel.add(scrollButtonPanel, BorderLayout.EAST);

        setTabPanel(scrollingPanel);

        componentPanel.setBorder(null);
        add(componentPanel, BorderLayout.CENTER);

        //setBorder(BorderFactory.createLineBorder(tabPanel.controlShadow));

        viewport.movePanel(0);
    }

    /**
     * Indicates a top-level focus change.
     */
    protected void focusChanged() {
        tabPanel.repaint();
        scrollButtonPanel.repaint();
    }

    /**
     * Sets the title at index to title which can be null.
     * An internal exception is raised if there is no tab at that index.
     *
     * @param the tab index where the title should be set
     * @param the title to be displayed in the tab
     */
    public void setTabTitleAt(int index, String title) {
        super.setTabTitleAt(index, title);
        TabComponent tabComponent = components.get(index);
        if (tabComponent.getTitleSuffix() != null) {
            title += tabComponent.getTitleSuffix();
        }
        tabSelectionPopupMenu.renameTabMenuItem(index, title);
        tabPanel.repaint();
    }

    /**
     * Adds the specified tab component to the pane.
     *
     * @param the component to be added
     */
    public void addTab(TabComponent tabComponent) {
        if (components.size() == 0) {
            tabPanel.setVisible(true);
            componentPanel.setVisible(true);
        }

        components.add(tabComponent);

        // make sure the title is unique
        String suffix = getTitleSuffix(tabComponent);
        if (suffix != null) {
            tabComponent.setTitleSuffix(suffix);
        }

        Component component = tabComponent.getComponent();

        String layoutName = tabComponent.getLayoutName();
        componentPanel.add(component, layoutName);
        cardLayout.addLayoutComponent(component, layoutName);

        if (components.size() == 1) {
            setSelectedTab(tabComponent);
        }

        if (tabSelectionPopupMenu == null) {
            tabSelectionPopupMenu = new TabSelectionPopupMenu();
        }
        tabSelectionPopupMenu.addTabMenuItem(tabComponent);
        scrollButtonPanel.setVisible(true);
        scrollButtonPanel.enableButton(SOUTH, true);
        componentPanel.setBorder(BorderFactory.createLineBorder(tabPanel.controlShadow));
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
     * Calculates the close icon rectangle for the specified
     * tab bounds.
     *
     * @param the tab bounds
     * @return the close icon bounds
     */
    private Rectangle getCloseIconRectangle(Rectangle tabRect) {
        int y = tabRect.y + ((tabRect.height - TabControlIcon.ICON_HEIGHT) / 2);
        int x = tabRect.x + tabRect.width - TabControlIcon.ICON_WIDTH - 6;
        return new Rectangle(x, y, TabControlIcon.ICON_WIDTH, TabControlIcon.ICON_HEIGHT);
    }

    /**
     * Returns the tab bounds at the specified x-y coordinate
     *
     * @param the x coordinate
     * @param the x coordinate
     * @return the tab bounds
     */
    public Rectangle getBoundsAt(int x, int y) {
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

    /**
     * Returns the tab count for this component.
     *
     * @return the tab count
     */
    public int getTabCount() {
        if (components != null) {
            return components.size();
        }
        return 0;
    }

    /**
     * Sets the selected index to that specified.
     *
     * @param the index to set selected
     */
    public void setSelectedIndex(int index) {
        if (selectedIndex != -1) {
            // fire the deselected event

            // ------------
            // the selected index may however be invalid 
            // after a close others command - so check

            if (selectedIndex < components.size()) {

                TabComponent tabComponent = components.get(selectedIndex);
                if (tabComponent.getComponent() instanceof TabView) {
                    TabView dockedView = (TabView) tabComponent.getComponent();
                    if (dockedView.tabViewDeselected()) {
                        fireTabDeselected(new DockedTabEvent(tabComponent));
                    } else {
                        return;
                    }
                }

            }
        }

        selectedIndex = index;
        TabComponent tabComponent = components.get(index);
        cardLayout.show(componentPanel, tabComponent.getLayoutName());

        // make sure we can see the selected tab in full
        ensureIndexVisible(index);

        /*
        // make sure we can see the selected tab in full
        tabPanel.calculateTabRects(components.size());
        Rectangle tabRect = tabRects[index];
        Rectangle viewRect = new Rectangle(
                                    viewport.getViewPosition().x, 
                                    0, 
                                    viewport.getWidth(),
                                    viewport.getHeight());
        
        if (!viewRect.contains(tabRect)) {
            int x = ((tabRect.x - 5) < 0) ? 0 : (tabRect.x - 5);
            viewport.setViewPosition(new Point(x, 0));
        }
        */
        viewport.validate();
        viewport.repaint();
        tabPanel.repaint();
        focusGained();
        fireTabSelected(new DockedTabEvent(components.get(index)));
    }

    /**
     * Ensures the visibility of the tab at the specified
     * index within the scrolling viewport.
     *
     * @param the tab index
     */
    protected void ensureIndexVisible(int index) {
        // make sure we can see the selected tab in full
        tabPanel.calculateTabRects(components.size());

        Rectangle tabRect = tabRects[index];
        Rectangle viewRect = new Rectangle(
                viewport.getViewPosition().x,
                0,
                viewport.getWidth(),
                viewport.getHeight());

        // check if all the tabs fit in the view
        if (tabPanel.getWidth() <= viewRect.width) {
            viewport.setViewPosition(new Point(0, 0));
        }
        // otherwise just make sure the index is visible
        else if (!viewRect.contains(tabRect)) {
            int x = ((tabRect.x - 5) < 0) ? 0 : (tabRect.x - 5);
            viewport.setViewPosition(new Point(x, 0));
        }
    }

    /**
     * Removes all tab components except that at the
     * specified index.
     *
     * @param the tab index NOT to remove
     */
    protected void removeOthers(int exceptIndex) {
        // populate a temp list
        int count = components.size();
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            if (i != exceptIndex) {
                names[i] = components.get(i).getDisplayName();
            }
        }

        // work on the temp list and remove as required
        for (int i = 0; i < count; i++) {
            if (names[i] != null) {
                closeTabComponent(names[i]);
            }
        }
        names = null;
        viewport.setViewPosition(new Point(0, 0));
    }

    /**
     * Overide to do nothing.
     */
    protected void fireTabMinimised(DockedTabEvent e) {
    }

    /**
     * Overide to do nothing.
     */
    protected void fireTabRestored(DockedTabEvent e) {
    }

    /**
     * Removes all tab components from this panel
     */
    public void removeAllTabs() {
        // populate a temp list
        int count = components.size();
        String[] names = new String[count];
        for (int i = 0; i < count; i++) {
            names[i] = components.get(i).getDisplayName();
        }

        // work on the temp list and remove as required
        for (int i = 0; i < count; i++) {
            closeTabComponent(names[i]);
        }
        names = null;

        if (components.size() == 0) {
            viewport.setViewPosition(new Point(0, 0));
            viewport.validate();
            viewport.repaint();
        }
    }

    /**
     * Cleanup method following removal of all tabs.
     */
    private void allTabsRemoved() {
        tabPanel.setVisible(false);
        componentPanel.setVisible(false);
        selectedIndex = -1;
        tabSelectionPopupMenu.removeAll();

//        scrollButtonPanel.enableButton(SOUTH, false);
//        scrollButtonPanel.repaint();

        scrollButtonPanel.setVisible(false);
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
        // remove from the component cache
        TabComponent removed = components.get(index);
        if (!okToClose(removed)) {
            return;
        }

        // remove from the component cache
        components.remove(index);

        // remove from the layout
        cardLayout.removeLayoutComponent(removed.getComponent());

        // remove from the base panel
        componentPanel.remove(removed.getComponent());

        // remove from the popup menu
        tabSelectionPopupMenu.removeTabMenuItem(index);

        int tabCount = components.size();
        if (tabCount == 0) {
            allTabsRemoved();
            fireTabClosed(new DockedTabEvent(removed));
            return;
        }

        // fire the close event
        fireTabClosed(new DockedTabEvent(removed));

        if (selectedIndex >= index) {
            if (selectedIndex > 0) {
                selectedIndex--;
            }
        }

        TabComponent tabComponent = components.get(selectedIndex);
        cardLayout.show(componentPanel, tabComponent.getLayoutName());

        // ensure we can still see the selected index
        ensureIndexVisible(selectedIndex);
        
        /*
        // move the viewport if it all fits now
        if (tabPanel.getWidth() < (viewport.getViewPosition().x + viewport.getWidth())) {
            viewport.setViewPosition(new Point(0, 0));
        }
        */

        viewport.validate();
        viewport.repaint();
        tabPanel.repaint();
        fireTabSelected(new DockedTabEvent(tabComponent));
        removed = null;
    }

    // -------------------------------------------------
    // scrolling panel controls
    // -------------------------------------------------

    private class ScrollableTabViewport extends JViewport
            implements ChangeListener {

        protected int pgHorz = 1;

        public ScrollableTabViewport() {
            super();
            setScrollMode(SIMPLE_SCROLL_MODE);
            addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            enableButtons(getViewPosition());
        }

        protected void movePanel(int x) {
            // moving in x direction only
            Point pt = getViewPosition();
            pt.x += pgHorz * x;

            pt.x = Math.max(0, pt.x);
            pt.x = Math.min(getMaxXExtent(), pt.x);

            setViewPosition(pt);
            tabPanel.repaint();
        }

        public void setViewPosition(Point p) {
            super.setViewPosition(p);
            enableButtons(p);
        }

        protected void enableButtons(Point pt) {
            boolean visible = pt.x > 0 || pt.x < getMaxXExtent();
            scrollButtonPanel.setVisible(visible);
            if (visible) {
                scrollButtonPanel.enableButton(WEST, pt.x > 0);
                scrollButtonPanel.enableButton(EAST, pt.x < getMaxXExtent());
            }
        }

        protected int getMaxXExtent() {
            return getView().getWidth() - getWidth();
        }

        /*
        protected int getMaxYExtent() {
            return getView().getHeight() - getHeight();
        }
        */

    } // class ScrollableTabViewport

    private class ScrollButtonPanel extends JPanel {

        protected static final int SCROLL_EXTENT = 50;

        // scroll and menu buttons
        protected ScrollButton scrollLeft;
        protected ScrollButton scrollRight;
        protected ScrollButton tabMenuButton;

        public ScrollButtonPanel() {
            super(new GridBagLayout());

            scrollLeft = new ScrollButton(WEST);
            scrollRight = new ScrollButton(EAST);
            tabMenuButton = new ScrollButton(SOUTH);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = ApplicationConstants.EMPTY_INSETS;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridy = 0;
            gbc.gridx = 0;
            gbc.insets.left = 2;
            gbc.insets.bottom = 4;
            add(scrollLeft, gbc);
            gbc.gridx++;
            gbc.insets.left = 0;
            add(scrollRight, gbc);
            gbc.insets.right = 2;
            gbc.insets.left = 2;
            gbc.gridx++;
            add(tabMenuButton, gbc);
        }

        protected void enableButton(int direction, boolean enable) {
            switch (direction) {
                case EAST:
                    scrollRight.setEnabled(enable);
                    break;
                case WEST:
                    scrollLeft.setEnabled(enable);
                    break;
                case SOUTH:
                    tabMenuButton.setEnabled(enable);
                    break;
            }
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // evil hack to continue the tab selection border
            if (selectedIndex != -1) {

                //g.drawLine(0, getHeight() - TabPanel.TAB_BOTTOM_BORDER_HEIGHT - 1,
                //getWidth(), getHeight() - TabPanel.TAB_BOTTOM_BORDER_HEIGHT - 1);
                if (isFocusedTabPane) {
                    g.setColor(tabPanel.activeColor);
                } else {
                    g.setColor(tabPanel.activeNoFocusColor);
                }
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, getHeight() - TabPanel.TAB_BOTTOM_BORDER_HEIGHT,
                        getWidth(), TabPanel.TAB_BOTTOM_BORDER_HEIGHT);
            }
        }

    }


    private class ScrollButton extends JButton
            implements ActionListener {

        /**
         * the disabled button colour
         */
        protected Color disabledColour;

        /**
         * the direction this button faces
         */
        protected int direction;

        public ScrollButton(int direction) {
            this.direction = direction;
            setBorder(null);
            setMargin(ApplicationConstants.EMPTY_INSETS);
            addActionListener(this);
            disabledColour = UIManager.getColor("Button.disabledText");
        }

        public void paintComponent(Graphics g) {
            // paint the background
            g.setColor(tabPanel.background);
            g.fillRect(0, 0, getWidth(), getHeight());

            // draw the border
            g.setColor(tabPanel.controlShadow);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

            if (isEnabled()) {
                g.setColor(Color.BLACK);
            } else {
                g.setColor(disabledColour);
            }
            int iconWidth = 10;
            int iconHeight = 6;

            Graphics2D g2 = (Graphics2D) g;
            AffineTransform oldTransform = g2.getTransform();

            // rotate as required
            if (direction != NORTH) {
                double theta = 0;
                double xOrigin = getWidth() / (double) 2;
                double yOrigin = getHeight() / (double) 2;

                switch (direction) {
                    case EAST:
                        theta = Math.PI / 2;
                        break;
                    case WEST:
                        theta = Math.PI * 1.5;
                        break;
                    case SOUTH:
                        theta = Math.PI;
                        break;
                }
                g2.rotate(theta, xOrigin, yOrigin);
            }

            int x = (getWidth() - iconWidth) / 2;
            int y = getHeight() - ((getHeight() - iconHeight) / 2) - 1;

            int width = iconWidth;
            g.drawLine(x, y, x + (width / 2), y - iconHeight);
            g.drawLine(x + (width / 2), y - iconHeight, x + width, y);

            g2.setTransform(oldTransform);

        }

        public void actionPerformed(ActionEvent e) {
            switch (direction) {
                case EAST:
                    viewport.movePanel(ScrollButtonPanel.SCROLL_EXTENT);
                    break;
                case WEST:
                    viewport.movePanel(-ScrollButtonPanel.SCROLL_EXTENT);
                    break;
                case SOUTH:
                    tabSelectionPopupMenu.showPopup();
                    break;
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }

        public int getWidth() {
            return 18;
        }

        public int getHeight() {
            return 18;
        }

    }

    // -------------------------------------------------
    // tab popup menus
    // -------------------------------------------------

    /**
     * font for the popup menus
     */
    private Font popupMenuFont;

    /**
     * Inits the popup menu font
     */
    private void initPopupMenuFont() {
        popupMenuFont = UIManager.getFont("PopupMenu.font");
        popupMenuFont = popupMenuFont.deriveFont(Font.PLAIN, 10);
    }

    /**
     * Popup menu for tab components accessible through
     * mouse right-click action.
     */
    private class TabPopupMenu extends JPopupMenu
            implements ActionListener {

        private int popupTabIndex;
        private JMenuItem close;
        private JMenuItem closeAll;
        private JMenuItem closeOther;

        public TabPopupMenu() {
            if (popupMenuFont == null) {
                initPopupMenuFont();
            }
            setFont(popupMenuFont);

            close = MenuItemFactory.createMenuItem(bundledString("menuItem.close"));
            closeAll = MenuItemFactory.createMenuItem(bundledString("menuItem.closeAll"));
            closeOther = MenuItemFactory.createMenuItem(bundledString("menuItem.closeOther"));

            close.addActionListener(this);
            closeAll.addActionListener(this);
            closeOther.addActionListener(this);

            add(close);
            add(closeAll);
            add(closeOther);

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
                } else if (source == closeAll) {
                    removeAllTabs();
                } else if (source == closeOther) {
                    removeOthers(popupTabIndex);
                }
            } finally {
                popupTabIndex = -1;
            }

        }

    }

    /**
     * Popup menu for the tab selection button
     */
    private class TabSelectionPopupMenu extends JPopupMenu
            implements ActionListener {
//                                                   PopupMenuListener {

        protected boolean menuVisible;
        protected List<JMenuItem> menuItems;

        public TabSelectionPopupMenu() {
            if (popupMenuFont == null) {
                initPopupMenuFont();
            }
            setFont(popupMenuFont);
            //addPopupMenuListener(this);
        }

        public void addTabMenuItem(TabComponent tabComponent) {
            if (menuItems == null) {
                menuItems = new ArrayList<JMenuItem>();
            }
            JMenuItem menuItem = MenuItemFactory.createMenuItem(tabComponent.getDisplayName(),
                    tabComponent.getIcon());
            menuItem.addActionListener(this);
            menuItems.add(menuItem);
            add(menuItem);
        }

        public void removeTabMenuItem(int index) {
            JMenuItem menuItem = menuItems.get(index);
            menuItems.remove(index);
            remove(menuItem);
        }

        public void renameTabMenuItem(int index, String newTitle) {
            JMenuItem menuItem = menuItems.get(index);
            menuItem.setText(newTitle);
        }

        public void removeAll() {
            super.removeAll();
            menuItems.clear();
        }

        public void showPopup() {

            if (menuVisible) {
                menuVisible = false;
                return;
            }

            menuVisible = true;

            Rectangle bounds = scrollButtonPanel.tabMenuButton.getBounds();
            int x = (int) (bounds.getX() + bounds.getWidth());
            int y = (int) (bounds.getY() + bounds.getHeight());

            pack();
            setVisible(true);
            show(scrollButtonPanel, x - getWidth(), y + 1);
        }

        protected void firePopupMenuCanceled() {
        }

        protected void firePopupMenuWillBecomeInvisible() {
        }

        public void actionPerformed(ActionEvent e) {
            menuVisible = false;
            JMenuItem menuItem = (JMenuItem) e.getSource();
            int index = menuItems.indexOf(menuItem);
            setSelectedIndex(index);
        }

        /*
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            Log.debug("invisible");
            //menuVisible = false;
            wasCancelled = false;
        }
        
        private boolean wasCancelled = true;
        public void popupMenuCanceled(PopupMenuEvent e) {
            Log.debug("cancelling");
            wasCancelled = true;
        }
        */
    }

    // -------------------------------------------------
    // tab display panel and controls
    // -------------------------------------------------

    /**
     * The tab rectangles
     */
    private Rectangle[] tabRects;

    private class TabPanel extends JPanel {

        protected Font font;

        protected Color foreground;
        protected Color background;
        protected Color activeColor;
        protected Color controlShadow;
        protected Color activeNoFocusColor;

        protected int textIconGap;

        protected Insets tabInsets;
        private int height;

        protected static final int TOP_INSET = 5;

        protected static final int TAB_BOTTOM_BORDER_HEIGHT = 3;

        protected TabPanel() {
            initDefaults();
            tabRects = new Rectangle[0];
            MouseHandler mouseHandler = new MouseHandler();
            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        /**
         * Returns the tool tip text of the current
         * mouse rollover tab.
         *
         * @param the mouse event
         * @return the tool tip of the rolled over tab - or null
         */
        public String getToolTipText(MouseEvent e) {
            // check if we are over a button
            if (currentCloseRolloverIndex != -1) {
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

        /**
         * Returns the calculated tab height for this panel.
         *
         * @return the tab height
         */
        public int getHeight() {
            if (height == 0) {
                calculateTabHeight();
            }
            return height + TOP_INSET;
        }

        /**
         * Returns the width based on the position of the last
         * tab rectangle.
         *
         * @return the tab panel width
         */
        public int getWidth() {
            int tabCount = components.size();
            if (tabCount == 0) {
                return 0;
            }

            if (tabRects.length != tabCount) {
                calculateTabRects(tabCount);
            }
            Rectangle lastRect = tabRects[tabRects.length - 1];
            int width = lastRect.x + lastRect.width + 2;
            return Math.max(width, viewport.getWidth());
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        protected void calculateTabHeight() {
            FontMetrics metrics = getFontMetrics(font);
            height = metrics.getHeight() + tabInsets.top +
                    tabInsets.bottom + TAB_BOTTOM_BORDER_HEIGHT + 7;
        }

        private int getTabHeight() {
            return height - TAB_BOTTOM_BORDER_HEIGHT;
        }

        protected void calculateTabRects(int tabCount) {

            // check that we still have the right count
            if (tabRects.length != tabCount) {
                tabRects = new Rectangle[tabCount];
            }

            FontMetrics metrics = getFontMetrics(font);

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
                rect.y = TOP_INSET;

                // add the left and right insets
                rect.width = 0;
                rect.width += tabInsets.left + tabInsets.right;
                TabComponent tabComponent = components.get(i);

                // add the icon width
                if (tabComponent.hasIcon()) {
                    Icon icon = tabComponent.getIcon();
                    rect.width += icon.getIconWidth() + textIconGap;
                }

                // compute and add the text width
                rect.width += SwingUtilities.computeStringWidth(
                        metrics, tabComponent.getDisplayName());

                // add the close icon width
                rect.width += TabControlIcon.ICON_WIDTH + 6;
                rect.height = tabPanel.getTabHeight();
            }

        }

        public void paintComponent(Graphics g) {
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

            int viewX = viewport.getViewPosition().x;

            g.setClip(viewX, 0, viewport.getWidth(), getHeight());

            int w = getWidth();
            int h = getHeight();

            // fill the background
            g.setColor(background);
            g.fillRect(0, 0, w, h);

            // fill the selected tab background
            if (selectedIndex != -1) {
                if (isFocusedTabPane) {
                    g.setColor(activeColor);
                } else {
                    g.setColor(activeNoFocusColor);
                }
                Rectangle selected = tabRects[selectedIndex];
                g.fillRect(selected.x + 1, selected.y + 1,
                        selected.width - 2, selected.height - 1);
            }

            // -----------------------------------
            // draw the borders

            g.setColor(controlShadow);

            // left far side
            g.drawLine(x, y + h - 3, x - 1, y + h + 3);

            // right far side
            g.drawLine(viewX + w, y + h - 5, x + w, y + h);

            for (int i = 0; i < tabCount; i++) {
                Rectangle rect = tabRects[i];
                x = rect.x;
                y = rect.y;
                w = rect.width;
                h = rect.height;

                g.drawLine(x, y + 2, x, y + h - 1); // left side
                g.drawLine(x + 1, y + 1, x + 1, y + 1); // top-left side

                g.drawLine(x + 2, y, x + w - 3, y); // top side

                g.drawLine(x + w - 2, y + 1, x + w - 2, y + 1); // top-right side
                g.drawLine(x + w - 1, y + 2, x + w - 1, y + h - 1); // right side

                // bottom side
                if (i != selectedIndex) {
                    g.drawLine(x - 1, y + h - 1, x + w, y + h - 1);
                }

            }

            // fill the bottom border with the active colour 
            // if a selected index is valid
            if (selectedIndex != -1) {

                if (isFocusedTabPane) {
                    g.setColor(activeColor);
                } else {
                    g.setColor(activeNoFocusColor);
                }
                g.fillRect(viewX,
                        getHeight() - TAB_BOTTOM_BORDER_HEIGHT,
                        getWidth(),
                        TAB_BOTTOM_BORDER_HEIGHT);
                g.setColor(controlShadow);
            }

            // complete the bottom border from the last rectangle
            g.drawLine(x + w - 1, y + h - 1,
                    viewport.getViewPosition().x + viewport.getWidth() - 1, y + h - 1);

            // -------------------------------
            // draw the text and any icons

            g.setFont(font);
            FontMetrics metrics = getFontMetrics(font);

            for (int i = 0; i < tabCount; i++) {
                Rectangle tabRect = new Rectangle(tabRects[i]);
                TabComponent tabComponent = components.get(i);

                // paint the close button
                Rectangle buttonRect = getCloseIconRectangle(tabRect);
                closeIcon.paintIcon(this, g, buttonRect.x, buttonRect.y);

                if (i == currentCloseRolloverIndex) {
                    g.setColor(TabControlIcon.ICON_COLOR);
                    g.drawRect(buttonRect.x - 2,
                            buttonRect.y - 2,
                            buttonRect.width + 3,
                            buttonRect.height + 3);
                }

                // add the text
                g.setColor(foreground);
                if (i != selectedIndex || !isFocusedTabPane) {
                    g.setColor(UIManager.getColor("TabbedPane.foreground"));
                }
                x = tabRect.x + tabInsets.left;

                if (tabComponent.hasIcon()) {
                    // draw the icon
                    y = tabRect.y + tabInsets.top + 4;
                    tabComponent.getIcon().paintIcon(this, g, x, y);
                    // increment x position
                    x += tabComponent.getIcon().getIconWidth() + textIconGap;
                }

                y = metrics.getHeight() + tabRect.y + tabInsets.top + 1;
                g.drawString(tabComponent.getDisplayName(), x, y);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasHint);
            }
        }

        /**
         * the close icon for each tab
         */
        private transient Icon closeIcon = new DockedTabCloseIcon();

        private void initDefaults() {
            Font _font = getFont();
            if (_font != null) {
                font = _font.deriveFont(Font.PLAIN);
            } else {
                font = UIManager.getFont("TabbedPane.font").deriveFont(Font.PLAIN);
            }

            background = getTabBackground();
            activeNoFocusColor = getNofocusTabBackground();

            foreground = getTabForeground();
            activeColor = getSelectedTabBackground();

            Color color = UIManager.getColor("executequery.TabbedPane.border");
            if (color != null) {

                controlShadow = color;

            } else {

                controlShadow = UIManager.getColor("controlShadow");
            }

            textIconGap = UIManager.getInt("TabbedPane.textIconGap");
            if (textIconGap == 0) {

                textIconGap = 4;
            }
            tabInsets = tabInsets();
            tabInsets.left = 5;

            tabInsets.top += 1;
            tabInsets.bottom += 1;
            tabInsets.left += 1;
            tabInsets.right += 1;

        }

    } // class TabPanel

    /**
     * The tab's tool tip
     */
    protected DockedTabToolTip toolTip;

    /**
     * Indicates the current rollover index for the close button
     */
    protected int currentCloseRolloverIndex = -1;

    private class MouseHandler implements MouseInputListener {

        public void mouseMoved(MouseEvent e) {
            boolean doRepaint = false;

            try {
                if (currentCloseRolloverIndex != -1) {
                    doRepaint = true;
                    currentCloseRolloverIndex = -1;
                }

                int x = e.getX();
                int y = e.getY();
                int index = -1;

                //Log.debug("mouse x: " + x);

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
                    if (!toolTipRegistered) {
                        toolTipRegistered = true;
                        ToolTipManager.sharedInstance().registerComponent(tabPanel);
                    }
                    /*
                    if (toolTip == null) {
                        toolTip = new DockedTabToolTip();
                        ToolTipManager.sharedInstance().registerComponent(tabPanel);
                    }
                    toolTip.setVisible(true);
                    */
                }

                // --------------------------------------------
            } finally {
                if (doRepaint) {
                    tabPanel.repaint();
                }
            }

        }

        private boolean toolTipRegistered;


        public void mouseClicked(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
            if (currentCloseRolloverIndex != -1) {
                currentCloseRolloverIndex = -1;
                tabPanel.repaint();
            }
            /*
            if (toolTip != null && toolTip.isVisible()) {
                toolTip.setVisible(false);
            }
             */
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

        private void resetRepaint() {
            currentCloseRolloverIndex = -1;
            tabPanel.repaint();
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
                if (index == -1) {
                    return;
                }

                resetRepaint();

                // check if a close button was pushed
                Rectangle iconRect = getCloseIconRectangle(tabRects[index]);
                if (iconRect.contains(x, y)) {
                    removeIndex(index);
                    return;
                }

                // if the index is not the current index, select the tab
                if (index != selectedIndex) {
                    setSelectedIndex(index);
                    return;
                }

            } finally {
                currentCloseRolloverIndex = -1;
            }
        }

    }

}

