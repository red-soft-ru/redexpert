package org.executequery.gui.components;

import org.executequery.components.BottomButtonPanel;
import org.executequery.gui.BaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public abstract class AbstractDialogPanel extends JPanel {
    BottomButtonPanel bottomButtonPanel;

    BaseDialog dialog;

    boolean success = false;

    protected JPanel mainPanel;

    public AbstractDialogPanel() {
        init();
    }

    private void init() {
        mainPanel = new JPanel();
        ActionListener bottomButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Objects.equals(e.getActionCommand(), "ok")) {
                    ok();
                    success = true;
                    dialog.dispose();
                }
            }
        };
        bottomButtonPanel = new BottomButtonPanel(bottomButtonListener, "OK", "", "ok", true);
        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        add(mainPanel, gbh.fillBoth().spanX().setMaxWeightY().get());
        add(bottomButtonPanel, gbh.nextRowFirstCol().fillHorizontally().spanX().spanY().get());
    }

    public BaseDialog getDialog() {
        return dialog;
    }

    public void setDialog(BaseDialog dialog) {
        this.dialog = dialog;
    }

    public void display() {
        success = false;
    }

    protected abstract void ok();

    public boolean isSuccess() {
        return success;
    }
}
