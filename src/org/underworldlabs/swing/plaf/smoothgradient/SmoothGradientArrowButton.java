/*
 * SmoothGradientArrowButton.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.ButtonModel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalScrollButton;

/**
 *
 *
 * @author Karsten Lentzsch
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
final class SmoothGradientArrowButton extends MetalScrollButton {
	
	private static Color shadowColor;
	private static Color highlightColor;
	
	private boolean isFreeStanding;


	public SmoothGradientArrowButton(int direction, int width, boolean freeStanding) {
		super(direction, width, freeStanding);
	    shadowColor    = UIManager.getColor("ScrollBar.darkShadow");
	    highlightColor = UIManager.getColor("ScrollBar.highlight");
		isFreeStanding = freeStanding;
	}


    public void setFreeStanding(boolean freeStanding) {
    	super.setFreeStanding(freeStanding);
	    isFreeStanding = freeStanding;
    }
    

	public void paint(Graphics g) {
		boolean leftToRight = SmoothGradientUtils.isLeftToRight(this);
		boolean isEnabled   = getParent().isEnabled();
		boolean isPressed   = getModel().isPressed();

		Color arrowColor = isEnabled
				? MetalLookAndFeel.getControlInfo()
				: MetalLookAndFeel.getControlDisabled();
		int width  = getWidth();
		int height = getHeight();
		int w = width;
		int h = height;
		int arrowHeight = (height + 1) / 4;

		g.setColor(isPressed ? MetalLookAndFeel.getControlShadow() : getBackground());
		g.fillRect(0, 0, width, height);

		if (getDirection() == NORTH) {
			paintNorth(g, leftToRight, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		} else if (getDirection() == SOUTH) {
			paintSouth(g, leftToRight, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		} else if (getDirection() == EAST) {
			paintEast(g, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		} else if (getDirection() == WEST) {
			paintWest(g, isEnabled, arrowColor, isPressed,
				width, height, w, h, arrowHeight);
		}
		if (SmoothGradientUtils.is3D("ScrollBar."))
			paint3D(g);
	}


	private void paintWest(Graphics g, boolean isEnabled, Color arrowColor,
		boolean isPressed, int width, int height, int w, int h, int arrowHeight) {
			
		if (!isFreeStanding) {
			height += 2;
			width  += 1;
			g.translate(-1, 0);
		}
		
		// Draw the arrow
		g.setColor(arrowColor);
		
		int startX = (((w + 1) - arrowHeight) / 2);
		int startY = (h / 2);
		
		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX + line,
				startY - line,
				startX + line,
				startY + line + 1);
		}
		
		if (isEnabled) {
			g.setColor(highlightColor);
		
			if (!isPressed) {
				g.drawLine(1, 1, width - 1, 1);
				g.drawLine(1, 1, 1, height - 3);
			}
			g.drawLine(1, height - 1, width - 1, height - 1);
		
			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 1, 0);
			g.drawLine(0, 0, 0, height - 2);
			g.drawLine(2, height - 2, width - 1, height - 2);
		} else {
			SmoothGradientUtils.drawDisabledBorder(g, 0, 0, width + 1, height);
		}
		
		if (!isFreeStanding) {
			height -= 2;
			width  -= 1;
			g.translate(1, 0);
		}
	}


	private void paintEast(Graphics g, boolean isEnabled, Color arrowColor,
		boolean isPressed, int width, int height, int w, int h, int arrowHeight) {
		if (!isFreeStanding) {
			height += 2;
			width  += 1;
		}
		
		// Draw the arrow
		g.setColor(arrowColor);
		
		int startX = (((w + 1) - arrowHeight) / 2) + arrowHeight - 1;
		int startY = (h / 2);
		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX - line,
				startY - line,
				startX - line,
				startY + line + 1);
		}
		
		if (isEnabled) {
			g.setColor(highlightColor);
			if (!isPressed) {
				g.drawLine(0, 1, width - 3, 1);
				g.drawLine(0, 1, 0, height - 3);
			}
			g.drawLine(width - 1, 1, width - 1, height - 1);
			g.drawLine(0, height - 1, width - 1, height - 1);
		
			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 2, 0);
			g.drawLine(width - 2, 1, width - 2, height - 2);
			g.drawLine(0, height - 2, width - 2, height - 2);
		} else {
			SmoothGradientUtils.drawDisabledBorder(g, -1, 0, width + 1, height);
		}
		if (!isFreeStanding) {
			height -= 2;
			width  -= 1;
		}
	}


	private void paintSouth(Graphics g, boolean leftToRight, boolean isEnabled,
		Color arrowColor, boolean isPressed, int width, int height, int w, int h,
		int arrowHeight) {
			
		if (!isFreeStanding) {
			height += 1;
			if (!leftToRight) {
				width += 1;
				g.translate(-1, 0);
			} else {
				width += 2;
			}
		}
		
		// Draw the arrow
		g.setColor(arrowColor);
		
		int startY = (((h + 1) - arrowHeight) / 2) + arrowHeight - 1;
		int startX = (w / 2);
		
		//	    System.out.println( "startX2 :" + startX + " startY2 :"+startY);
		
		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX - line,
				startY - line,
				startX + line + 1,
				startY - line);
		}
		
		if (isEnabled) {
			g.setColor(highlightColor);
			if (!isPressed) {
				g.drawLine(1, 0, width - 3, 0);
				g.drawLine(1, 0, 1, height - 3);
			}
			g.drawLine(1, height - 1, width - 1, height - 1);
			g.drawLine(width - 1, 0, width - 1, height - 1);
		
			g.setColor(shadowColor);
			g.drawLine(0, 0, 0, height - 2);
			g.drawLine(width - 2, 0, width - 2, height - 2);
			g.drawLine(1, height - 2, width - 2, height - 2);
		} else {
			SmoothGradientUtils.drawDisabledBorder(g, 0, -1, width, height + 1);
		}
		
		if (!isFreeStanding) {
			height -= 1;
			if (!leftToRight) {
				width -= 1;
				g.translate(1, 0);
			} else {
				width -= 2;
			}
		}
	}


	private void paintNorth(Graphics g, boolean leftToRight, boolean isEnabled, 
		Color arrowColor, boolean isPressed, 
		int width, int height, int w, int h, int arrowHeight) {
		if (!isFreeStanding) {
			height += 1;
			g.translate(0, -1);
			if (!leftToRight) {
				width += 1;
				g.translate(-1, 0);
			} else {
				width += 2;
			}
		}
		
		// Draw the arrow
		g.setColor(arrowColor);
		int startY = ((h + 1) - arrowHeight) / 2;
		int startX = (w / 2);
		// System.out.println( "startX :" + startX + " startY :"+startY);
		for (int line = 0; line < arrowHeight; line++) {
			g.drawLine(
				startX - line,
				startY + line,
				startX + line + 1,
				startY + line);
		}
		
		if (isEnabled) {
			g.setColor(highlightColor);
		
			if (!isPressed) {
				g.drawLine(1, 1, width - 3, 1);
				g.drawLine(1, 1, 1, height - 1);
			}
		
			g.drawLine(width - 1, 1, width - 1, height - 1);
		
			g.setColor(shadowColor);
			g.drawLine(0, 0, width - 2, 0);
			g.drawLine(0, 0, 0, height - 1);
			g.drawLine(width - 2, 2, width - 2, height - 1);
		} else {
			SmoothGradientUtils.drawDisabledBorder(g, 0, 0, width, height + 1);
		}
		if (!isFreeStanding) {
			height -= 1;
			g.translate(0, 1);
			if (!leftToRight) {
				width -= 1;
				g.translate(1, 0);
			} else {
				width -= 2;
			}
		}
	}
	

	private void paint3D(Graphics g) {
		ButtonModel buttonModel = getModel();
		if (buttonModel.isArmed() && buttonModel.isPressed() || buttonModel.isSelected())
			return;
			
		int width  = getWidth();
		int height = getHeight();
		if (getDirection() == EAST) 
			width -= 2;
		else if (getDirection() == SOUTH) 
			height -= 2;

		Rectangle r = new Rectangle(1, 1, width, height);
		boolean isHorizontal = (getDirection() == EAST || getDirection() == WEST);
		SmoothGradientUtils.addLight3DEffekt(g, r, isHorizontal);
	}
}








