/*
 * WizardProcessPanel.java
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

package org.underworldlabs.swing.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Base wizard process panel.
 *
 * @author   Takis Diakoumis
 */
public abstract class WizardProcessPanel extends JPanel 
                                         implements ActionListener {
    
    /** the selection model */
    private WizardProcessModel model;
    
    /** the left panel */
    private JPanel leftPanel;
    
    /** the right panel */
    private JPanel rightPanel;
    
    /** the right panel layout */
    private CardLayout cardLayout;
    
    /** the next button */
    private JButton nextButton;

    /** the previous button */
    private JButton backButton;

    /** the cancel button */
    private JButton cancelButton;

    /** the help button */
    protected JButton helpButton;

    /** the title label */
    private JLabel titleLabel;
    
    /** the step label list */
    private List<JLabel> stepLabels;
    
    /** the normal label font */
    private Font labelFont;

    /** the selected label font */
    private Font selectedLabelFont;

    /** whether buttons are enabled */
    private boolean buttonsEnabled;
    
    /** Creates a new instance of WizardProcessPanel */
    public WizardProcessPanel() {
        this(null);
    }
    
    /** Creates a new instance of WizardProcessPanel */
    public WizardProcessPanel(WizardProcessModel model) {
        super(new BorderLayout());
        this.model = model;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {

        buttonsEnabled = true;

        Border labelBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.getDefaultBorderColour()),
                BorderFactory.createEmptyBorder(0, 0, 4, 0));
        
        // setup the title label and right panel
        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);

        titleLabel = new JLabel("", JLabel.LEFT);
        titleLabel.setBorder(labelBorder);
        
        // store the fonts
        Font font = titleLabel.getFont();
        selectedLabelFont = font.deriveFont(Font.BOLD);
        labelFont = font.deriveFont(Font.PLAIN);

        titleLabel.setFont(selectedLabelFont);
        
        JPanel rightContentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.insets.bottom = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        rightContentPanel.add(titleLabel, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        rightContentPanel.add(rightPanel, gbc);
        
        // setup the left panel
        JLabel stepsLabel = new JLabel("Steps", JLabel.LEFT);
        stepsLabel.setOpaque(false);
        stepsLabel.setBorder(labelBorder);
        stepsLabel.setFont(selectedLabelFont);
        
        leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(170, getHeight()));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel leftContentPanel = new StepListPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.insets.bottom = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftContentPanel.add(stepsLabel, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        leftContentPanel.add(leftPanel, gbc);
        
        // add the panels to the base
        JPanel base = new JPanel(new BorderLayout());
        base.add(leftContentPanel, BorderLayout.WEST);
        base.add(rightContentPanel, BorderLayout.CENTER);
        add(base, BorderLayout.CENTER);

        // setup the button panel
        nextButton = new WizardPanelButton("Next");
        nextButton.setMnemonic('N');

        backButton = new WizardPanelButton("Back");
        backButton.setMnemonic('B');

        cancelButton = new WizardPanelButton("Cancel");
        cancelButton.setMnemonic('C');

        nextButton.addActionListener(this);
        backButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        helpButton = new WizardPanelButton("Help");
        helpButton.setMnemonic('H');

//        Dimension btnDim = new Dimension(75,25);
//        nextButton.setPreferredSize(btnDim);
//        backButton.setPreferredSize(btnDim);
//        cancelButton.setPreferredSize(btnDim);
//        helpButton.setPreferredSize(btnDim);
//        
//        Insets buttonInsets = new Insets(2, 2, 2, 2);
//        nextButton.setMargin(buttonInsets);
//        backButton.setMargin(buttonInsets);
//        cancelButton.setMargin(buttonInsets);
        
        backButton.setEnabled(false);
        
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        gbc.insets.top = 7;
        gbc.insets.bottom = 5;
        gbc.insets.right = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weighty = 0;
        gbc.weightx = 0.5;
        buttonPanel.add(helpButton, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.left = 7;
        buttonPanel.add(backButton, gbc);
        gbc.gridx = 2;
        buttonPanel.add(nextButton, gbc);
        gbc.gridx = 3;
        gbc.insets.right = 5;
        buttonPanel.add(cancelButton, gbc);

        // add a border to the button panel
        buttonPanel.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, 
                        UIManager.getColor("controlDkShadow")));
        add(buttonPanel, BorderLayout.SOUTH);
        
    }

    protected void prepare() {
        
        // setup the step labels
        String[] steps = model.getSteps();
        stepLabels = new ArrayList<JLabel>(steps.length);
        for (int i = 0; i < steps.length; i++) {
            JLabel label = new WizardStepLabel(i + 1, steps[i]);
            label.setFont(labelFont);
            leftPanel.add(label);
            stepLabels.add(label);
        }

        // prepare the first panel
        setTitleLabelText(model.getTitle(0));
        rightPanel.add(model.getPanelAt(0), String.valueOf(0));
        cardLayout.first(rightPanel);
        setSelectedStep(0);
    }
    
    /**
     * Reformats the label display at the specified index.
     *
     * @param index - the label index
     */
    protected void setSelectedStep(int index) {
        for (int i = 0, n = stepLabels.size(); i < n; i++) {
            JLabel label = stepLabels.get(i);
            if (i != index) {
                label.setFont(labelFont);
            } else {
                label.setFont(selectedLabelFont);
            }
        }
    }
    
    /**
     * Sets the action for the help button to that specified.
     *
     * @param a - the help action to be applied
     */
    protected void setHelpAction(Action a) {
        helpButton.setAction(a);
        helpButton.setIcon(null);
        helpButton.setText("Help");
    }

    /**
     * Sets the action for the help button to that specified with
     * the specified action command string.
     * This will also set a null icon value and set the button
     * text to 'Help'.
     *
     * @param a - the help action to be applied
     * @param actionCommand - the action command string
     */
    protected void setHelpAction(Action a, String actionCommand) {
        helpButton.setAction(a);
        helpButton.setIcon(null);
        helpButton.setText("Help");
        helpButton.setActionCommand(actionCommand);
    }

    /**
     * Sets the title label text to that specified.
     *
     * @param text - the title text
     */
    protected void setTitleLabelText(String text) {
        titleLabel.setText(text);
        Dimension dim = titleLabel.getSize();
        titleLabel.paintImmediately(0, 0, dim.width, dim.height);
    }

    /**
     * Returns whether the buttons are enabled.
     * If the buttons are not set enabled, any changes
     * to them using setEnabled(..) is ignored.
     *
     * @return true | false
     */
    public boolean isButtonsEnabled() {
        return buttonsEnabled;
    }

    /**
     * Sets the buttons to be enabled.
     *
     * @param buttonsEnabled - true | false
     */
    public void setButtonsEnabled(boolean buttonsEnabled) {
        this.buttonsEnabled = buttonsEnabled;
    }

    /**
     * Sets the text label on the next button to that specified.
     *
     * @param text - the 'NEXT' button text
     */
    public void setNextButtonText(String text) {
        nextButton.setText(text);
    }

    /**
     * Sets the text label on the back (previous) button to that specified.
     *
     * @param text - the 'BACK' button text
     */
    public void setBackButtonText(String text) {
        backButton.setText(text);
    }

    /**
     * Sets the text label on the cancel button to that specified.
     *
     * @param text - the 'CANCEL' button text
     */
    public void setCancelButtonText(String text) {
        cancelButton.setText(text);
    }

    /**
     * Enables/disables the next button.
     *
     * @param true | false
     */
    public void setNextButtonEnabled(boolean enable) {
        if (buttonsEnabled) {
            nextButton.setEnabled(enable);
        }
    }

    /**
     * Enables/disables the next button.
     *
     * @param true | false
     */
    public void setBackButtonEnabled(boolean enable) {
        if (buttonsEnabled) {
            backButton.setEnabled(enable);
        }
    }

    /**
     * Enables/disables the next button.
     *
     * @param true | false
     */
    public void setCancelButtonEnabled(boolean enable) {
        if (buttonsEnabled) {
            cancelButton.setEnabled(enable);
        }
    }

    /**
     * Returns the currently selected index.
     *
     * @return the current index
     */
    public int getSelectedIndex() {
        return model.getSelectedIndex();
    }
    

    /**
     * Adds the specified panel to the layout with the specified name.
     *
     * The name must be the string value of the index of the specified panel.
     *
     * @param panel - the panel
     * @param title - the layout name
     */
    public void addPanel(JPanel panel, String title) {
        rightPanel.add(panel, title);
    }

    /**
     * Enables/diables buttons based on the current selected index.
     */
    protected void resetButtons() {
        setNextButtonEnabled(model.hasNext());
        setBackButtonEnabled(model.hasPrevious());
    }
    
    /**
     * Performs the cancel action.
     */
    public abstract void cancel();

    /**
     * Performs the action on the selection on next.
     */
    protected void next() {
        if (model.hasNext() && model.next()) {
            int index = model.getSelectedIndex();
            String layoutName = String.valueOf(index);
            rightPanel.add(model.getPanelAt(index), layoutName);
            setTitleLabelText(model.getTitle(index));
            cardLayout.show(rightPanel, layoutName);
            setSelectedStep(index);
            resetButtons();
        }
    }

    /**
     * Performs the action on the selection on back.
     */
    protected void back() {
        if (model.hasPrevious() && model.previous()) {
            int index = model.getSelectedIndex();
            String layoutName = String.valueOf(index);
            setTitleLabelText(model.getTitle(index));
            cardLayout.show(rightPanel, layoutName);
            setSelectedStep(index);
            resetButtons();
        }
    }

    /**
     * Executes the actions associated with the button selctions.
     *
     * @param e - the originating event
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == nextButton) {
            next();
        }
        else if (source == backButton) {
            back();
        } 
        else if (source == cancelButton) {
            cancel();
        }
    }
    
    /**
     * Returns the wizard model for this instance.
     *
     * @return the model
     */
    public WizardProcessModel getModel() {
        return model;
    }

    /**
     * Sets the wizard model to that specified.
     *
     * @param model - the model to be used
     */
    public void setModel(WizardProcessModel model) {
        this.model = model;
    }
    
    // the steps left hand panel
    private class StepListPanel extends JPanel {
        
        private Color darkColor;
        private Color lightColor;
        
        public StepListPanel(LayoutManager layout) {
            super(layout);
            darkColor = UIUtils.getDefaultActiveBackgroundColour();
            lightColor = getBackground();
        }
        
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;
            /*
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            */

            super.paintComponent(g);

            int width = getWidth();
            int height = getHeight();

            Paint originalPaint = g2.getPaint();
            GradientPaint fade = new GradientPaint(0, height, darkColor,
                    0, (int)(height * 0.2), lightColor);

            g2.setPaint(fade);
            g2.fillRect(0,0, width, height);

            g2.setPaint(originalPaint);
        }

        public boolean isOpaque() {
            return false;
        }

    }
    
    private class WizardStepLabel extends JLabel {
        
        public WizardStepLabel(int index, String text) {
            StringBuffer sb = new StringBuffer();
            sb.append("<html><table border=\"0\" cellpadding=\"2\"><tr><td valign=\"top\" nowrap>");
            sb.append(index);
            sb.append(".</td><td>");
            sb.append(text.replaceAll("\n", "<br>"));
            sb.append("</td></tr></table></html>");
            setText(sb.toString());
        }

        public boolean isOpaque() {
            return false;
        }
        
    }
    
}















