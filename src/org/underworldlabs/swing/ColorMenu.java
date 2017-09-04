/*
 * ColorMenu.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

class ColorMenu extends JMenu implements ActionListener {
    
    public static final int DESKTOP_BG = 0;
    
    protected Border unselectedBorder;
    protected Border selectedBorder;
    protected Border activeBorder;
    
    protected Hashtable panes;
    protected ColourPane selectedColor;
    
    private int type;
    
    private JPanel colourPanel;
    
    private Color bgColor;
    
    private Frame menuOwner;
    
    public ColorMenu(Frame menuOwner, Color selectedColour, String name, int type) {
        super(name);
        this.type = type;
        this.menuOwner = menuOwner;
        
        bgColor = selectedColour;
        unselectedBorder = new MatteBorder(0, 0, 1, 1, getBackground());

        selectedBorder = new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.red),
                                            new MatteBorder(1, 1, 1, 1, getBackground()));
        
        activeBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, Color.blue),
                                          new MatteBorder(1, 1, 1, 1, getBackground()));
        
        initialiseColourPanel();
        
        JButton customButton = new JButton("Custom");
        customButton.setPreferredSize(new Dimension(50,25));
        
        customButton.addActionListener(this);
        
        JPanel base = new JPanel(new BorderLayout());
        base.setBorder(new EmptyBorder(2, 2, 2, 2));
        base.add(colourPanel, BorderLayout.NORTH);
        base.add(customButton, BorderLayout.SOUTH);
        
        add(base);
        
    }
    
    private void initialiseColourPanel() {
        colourPanel = new JPanel();
        colourPanel.setLayout(new GridLayout(5, 5));
        panes = new Hashtable();
        
        int[] values = new int[] {0, 153, 192, 204, 255};
        
        for (int r = 0; r < values.length; r++)
            for (int g = 0; g < values.length; g++)
                for (int b = 0; b < values.length; b++) {
                    
                    if (g%2 == 0)
                        continue;
                    
                    Color c = new Color(values[r], values[g], values[b]);
                    ColourPane pn = new ColourPane(this, c);
                    colourPanel.add(pn);
                    panes.put(c, pn);
                    
                }
        
        // add Color.WHITE
        Color c = new Color(255,255,255);
        ColourPane pn = new ColourPane(this, c);
        colourPanel.add(pn);
        panes.put(c, pn);
        
        setColor(bgColor, false);
        
    }
    
    private void addColourPanel(ColourPane colourPane) {
        colourPanel.add(colourPane);
        panes.put(colourPane.getColor(), colourPane);
    }
    
    public void actionPerformed(ActionEvent e) {
        MenuSelectionManager.defaultManager().clearSelectedPath();
        Color c = JColorChooser.showDialog(menuOwner,
                                           "Select Background", bgColor);
        
        if (c == null) {
            return;
        }        
        setColor(c, true);
    }
    
    public void setColor(Color c, boolean reset) {
        Object obj = panes.get(c);
        
        if (obj == null) {
            obj = new ColourPane(this, c);
            colourPanel.add((ColourPane)obj);
            panes.put(c, obj);
        }
        
        if (selectedColor != null) {
            selectedColor.setSelected(false);
        }

        selectedColor = (ColourPane)obj;
        selectedColor.setSelected(true);
    }
    
    public Color getColor() {
        
        if (selectedColor == null)
            return null;
        
        return selectedColor.getColor();
    }
    
    public void doSelection() {
        fireActionPerformed(new ActionEvent(this,
        ActionEvent.ACTION_PERFORMED, getActionCommand()));
    }
    
}

class ColourPane extends JComponent implements MouseListener {
    
   
/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */
 /** The colour menu */
    private ColorMenu colorMenu;
    /** This pane's colour */
    protected Color colour;
    /** Whether the pane is selected */
    protected boolean selectedColour;
    /** The pane's size as a <code>Dimension</code> object */
    protected static final Dimension dim = new Dimension(18, 18);
    
    /** <p>Creates a new instance of this object
     *  with the specified colour.
     *
     *  @param the color of this panel
     */
    public ColourPane(ColorMenu colorMenu, Color c) {
        colour = c;
        setBorder(colorMenu.unselectedBorder);
        String msg = "R " + colour.getRed()+ ", G " +
        colour.getGreen() + ", B " + colour.getBlue();
        setToolTipText(msg);
        this.colorMenu = colorMenu;
        addMouseListener(this);
    }
    
    public ColourPane(ColorMenu colorMenu, int red, int green, int blue) {
        this(colorMenu, new Color(red, green, blue));
    }
    
    public void paintComponent(Graphics g) {
        g.setColor(colour);
        g.fillRect(0, 0, dim.width, dim.height);
    }
    
    public Color getColor() {
        return colour;
    }
    
    public boolean isOpaque() {
        return true;
    }
    
    public Dimension getPreferredSize() {
        return dim;
    }
    
    public Dimension getMaximumSize() {
        return dim;
    }
    
    public Dimension getMinimumSize() {
        return dim;
    }
    
    public void setSelected(boolean selected) {
        selectedColour = selected;
        
        if (selectedColour)
            setBorder(colorMenu.selectedBorder);
        else
            setBorder(colorMenu.unselectedBorder);
    }
    
    public boolean isSelected() {
        return selectedColour;
    }
    
    public void mouseReleased(MouseEvent e) {
        colorMenu.setColor(colour, true);
        MenuSelectionManager.defaultManager().clearSelectedPath();
        colorMenu.doSelection();
    }
    
    public void mouseEntered(MouseEvent e) {
        setBorder(colorMenu.activeBorder);
    }
    
    public void mouseExited(MouseEvent e) {
        setBorder(selectedColour ? colorMenu.selectedBorder : colorMenu.unselectedBorder);
    }
    
    public void mousePressed(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    
} // ColourPane














