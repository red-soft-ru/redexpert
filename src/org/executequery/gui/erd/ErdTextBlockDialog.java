package org.executequery.gui.erd;

import org.executequery.GUIUtilities;
import org.executequery.components.TextFieldPanel;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.localization.Bundles;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ErdTextBlockDialog extends AbstractBaseDialog {
    /**
     * The ERD parent panel
     */
    private final ErdViewerPanel parent;
    /**
     * Whether this is a new title panel
     */
    private final boolean isNew;
    private RSyntaxTextArea descTextArea;
    private ErdTextPanel erdTextPanel;

    public ErdTextBlockDialog(ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), bundleString("textBlock"), true);
        this.parent = parent;
        isNew = true;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        display();
    }

    public ErdTextBlockDialog(ErdViewerPanel parent, ErdTextPanel erdTextPanel) {

        super(GUIUtilities.getParentFrame(), bundleString("textBlock"), true);
        this.parent = parent;
        isNew = false;
        this.erdTextPanel = erdTextPanel;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        descTextArea.setText(erdTextPanel.getErdDescription());

        display();
    }

    private static String bundleString(String key) {
        return Bundles.get(ErdTitlePanelDialog.class, key);
    }

    private void display() {
        pack();
        Dimension dialogSize = new Dimension(700, 420);
        setSize(dialogSize);
        this.setLocation(GUIUtilities.getLocationForDialog(dialogSize));

        descTextArea.requestFocusInWindow();

        setVisible(true);
    }

    private void jbInit() throws Exception {
        JButton createButton = new DefaultPanelButton(Bundles.get("common.add.button"));
        createButton.setActionCommand("Add");
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        cancelButton.setActionCommand("Cancel");

        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e);
            }
        };

        cancelButton.addActionListener(btnListener);
        createButton.addActionListener(btnListener);

        descTextArea = new RSyntaxTextArea();

        descTextArea.setLineWrap(true);
        descTextArea.setWrapStyleWord(true);

        TextFieldPanel panel = new TextFieldPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        panel.add(new JScrollPane(descTextArea), gbh.spanX().setMaxWeightY().fillBoth().get());
        panel.add(new JPanel(), gbh.nextRowFirstCol().setLabelDefault().fillHorizontally().setMaxWeightX().get());
        panel.add(createButton, gbh.nextCol().setLabelDefault().get());
        panel.add(cancelButton, gbh.nextCol().get());

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new GridBagLayout());

        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(7, 7, 7, 7), 0, 0));

    }

    private void create() {

        if (isNew) {
            ErdTextPanel erdTextPanel = new ErdTextPanel(parent,
                    descTextArea.getText());
            parent.addTextPanel(erdTextPanel);
        } else {
            erdTextPanel.setErdDescription(descTextArea.getText());
            parent.repaintLayeredPane();
        }

        GUIUtilities.scheduleGC();
        dispose();
    }

    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Cancel"))
            dispose();

        else if (command.equals("Add"))
            create();

    }
}
