/*
 * SimpleHtmlContentPane.java
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

package org.executequery.components;

import java.awt.Dialog;
import java.awt.Font;

import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import org.executequery.util.SystemWebBrowserLauncher;
import org.underworldlabs.swing.DialogMessageContent;

public class SimpleHtmlContentPane extends JEditorPane implements DialogMessageContent {

    private Dialog dialog;

    public SimpleHtmlContentPane(String message) {

        super("text/html", message.replaceAll("\n", "<br>"));

        setEditable(false);
        setOpaque(false);

        setSelectionColor(UIManager.getColor("OptionPane.background"));

        Font font = UIManager.getFont("OptionPane.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + (font.getSize()) + "pt; }";

        ((HTMLDocument) getDocument()).getStyleSheet().addRule(bodyRule);
        
        addHyperlinkListener(new OpensLinkInBrowserListener());
    }

    class OpensLinkInBrowserListener implements HyperlinkListener {

        public void hyperlinkUpdate(HyperlinkEvent e) {

            if (isActiveEvent(e)) {

                try {
                
                    new SystemWebBrowserLauncher().launch(e.getDescription());
                
                } finally {
                    
                    dialog.dispose();
                }

            }

        }

        private boolean isActiveEvent(HyperlinkEvent e) {

            return HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType());
        }
        
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
    
    
}





