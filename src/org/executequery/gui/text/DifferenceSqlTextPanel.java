package org.executequery.gui.text;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.executequery.components.LineNumber;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DifferenceSqlTextPanel extends JPanel {

    private final String topTitle;
    private final String botTitle;

    private SimpleSqlTextPanel topTextPanel;
    private SimpleSqlTextPanel botTextPanel;

    private List<Object[]> highlights;

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

        topTextPanel.getTextPane().getLineBorder().setFont(topTextPanel.getTextPane().getFont());
        botTextPanel.getTextPane().getLineBorder().setFont(botTextPanel.getTextPane().getFont());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(topTextPanel);
        splitPane.setBottomComponent(botTextPanel);

        add(splitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setTexts(String value1, String value2) {

        DiffRowGenerator diffGenerator = DiffRowGenerator.create().build();
        if (highlights == null)
            highlights = new ArrayList<>();
        highlights.clear();
        topTextPanel.setSQLText("");
        topTextPanel.getTextPane().removeAllLineHighlights();
        botTextPanel.setSQLText("");
        botTextPanel.getTextPane().removeAllLineHighlights();

        List<String> topLineBorders = new LinkedList<>();
        List<String> botLineBorders = new LinkedList<>();

        for (DiffRow row : diffGenerator.generateDiffRows(
                value2 != null ? Arrays.asList(value2.split("\n")) : new ArrayList<>(),
                value1 != null ? Arrays.asList(value1.split("\n")) : new ArrayList<>())
        ) {

            if (row.getTag().equals(DiffRow.Tag.INSERT)) {
                topTextPanel.getTextPane().append(row.getNewLine() + "\n");
                topLineBorders.add("+++");
                addHighLightLine(topTextPanel.getTextPane(), Color.GREEN);

            } else if (row.getTag().equals(DiffRow.Tag.DELETE)) {
                botTextPanel.getTextPane().append(row.getOldLine() + "\n");
                botLineBorders.add("---");
                addHighLightLine(botTextPanel.getTextPane(), Color.RED);

            } else if (row.getTag().equals(DiffRow.Tag.CHANGE)) {
                topTextPanel.getTextPane().append(row.getNewLine() + "\n");
                botTextPanel.getTextPane().append(row.getOldLine() + "\n");
                topLineBorders.add("***");
                botLineBorders.add("***");
                addHighLightLine(topTextPanel.getTextPane(), Color.BLUE);
                addHighLightLine(botTextPanel.getTextPane(), Color.BLUE);

            } else {
                topTextPanel.getTextPane().append(row.getNewLine() + "\n");
                botTextPanel.getTextPane().append(row.getOldLine() + "\n");
                topLineBorders.add("   ");
                botLineBorders.add("   ");
            }
        }
        for (Object[] objs : highlights) {
            addHighLightLine((SQLTextArea) objs[0], (int) objs[1], (Color) objs[2]);
        }

        ((LineNumber) topTextPanel.getTextPane().getLineBorder()).setBorderLabels(topLineBorders);
        ((LineNumber) botTextPanel.getTextPane().getLineBorder()).setBorderLabels(botLineBorders);
    }


    protected void addHighLightLine(SQLTextArea sqlTextArea, Color color) {
        if (highlights == null)
            highlights = new ArrayList<>();
        Object[] objects = new Object[3];
        objects[0] = sqlTextArea;
        objects[1] = sqlTextArea.getLineCount() - 2;
        objects[2] = color;
        highlights.add(objects);
    }

    protected void addHighLightLine(SQLTextArea sqlTextArea, int line, Color color) {
        try {
            sqlTextArea.addLineHighlight(line, color);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}
