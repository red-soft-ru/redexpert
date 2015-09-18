/*
 * AbstractTabPopupMenuContainer.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.underworldlabs.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.plaf.TabbedPaneUI;

import org.underworldlabs.swing.plaf.CloseTabbedPaneUI;
import org.underworldlabs.swing.plaf.TabMenuItem;

public class AbstractTabPopupMenuContainer extends JTabbedPane implements TabPopupMenuContainer {

    public AbstractTabPopupMenuContainer() {
        super();
    }

    public AbstractTabPopupMenuContainer(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }

    public AbstractTabPopupMenuContainer(int tabPlacement) {
        super(tabPlacement);
    }

    private boolean tabPopupEnabled;
    
    private TabPopupMenu popup;

    public void addTab(String title, Component component) {
        addTab(title, null, component, null);
    }
    
    public void addTab(String title, Icon icon, Component component) {
        addTab(title, icon, component, null);
    }

    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        // make sure the pane is visible - may have been empty
        if (!isVisible()) {
            setVisible(true);
        }

        Component _component = tabContentPanelForComponent(component);
        super.insertTab(title, icon, _component, tip, index);

        /*
        if (tabPopupEnabled) {
            TabMenuItem menuItem = addAssociatedMenu(title, icon, _component);
            _component.setTabMenuItem(menuItem);
        }
        */
    }

    protected Component tabContentPanelForComponent(Component component) {

        return component;
    }
    
    public void addTab(String title, Icon icon, Component component, String tip) {
        
        // make sure the pane is visible - may have been empty
        if (!isVisible()) {

            setVisible(true);
        }

        Component _component = tabContentPanelForComponent(component);
        super.addTab(title, icon, _component, tip);
    }

    protected TabMenuItem addAssociatedMenu(String title, Icon icon, Component component) {
        TabMenuItem menuItem = new TabMenuItem(title, icon, component);
        popup.addTabSelectionMenuItem(menuItem);
        return menuItem;
    }
    
    public void removeAll() {
        popup.removeAllTabSelectionMenuItems();
        super.removeAll();
        setVisible(false);
    }

    public void remove(int index) {

        super.remove(index);
        if (getTabCount() == 0) {

            setVisible(false);
        }

    }

    public boolean isTabPopupEnabled() { 
        return tabPopupEnabled;
    }

    public void setTabPopupEnabled(boolean tabPopupEnabled) {
        this.tabPopupEnabled = tabPopupEnabled;
        if (tabPopupEnabled && popup == null) {
            popup = new TabPopupMenu(this);
        }
    }

    public void showPopup(int index, int x, int y) {
        popup.setHoverTabIndex(index);
        popup.show(this, x, y);
    }

    private class TabPopupMenu extends JPopupMenu implements ActionListener {

        private JMenu openTabs;
        private JMenuItem close;
        private JMenuItem closeAll;
        private JMenuItem closeOther;
        
        private JTabbedPane tabPane;
        
        private int hoverTabIndex;
        
        public TabPopupMenu(JTabbedPane tabPane) {
            this.tabPane = tabPane;

            close = new JMenuItem("Close");
            closeAll = new JMenuItem("Close All");
            closeOther = new JMenuItem("Close Others");
            
            close.addActionListener(this);
            closeAll.addActionListener(this);
            closeOther.addActionListener(this);

            add(close);
            add(closeAll);
            add(closeOther);
            
            hoverTabIndex = -1;
        }

        public void addTabSelectionMenuItem(TabMenuItem menuItem) {
/*
            if (openTabs == null) {
                addSeparator();
                openTabs = new JMenu("Select");
                add(openTabs);
            }
*/
            menuItem.addActionListener(this);
            openTabs.add(menuItem);
        }
        
        public void removeAllTabSelectionMenuItems() {
            if (openTabs == null) {
                return;
            }
            openTabs.removeAll();            
        }

        public void removeTabSelectionMenuItem(TabMenuItem menuItem) {
            if (openTabs == null) {
                return;
            }
            openTabs.remove(menuItem);
        }
        
        public void actionPerformed(ActionEvent e) {

            if (hoverTabIndex == -1) {
                return;
            }
            
            Object source = e.getSource();
            if (source == close) {
                tabPane.remove(hoverTabIndex);
            }
            else if (source == closeAll) {
                tabPane.removeAll();
            }
            else if (source == closeOther) {
                int count = 0;
                int tabCount = tabPane.getTabCount();
                Component[] tabs = new Component[tabCount -1];
                for (int i = 0; i < tabCount; i++) {
                    if (i != hoverTabIndex) {
                        tabs[count++] = tabPane.getComponentAt(i);
                    }
                }
                for (int i = 0; i < tabs.length; i++) {
                    tabPane.remove(tabs[i]);
                }
            }
            else if (source instanceof TabMenuItem) {
                TabMenuItem item = (TabMenuItem)source;
                tabPane.setSelectedComponent(item.getTabComponent());
            }

        }

        public int getHoverTabIndex() {
            return hoverTabIndex;
        }

        public void setHoverTabIndex(int hoverTabIndex) {
            this.hoverTabIndex = hoverTabIndex;
        }
        
    } // class TabPopupMenu

    protected CloseTabbedPaneUI tabUI;
    
    public TabbedPaneUI getUI() {
        return tabUI;
    }
    
    public void updateUI() {
        tabUI = new CloseTabbedPaneUI();
        setUI(tabUI);
    }

    public boolean isTabPopupShowing() {
        return popup.isShowing();
    }

}






