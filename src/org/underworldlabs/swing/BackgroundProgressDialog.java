package org.underworldlabs.swing;

import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public class BackgroundProgressDialog extends DefaultProgressDialog {

    public BackgroundProgressDialog(String description) {
        super(description);
        init();
    }

    private void init() {

        JButton backgroundButton = new ProgressDialogButton(bundledString("CollapseButtonText"));
        backgroundButton.addActionListener(e -> dispose());
        backgroundButton.setToolTipText(bundledString("CollapseButtonToolTipText"));

        buttonPanel.add(backgroundButton,
                new GridBagHelper().setInsets(5, 5, 5, 5).anchorCenter().nextCol().get());
        pack();
    }

}
