/*
 * UpdatableLabel.java
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

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class UpdatableLabel extends JLabel {

	public UpdatableLabel() {
		super();
	}

	public UpdatableLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public UpdatableLabel(Icon image) {
	    super(image);
	}

	public UpdatableLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public UpdatableLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public UpdatableLabel(String text) {
		super(text);
	}

	@Override
	public void setText(String text) {
	    super.setText(text);
	    scheduleRepaint();
	}

    private void scheduleRepaint() {

        Runnable update = new Runnable() {
            public void run() {
                Dimension dim = getSize();
                paintImmediately(0, 0, dim.width, dim.height);
            }
        };
        SwingUtilities.invokeLater(update);
    }
	
}


