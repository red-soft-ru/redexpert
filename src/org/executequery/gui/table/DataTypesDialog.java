/*
 * DataTypesDialog.java
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

package org.executequery.gui.table;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultList;
import org.underworldlabs.swing.AbstractBaseDialog;

/**
 * Simple data type selection dialog.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DataTypesDialog extends AbstractBaseDialog 
                             implements MouseListener,
                                        KeyListener {
    
    public static final String TITLE = "Data Type Selection";
    
    /** the type values */
    private String[] values;
    
    /** the list object */
    private JList list;
    
    /** the parent listener */
    private DataTypeSelectionListener parent;
    
    /** Creates a new instance of DataTypesPanel */
    public DataTypesDialog(Frame owner, DataTypeSelectionListener parent, String[] values) {
        super(owner, TITLE, true);
        this.parent = parent;
        this.values = values;
        init();
    }

    /** Creates a new instance of DataTypesPanel */
    public DataTypesDialog(Dialog owner, DataTypeSelectionListener parent, String[] values) {
        super(owner, TITLE, true);
        this.parent = parent;
        this.values = values;
        init();
    }

    private void init() {

        list = new DefaultList(values);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(this);
        list.addKeyListener(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(300, 450));

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.add(panel, BorderLayout.CENTER);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        display();
    }

    public void dispose() {
        if (!selected) {
            parent.dataTypeSelectionCancelled();
        }
        super.dispose();
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            selectionMade();
        }
    }

    public void keyReleased(KeyEvent e) {
        keyPressed(e);
    }

    public void keyTyped(KeyEvent e) {}

    private void selectionMade() {
        if (selected) {
            return;
        }
        Object value = list.getSelectedValue();
        parent.dataTypeSelected(value.toString());
        selected = true;
        dispose();
    }
    
    /** Indicates a selection has been made */
    private boolean selected;
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() < 2) {
            return;
        }
        selectionMade();
    }

    public void mousePressed(MouseEvent e) {
        mouseClicked(e);
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    private void display() {
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        GUIUtilities.registerDialog(this);
        setVisible(true);
    }
    
}





