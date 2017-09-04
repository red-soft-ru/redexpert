/*
 * BumpyGradientInternalFrameUI.java
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


import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.plaf.ComponentUI;

import org.underworldlabs.swing.plaf.smoothgradient.SmoothGradientInternalFrameUI;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class BumpyGradientInternalFrameUI 
                        extends SmoothGradientInternalFrameUI {

	private BumpyGradientInternalFrameTitlePane titlePane;

    public BumpyGradientInternalFrameUI(JInternalFrame b) {
		super(b);
	}

	public static ComponentUI createUI(JComponent c) {
		return new BumpyGradientInternalFrameUI((JInternalFrame) c);
	}

	protected JComponent createNorthPane(JInternalFrame w) {
		titlePane = new BumpyGradientInternalFrameTitlePane(w);
		return titlePane;
	}

}















