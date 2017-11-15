/*
 * ErdLineStyleDialog.java
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.executequery.GUIUtilities;
import org.executequery.components.ColourChooserButton;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 */
public class ErdLineStyleDialog extends AbstractBaseDialog {
    
    /** The line weight combo box */
    private JComboBox weightCombo;
    
    /** The line style combo box */
    private JComboBox styleCombo;
    
    /** The arrow style combo box */
    private JComboBox arrowCombo;
    
    /** The colour selection button */
    private ColourChooserButton colourButton;
    
    /** The dependency panel where changes will occur */
    private ErdDependanciesPanel dependsPanel;
    
    /** <p>Creates a new instance with the specified values
     *  pre-selected within respective combo boxes.
     *
     *  @param the <code>ErdDependanciesPanel</code> where
     *         changes will occur
     *  @param the line weight
     *  @param the line style index to be selected:<br>
     *         0 - solid line
     *         1 - dotted line
     *         2 - dashed line
     *  @param the arrow index to be selected:<br>
     *         0 - filled arrow
     *         1 - outline arrow
     *  @param the line colour
     */
    public ErdLineStyleDialog(ErdDependanciesPanel dependsPanel) {
        
        super(GUIUtilities.getParentFrame(), "Line Style", true);
        
        this.dependsPanel = dependsPanel;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        determineWeightComboSelection(dependsPanel);
        
        styleCombo.setSelectedIndex(dependsPanel.getLineStyleIndex());
        arrowCombo.setSelectedIndex(dependsPanel.getArrowStyleIndex());
        
        pack();
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
        
    }

    private void determineWeightComboSelection(ErdDependanciesPanel dependsPanel) {
        
        float lineWeight = dependsPanel.getLineWeight();
        
        if (isFloatEqual(lineWeight, 0.5f)) {
           
            weightCombo.setSelectedIndex(0);
            
        } else if (isFloatEqual(lineWeight, 1.0f)) {
            
            weightCombo.setSelectedIndex(1);
            
        } else if (isFloatEqual(lineWeight, 1.5f)) {
            
            weightCombo.setSelectedIndex(2);
            
        } else if (isFloatEqual(lineWeight, 2.0f)) {
         
            weightCombo.setSelectedIndex(3);
        }
    }
    
    private void jbInit() throws Exception {
        LineStyleRenderer renderer = new LineStyleRenderer();
        
        LineWeightIcon[] weightIcons = {new LineWeightIcon(0),
        new LineWeightIcon(1),
        new LineWeightIcon(2),
        new LineWeightIcon(3)};
        weightCombo = WidgetFactory.createComboBox(weightIcons);
        weightCombo.setRenderer(renderer);
        
        LineStyleIcon[] styleIcons = {new LineStyleIcon(0),
        new LineStyleIcon(1),
        new LineStyleIcon(2)};
        styleCombo = WidgetFactory.createComboBox(styleIcons);
        styleCombo.setRenderer(renderer);
        
        ArrowStyleIcon[] arrowIcons = {new ArrowStyleIcon(0),
        new ArrowStyleIcon(1)};
        arrowCombo = WidgetFactory.createComboBox(arrowIcons);
        arrowCombo.setRenderer(renderer);
        
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        JButton okButton = new DefaultPanelButton(Bundles.get("common.ok.button"));
        
        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e); }
        };
        
        cancelButton.addActionListener(btnListener);
        okButton.addActionListener(btnListener);
        
        colourButton = new ColourChooserButton(dependsPanel.getLineColour());
        
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14,10,5,10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Line Style:"), gbc);
        gbc.gridwidth = 2;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = 10;
        gbc.weightx = 1.0;
        panel.add(styleCombo, gbc);
        gbc.insets.top = 0;
        gbc.gridy = 1;
        panel.add(weightCombo, gbc);
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx = 0;
        panel.add(new JLabel("Line Weight:"), gbc);
        gbc.gridy = 2;
        panel.add(new JLabel("Arrow Style:"), gbc);
        gbc.gridwidth = 2;
        gbc.insets.top = 0;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(arrowCombo, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(colourButton, gbc);
        gbc.insets.left = 10;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Line Colour:"), gbc);
        
        
        gbc.gridx = 1;
        gbc.insets.right = 5;
        gbc.ipadx = 25;
        gbc.insets.left = 143;
        gbc.insets.top = 5;
        gbc.insets.bottom = 10;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(okButton, gbc);
        gbc.ipadx = 0;
        gbc.insets.right = 10;
        gbc.insets.left = 0;
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(cancelButton, gbc);
        
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(450, 200));
        
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                                new Insets(7, 7, 7, 7), 0, 0));
        
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
    }
    
    /** <p>Performs the respective action upon selection
     *  of a button within this dialog.
     *
     *  @param the <code>ActionEvent</code>
     */
    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Cancel"))
            dispose();
        
        else if (command.equals("OK")) {
            int index = weightCombo.getSelectedIndex();
            float lineWeight = 0f;
            
            switch (index) {
                case 0:
                    lineWeight = 0.5f;
                    break;
                case 1:
                    lineWeight = 1.0f;
                    break;
                case 2:
                    lineWeight = 1.5f;
                    break;
                case 3:
                    lineWeight = 2.0f;
                    break;
            }
            
            dependsPanel.setLineWeight(lineWeight);
            dependsPanel.setArrowStyle(arrowCombo.getSelectedIndex() == 0 ? true : false);
            dependsPanel.setLineColour(colourButton.getColour());
            dependsPanel.setLineStyle(styleCombo.getSelectedIndex());
            dependsPanel.repaint();
            dispose();
        }
        
    }
    
    private boolean isFloatEqual(float value1, float value2) {
        
        return (Math.abs(value1 - value2) < .0000001);
    }
    
    
} // class

/** <p>Draws the available arrow styles as an
 *  <code>ImageIcon</code> to be added to the combo
 *  box through the renderer.
 */
class ArrowStyleIcon extends ImageIcon {
    
    private int type;
    
    public ArrowStyleIcon(int type) {
        super();
        this.type = type;
    }
    
    public int getIconWidth() {
        return 250;
    }
    
    public int getIconHeight() {
        return 20;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        
        // fill the background
        g.setColor(UIUtils.getColour("executequery.Erd.background", Color.WHITE));
        g.fillRect(0, 0, 290, 20);
        // draw the line
        g.setColor(Color.BLACK);
        g.drawLine(5, 10, 250, 10);
        
        int[] polyXs = {240, 250, 240};
        int[] polyYs = {16, 10, 4};
        
        switch (type) {
            case 0:
                g.fillPolygon(polyXs, polyYs, 3);
                break;
            case 1:
                g.drawPolyline(polyXs, polyYs, 3);
                break;
        }
        
    }
    
} // ArrowStyleIcon

class LineStyleRenderer extends JLabel
                        implements ListCellRenderer {
    
    private static final Color focusColour =
    UIManager.getColor("ComboBox.selectionBackground");
    
    public LineStyleRenderer() {
        super();
    }
    
    public Component getListCellRendererComponent(JList list, Object obj, int row,
    boolean sel, boolean hasFocus) {
        if (obj instanceof ImageIcon) {
            setIcon((ImageIcon)obj);
            
            if (sel)
                setBorder(BorderFactory.createLineBorder(focusColour, 2));
            else
                setBorder(null);
            
        } else
            setText("ERROR");
        
        return this;
        
    }
    
} // LineStyleRenderer


/** <p>Draws the available line weights as an
 *  <code>ImageIcon</code> to be added to the combo
 *  box through the renderer.
 */
class LineWeightIcon extends ImageIcon {
    
    private static final BasicStroke solidStroke_1 = new BasicStroke(0.5f);
    private static final BasicStroke solidStroke_2 = new BasicStroke(1.0f);
    private static final BasicStroke solidStroke_3 = new BasicStroke(1.5f);
    private static final BasicStroke solidStroke_4 = new BasicStroke(2.0f);
    
    private static final String HALF = "0.5";
    private static final String ONE = "1.0";
    private static final String ONE_FIVE = "1.5";
    private static final String TWO = "2.0";
    
    private int type;
    
    public LineWeightIcon(int type) {
        super();
        this.type = type;
    }
    
    public int getIconWidth() {
        return 250;
    }
    
    public int getIconHeight() {
        return 20;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        String text = null;
        
        switch (type) {
            case 0:
                g2d.setStroke(solidStroke_1);
                text = HALF;
                break;
            case 1:
                g2d.setStroke(solidStroke_2);
                text = ONE;
                break;
            case 2:
                g2d.setStroke(solidStroke_3);
                text = ONE_FIVE;
                break;
            case 3:
                g2d.setStroke(solidStroke_4);
                text = TWO;
                break;
        }
        
        // fill the background
        g2d.setColor(UIUtils.getColour("executequery.Erd.background", Color.WHITE));
        g2d.fillRect(0, 0, 290, 20);
        
        FontMetrics fm = g2d.getFontMetrics();
        
        // draw the line style
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 5, fm.getHeight());
        g2d.drawLine(30, 10, 250, 10);
    }
    
} // LineWeightIcon


/** <p>Draws the available line styles as an
 *  <code>ImageIcon</code> to be added to the combo
 *  box through the renderer.
 */
class LineStyleIcon extends ImageIcon {
    
    private static final BasicStroke solidStroke = new BasicStroke(1.0f);
    
    private static final float dash1[] = {2.0f};
    private static final BasicStroke dashedStroke_1 =
    new BasicStroke(1.0f, 0, 0, 10f, dash1, 0.0f);
    
    private static final float dash2[] = {5f, 2.0f};
    private static final BasicStroke dashedStroke_2 =
    new BasicStroke(1.0f, 0, 0, 10f, dash2, 0.0f);
    
    private int type;
    
    public LineStyleIcon(int type) {
        super();
        this.type = type;
    }
    
    public int getIconWidth() {
        return 250;
    }
    
    public int getIconHeight() {
        return 20;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D)g;
        
        switch (type) {
            case 0:
                g2d.setStroke(solidStroke);
                break;
            case 1:
                g2d.setStroke(dashedStroke_1);
                break;
            case 2:
                g2d.setStroke(dashedStroke_2);
                break;
        }
        
        // fill the background
        g2d.setColor(UIUtils.getColour("executequery.Erd.background", Color.WHITE));
        g2d.fillRect(0, 0, 290, 20);
        
        // draw the line style
        g2d.setColor(Color.BLACK);
        g2d.drawLine(5, 10, 250, 10);
    }
    
} // LineStyleIcon















