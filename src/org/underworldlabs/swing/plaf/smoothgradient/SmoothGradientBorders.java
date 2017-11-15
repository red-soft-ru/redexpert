/*
 * SmoothGradientBorders.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;

/**
 *
 * @author   Takis Diakoumis
 */
final class SmoothGradientBorders {
    
    
    // Accessing and Creating Borders ***************************************
    
    private static Border buttonBorder;
    private static Border comboBoxEditorBorder;
    private static Border comboBoxArrowButtonBorder;
    private static Border etchedBorder;
    private static Border flush3DBorder;
    private static Border menuBarHeaderBorder;
    private static Border menuBorder;
    private static Border menuItemBorder;
    private static Border popupMenuBorder;
    private static Border rolloverButtonBorder;
    private static Border scrollPaneBorder;
    private static Border separatorBorder;
    private static Border textFieldBorder;
    private static Border thinLoweredBorder;
    private static Border thinRaisedBorder;
    private static Border toggleButtonBorder;
    private static Border toolBarHeaderBorder;
    
    
    /**
     * Returns a border instance for a <code>JButton</code>.
     */
    static Border getButtonBorder() {
        if (buttonBorder == null) {
            buttonBorder = new BorderUIResource.CompoundBorderUIResource(
                    new ButtonBorder(),
                    new BasicBorders.MarginBorder());
        }
        return buttonBorder;
    }
    
    /**
     * Returns a border for a <code>JComboBox</code>'s editor.
     */
    static Border getComboBoxEditorBorder() {
        if (comboBoxEditorBorder == null) {
            comboBoxEditorBorder = new CompoundBorder( // No UIResource
                    new ComboBoxEditorBorder(),
                    new BasicBorders.MarginBorder());
        }
        return comboBoxEditorBorder;
    }
    
    /**
     * Returns a border for a <code>JComboBox</code>'s button.
     */
    static Border getComboBoxArrowButtonBorder() {
        if (comboBoxArrowButtonBorder == null) {
            comboBoxArrowButtonBorder = new CompoundBorder(  // No UIResource
                    new ComboBoxArrowButtonBorder(),
                    new BasicBorders.MarginBorder());
        }
        return comboBoxArrowButtonBorder;
    }
    
    /**
     * Returns an etched border instance for <code>JMenuBar</code> or
     * <code>JToolBar</code>.
     */
    static Border getEtchedBorder() {
        if (etchedBorder == null) {
            etchedBorder = new BorderUIResource.CompoundBorderUIResource(
                    new EtchedBorder(),
                    new BasicBorders.MarginBorder());
        }
        return etchedBorder;
    }
    
    /**
     * Returns a flushed 3D border.
     */
    static Border getFlush3DBorder() {
        if (flush3DBorder == null) {
            flush3DBorder = new Flush3DBorder();
        }
        return flush3DBorder;
    }
    
    /**
     * Returns a border for a <code>JInternalFrame</code>.
     */
    static Border getInternalFrameBorder() {
        return new InternalFrameBorder();
    }
    
    /**
     * Returns a special border for a <code>JMenuBar</code> that
     * is used in a header just above a <code>JToolBar</code>.
     */
    static Border getMenuBarHeaderBorder() {
        if (menuBarHeaderBorder == null) {
            menuBarHeaderBorder = new BorderUIResource.CompoundBorderUIResource(
                    new MenuBarHeaderBorder(),
                    new BasicBorders.MarginBorder());
        }
        return menuBarHeaderBorder;
    }
    
    /**
     * Returns a border instance for a <code>JMenu</code>.
     */
    static Border getMenuBorder() {
        if (menuBorder == null) {
            menuBorder = new BorderUIResource.CompoundBorderUIResource(
                    new MenuBorder(),
                    new BasicBorders.MarginBorder());
        }
        return menuBorder;
    }
    
    /**
     * Returns a border instance for a <code>JMenuItem</code>.
     */
    static Border getMenuItemBorder() {
        if (menuItemBorder == null) {
            menuItemBorder =
                    new BorderUIResource(new BasicBorders.MarginBorder());
        }
        return menuItemBorder;
    }
    
    /**
     * Returns a border instance for a <code>JPopupMenu</code>.
     */
    static Border getPopupMenuBorder() {
        if (popupMenuBorder == null) {
            popupMenuBorder = new PopupMenuBorder();
        }
        return popupMenuBorder;
    }
    
    /**
     * Returns a border for a <code>JInternalFrame</code>'s palette.
     */
    static Border getPaletteBorder() {
        return new PaletteBorder();
    }
    
    /**
     * Returns a rollover border for buttons in a <code>JToolBar</code>.
     */
    static Border getRolloverButtonBorder() {
        if (rolloverButtonBorder == null) {
            rolloverButtonBorder = new BorderUIResource.CompoundBorderUIResource(
                    new RolloverButtonBorder(),
                    new BasicBorders.MarginBorder());
        }
        return rolloverButtonBorder;
    }
    
    /**
     * Returns a separator border instance for <code>JScrollPane</code>.
     */
    static Border getScrollPaneBorder() {
        if (scrollPaneBorder == null) {
            scrollPaneBorder = new ScrollPaneBorder();
        }
        return scrollPaneBorder;
    }
    
    /**
     * Returns a separator border instance for <code>JMenuBar</code> or
     * <code>JToolBar</code>.
     */
    static Border getSeparatorBorder() {
        if (separatorBorder == null) {
            separatorBorder = new BorderUIResource.CompoundBorderUIResource(
                    new SeparatorBorder(),
                    new BasicBorders.MarginBorder());
        }
        return separatorBorder;
    }
    
    /**
     * Returns a border instance for a JTextField.
     */
    static Border getTextFieldBorder() {
        if (textFieldBorder == null) {
            textFieldBorder = new BorderUIResource.CompoundBorderUIResource(
                    new TextFieldBorder(),
                    new BasicBorders.MarginBorder());
        }
        return textFieldBorder;
    }
    
    /**
     * Returns a thin lowered border.
     */
    static Border getThinLoweredBorder() {
        if (thinLoweredBorder == null) {
            thinLoweredBorder = new ThinLoweredBorder();
        }
        return thinLoweredBorder;
    }
    
    /**
     * Returns a thin raised border.
     */
    static Border getThinRaisedBorder() {
        if (thinRaisedBorder == null) {
            thinRaisedBorder = new ThinRaisedBorder();
        }
        return thinRaisedBorder;
    }
    
    /**
     * Returns a border instance for a JToggleButton.
     */
    static Border getToggleButtonBorder() {
        if (toggleButtonBorder == null) {
            toggleButtonBorder = new BorderUIResource.CompoundBorderUIResource(
                    new ToggleButtonBorder(),
                    new BasicBorders.MarginBorder());
        }
        return toggleButtonBorder;
    }
    
    /**
     * Returns a special border for a <code>JToolBar</code> that
     * is used in a header just below a <code>JMenuBar</code>.
     */
    static Border getToolBarHeaderBorder() {
        if (toolBarHeaderBorder == null) {
            toolBarHeaderBorder = new BorderUIResource.CompoundBorderUIResource(
                    new ToolBarHeaderBorder(),
                    new BasicBorders.MarginBorder());
        }
        return toolBarHeaderBorder;
    }
    
    private static Border optionDialogBorder;
    static Border getOptionDialogBorder() {
        if (optionDialogBorder == null) {
            optionDialogBorder = new OptionDialogBorder();
        }
        return optionDialogBorder;
    }
    
    private static class OptionDialogBorder extends AbstractBorder implements UIResource {
        private static final Insets insets = new Insets(3, 3, 3, 3);
        int titleHeight = 0;
        
        public void paintBorder( Component c, Graphics g, int x, int y, int w, int h ) {
            
            g.translate(x,y);
            
            int messageType = JOptionPane.PLAIN_MESSAGE;
            if (c instanceof JInternalFrame) {
                Object obj = ((JInternalFrame) c).getClientProperty(
                        "JInternalFrame.messageType");
                if (obj != null && (obj instanceof Integer)) {
                    messageType = ((Integer) obj).intValue();
                }
            }
            
            Color borderColor;
            
            switch (messageType) {
                case(JOptionPane.ERROR_MESSAGE):
                    borderColor = UIManager.getColor(
                            "OptionPane.errorDialog.border.background");
                    break;
                case(JOptionPane.QUESTION_MESSAGE):
                    borderColor = UIManager.getColor(
                            "OptionPane.questionDialog.border.background");
                    break;
                case(JOptionPane.WARNING_MESSAGE):
                    borderColor = UIManager.getColor(
                            "OptionPane.warningDialog.border.background");
                    break;
                case(JOptionPane.INFORMATION_MESSAGE):
                case(JOptionPane.PLAIN_MESSAGE):
                default:
                    borderColor = MetalLookAndFeel.getPrimaryControlDarkShadow();
                    break;
            }
            
            g.setColor(borderColor);
            
            // Draw outermost lines
            g.drawLine( 1, 0, w-2, 0);
            g.drawLine( 0, 1, 0, h-2);
            g.drawLine( w-1, 1, w-1, h-2);
            g.drawLine( 1, h-1, w-2, h-1);
            
            // Draw the bulk of the border
            for (int i = 1; i < 3; i++) {
                g.drawRect(i, i, w-(i*2)-1, h-(i*2)-1);
            }
            
            g.translate(-x,-y);
            
        }
        
        public Insets getBorderInsets(Component c)       {
            return insets;
        }
        
        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top = insets.top;
            newInsets.left = insets.left;
            newInsets.bottom = insets.bottom;
            newInsets.right = insets.right;
            return newInsets;
        }
    }
    
    private static Border dialogBorder;
    static Border getDialogBorder() {
        if (dialogBorder == null) {
            dialogBorder = new DialogBorder();
        }
        return dialogBorder;
    }
    
    private static class DialogBorder extends AbstractBorder implements UIResource {
        private static final Insets insets = new Insets(2, 2, 2, 2);
        private static final int corner = 14;
        
        protected Color getActiveBackground() {
            return SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
        }
        
        protected Color getActiveHighlight() {
            return SmoothGradientLookAndFeel.getPrimaryControlShadow();
        }
        
        protected Color getActiveShadow() {
            return SmoothGradientLookAndFeel.getPrimaryControlInfo();
        }
        
        protected Color getInactiveBackground() {
            return SmoothGradientLookAndFeel.getControlDarkShadow();
        }
        
        protected Color getInactiveHighlight() {
            return SmoothGradientLookAndFeel.getControlShadow();
        }
        
        protected Color getInactiveShadow() {
            return SmoothGradientLookAndFeel.getControlInfo();
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Color background;
            Color highlight;
            Color shadow;
            
            Window window = SwingUtilities.getWindowAncestor(c);
            if (window != null && window.isActive()) {
                background = getActiveBackground();
                highlight = getActiveHighlight();
                shadow = getActiveShadow();
            } else {
                background = getInactiveBackground();
                highlight = getInactiveHighlight();
                shadow = getInactiveShadow();
            }
            
            g.setColor(background);
            // Draw outermost lines
            g.drawLine( x + 1, y + 0, x + w-2, y + 0);
            g.drawLine( x + 0, y + 1, x + 0, y + h - 2);
            g.drawLine( x + w - 1, y + 1, x + w - 1, y + h - 2);
            g.drawLine( x + 1, y + h - 1, x + w - 2, y + h - 1);
            
            // Draw the bulk of the border
            for (int i = 1; i < 5; i++) {
                g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
            }
            
            
            if ((window instanceof Dialog) && ((Dialog) window).isResizable()) {
                g.setColor(highlight);
                // Draw the Long highlight lines
                g.drawLine( corner+1, 3, w-corner, 3);
                g.drawLine( 3, corner+1, 3, h-corner);
                g.drawLine( w-2, corner+1, w-2, h-corner);
                g.drawLine( corner+1, h-2, w-corner, h-2);
                
                g.setColor(shadow);
                // Draw the Long shadow lines
                g.drawLine( corner, 2, w-corner-1, 2);
                g.drawLine( 2, corner, 2, h-corner-1);
                g.drawLine( w-3, corner, w-3, h-corner-1);
                g.drawLine( corner, h-3, w-corner-1, h-3);
            }
            
        }
        
        public Insets getBorderInsets(Component c)       {
            return insets;
        }
        
        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top = insets.top;
            newInsets.left = insets.left;
            newInsets.bottom = insets.bottom;
            newInsets.right = insets.right;
            return newInsets;
        }
    }
    

    private static Border errorDialogBorder;
    static Border getErrorDialogBorder() {
        if (errorDialogBorder == null) {
            errorDialogBorder = new ErrorDialogBorder();
        }
        return errorDialogBorder;
    }

    private static class ErrorDialogBorder extends DialogBorder implements UIResource {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.errorDialog.border.background");
        }
    }
    
    private static Border questionDialogBorder;
    static Border getQuestionDialogBorder() {
        if (questionDialogBorder == null) {
            questionDialogBorder = new QuestionDialogBorder();
        }
        return questionDialogBorder;
    }    

    private static class QuestionDialogBorder extends DialogBorder implements UIResource {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.questionDialog.border.background");
        }
    }
    
    private static Border warningDialogBorder;
    static Border getWarningDialogBorder() {
        if (warningDialogBorder == null) {
            warningDialogBorder = new WarningDialogBorder();
        }
        return warningDialogBorder;
    }    

    private static class WarningDialogBorder extends DialogBorder implements UIResource {
        protected Color getActiveBackground() {
            return UIManager.getColor("OptionPane.warningDialog.border.background");
        }
    }
    
    private static class Flush3DBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(2, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            if (c.isEnabled())
                SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
            else
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w, h);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
        
        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top	 = INSETS.top;
            newInsets.left	 = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right	 = INSETS.right;
            return newInsets;
        }
    }
    
    
    private static class ButtonBorder extends AbstractBorder implements UIResource {
        
        protected static final Insets INSETS = new Insets(2, 3, 2, 3);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            
            if (model.isEnabled()) {
                boolean isPressed = model.isPressed() && model.isArmed();
                boolean isDefault = button instanceof JButton
                        && ((JButton) button).isDefaultButton();
                
                if (isPressed && isDefault)
                    SmoothGradientUtils.drawDefaultButtonPressedBorder(g, x, y, w, h);
                else if (isPressed)
                    SmoothGradientUtils.drawPressed3DBorder(g, x, y, w, h);
                else if (isDefault)
                    SmoothGradientUtils.drawDefaultButtonBorder(g, x, y, w, h, false);
                else
                    SmoothGradientUtils.drawButtonBorder(g, x, y, w, h, false);
            } else { // disabled state
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w - 1, h - 1);
            }
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }

        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top	 = INSETS.top;
            newInsets.left	 = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right  = INSETS.right;
            return newInsets;
        }
    }
    
    
    private static class ComboBoxEditorBorder extends AbstractBorder {
        
        private static final Insets INSETS  = new Insets(2, 2, 2, 0);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            if (c.isEnabled())
                SmoothGradientUtils.drawFlush3DBorder(g, x, y, w + 2, h);
            else {
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w + 2, h-1);
                g.setColor(UIManager.getColor("control"));
                g.drawLine(x, y + h-1, x + w, y + h-1);
            }
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    private static class ComboBoxArrowButtonBorder extends AbstractBorder implements UIResource {
        
        protected static final Insets INSETS = new Insets(2, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            
            if (model.isEnabled()) {
                boolean isPressed = model.isPressed() && model.isArmed();
                
                if (isPressed)
                    SmoothGradientUtils.drawPressed3DBorder(g, x, y, w, h);
                else
                    SmoothGradientUtils.drawButtonBorder(g, x, y, w, h, false);
            } else {
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w - 1, h - 1);
            }
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    /**
     * A border used for <code>JInternalFrame</code>s.
     */
    private static class InternalFrameBorder extends AbstractBorder implements UIResource {
        
        private static final Insets NORMAL_INSETS	= new Insets(3, 3, 3, 3);
        
        private static final int corner = 14;
        
        private static final Insets MAXIMIZED_INSETS	= new Insets(2, 2, 2, 2);
        static final int   ALPHA1			= 150;
        static final int   ALPHA2			=  50;
        
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            
            Color background;
            Color highlight;
            Color shadow;
            
            if (c instanceof JInternalFrame && ((JInternalFrame)c).isSelected()) {
                background = SmoothGradientLookAndFeel.getPrimaryControlDarkShadow();
                highlight = SmoothGradientLookAndFeel.getPrimaryControlShadow();
                shadow = SmoothGradientLookAndFeel.getPrimaryControlInfo();
            } else {
                background = SmoothGradientLookAndFeel.getControlDarkShadow();
                highlight = SmoothGradientLookAndFeel.getControlShadow();
                shadow = SmoothGradientLookAndFeel.getControlInfo();
            }
            
            g.setColor(background);
            // Draw outermost lines
            g.drawLine( 1, 0, w-2, 0);
            g.drawLine( 0, 1, 0, h-2);
            g.drawLine( w-1, 1, w-1, h-2);
            g.drawLine( 1, h-1, w-2, h-1);
            
            // Draw the bulk of the border
            for (int i = 1; i < 5; i++) {
                g.drawRect(x+i,y+i,w-(i*2)-1, h-(i*2)-1);
            }
            
            if (c instanceof JInternalFrame && ((JInternalFrame)c).isResizable()) {
                g.setColor(highlight);
                // Draw the Long highlight lines
                g.drawLine( corner+1, 3, w-corner, 3);
                g.drawLine( 3, corner+1, 3, h-corner);
                g.drawLine( w-2, corner+1, w-2, h-corner);
                g.drawLine( corner+1, h-2, w-corner, h-2);
                
                g.setColor(shadow);
                // Draw the Long shadow lines
                g.drawLine( corner, 2, w-corner-1, 2);
                g.drawLine( 2, corner, 2, h-corner-1);
                g.drawLine( w-3, corner, w-3, h-corner-1);
                g.drawLine( corner, h-3, w-corner-1, h-3);
            }
/*
            JInternalFrame frame = (JInternalFrame) c;
            if (frame.isMaximum())
                SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
            else
                paintShadowedBorder(g, x, y, w, h);
 */
        }
        
        private void paintShadowedBorder(Graphics g, int x, int y, int w, int h) {
            
            
            /*
            Color background	= UIManager.getColor("desktop");
            Color highlight		= UIManager.getColor("controlLtHighlight");
            Color darkShadow    = UIManager.getColor("controlDkShadow");
            Color lightShadow   = new Color(darkShadow.getRed(),
                                            darkShadow.getGreen(),
                                            darkShadow.getBlue(),
                                            ALPHA1);
            Color lighterShadow = new Color(darkShadow.getRed(),
                                            darkShadow.getGreen(),
                                            darkShadow.getBlue(),
                                            ALPHA2);
            g.translate(x, y);
            // Dark border
            g.setColor(darkShadow);
            g.drawRect(0,   0, w-3, h-3);
            // Highlight top and left
            g.setColor(highlight);
            g.drawLine(1, 1, w - 4, 1);
            g.drawLine(1, 1, 1, h - 4);
            // Paint background before painting the shadow
            g.setColor(background);
            g.fillRect(w - 2, 0, 2, h);
            g.fillRect(0, h-2, w, 2);
            // Shadow line 1
            g.setColor(lightShadow);
            g.drawLine(w - 2, 1, w - 2, h - 2);
            g.drawLine(1, h - 2, w - 3, h - 2);
            // Shadow line2
            g.setColor(lighterShadow);
            g.drawLine(w - 1, 2, w - 1, h - 2);
            g.drawLine(2, h - 1, w - 2, h - 1);
            g.translate(-x, -y);
             **/
        }
        
        public Insets getBorderInsets(Component c) {
            return ((JInternalFrame) c).isMaximum() ? MAXIMIZED_INSETS : NORMAL_INSETS;
        }
    }
    
    
    /**
     * A border used for the palette of <code>JInternalFrame</code>s.
     */
    private static class PaletteBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(1, 1, 1, 1);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h ) {
            g.translate(x,y);
            g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w-1, h-1);
            g.translate(-x,-y);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    /**
     * A border that looks like a separator line; used for menu bars
     * and tool bars.
     */
    private static class SeparatorBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(0, 0, 2, 1);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor( UIManager.getColor("Separator.foreground"));
            g.drawLine( 0, h - 2, w - 1, h - 2 );
            
            g.setColor( UIManager.getColor("Separator.background"));
            g.drawLine( 0, h - 1, w - 1, h - 1 );
            g.translate(-x, -y);
        }
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    private static class ThinRaisedBorder extends AbstractBorder implements UIResource {
        private static final Insets INSETS = new Insets(2, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            SmoothGradientUtils.drawThinFlush3DBorder(g, x, y, w, h);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    private static class ThinLoweredBorder extends AbstractBorder implements UIResource {
        private static final Insets INSETS = new Insets(2, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            SmoothGradientUtils.drawThinPressed3DBorder(g, x, y, w, h);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    /**
     * A border used for menu bars and tool bars in
     * <code>HeaderStyle.SINGLE</code>. The bar is wrapped by an inner thin
     * raised border, which in turn is wrapped by an outer thin lowered
     * border.
     */
    private static class EtchedBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(2, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            SmoothGradientUtils.drawThinPressed3DBorder(g, x, y, w, h);
            SmoothGradientUtils.drawThinFlush3DBorder  (g, x + 1, y + 1, w - 2, h - 2);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    /**
     * A border used for menu bars in <code>HeaderStyle.BOTH</code>.
     * The menu bar and tool bar are wrapped by a thin raised border,
     * both together are wrapped by a thin lowered border.
     */
    private static class MenuBarHeaderBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(2, 2, 1, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            SmoothGradientUtils.drawThinPressed3DBorder(g, x, y, w, h + 1);
            SmoothGradientUtils.drawThinFlush3DBorder  (g, x + 1, y + 1, w - 2, h - 1);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    /**
     * A border used for tool bars in <code>HeaderStyle.BOTH</code>.
     * The menu bar and tool bar are wrapped by a thin raised border,
     * both together are wrapped by a thin lowered border.
     */
    private static class ToolBarHeaderBorder extends AbstractBorder implements UIResource {
        
        private static final Insets INSETS = new Insets(1, 2, 2, 2);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            SmoothGradientUtils.drawThinPressed3DBorder(g, x, y - 1, w, h + 1);
            SmoothGradientUtils.drawThinFlush3DBorder  (g, x + 1, y, w - 2, h - 1);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    private static class MenuBorder extends AbstractBorder implements UIResource {
        private static final Insets INSETS = new Insets( 2, 2, 2, 2 );
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            JMenuItem b = (JMenuItem) c;
            ButtonModel model = b.getModel();
            
            if (model.isArmed() || model.isSelected()) {
                g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
                g.drawLine(0, 0, w - 2, 0 );
                g.drawLine(0, 0, 0, h - 1 );
                //g.drawLine(w - 2, 2, w - 2, h - 1 );
                
                g.setColor(SmoothGradientLookAndFeel.getPrimaryControlHighlight());
                g.drawLine(w - 1, 0, w - 1, h - 1 );
            } else if (model.isRollover()) {
                g.translate(x, y);
                SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
                g.translate(-x, -y);
            }
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
        
        public Insets getBorderInsets(Component c, Insets newInsets) {
            newInsets.top	 = INSETS.top;
            newInsets.left	 = INSETS.left;
            newInsets.bottom = INSETS.bottom;
            newInsets.right	 = INSETS.right;
            return newInsets;
        }
    }
    
    
    private static class PopupMenuBorder extends AbstractBorder implements UIResource {
        private static final Insets INSETS = new Insets(3, 3, 3, 3);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w-1, h-1);
            g.setColor(SmoothGradientLookAndFeel.getPrimaryControlHighlight());
            g.drawLine(1, 1, w-2, 1);
            g.drawLine(1, 1, 1, h-2);
            g.setColor(SmoothGradientLookAndFeel.getMenuBackground());
            g.drawRect(2, 2, w-5, h-5);
            g.translate(-x, -y);
        }
        
        public Insets getBorderInsets(Component c) { return INSETS; }
    }
    
    
    private static class RolloverButtonBorder extends ButtonBorder {
        private static final Insets INSETS_3 = new Insets(3, 3, 3, 3);
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();
            
            if (!model.isEnabled())
                return;
            
            if (!(c instanceof JToggleButton)) {
                if ( model.isRollover() && !( model.isPressed() && !model.isArmed() ) ) {
                    super.paintBorder( c, g, x, y, w, h );
                }
                return;
            }
            
            //if ( model.isRollover() && !( model.isPressed() && !model.isArmed() ) ) {
            //super.paintBorder( c, g, x, y, w, h );
            //}
            
            if (model.isRollover()) {
                if (model.isPressed() && model.isArmed()) {
                    SmoothGradientUtils.drawPressed3DBorder(g, x, y, w, h);
                } else {
                    SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
                }
            } else if (model.isSelected())
                SmoothGradientUtils.drawDark3DBorder(g, x, y, w, h);
        }
        public Insets getBorderInsets(Component c) { return INSETS_3; }
    }
    
    
    /**
     * Unlike Metal we don't paint the (misplaced) control color edges.
     * Being a subclass of MetalBorders.ScrollPaneBorders ensures that
     * the ScrollPaneUI will update the ScrollbarsFreeStanding property.
     */
    private static class ScrollPaneBorder extends MetalBorders.ScrollPaneBorder {
        
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            g.translate(x, y);
            
            g.setColor(SmoothGradientLookAndFeel.getControlDarkShadow());
            g.drawRect(0, 0, w - 2, h - 2);
            g.setColor(SmoothGradientLookAndFeel.getControlHighlight());
            g.drawLine(w - 1, 0, w - 1, h - 1);
            g.drawLine(0, h - 1, w - 1, h - 1);
            
            g.translate(-x, -y);
        }
    }
    
    
    private static class TextFieldBorder extends Flush3DBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            
            if (!(c instanceof JTextComponent)) {
                // special case for non-text components (bug ID 4144840)
                if (c.isEnabled()) {
                    SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
                } else {
                    SmoothGradientUtils.drawDisabledBorder(g, x, y, w, h);
                }
                return;
            }
            
            if (c.isEnabled() && ((JTextComponent) c).isEditable())
                SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
            else
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w, h);
        }
    }
    
    
    private static class ToggleButtonBorder extends ButtonBorder {
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            if (!c.isEnabled()) {
                SmoothGradientUtils.drawDisabledBorder(g, x, y, w - 1, h - 1);
            } else {
                AbstractButton button = (AbstractButton) c;
                ButtonModel    model  = button.getModel();
                if (model.isPressed() && model.isArmed())
                    SmoothGradientUtils.drawPressed3DBorder(g, x, y, w, h);
                else if (model.isSelected())
                    SmoothGradientUtils.drawDark3DBorder(g, x, y, w, h);
                else
                    SmoothGradientUtils.drawFlush3DBorder(g, x, y, w, h);
            }
        }
    }
    
    
}















