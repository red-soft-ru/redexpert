package org.executequery.gui.components;

import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractDialogPanel extends JPanel {

    protected JPanel mainPanel;
    protected BaseDialog dialog;
    protected boolean success = false;

    private JButton applyButton;
    private JButton cancelButton;

    protected abstract void apply();

    public AbstractDialogPanel() {
        init();
        arrange();
    }

    private void init() {
        mainPanel = WidgetFactory.createPanel("mainPanel");

        applyButton = WidgetFactory.createButton(
                "applyButton",
                Bundles.get("common.ok.button"),
                e -> applyAndDispose()
        );

        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> dialog.dispose()
        );
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = WidgetFactory.createPanel("buttonPanel");

        gbh = new GridBagHelper().fillHorizontally();
        buttonPanel.add(applyButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillBoth();
        add(mainPanel, gbh.setMaxWeightY().spanX().get());
        add(buttonPanel, gbh.nextRowFirstCol().setMinWeightY().bottomGap(5).fillHorizontally().spanY().get());
    }

    private void applyAndDispose() {
        apply();
        success = true;
        dialog.dispose();
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

    public boolean isSuccess() {
        return success;
    }

}
