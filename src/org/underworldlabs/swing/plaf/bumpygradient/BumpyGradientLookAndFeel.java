/*
 * BumpyGradientLookAndFeel.java
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

package org.underworldlabs.swing.plaf.bumpygradient;

import org.underworldlabs.swing.plaf.smoothgradient.SmoothGradientLookAndFeel;

import javax.swing.*;
import java.awt.*;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the
 *           release of version 3.0.0beta1 has meant a
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * @author Takis Diakoumis
 */
public class BumpyGradientLookAndFeel extends SmoothGradientLookAndFeel {

    /**
     * The modified darker highlight for internal frame bumps
     */
    private static Color internalFrameBumpsHighlight;

    /**
     * Constructs the <code>PlasticLookAndFeel</code>.
     */
    public BumpyGradientLookAndFeel() {
        if (internalFrameBumpsHighlight == null)
            internalFrameBumpsHighlight = new Color(198, 198, 246);
    }

    public String getID() {
        return "BumpyGradient";
    }

    public String getName() {
        return "Bumpy Gradient Look and Feel";
    }

    public String getDescription() {
        return "The RedXpert Bumpy Gradient Look and Feel - modified from " +
                "The JGoodies Plastic Look and Feel";
    }

    // Overriding Superclass Behavior ***************************************

    /**
     * Initializes the class defaults, that is, overrides some UI delegates
     * with JGoodies Plastic implementations.
     *
     * @see javax.swing.plaf.basic.BasicLookAndFeel#getDefaults
     */
    protected void initClassDefaults(UIDefaults table) {
        super.initClassDefaults(table);

        String NAME_PREFIX = "org.underworldlabs.swing.plaf.bumpygradient.BumpyGradient";

        // Overwrite some of the uiDefaults.
        Object[] uiDefaults = {
                "RootPaneUI", NAME_PREFIX + "RootPaneUI",
                "InternalFrameUI", NAME_PREFIX + "InternalFrameUI",
        };

        table.putDefaults(uiDefaults);

    }

    public static Color getInternalFrameBumpsHighlight() {
        if (internalFrameBumpsHighlight == null) {
            internalFrameBumpsHighlight = new Color(198, 198, 246);
        }
        return internalFrameBumpsHighlight;
    }

}















