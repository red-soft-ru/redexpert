package org.executequery.gui.text;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.executequery.log.Log;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DifferenceSqlTextPanel extends JPanel {

    private static final Color INSERT_COLOR = new Color(100, 255, 100, 77);
    private static final Color DELETE_COLOR = new Color(255, 100, 100, 77);
    private static final Color CHANGE_COLOR = new Color(100, 100, 255, 77);

    private final String newTitle;
    private final String oldTitle;

    private SimpleSqlTextPanel newTextPanel;
    private SimpleSqlTextPanel oldTextPanel;

    private List<Object[]> highlights;

    public DifferenceSqlTextPanel(String newTitle, String oldTitle, boolean showOldPanel) {
        super(new BorderLayout());

        this.newTitle = newTitle;
        this.oldTitle = oldTitle;

        init(showOldPanel);
    }

    private void init(boolean showOldPanel) {

        highlights = new ArrayList<>();

        newTextPanel = new SimpleSqlTextPanel(newTitle);
        oldTextPanel = new SimpleSqlTextPanel(oldTitle);

        newTextPanel.setSQLTextEditable(false);
        oldTextPanel.setSQLTextEditable(false);

        if (showOldPanel) {

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            splitPane.setTopComponent(oldTextPanel);
            splitPane.setBottomComponent(newTextPanel);

            add(splitPane, BorderLayout.CENTER);

        } else
            add(newTextPanel, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setTexts(String value1, String value2) {

        DiffRowGenerator diffGenerator = DiffRowGenerator.create().build();

        highlights.clear();

        newTextPanel.setSQLText("");
        oldTextPanel.setSQLText("");

        newTextPanel.getTextPane().removeAllLineHighlights();
        oldTextPanel.getTextPane().removeAllLineHighlights();

        for (DiffRow row : diffGenerator.generateDiffRows(
                value2 != null ? Arrays.asList(value2.split("\n")) : new ArrayList<>(),
                value1 != null ? Arrays.asList(value1.split("\n")) : new ArrayList<>())
        ) {

            if (row.getTag().equals(DiffRow.Tag.INSERT))
                addRow(row, INSERT_COLOR);

            else if (row.getTag().equals(DiffRow.Tag.DELETE))
                addRow(row, DELETE_COLOR);

            else if (row.getTag().equals(DiffRow.Tag.CHANGE))
                addRow(row, CHANGE_COLOR);

            else
                addRow(row, null);
        }

        for (Object[] highlight : highlights)
            addHighlightLine((SQLTextArea) highlight[0], (int) highlight[1], (Color) highlight[2]);
    }

    private void addRow(DiffRow row, Color color) {

        //add rows
        newTextPanel.getTextPane().append(row.getNewLine() + "\n");
        oldTextPanel.getTextPane().append(row.getOldLine() + "\n");

        //add highlight properties
        if (color != null) {
            highlights.add(getHighlight(newTextPanel.getTextPane(), color));
            highlights.add(getHighlight(oldTextPanel.getTextPane(), color));
        }

    }

    private Object[] getHighlight(SQLTextArea sqlTextArea, Color color) {
        return new Object[]{sqlTextArea, sqlTextArea.getLineCount() - 2, color};
    }

    protected void addHighlightLine(SQLTextArea sqlTextArea, int line, Color color) {
        try {
            sqlTextArea.addLineHighlight(line, color);
        } catch (BadLocationException e) {
            Log.error(e.getMessage(), e);
        }
    }

}
