package org.underworldlabs.swing;

import org.japura.gui.CheckComboBox;

public class EQCheckCombox extends CheckComboBox {

    protected void updateComboBox() {
        this.getComboBox().removeAllItems();
        StringBuilder sb=new StringBuilder();
        boolean first = true;
        for(Object obj: getModel().getCheckeds())
        {
            if(!first)
                sb.append(", ");
            first=false;
            sb.append(obj.toString());
        }
        this.getComboBox().addItem(sb.toString());
    }
}
