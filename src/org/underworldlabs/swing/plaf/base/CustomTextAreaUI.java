/*
 * CustomTextAreaUI.java
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

package org.underworldlabs.swing.plaf.base;

import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;

/**
 * An alternative UI delegate for JTextArea that paints the
 * selection highlight correctly.  For example, when a selection
 * starts on one line and ends on the next, the highlight on the
 * first line will extend all the way to the right margin.
 *
 * @author Alan Moore
 */
/**
 *
 * @author   Takis Diakoumis
 */
public class CustomTextAreaUI extends BasicTextAreaUI {
    /**
     * Informs the UIManager that this class should be used as the UI
     * delegate for JTextArea's.  This method should be called during
     * app initialization, before any components are created.
     */
    public static void initialize() {
        
        String key = "TextAreaUI";
        
        Class<CustomTextAreaUI> cls = CustomTextAreaUI.class;
        
        String name = cls.getName();
        
        UIManager.put(key, name);
        UIManager.put(name, cls);
    }
    
    /**
     * Creates a UI for a JTextArea.
     *
     * @param c a text area
     * @return a CustomTextAreaUI instance
     */
    public static ComponentUI createUI(JComponent c) {
        return new CustomTextAreaUI();
    }
    
    /**
     * Creates the object to use for adding highlights.  This will
     * be a non-layered version of DefaultHighlighter, so that
     * multi-line selections will be painted properly.
     *
     * @return the highlighter
     */
    protected Highlighter createHighlighter() {
        DefaultHighlighter h = new DefaultHighlighter();
        h.setDrawsLayeredHighlights(false);
        return h;
    }
    
    /**
     * Causes the portion of the view responsible for the given part
     * of the model to be repainted. This is overridden to repaint the
     * whole width of the textarea, so that selection highlighting will
     * be rendered properly.
     *
     * @param p0 the beginning of the range >= 0
     * @param p1 the end of the range >= p0
     */
    public void damageRange(JTextComponent t, int p0, int p1,
                            Position.Bias p0Bias, Position.Bias p1Bias) {
        View rv = getRootView(t);
        Rectangle alloc = getVisibleEditorRect();
        Document doc = t.getDocument();
        if (rv != null && alloc != null && doc != null) {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument)doc).readLock();
            }
            try {
                rv.setSize(alloc.width, alloc.height);
                Shape toDamage = rv.modelToView(p0, p0Bias, p1, p1Bias, alloc);
                Rectangle rect = (toDamage instanceof Rectangle)
                                    ? (Rectangle)toDamage
                                    : toDamage.getBounds();
                t.repaint(alloc.x, rect.y, alloc.width, rect.height);
            }
            catch (BadLocationException ex) {}
            finally {
                if (doc instanceof AbstractDocument) {
                    ((AbstractDocument)doc).readUnlock();
                }
            }
        }
    }
}


