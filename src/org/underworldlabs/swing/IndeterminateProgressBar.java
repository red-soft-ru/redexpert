/*
 * IndeterminateProgressBar.java
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class IndeterminateProgressBar extends JComponent
                                      implements Runnable, ProgressBar {
    
    private Color scrollbarColour;
    
    private int scrollerWidth;
    private int animationOffset;
    
    private boolean inProgress;
    private boolean paintBorder;
    
    private boolean stopped;
    private boolean fillWhenStopped;
    
    private Timer timer;
    
    public IndeterminateProgressBar() {
        this(true);
    }
    
    public IndeterminateProgressBar(boolean paintBorder) {

        inProgress = false;
        scrollerWidth = 20;
        this.paintBorder = paintBorder;

        animationOffset = scrollerWidth * -1;
        Color foregroundColour = UIManager.getColor("ProgressBar.foreground");
        
        if (UIUtils.isNativeMacLookAndFeel()) {
            foregroundColour = UIManager.getColor("Focus.color");
        }
        
        setScrollbarColour(foregroundColour);
		
        createTimer();
    }

    private void createTimer() {
        timer = new Timer(25, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        timer.setInitialDelay(0);
    }
    
    public void run() {
        animationOffset++;
        repaint();
        if (animationOffset >= 0) {
            animationOffset = scrollerWidth * -1;
        }
    }
    
    public void stop() {
        if (hasTimer()) {
            timer.stop();
        }
        inProgress = false;
        stopped = true;
        repaint();
    }

    private boolean hasTimer() {
        return timer != null;
    }
    
    public void start() {
        if (!hasTimer()) {
            createTimer();
        }
        timer.start();
        inProgress = true;
        repaint();
    }
    
    public void cleanup() {
        if (hasTimer()) {
            timer.stop();
        }
        timer = null;
    }
    
    @Override
    public int getHeight() {

        return (int) Math.max(super.getHeight(), getPreferredSize().getHeight()); 
    }
    
    public void paintComponent(Graphics g) {
        
    	UIUtils.antialias(g);
    	
        int width = getWidth();
        int height = getHeight();

        int y1 = height - 2;
        int y4 = height - 3;
        
        if (paintBorder) {
        
            // draw the line border
            g.setColor(getScrollbarColour());
            g.drawRect(0, 0, width - 2, height - 2);
            width--;
        
        } else {
            
            // draw the default border
//            paintBorder(g);
//            y1 = height;
//            y4 = height - 1;
        }
        
        if (!inProgress) {

            if (stopped && fillWhenStopped) {
                
                g.setColor(getScrollbarColour());
                g.fillRect(0, 0, width, height);
            }
            
            return;
        }

        // set the polygon points
        int[] xPosns = {0, 0, 0, 0};
        int[] yPosns = {y1, 1, 1, y1};
        
        // constrain the clip
        //g.setClip(1, 1, width - 3, y4);
        g.setClip(0, 1, width, y4);
        
        g.setColor(getScrollbarColour());
        
        for (int i = 0, k = width + scrollerWidth; i < k; i += scrollerWidth) {
            
            xPosns[0] = i + animationOffset;
            xPosns[1] = xPosns[0] + (scrollerWidth / 2);
            xPosns[2] = xPosns[0] + scrollerWidth;
            xPosns[3] = xPosns[1];

            g.fillPolygon(xPosns, yPosns, 4);
            
        }
        
    }

    @Override
    public void fillWhenStopped() {
        this.fillWhenStopped = true;
    }

    public Color getScrollbarColour() {
        return scrollbarColour;
    }

    public void setScrollbarColour(Color scrollbarColour) {
        this.scrollbarColour = scrollbarColour;
    }
   
}


