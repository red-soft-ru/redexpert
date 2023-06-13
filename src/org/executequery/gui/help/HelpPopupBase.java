/*
 * HelpPopupBase.java
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

package org.executequery.gui.help;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.GlassCapturePanel;
import org.underworldlabs.swing.GlassPaneSelectionListener;
import org.underworldlabs.swing.LinkButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * @author Takis Diakoumis
 */
public class HelpPopupBase extends JPanel
        implements ActionListener,
        GlassPaneSelectionListener {

    /**
     * the popup owner
     */
    private Component owner;

    /**
     * the owner's original glass pane
     */
    private Component ownersGlassPane;

    /**
     * the temp glass pane added to the owner
     */
    private GlassCapturePanel glassPane;

    public HelpPopupBase(String title,
                         Component viewComponent,
                         MouseEvent e) {
        this(title, viewComponent, null, e);
    }

    public HelpPopupBase(String title,
                         Component viewComponent,
                         Component owner,
                         MouseEvent event) {
        super(new GridBagLayout());

        this.owner = owner;

        if (owner instanceof JDialog) {
            JDialog dialog = (JDialog) owner;
            ownersGlassPane = dialog.getGlassPane();
            glassPane = new GlassCapturePanel(dialog.getContentPane());
            dialog.setGlassPane(glassPane);
            glassPane.setVisible(true);
        }

        glassPane.addGlassPaneSelectionListener(this);

        JLabel titleLabel = new JLabel(title);
        Font font = titleLabel.getFont();
        titleLabel.setFont(font.deriveFont(Font.BOLD));

        LinkButton linkButton = new LinkButton("Hide");
        linkButton.setAlignmentX(LinkButton.RIGHT);
        linkButton.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.top = 3;
        gbc.insets.bottom = 3;
        gbc.insets.left = 4;
        gbc.insets.right = 5;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel(GUIUtilities.loadIcon("TipOfTheDay16.svg")), gbc);
        gbc.gridx++;
        gbc.insets.left = 0;
        gbc.weightx = 1.0;
        add(titleLabel, gbc);
        gbc.gridx++;
        gbc.insets.right = 5;
        gbc.anchor = GridBagConstraints.EAST;
        add(linkButton, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        add(viewComponent, gbc);

        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        showPopup(event);
    }

    public void glassPaneSelected(MouseEvent e) {
        int id = e.getID();
        switch (id) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_DRAGGED:
            case MouseEvent.MOUSE_RELEASED:
                hidePopup();
                return;
        }
    }

    public void actionPerformed(ActionEvent e) {
        hidePopup();
    }

    public void hidePopup() {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = null;
        } finally {
            if (glassPane != null) {
                glassPane.setVisible(false);
            }
            // replace the original glass pane
            if (ownersGlassPane != null) {
                if (owner instanceof JDialog) {
                    JDialog dialog = (JDialog) owner;
                    dialog.setGlassPane(ownersGlassPane);
                }
            }
        }
    }

    /**
     * the popup panel
     */
    private Popup popup;

    public void showPopup(MouseEvent event) {
        if (owner == null) {
            owner = GUIUtilities.getInFocusDialogOrWindow();
        }

        Point p = SwingUtilities.convertPoint(
                (Component) event.getSource(),
                event.getX(),
                event.getY(),
                owner);

        popup = PopupFactory.getSharedInstance().getPopup(owner, this, p.x, p.y);
        popup.show();
    }


    public void setViewComponent(Component c) {
        add(c, BorderLayout.CENTER);
    }

}






