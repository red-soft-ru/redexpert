/*
 * TreeFindAction.java
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

package org.executequery.gui.browser;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.executequery.localization.Bundles;

/**
 *
 * Modified from the original by Santhosh Kumar 
 * from http://www.jroller.com/santhosh/category/Swing
 * 
 * Usage: new TreeFindAction().install(tree);
 *
 * @author   Santhosh Kumar, Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class TreeFindAction extends FindAction<TreePath> {

	public TreeFindAction() {

	    super();
	    
        putValue(Action.SHORT_DESCRIPTION, Bundles.get("BrowserTreeRootPopupMenu.searchNodes"));
    }

    protected boolean changed(JComponent comp, String searchString, Position.Bias bias) {

        if (StringUtils.isBlank(searchString)) {
            
            return false;
        }
        
		JTree tree = (JTree) comp;
		String prefix = searchString;

		if (ignoreCase()) {

            prefix = prefix.toUpperCase();
        }

		boolean wildcardStart = prefix.startsWith("*");
		if (wildcardStart) {

		    prefix = prefix.substring(1);
		
		} else {

		    prefix = "^" + prefix;
		}
		prefix = prefix.replaceAll("\\*", ".*");
		
		Matcher matcher = Pattern.compile(prefix).matcher("");
		List<TreePath> matchedPaths = new ArrayList<TreePath>();
		for (int i = 1; i < tree.getRowCount(); i++) {

            TreePath path = tree.getPathForRow(i);
            String text = tree.convertValueToText(path.getLastPathComponent(),
                    tree.isRowSelected(i), tree.isExpanded(i), true, i, false);

            if (ignoreCase()) {

                text = text.toUpperCase();
            }

//            if ((wildcardStart && text.contains(prefix)) || text.startsWith(prefix, 0)) {
//
//                matchedPaths.add(path);
//            }

            matcher.reset(text);
            if (matcher.find()) {
                
                matchedPaths.add(path);
            }
		    
		}

		foundValues(matchedPaths);

		return !(matchedPaths.isEmpty());
	}

	private void changeSelection(JTree tree, TreePath path) {
	    tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);
	}

	public TreePath getNextMatch(JTree tree, String prefix, int startingRow,
			Position.Bias bias) {

		int max = tree.getRowCount();
		if (prefix == null) {
			throw new IllegalArgumentException();
		}
		if (startingRow < 0 || startingRow >= max) {
			throw new IllegalArgumentException();
		}

		if (ignoreCase()) {
			prefix = prefix.toUpperCase();
	    }

		// start search from the next/previous element froom the
		// selected element
		int increment = (bias == null || bias == Position.Bias.Forward) ? 1 : -1;

		int row = startingRow;
		do {

		    TreePath path = tree.getPathForRow(row);
			String text = tree.convertValueToText(path.getLastPathComponent(),
					tree.isRowSelected(row), tree.isExpanded(row), true, row,
					false);

			if (ignoreCase()) {

			    text = text.toUpperCase();
			}

			if (text.startsWith(prefix)) {
				
			    return path;
			}
		
			row = (row + increment + max) % max;

		} while (row != startingRow);

		return null;
	}
	
	protected void listValueSelected(JComponent component, TreePath selection) {
	    
	    changeSelection((JTree) component, selection);
	}
	
	protected boolean ignoreCase() {

		return true;
	}

	protected ListCellRenderer getListCellRenderer() {
	    
	    return new TreePathListCellRenderer();
	}

	private static final Border cellRendererBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	
	class TreePathListCellRenderer extends JLabel implements ListCellRenderer {

        public TreePathListCellRenderer() {

	        super();
	        setBorder(cellRendererBorder);
        }

	    public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            TreePath treePath = (TreePath) value;
            
            setText(treePath.getLastPathComponent().toString());

            if (isSelected) {
            
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());

            } else {
              
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);

            return this;
        }
	    
	}
	
}







