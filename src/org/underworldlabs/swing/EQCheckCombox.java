package org.underworldlabs.swing;

import org.japura.gui.CheckComboBox;

import java.awt.*;

public class EQCheckCombox extends CheckComboBox {

    @Override
    protected void updateComboBox() {
        this.getComboBox().removeAllItems();

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Object obj : getModel().getCheckeds()) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(obj.toString());
        }

        //noinspection unchecked
        this.getComboBox().addItem(sb.toString());
    }

    @Override
    public void setPopupVisible(boolean visible) {

        if (!visible) {
            getPopup().setVisible(false);
            return;
        }

        int modelSize = getModel().getSize();
        if (modelSize < 1)
            return;

        Dimension componentSize = getSize();
        Dimension invokerSize = this.getSize();
        Dimension popupSize = getPopup().getPreferredSize();

        int yPos = componentSize.height;
        if (invokerSize.height < getLocation().y + popupSize.height)
            yPos = -popupSize.height;

        setVisibleRowCount(Math.min(getVisibleRowCount(), modelSize));
        getPopup().setPreferredSize(new Dimension(componentSize.width, popupSize.height));
        getPopup().show(this, 0, yPos);
    }

}
