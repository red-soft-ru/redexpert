package org.underworldlabs.swing.treetable;

import javax.accessibility.Accessible;
import javax.swing.*;

public interface ProfilerRenderer extends Movable, Accessible {

    public void setValue(Object value, int row);

    public int getHorizontalAlignment();

    JComponent getComponent();

}
