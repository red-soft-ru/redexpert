/*
 * GlassCapturePanel.java
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
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class GlassCapturePanel extends JPanel 
                               implements MouseInputListener,
                                          MouseWheelListener {
    
    /** The primary component */
    protected Container mainComponent;

    private List<GlassPaneSelectionListener> listeners;

    /** Creates a new instance of GlassCapturePanel */
    public GlassCapturePanel(Container mainComponent) {
        this.mainComponent = mainComponent;
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        setOpaque(false);
    }

    public void removeGlassPaneSelectionListener(GlassPaneSelectionListener listener) {
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);        
    }
    
    public void addGlassPaneSelectionListener(GlassPaneSelectionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<GlassPaneSelectionListener>();
        }
        listeners.add(listener);
    }
    
    private Component mouseEventTarget = null;
    private Component dragSource = null;

    public void mouseWheelMoved(MouseWheelEvent e) {
        forwardMouseEvent(e);
    }

    /**
     * When inactive, mouse events are forwarded as appropriate either to
     * the UI to activate the frame or to the underlying child component.
     */
    public void mousePressed(MouseEvent e) {
        forwardMouseEvent(e);
    }

    /**
     * Forward the mouseEntered event to the underlying child container.
     * @see #mousePressed
     */
    public void mouseEntered(MouseEvent e) {
        forwardMouseEvent(e);
    }
    /**
     * Forward the mouseMoved event to the underlying child container.
     * @see #mousePressed
     */
    public void mouseMoved(MouseEvent e) {
        forwardMouseEvent(e);
    }
    /**
     * Forward the mouseExited event to the underlying child container.
     * @see #mousePressed
     */
    public void mouseExited(MouseEvent e) {
        forwardMouseEvent(e);
    }
    /**
     * Ignore mouseClicked events.
     * @see #mousePressed
     */
    public void mouseClicked(MouseEvent e) {            
        forwardMouseEvent(e);
    }

    /**
     * Forward the mouseReleased event to the underlying child container.
     * @see #mousePressed
     */
    public void mouseReleased(MouseEvent e) {
        forwardMouseEvent(e);
    }

    /**
     * Forward the mouseDragged event to the underlying child container.
     * @see #mousePressed
     */
    public void mouseDragged(MouseEvent e) {
        forwardMouseEvent(e);
    }

    /**
     * Forward a mouse event to the current mouse target, setting it
     * if necessary.
     */
    private void forwardMouseEvent(MouseEvent e) {
        Component target = findComponentAt(mainComponent, e.getX(), e.getY());          

        int id = e.getID();

        if (e instanceof MouseWheelEvent) {
            if (target == null) {
                return;
            }

            MouseWheelEvent wheelEvent = (MouseWheelEvent)e;

            // do the retarget here -  not a focus event
            if (target != mouseEventTarget) {
                mouseEventTarget = target;
            }

            Point p = SwingUtilities.convertPoint(mainComponent,
                                                  e.getX(), e.getY(),
                                                  target);

            MouseWheelEvent retargeted = 
                    new MouseWheelEvent(target,
                                        id,
                                        e.getWhen(),
                                        wheelEvent.getModifiers() | wheelEvent.getModifiersEx(),
                                        p.x,
                                        p.y,
                                        wheelEvent.getClickCount(),
                                        wheelEvent.isPopupTrigger(),
                                        wheelEvent.getScrollType(),
                                        wheelEvent.getScrollAmount(),
                                        wheelEvent.getWheelRotation());

            target.dispatchEvent(retargeted);

        }

        switch(id) {
            case MouseEvent.MOUSE_ENTERED:

                if (target != mouseEventTarget) {
                    mouseEventTarget = target;
                }

                retargetMouseEvent(id, e, mouseEventTarget);
                break;

            case MouseEvent.MOUSE_PRESSED:

                if (target != mouseEventTarget) {
                    mouseEventTarget = target;
                }

                retargetMouseEvent(id, e, mouseEventTarget);
                // Set the drag source in case we start dragging.
                dragSource = target;
                break;

            case MouseEvent.MOUSE_EXITED:
                retargetMouseEvent(id, e, mouseEventTarget);
                break;

            case MouseEvent.MOUSE_CLICKED:
                retargetMouseEvent(id, e, mouseEventTarget);
                break;

            case MouseEvent.MOUSE_MOVED:

                if (target != mouseEventTarget) {
                    retargetMouseEvent(MouseEvent.MOUSE_EXITED, e, mouseEventTarget);
                    mouseEventTarget = target;
                    retargetMouseEvent(MouseEvent.MOUSE_ENTERED, e, mouseEventTarget);
                }

                retargetMouseEvent(id, e, mouseEventTarget);
                break;

            case MouseEvent.MOUSE_DRAGGED:
                retargetMouseEvent(id, e, dragSource);
                break;

            case MouseEvent.MOUSE_RELEASED:
                retargetMouseEvent(id, e, mouseEventTarget);
                break;

        }

        // notify listeners of the selection
        if (listeners != null) {
            
            for (int i = 0, n = listeners.size(); i < n; i++) {
                listeners.get(i).glassPaneSelected(e);
            }
            
        }
        
    }

    /*
     * Find the lightweight child component which corresponds to the
     * specified location.  This is similar to the new 1.2 API in
     * Container, but we need to run on 1.1.  The other changes are
     * due to Container.findComponentAt's use of package-private data.
     */
    private Component findComponentAt(Container c, int x, int y) {

        if (!c.contains(x, y)) {
            return c;
        }

        int ncomponents = c.getComponentCount();
        Component component[] = c.getComponents();

        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = component[i];
            Point loc = comp.getLocation();

            if ((comp != null) && (comp.contains(x - loc.x, y - loc.y)) &&
                //(comp.getPeer() instanceof java.awt.peer.LightweightPeer) &&
                    comp.isDisplayable() && comp.isVisible()) {

                // found a component that intersects the point, see if there
                // is a deeper possibility.
                if (comp instanceof Container) {

                    Container child = (Container) comp;
                    Point childLoc = child.getLocation();
                    Component deeper = findComponentAt(child,
                                x - childLoc.x, y - childLoc.y);

                    if (deeper != null) {
                        return deeper;
                    }

                }
                else {
                    return comp;
                }

            }

        }

        return c;
    }

    /*
     * Dispatch an event clone, retargeted for the specified target.
     */
    protected void retargetMouseEvent(int id, MouseEvent e, Component target) {

        if (target == null) {
            return;
        }

        Point p = SwingUtilities.convertPoint(mainComponent,
                                              e.getX(), e.getY(),
                                              target);

        MouseEvent retargeted = new MouseEvent(target,
                                        id,
                                        e.getWhen(),
                                        e.getModifiers() | e.getModifiersEx(),
                                        p.x,
                                        p.y,
                                        e.getClickCount(),
                                        e.isPopupTrigger());

        target.dispatchEvent(retargeted);
    }

    /**
     * Returns the components below this glass pane.
     */
    public Container getComponentBelow() {
        return mainComponent;
    }

}











