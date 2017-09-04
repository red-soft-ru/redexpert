/*
 * ProgressBarFactory.java
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

import org.underworldlabs.swing.plaf.UIUtils;

public class ProgressBarFactory {

	public static ProgressBar create() {
            return create(true);
	}
	
	public static ProgressBar create(boolean paintBorder) {
            return create(paintBorder, false);		
	}
	
	public static ProgressBar create(boolean paintBorder, boolean ignoreLaF) {

            if (!ignoreLaF) {
                    if (UIUtils.isNativeMacLookAndFeel()) {
                            return new BasicProgressBar(paintBorder); 
                    }
            }

            return new IndeterminateProgressBar(paintBorder);
	}
	
}




