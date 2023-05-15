package org.underworldlabs.swing.menu;

import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.actions.ReflectiveAction;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class PasswordTextComponentPopUpMenu extends JPopupMenu {
    private ReflectiveAction reflectiveAction;

    /**
     * the text component this popup belongs to
     */
    private JTextComponent textComponent;

    public PasswordTextComponentPopUpMenu(JTextComponent textComponent) {

        // create the listener
        reflectiveAction = new ReflectiveAction(this);

        this.textComponent = textComponent;
        textComponent.addMouseListener(new PopupListener(this));

        // the menu label text
        String[] menuLabels = {"Paste"};

        // cached actions from which to retrieve common accels and mnemonics
        String[] actionNames = {"paste-command"};

        // action command settings to map to method names in this class
        String[] actionCommands = {"paste"};

        for (int i = 0; i < menuLabels.length; i++) {

            add(createMenuItem(menuLabels[i], actionNames[i], actionCommands[i]));
        }

    }

    /**
     * Executes the paste action on the registered text component.
     */
    public void paste(ActionEvent e) {
        textComponent.paste();
    }

    private JMenuItem createMenuItem(String text,
                                     String actionName,
                                     String actionCommand) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(text);
        Action action = ActionBuilder.get(actionName);
        Object object = action.getValue(Action.ACCELERATOR_KEY);
        if (object != null) {
            menuItem.setAccelerator((KeyStroke) object);
        }

        object = action.getValue(Action.MNEMONIC_KEY);
        if (object != null) {
            menuItem.setMnemonic(((Integer) object).intValue());
        }

        menuItem.setActionCommand(actionCommand);
        menuItem.addActionListener(reflectiveAction);
        return menuItem;
    }


    class PopupListener extends MouseAdapter {

        private JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }
}
