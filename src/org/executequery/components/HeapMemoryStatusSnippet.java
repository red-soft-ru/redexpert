/*
 * HeapMemoryStatusSnippet.java
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

package org.executequery.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.MouseInputAdapter;

import org.executequery.GUIUtilities;
import org.executequery.log.Log;
import org.underworldlabs.swing.HeapMemoryDialog;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1569 $
 * @date     $Date: 2016-02-03 12:33:44 +1100 (Wed, 03 Feb 2016) $
 */
public class HeapMemoryStatusSnippet extends JPanel
                                     implements ActionListener {
    
    /** Timer for the heap memory display */
    private java.util.Timer progTimer;
    
    /** The progress bar */
    private JProgressBar memProgress;
    
    public HeapMemoryStatusSnippet() {
        super(new BorderLayout());
        
        try {
            jbInit();
        }
        
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void jbInit() throws Exception {
        ProgressModel progModel = new ProgressModel();
        memProgress = new JProgressBar(progModel);
        memProgress.addMouseListener(new ProgressMouseAdapter());
        
        JButton gcButton = new NoFocusButton("GcDelete16.png", "GcDeletePressed16.png");
        gcButton.addActionListener(this);

        memProgress.setBorder(null);
        memProgress.setBorderPainted(false);

        add(memProgress, BorderLayout.CENTER);
        add(gcButton, BorderLayout.EAST);
        
        startMeasure(progModel);
    }
    
    public void actionPerformed(ActionEvent e) {
        int total = (int)Runtime.getRuntime().totalMemory();
        int free = (int)Runtime.getRuntime().freeMemory();
        int totalUsedBefore = total - free;
        
        System.gc();
        
        total = (int)Runtime.getRuntime().totalMemory();
        free = (int)Runtime.getRuntime().freeMemory();
        int totalUserAfter = total - free;
        
        Log.info("Garbage collection released " +
            ((totalUsedBefore - totalUserAfter) / 1000) + "Kb.");
    }
    
    private void startMeasure(final ProgressModel progModel) {
        
        memProgress.setStringPainted(true);
        memProgress.setFont(new Font("dialog", 0, 10));
        final String mb_slash = "Mb/";
        final String mb = "Mb";
        final int million = 1000000;
        
        final Runnable showProgress = new Runnable() {
            public void run() {
                int total = (int)Runtime.getRuntime().totalMemory();
                int free = (int)Runtime.getRuntime().freeMemory();
                int used = total - free;
                progModel.setMaximum(total);
                progModel.setValue(used);
                memProgress.setString((used/million) + mb_slash + (total/million) + mb);
            }
        };
        
        java.util.TimerTask updateProgress = new java.util.TimerTask() {
            public void run() {
                java.awt.EventQueue.invokeLater(showProgress);
            }
        };
        
        progTimer = new java.util.Timer();
        progTimer.schedule(updateProgress, 0, 2000);
        System.gc();
    }
    
    class ProgressMouseAdapter extends MouseInputAdapter {
        
        public ProgressMouseAdapter() {}
        
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                new HeapMemoryDialog(GUIUtilities.getParentFrame());
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            int total = (int)Runtime.getRuntime().totalMemory();
            int free = (int)Runtime.getRuntime().freeMemory();
            int used = total - free;
            String text = (used/1000) + "Kb/" + (total/1000) + "Kb";
            memProgress.setToolTipText("Java heap size: " + text);
        }
        
    } // class ProgressMouseAdapter
    
    static class ProgressModel extends DefaultBoundedRangeModel {
        
        private int max;

        public ProgressModel() {}
        
        public int getMaximum() {
            return max;
        }
        
        public int getMinimum() {
            return 0;
        }
        
        public int getValue() {
            return getMaximum() - (int)Runtime.getRuntime().freeMemory();
        }
        
        public void setMaximum(int i) {
            max = i;
        }
        
        public void setValue(int i) {
            fireStateChanged();
        }
        
    } // ProgressModel

}




