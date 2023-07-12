package org.executequery.gui.text;

import javax.swing.*;
import java.awt.*;

public class DifferenceSqlTextPanel extends JPanel {

    private final String topTitle;
    private final String botTitle;

    private SimpleSqlTextPanel topTextPanel;
    private SimpleSqlTextPanel botTextPanel;

    public DifferenceSqlTextPanel(String topTitle, String botTitle) {
        super(new BorderLayout());

        this.topTitle = topTitle;
        this.botTitle = botTitle;

        init();
    }

    private void init() {

        topTextPanel = new SimpleSqlTextPanel(topTitle);
        botTextPanel = new SimpleSqlTextPanel(botTitle);

        topTextPanel.setSQLTextEditable(false);
        botTextPanel.setSQLTextEditable(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(topTextPanel);
        splitPane.setBottomComponent(botTextPanel);

        add(splitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setTexts(String value1, String value2) {
        topTextPanel.setSQLText(value1);
        botTextPanel.setSQLText(value2);
    }

}
