package org.executequery.gui.text;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DifferenceTextPanel extends JPanel {

    private static final Color INSERT_COLOR = new Color(100, 255, 100, 77);
    private static final Color DELETE_COLOR = new Color(255, 100, 100, 77);
    private static final Color CHANGE_COLOR = new Color(100, 100, 255, 77);

    private final String newTitle;
    private final String oldTitle;

    private SimpleTextArea newTextPanel;
    private SimpleTextArea oldTextPanel;

    private java.util.List<Object[]> highlights;
    private java.util.List<String> newLineBorders;
    private List<String> oldLineBorders;

    public DifferenceTextPanel(String newTitle, String oldTitle) {
        super(new BorderLayout());

        this.newTitle = newTitle;
        this.oldTitle = oldTitle;

        init();
    }

    private void init() {

        highlights = new ArrayList<>();
        newLineBorders = new LinkedList<>();
        oldLineBorders = new LinkedList<>();

        newTextPanel = new SimpleTextArea(newTitle);
        oldTextPanel = new SimpleTextArea(oldTitle);

        newTextPanel.getTextAreaComponent().setEditable(false);
        oldTextPanel.getTextAreaComponent().setEditable(false);

       /* newTextPanel.getTextPane().getLineBorder().setFont(newTextPanel.getTextPane().getFont());
        oldTextPanel.getTextPane().getLineBorder().setFont(oldTextPanel.getTextPane().getFont());*/

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(oldTextPanel);
        splitPane.setBottomComponent(newTextPanel);

        add(splitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void setTexts(String value1, String value2) {

        DiffRowGenerator diffGenerator = DiffRowGenerator.create().build();

        highlights.clear();
        newLineBorders.clear();
        oldLineBorders.clear();

        newTextPanel.getTextAreaComponent().setText("");
        oldTextPanel.getTextAreaComponent().setText("");

        newTextPanel.getTextAreaComponent().removeAllLineHighlights();
        oldTextPanel.getTextAreaComponent().removeAllLineHighlights();

        for (DiffRow row : diffGenerator.generateDiffRows(
                value2 != null ? Arrays.asList(value2.split("\n")) : new ArrayList<>(),
                value1 != null ? Arrays.asList(value1.split("\n")) : new ArrayList<>())
        ) {

            if (row.getTag().equals(DiffRow.Tag.INSERT))
                addRow(row, INSERT_COLOR, "+++");

            else if (row.getTag().equals(DiffRow.Tag.DELETE))
                addRow(row, DELETE_COLOR, "---");

            else if (row.getTag().equals(DiffRow.Tag.CHANGE))
                addRow(row, CHANGE_COLOR, "***");

            else
                addRow(row, null, "   ");
        }

        for (Object[] highlight : highlights)
            addHighlightLine((RSyntaxTextArea) highlight[0], (int) highlight[1], (Color) highlight[2]);

       /* ((LineNumber) newTextPanel.getTextPane().getLineBorder()).setBorderLabels(newLineBorders);
        ((LineNumber) oldTextPanel.getTextPane().getLineBorder()).setBorderLabels(oldLineBorders);*/
    }

    private void addRow(DiffRow row, Color color, String label) {

        //add rows
        newTextPanel.getTextAreaComponent().append(row.getNewLine() + "\n");
        oldTextPanel.getTextAreaComponent().append(row.getOldLine() + "\n");

        //add highlight properties
        if (color != null) {
            highlights.add(getHighlight(newTextPanel.getTextAreaComponent(), color));
            highlights.add(getHighlight(oldTextPanel.getTextAreaComponent(), color));
        }

        //add border labels
        newLineBorders.add(label);
        oldLineBorders.add(label);

    }

    private Object[] getHighlight(RSyntaxTextArea textArea, Color color) {
        return new Object[]{textArea, textArea.getLineCount() - 2, color};
    }

    protected void addHighlightLine(RSyntaxTextArea textArea, int line, Color color) {
        try {
            textArea.addLineHighlight(line, color);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}
