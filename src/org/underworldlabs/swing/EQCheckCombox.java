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

        setVisibleRowCount(Math.min(getVisibleRowCount(), modelSize));

        Dimension componentSize = getSize();
        Dimension invokerSize = getParent().getSize();
        Dimension popupSize = getPopup().getPreferredSize();
        popupSize = new Dimension(componentSize.width, popupSize.height);

        int yPos = componentSize.height;
        if (invokerSize.height < getLocation().y + popupSize.height)
            yPos = -popupSize.height;

        getPopup().setPreferredSize(popupSize);
        getPopup().show(this, 0, yPos);
    }

}
