/*
 * DefaultWizardProcessModel.java
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

package org.underworldlabs.swing.wizard;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * The default model for a wizard process panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultWizardProcessModel implements WizardProcessModel {
    
    /** the current selected index */
    private int selectedIndex;
    
    /** the panels collection */
    private List<JPanel> panels;

    /** the steps descriptions */
    private String[] steps;
    
    /** the panel titles */
    private String[] titles;
    
    /** Creates a new instance of WizardProcessModel */
    public DefaultWizardProcessModel() {}
    
    /** Creates a new instance of WizardProcessModel */
    public DefaultWizardProcessModel(List<JPanel> panels) {
        this(panels, null, null);
    }

    /** Creates a new instance of WizardProcessModel */
    public DefaultWizardProcessModel(List<JPanel> panels, String[] steps, String[] titles) {
        this.panels = panels;
        this.steps = steps;
        this.titles = titles;
    }

    /**
     * Returns the descriptive values for the wizard steps.
     *
     * @return the descriptive steps
     */
    public String[] getSteps() {
        return steps;
    }

    /**
     * Returns the titles for each panel.
     *
     * @return the panel titles
     */
    public String[] getTitles() {
        return titles;
    }

    /**
     * Returns the step text at the specified index.
     *
     * @param index - the index
     */
    public String getStep(int index) {
        if (getSteps() == null) {
            return null;
        }
        return getSteps()[index];
    }

    /**
     * Returns the title text at the specified index.
     *
     * @param index - the index
     */
    public String getTitle(int index) {
        if (getTitles() == null) {
            return null;
        }
        return getTitles()[index];
    }

    /**
     * Increments the current index.
     */
    public boolean next() {
        selectedIndex++;
        return true;
    }
    
    /**
     * Decrements the current index.
     */
    public boolean previous() {
        selectedIndex--;
        return true;
    }
    
    /**
     * Returns the next panel in the wizard and increments the 
     * selected index.
     *
     * @return the next panel
     */
    public JPanel getNextPanel() {
        if (hasNext()) {
            selectedIndex++;
            return panels.get(selectedIndex);
        }
        return null;
    }

    /**
     * Returns the previous panel in the wizard and decrements the 
     * selected index.
     *
     * @return the previous panel
     */
    public JPanel getPreviousPanel() {
        if (hasPrevious()) {
            selectedIndex--;
            return panels.get(selectedIndex);
        }
        return null;        
    }

    /**
     * Returns whether there is a valid panel to be selected next.
     *
     * @return true | false
     */
    public boolean hasNext() {
        return selectedIndex < (getSteps().length - 1);
    }

    /**
     * Returns whether there is a valid panel to be selected previous.
     *
     * @return true | false
     */
    public boolean hasPrevious() {
        return selectedIndex > 0;
    }

    /**
     * Returns the current index in the model.
     *
     * @return the currently selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selected index to that specified.
     *
     * @param the index to be selected
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * Returns the index of the specified panel in this model.
     *
     * @param panel - the panel
     * @return the panel index
     */
    public int getIndexOf(JPanel panel) {
        if (panels == null) {
            return -1;
        }
        return panels.indexOf(panel);
    }
    
    /**
     * Returns the panel at the specified index.
     *
     * @param index - the panel to be retrieved
     */
    public JPanel getPanelAt(int index) {
        if (panels == null) {
            return null;
        }
        return panels.get(index);
    }
    
    /**
     * Adds the specified panel to the end of the model.
     *
     * @param panel - the panel to be added
     */
    public void addPanel(JPanel panel) {
        if (panels == null) {
            panels = new ArrayList<JPanel>();
        }
        if (panels.indexOf(panel) == -1) {
            panels.add(panel);
        }
    }
    
    /**
     * Returns the number of panel components in this model.
     *
     * @return the panel count
     */
    public int getPanelCount() {
        if (getSteps() == null) {
            return 0;
        }
        return getSteps().length;
    }

    /**
     * Returns a list of panels making up this model.
     *
     * @return the panels collection
     */
    public List<JPanel> getPanels() {
        return panels;
    }

    /**
     * Sets the list of panels making up this model.
     *
     * @param panels - the panels
     */
    public void setPanels(List<JPanel> panels) {
        this.panels = panels;
    }

    public void setSteps(String[] steps) {
        this.steps = steps;
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
    }
    
}





