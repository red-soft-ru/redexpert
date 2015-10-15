/*
 * HeapMemoryPanel.java
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1506 $
 * @date     $Date: 2015-09-24 08:34:32 +1000 (Thu, 24 Sep 2015) $
 */
public class HeapMemoryPanel extends JPanel 
                             implements ActionListener {
    
    /** timer object for heap display */
    private java.util.Timer progTimer;
    
    /** progress bar model */
    private ProgressModel progressBarModel;
    
    /** the progress bar */
    private JProgressBar progressBar;
    
    /** Indicates the timer has started */
    private boolean timerStarted;
    
    public HeapMemoryPanel() {
        super(new GridBagLayout());
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void jbInit() {
        JPanel base = new JPanel(new GridBagLayout());
        
        JLabel line1 = new JLabel("Measures the size of the");
        JLabel line2 = new JLabel("Java Virtual Machine\'s object heap.");
        
        progressBarModel = new ProgressModel();
        progressBar = new JProgressBar(progressBarModel);
        progressBar.setPreferredSize(new Dimension(265, 25));
        progressBar.setBorderPainted(false);
        
        JPanel progressBarPanel = new JPanel(new BorderLayout());
        progressBarPanel.add(progressBar, BorderLayout.CENTER);
        progressBarPanel.setBorder(UIUtils.getDefaultLineBorder());
        
        JButton gcButton = new JButton("Run Garbage Collector");
        gcButton.addActionListener(this);
        
        base.setBorder(BorderFactory.createEtchedBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        Insets ins = new Insets(0,5,3,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = ins;
        base.add(line1, gbc);
        gbc.insets = ins;
        gbc.gridy++;
        base.add(line2, gbc);
        gbc.gridy++;
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(progressBarPanel, gbc);
        gbc.gridy++;
        gbc.insets.top = 5;
        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.insets.bottom = 0;
        gbc.ipady = 5;
        gbc.fill = GridBagConstraints.NONE;
        base.add(gcButton, gbc);
        
        startMeasure(progressBarModel, progressBar);
        
        setPreferredSize(new Dimension(339, 168));
        add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                         GridBagConstraints.SOUTHEAST,
                                         GridBagConstraints.BOTH,
                                         new Insets(3, 3, 3, 3), 0, 0));
    }

    public void actionPerformed(ActionEvent e) {
        int total = (int)Runtime.getRuntime().totalMemory();
        int free = (int)Runtime.getRuntime().freeMemory();
        int totalUsedBefore = total - free;
        
        System.gc();
        
        total = (int)Runtime.getRuntime().totalMemory();
        free = (int)Runtime.getRuntime().freeMemory();
        int totalUserAfter = total - free;
        
        System.err.println("Garbage collection realeased " +
            ((totalUsedBefore - totalUserAfter) / 1000) + "Kb.");
    }
    
    /**
     * Stops the timer controlling the heap bar.
     */
    public void stopTimer() {
        if (progTimer != null) {
            progTimer.cancel();
        }
        timerStarted = false;
        progTimer = null;
    }

    /**
     * Starts the timer controlling the heap bar.
     */
    public void startTimer() {
        if (!timerStarted) {
            if (progTimer != null) {
                startMeasure(progressBarModel, progressBar);
            }
        }
    }

    private void startMeasure(final ProgressModel progModel,
                              final JProgressBar memProgress) {
        memProgress.setStringPainted(true);
        final String used_s = " Kb used,  ";
        final String total_s = " Kb total";
        final int thou = 1000;
        
        final Runnable showProgress = new Runnable() {
            public void run() {
                int total = (int)Runtime.getRuntime().totalMemory();
                int free = (int)Runtime.getRuntime().freeMemory();
                int used = total - free;
                progModel.setMaximum(total);
                progModel.setValue(used);
                memProgress.setString((used/thou) + used_s + 
                                      (total/thou) + total_s);
            }
        };
        
        java.util.TimerTask updateProgress = new java.util.TimerTask() {
            public void run() {
                java.awt.EventQueue.invokeLater(showProgress);
            }
        };
        progTimer = new java.util.Timer();
        progTimer.schedule(updateProgress, 0, 1500);
        timerStarted = true;
    }
    
    
    @SuppressWarnings("unused")
    static class ProgressModel extends DefaultBoundedRangeModel {
        
        private int max;
        private int min;
        private int value;
        
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
        
        public void setMinimum(int i) {
            min = 0;
        }
        
        public void setValue(int i) {
            value = i;
            fireStateChanged();
        }
        
    } // ProgressModel
    
}






