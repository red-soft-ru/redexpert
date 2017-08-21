package org.underworldlabs.swing;

import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;

public class DefaultToolbarButton extends DefaultButton {

    public DefaultToolbarButton() {
        super();
    }

    public DefaultToolbarButton(Action a) {
        super(a);
    }

    public DefaultToolbarButton(ActionListener actionListener, String text, String actionCommand) {
        super(actionListener, text, actionCommand);
    }

    public DefaultToolbarButton(Icon icon) {
        super(icon);
    }

    public DefaultToolbarButton(String text, Icon icon) {
        super(text, icon);
    }

    public DefaultToolbarButton(String text) {
        super(text);
    }

    @Override
    public Insets getInsets() {
        
        return new Insets(5, 5, 5, 5);
    }
    
}
