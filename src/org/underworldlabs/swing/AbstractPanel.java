package org.underworldlabs.swing;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPanel extends JPanel {
    protected GridBagHelper gbh;

    public AbstractPanel() {
        initComponents();
        initGBH();
        arrangeComponents();
        postInitActions();
    }

    protected void initGBH() {
        setLayout(new GridBagLayout());
        gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
    }

    protected abstract void initComponents();

    protected abstract void arrangeComponents();

    protected abstract void postInitActions();
}
