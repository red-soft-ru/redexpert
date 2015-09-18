/*
 * FontSizeHints.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

/**
 * Describes font size hints used by the JGoodies Windows look&amp;feel; future
 * implementations of the Plastic l&amp;f will use the same hints. In 1.3
 * environments the sizes are used as absolute font sizes, in 1.4 environments
 * size deltas are used between SYSTEM and the specified sizes.<p>
 * 
 * NOTE: This is work in progress and will probably change in the
 * next release, to better reflect the font choice in the J2SE 1.4.1.
 * Currently, the size delta is used only for the Tahoma font!<p>
 * 
 * In 1.3 environments, the font guess is Tahoma on modern Windows,
 * "dialog" otherwise. In 1.4 environments, the system fonts will be used.
 *
 * @author Karsten Lentzsch
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 *
 * @see	Options#setGlobalFontSizeHints
 * @see	FontUtils
 */
public final class FontSizeHints {
	
	public static final FontSizeHints LARGE	= new FontSizeHints(12, 12, 14, 14);
	public static final FontSizeHints SYSTEM	= new FontSizeHints(11, 11, 14, 14);
    public static final FontSizeHints MIXED2  = new FontSizeHints(11, 11, 14, 13);
	public static final FontSizeHints MIXED	= new FontSizeHints(11, 11, 14, 12);
	public static final FontSizeHints SMALL	= new FontSizeHints(11, 11, 12, 12);
	public static final FontSizeHints FIXED	= new FontSizeHints(12, 12, 12, 12);
	
	public static final FontSizeHints DEFAULT = SYSTEM;
	
	
	private final int loResMenuFontSize;
	private final int loResControlFontSize;
	private final int hiResMenuFontSize;
	private final int hiResControlFontSize;
	
	
	/**
	 * Constructs <code>FontSizeHints</code> for the specified menu and
	 * control fonts, both for low and high resolution environments.
	 */
	public FontSizeHints(int loResMenuFontSize, int loResControlFontSize, 
					  	  int hiResMenuFontSize, int hiResControlFontSize) {
		this.loResMenuFontSize		= loResMenuFontSize; 
		this.loResControlFontSize	= loResControlFontSize;
		this.hiResMenuFontSize		= hiResMenuFontSize;
		this.hiResControlFontSize	= hiResControlFontSize;
	}
	
	
	/**
	 * Answers the low resolution menu font size.
	 */
	public int loResMenuFontSize()		{ return loResMenuFontSize;	}


	/**
	 * Answers the low resolution control font size.
	 */
	public int loResControlFontSize()	{ return loResControlFontSize;	}


	/**
	 * Answers the high resolution menu font size.
	 */
	public int hiResMenuFontSize()		{ return hiResMenuFontSize;	}


	/**
	 * Answers the high resolution control font size.
	 */
	public int hiResControlFontSize()	{ return hiResControlFontSize;	}
	
	
	/**
	 * Answers the menu font size.
	 */
	public int menuFontSize() {
		return hiResMenuFontSize();
	}
	
	
	/**
	 * Answers the control font size.
	 */
	public int controlFontSize() {
		return hiResControlFontSize();
	}
	
	
	/**
	 * Answers the delta between system menu font size and our menu font size hint.
	 */
	public float menuFontSizeDelta() {
		return menuFontSize() - SYSTEM.menuFontSize();
	}
	
	
	/**
	 * Answers the delta between system control font size and our control font size hint.
	 */
	public float controlFontSizeDelta() {
		return controlFontSize() - SYSTEM.controlFontSize();
	}
	
	
	
	/**
	 * Answers the <code>FontSizeHints</code> for the specified name.
	 */
	public static FontSizeHints valueOf(String name) {
		if (name.equalsIgnoreCase("LARGE"))
			return LARGE;
		else if (name.equalsIgnoreCase("SYSTEM"))
			return SYSTEM;
		else if (name.equalsIgnoreCase("MIXED"))
			return MIXED;
		else if (name.equalsIgnoreCase("SMALL"))
			return SMALL;
		else if (name.equalsIgnoreCase("FIXED"))
			return FIXED;
		else 
			throw new IllegalArgumentException("Unknown font size hints name: " + name);
	}
}














