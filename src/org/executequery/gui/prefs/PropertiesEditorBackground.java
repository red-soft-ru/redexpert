/*
 * PropertiesEditorBackground.java
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

package org.executequery.gui.prefs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.underworldlabs.util.SystemProperties;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.executequery.Constants;
import org.executequery.GUIUtilities;

/**
 *
 * @author   Takis Diakoumis
 */
public class PropertiesEditorBackground extends AbstractPropertiesBasePanel 
                                        implements PropertyChangeListener {

    private SimplePreferencesPanel preferencesPanel;
    
    private SamplePanel samplePanel;
    
    public PropertiesEditorBackground() {       
        try {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** <p>Initializes the state of this instance. */
    private void init() throws Exception {
    
    	List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                    UserPreference.CATEGORY_TYPE,
                    null,
                    "Query Editor Colours",
                    null));

        String key = "editor.caret.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Caret colour",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.linenumber.background";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Gutter background",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.linenumber.foreground";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Gutter foreground",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.text.background.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Editor background",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.results.background.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Results panel background",
                    SystemProperties.getColourProperty("user", key)));
        
        key = "editor.text.selection.foreground";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Text selection foreground",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.text.selection.background";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Text selection background",
                    SystemProperties.getColourProperty("user", key)));

        key = "editor.display.linehighlight.colour";
        list.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    key,
                    "Current Line Highlight",
                    SystemProperties.getColourProperty("user", key)));

        UserPreference[] preferences = 
                (UserPreference[])list.toArray(new UserPreference[list.size()]);

        preferencesPanel = new SimplePreferencesPanel(preferences);
        preferencesPanel.addPropertyChangeListener(this);

        samplePanel = new SamplePanel(preferences);
        samplePanel.setPreferredSize(new Dimension(365, 80));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 0.8;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(preferencesPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Sample Text:"), gbc);
        gbc.gridy++;
        gbc.insets.top = 5;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(samplePanel, gbc);
        addContent(panel);        
    }
    
    public void stopCaretDisplayTimer() {
        
        if (samplePanel != null) {
            samplePanel.stopTimer();
        }
        
    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }
    
    public void save() {
        preferencesPanel.savePreferences();
        stopCaretDisplayTimer();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == Constants.COLOUR_PREFERENCE) {
            samplePanel.repaint();
        }
    }

    private static final String TEXT_PLAIN = "Plain Text";
    private static final String TEXT_HIGHLIGHT = "Current Line Highlight";
    private static final String TEXT_NO_HIGHLIGHT = "Selected Text";
    private static final String ONE = "1";
    private static final String TWO = "2";
    private static final String THREE = "3";
    private static final Font FONT = new Font("monospaced", Font.PLAIN, 12);

    class SamplePanel extends JPanel {

        private Timer timer;
        
        private boolean showCaret;

        private UserPreference[] preferences;
        
        public SamplePanel(UserPreference[] preferences) {
            this.preferences = preferences;
            showCaret = false;

            final Runnable caret = new Runnable() {
                public void run() {
                    showCaret = !showCaret;
                    repaint();
                }
            };
            
            TimerTask caretTimer = new TimerTask() {
                public void run() {
                    EventQueue.invokeLater(caret);
                }
            };
            
            timer = new Timer();
            timer.schedule(caretTimer, 0, 500);
            
        }
        
        public void stopTimer() {

            if (timer != null) {

                timer.cancel();
            }
        }
        
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int width = getWidth();
            int height = getHeight();
            int lineGutter = 35;
            
            g.setFont(FONT);
            
            FontMetrics fm = g.getFontMetrics(FONT);
            int lineHeight = fm.getHeight() + 5;
            
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, 0, width - 1, height - 1);

            Color color = (Color)preferences[2].getValue();
            g.setColor(color);
            g.fillRect(1, 1, lineGutter, height - 2);
            
            g.setColor(GUIUtilities.getDefaultBorderColour());
            g.drawLine(lineGutter + 1, 1, lineGutter + 1, height - 2);
            
            color = (Color)preferences[3].getValue();
            g.setColor(color);

            g.drawString(ONE, lineGutter - 10, 15);
            g.drawString(TWO, lineGutter - 10, lineHeight + 15);
            g.drawString(THREE, lineGutter - 10, (lineHeight * 2) + 15);

            color = (Color)preferences[4].getValue();
            g.setColor(color);
            g.fillRect(lineGutter + 1, 1, width - lineGutter - 1, height - 2);
            
            g.setColor(Color.BLACK);
            g.drawString(TEXT_PLAIN, lineGutter + 5, 15);
            
            color = (Color)preferences[7].getValue();
            g.setColor(color);
            g.fillRect(lineGutter, lineHeight, width - lineGutter - 1, lineHeight);
            
            color = (Color)preferences[6].getValue();
            g.setColor(color);
            g.drawString(TEXT_NO_HIGHLIGHT, lineGutter + 5, lineHeight + 15);
            
            color = (Color)preferences[8].getValue();
            g.setColor(color);
            g.fillRect(lineGutter, lineHeight * 2, width - lineGutter - 1, lineHeight);
            
            g.setColor(Color.BLACK);
            g.drawString(TEXT_HIGHLIGHT, lineGutter + 5, (lineHeight * 2) + 15);
            
            if (showCaret) {
                color = (Color)preferences[1].getValue();
                g.setColor(color);
                
                int length_1 = fm.stringWidth(TEXT_PLAIN) + lineGutter + 4;
                int length_2 = fm.stringWidth(TEXT_NO_HIGHLIGHT) + lineGutter + 4;
                int length_3 = fm.stringWidth(TEXT_HIGHLIGHT) + lineGutter + 4;
                
                g.drawLine(length_1, 2, length_1, lineHeight - 2);
                g.drawLine(length_2, lineHeight + 2, length_2, (lineHeight * 2) - 2);
                g.drawLine(length_3, (lineHeight * 2) + 1, length_3, (lineHeight * 3) - 2);
            }
            
        }
        
    } // SamplePanel
    
}











