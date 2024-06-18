/*
 * RolloverButton.java
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

package org.underworldlabs.swing;

import org.underworldlabs.swing.plaf.base.AcceleratorToolTipUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

/**
 * This class creates a JButton where the borders are painted only
 * when the mouse is positioned over it. <br>
 * <p>
 * When the mouse pointer is moved away from the button, the borders are removed.
 *
 * @author Takis Diakoumis
 */
public class RolloverButton extends JButton
        implements MouseListener {

    private String toolTip;
    private boolean isPressedByUser;
    private boolean selectionEnabled;
    private boolean mouseEnteredContentAreaFill;

    public RolloverButton() {
        init();
    }

    public RolloverButton(String iconPath, String toolTip) {
        super();
        this.toolTip = toolTip;
        init();
        setButtonIcon(iconPath);
    }

    public RolloverButton(ImageIcon icon, String toolTip) {
        super(icon);
        this.toolTip = toolTip;
        init();
    }

    public RolloverButton(Action action, String toolTip) {
        super(action);
        this.toolTip = toolTip;
        init();
    }

    public RolloverButton(ImageIcon icon, String toolTip, int height, int width) {
        super(icon);
        this.toolTip = toolTip;

        init();
        setButtonSize(height, width);
    }

    public RolloverButton(String label, String toolTip, int height, int width) {
        super(label);
        this.toolTip = toolTip;

        init();
        setButtonSize(height, width);
    }

    private void init() {
        selectionEnabled = true;
        mouseEnteredContentAreaFill = true;

        setMargin(new Insets(1, 1, 1, 1));
        setToolTipText(toolTip);
        setBorderPainted(false);
        setContentAreaFilled(false);
        addMouseListener(this);
    }

    /**
     * Resets the buttons rollover state.
     */
    public void reset() {
        setBorderPainted(false);
        setContentAreaFilled(false);
    }

    private void setButtonSize(int height, int width) {
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
    }

    private void setPressedBackground(boolean paintBorder, boolean fillBackground) {
        setBorderPainted(paintBorder);
        setContentAreaFilled(fillBackground);
    }

    /**
     * Sets the image associated with the button.
     *
     * @param iconPath the path relative to this class of the button icon image
     */
    public void setButtonIcon(String iconPath) {
        URL iconUrl = RolloverButton.class.getResource(iconPath);
        if (iconUrl != null)
            setIcon(new ImageIcon(iconUrl));
    }

    public void enableSelectionRollover(boolean enable) {
        selectionEnabled = enable;
    }

    public boolean isSelectionRolloverEnabled() {
        return selectionEnabled;
    }

    public void setMouseEnteredContentAreaFill(boolean mouseEnteredContentAreaFill) {
        this.mouseEnteredContentAreaFill = mouseEnteredContentAreaFill;
    }

    public boolean isPressedByUser() {
        return isPressedByUser;
    }

    public void setPressed(boolean pressed) {
        this.isPressedByUser = pressed;
        setPressedBackground(pressed, pressed);
    }

    // --- MouseListener impl ---

    /**
     * Paints the button's borders as the mouse pointer enters
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        if (isEnabled() && isSelectionRolloverEnabled())
            setPressedBackground(true, mouseEnteredContentAreaFill);
    }

    /**
     * Sets the button's borders unpainted as the mouse
     * pointer exits
     */
    @Override
    public void mouseExited(MouseEvent e) {
        if (!isPressedByUser())
            setPressedBackground(false, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    // --- JComponent impl ---

    @Override
    public JToolTip createToolTip() {

        JToolTip jToolTip = new JToolTip() {
            @Override
            public void updateUI() {
                setUI(new AcceleratorToolTipUI());
            }
        };

        jToolTip.setComponent(this);
        return jToolTip;
    }

    // --- Component impl ---

    /**
     * Override the <code>isFocusable()</code>
     * method of <code>Component</code> (JDK1.4) to
     * return false so the button never maintains
     * the focus.
     *
     * @return false
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    // --- Object impl ---

    @Override
    public String toString() {
        return getName();
    }

}
