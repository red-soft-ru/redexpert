/*
 * SmoothGradientInternalFrameUI.java
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
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *
 * @author   Takis Diakoumis
 */
public class SmoothGradientInternalFrameUI extends BasicInternalFrameUI {

	private static final String FRAME_TYPE	= "JInternalFrame.frameType";
 	public  static final String IS_PALETTE	= "JInternalFrame.isPalette";
	private static final String PALETTE_FRAME	= "palette";
	private static final String OPTION_DIALOG	= "optionDialog";
	private static final Border EMPTY_BORDER	= new EmptyBorder(0, 0, 0, 0);

	private SmoothGradientInternalFrameTitlePane titlePane;
 	private PropertyChangeListener paletteListener;
 	private PropertyChangeListener contentPaneListener;

  	
	public SmoothGradientInternalFrameUI(JInternalFrame b) {
		super(b);
	}

	public static ComponentUI createUI(JComponent c) {
		return new SmoothGradientInternalFrameUI((JInternalFrame) c);
	}

	public void installUI(JComponent c) {
		frame = (JInternalFrame) c;
	
		paletteListener		= new PaletteListener(this);
		contentPaneListener = new ContentPaneListener(this);
		c.addPropertyChangeListener(paletteListener);
		c.addPropertyChangeListener(contentPaneListener);
	
		super.installUI(c);
	
		Object paletteProp = c.getClientProperty(IS_PALETTE);
		if (paletteProp != null) {
			setPalette(((Boolean) paletteProp).booleanValue());
		}
	
		Container content = frame.getContentPane();
		stripContentBorder(content);
	}
	
	
	public void uninstallUI(JComponent c) {
		frame = (JInternalFrame) c;
	
		c.removePropertyChangeListener(paletteListener);
		c.removePropertyChangeListener(contentPaneListener);
	
		Container cont = ((JInternalFrame) (c)).getContentPane();
		if (cont instanceof JComponent) {
			JComponent content = (JComponent) cont;
			if (content.getBorder() == EMPTY_BORDER) {
				content.setBorder(null);
			}
		}
		super.uninstallUI(c);
	}
	
	
    protected void installDefaults() {
    	super.installDefaults();
	
		/* Enable the content pane to inherit background color 
		 * from its parent by setting its background color to null. 
		 * Fixes bug#4268949, which has been fixed in 1.4, too. */
		JComponent contentPane = (JComponent) frame.getContentPane();
		if (contentPane != null) {
	          Color bg = contentPane.getBackground();
		  if (bg instanceof UIResource)
		    contentPane.setBackground(null);
		}
		frame.setBackground(UIManager.getLookAndFeelDefaults().getColor("control"));
    }
    
    
	protected void installKeyboardActions()	{}
	protected void uninstallKeyboardActions()	{}
	
	
	private void stripContentBorder(Object c) {
		if (c instanceof JComponent) {
			JComponent contentComp = (JComponent) c;
			Border contentBorder = contentComp.getBorder();
			if (contentBorder == null || contentBorder instanceof UIResource) {
				contentComp.setBorder(EMPTY_BORDER);
			}
		}
	}
	
	
	protected JComponent createNorthPane(JInternalFrame w) {
		titlePane = new SmoothGradientInternalFrameTitlePane(w);
		return titlePane;
	}


	public void setPalette(boolean isPalette) {
		String key = isPalette ? "InternalFrame.paletteBorder" : "InternalFrame.border";
		LookAndFeel.installBorder(frame, key);
		titlePane.setPalette(isPalette);
	}


	private void setFrameType(String frameType) {
		String   key;
		boolean hasPalette = frameType.equals(PALETTE_FRAME);
		if (frameType.equals(OPTION_DIALOG)) {
			key = "InternalFrame.optionDialogBorder";
		} else if (hasPalette) {
			key = "InternalFrame.paletteBorder";
		} else {
			key = "InternalFrame.border";
		}
		LookAndFeel.installBorder(frame, key);
		titlePane.setPalette(hasPalette);
	}
	
	
	private static class PaletteListener implements PropertyChangeListener {

		private final SmoothGradientInternalFrameUI ui;
		
		private PaletteListener(SmoothGradientInternalFrameUI ui) { this.ui = ui; }
		
		public void propertyChange(PropertyChangeEvent e) {
			String name  = e.getPropertyName();
			Object value = e.getNewValue();
			if (name.equals(FRAME_TYPE)) {
				if (value instanceof String) {
					ui.setFrameType((String) value);
				}
			} else if (name.equals(IS_PALETTE)) {
				ui.setPalette(Boolean.TRUE.equals(value));
			}
		}
	}
	
	private static class ContentPaneListener implements PropertyChangeListener {
		
		private final SmoothGradientInternalFrameUI ui;
		
		private ContentPaneListener(SmoothGradientInternalFrameUI ui) { this.ui = ui; }
		
		public void propertyChange(PropertyChangeEvent e) {
			String name = e.getPropertyName();
			if (name.equals(JInternalFrame.CONTENT_PANE_PROPERTY)) {
				ui.stripContentBorder(e.getNewValue());
			}
		}
	}

}
















