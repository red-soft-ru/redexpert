/*
 * PropertiesEditorColours.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.prefs;


import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.table.ColourTableCellRenderer;
import org.underworldlabs.swing.table.ComboBoxCellEditor;
import org.underworldlabs.swing.table.ComboBoxCellRenderer;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Timer;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class PropertiesEditorColours extends AbstractPropertiesColours implements Constants {

    private SamplePanel samplePanel;
    private SyntaxColorTableModel syntaxColoursTableModel;
    private EditorColourPreferencesTableModel editorColoursPreferencesTableModel;

    public PropertiesEditorColours() {
        super();
        try {
            init();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        // editor background etc colours

        Map<String, String> editorColourKeys = new HashMap<>();
        editorColourKeys.put("editor.caret.colour", bundledString("CaretColour"));
        editorColourKeys.put("editor.linenumber.background", bundledString("GutterBackground"));
        editorColourKeys.put("editor.linenumber.foreground", bundledString("GutterForeground"));
        editorColourKeys.put("editor.text.background.colour", bundledString("EditorBackground"));
        editorColourKeys.put("editor.text.background.alternate.color", bundledString("EditorAlternativeBackground"));
        editorColourKeys.put("editor.text.foreground.colour", bundledString("EditorForeground"));
        editorColourKeys.put("editor.results.background.colour", bundledString("ResultsPanelBackground"));
        editorColourKeys.put("editor.text.selection.foreground", bundledString("TextSelectionForeground"));
        editorColourKeys.put("editor.text.selection.background", bundledString("TextSelectionBackground"));
        editorColourKeys.put("editor.text.selection.background.alternative", "TextSelectionAlternativeBackground");
        editorColourKeys.put("editor.display.linehighlight.colour", bundledString("CurrentLineHighlight"));

        List<UserPreference> editorColoursPreferences = new ArrayList<>();
        for (Map.Entry<String, String> entry : editorColourKeys.entrySet()) {

            editorColoursPreferences.add(new UserPreference(UserPreference.COLOUR_TYPE, entry.getKey(), entry.getValue(), SystemProperties.getColourProperty("user", entry.getKey())));
        }

        editorColoursPreferencesTableModel = new EditorColourPreferencesTableModel(editorColoursPreferences);
        JTable editorColoursPreferencesTable = createTableWithModel(editorColoursPreferencesTableModel);
        editorColoursPreferencesTable.setTableHeader(null);
        editorColoursPreferencesTable.addMouseListener(new MouseHandler(editorColoursPreferencesTable));

        ColourTableCellRenderer colourRenderer = new ColourTableCellRenderer();
        colourRenderer.setFont(AbstractPropertiesBasePanel.panelFont);

        TableColumnModel tcm = editorColoursPreferencesTable.getColumnModel();

        int valueColumnWidth = 200;
        TableColumn column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(valueColumnWidth);
        column.setMaxWidth(valueColumnWidth);
        column.setMinWidth(valueColumnWidth);

        // syntax colours

        syntaxColoursTableModel = new SyntaxColorTableModel();
        JTable syntaxColoursTable = createTableWithModel(syntaxColoursTableModel);
        syntaxColoursTable.addMouseListener(new MouseHandler(syntaxColoursTable));

        tcm = syntaxColoursTable.getColumnModel();

        column = tcm.getColumn(0);
        column.setPreferredWidth(150);

        column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(120);

        column = tcm.getColumn(2);
        column.setCellRenderer(new ComboBoxCellRenderer());
        column.setPreferredWidth(70);

        ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(new String[]{PLAIN, ITALIC, BOLD});
        comboEditor.setFont(AbstractPropertiesBasePanel.panelFont);
        tcm.getColumn(2).setCellEditor(comboEditor);

        samplePanel = new SamplePanel();
        JScrollPane sampleScroller = new JScrollPane(samplePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets.bottom = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(bundledString("QueryEditorColours")), gbc);
        gbc.weighty = 0.4;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(editorColoursPreferencesTable), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(bundledString("SyntaxStyles")), gbc);
        gbc.weighty = 0.5;
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(syntaxColoursTable), gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.top = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(bundledString("EditorSample")), gbc);
        gbc.gridy++;
        gbc.weighty = 0.4;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(sampleScroller, gbc);

        addContent(panel);
    }

    private JTable createTableWithModel(TableModel tableModel) {

        JTable table = new JTable(tableModel);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(AbstractPropertiesBasePanel.panelFont);

        return table;
    }

    private String getLabelText(int index) {

        String[] sampleLabels = {
                "SampleNormalText",
                "SampleSelectedText",
                "SampleCurrentLineHighlight",
                "SampleKeywordText",
                "SampleQuoteText",
                "SampleSingleLineCommentText",
                "SampleMulti-lineCommentText",
                "SampleNumberText",
                "SampleOperatorText",
                "SampleLiteralText",
                "SampleObjectsDb",
                "SampleDatatype",
                "SampleText"
        };

        return sampleLabels[index];
    }

    @Override
    public void restoreDefaults() {
        editorColoursPreferencesTableModel.restoreAllDefaults();
        syntaxColoursTableModel.restoreAllDefaults();
    }

    @Override
    public void save() {
        syntaxColoursTableModel.save();
        editorColoursPreferencesTableModel.save();
    }

    private String styleNameForValue(Integer value) {

        switch (value) {
            case 1:
                return BOLD;
            case 2:
                return ITALIC;
            default:
                return PLAIN;
        }
    }

    class SamplePanel extends JPanel implements Scrollable {

        private final int size;
        private final String fontName;
        private boolean showCaret;

        public SamplePanel() {

            size = 14;
            fontName = "monospaced";

            final Runnable caret = () -> {
                showCaret = !showCaret;
                repaint();
            };

            TimerTask caretTimer = new TimerTask() {
                @Override
                public void run() {
                    EventQueue.invokeLater(caret);
                }
            };

            Timer timer = new Timer();
            timer.schedule(caretTimer, 0, 500);

        }

        @Override
        public void paintComponent(Graphics g) {

            UIUtils.antialias(g);

            List<SyntaxColour> labels = new ArrayList<>(syntaxColoursTableModel.getSyntaxColours());
            labels.add(1, new SyntaxColour("Sample selected text",
                    editorColoursPreferencesTableModel.getColorForKey("editor.text.selection.foreground"),
                    editorColoursPreferencesTableModel.getColorForKey("editor.text.selection.background"), 0, ""));

            labels.add(2, new SyntaxColour("Sample current line highlight",
                    syntaxColoursTableModel.getColorForKey("normal"),
                    editorColoursPreferencesTableModel.getColorForKey("editor.display.linehighlight.colour"), 0, ""));

            int row = size + 5;
            int width = getWidth();

            g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.text.background.colour"));
            g.fillRect(0, 0, width, (size + 5) * labels.size());

            int gutterWidth = 40;

            g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.linenumber.background"));
            g.fillRect(0, 0, gutterWidth, (size + 5) * (labels.size() + 2));

            g.setColor(GUIUtilities.getDefaultBorderColour().darker());
            g.drawLine(gutterWidth, 0, gutterWidth, getHeight() - 1);

            Color gutterForeground = editorColoursPreferencesTableModel.getColorForKey("editor.linenumber.foreground");
            for (int i = 0, k = labels.size(); i < k; i++) {

                SyntaxColour syntaxColour = labels.get(i);

                g.setColor(gutterForeground);
                int y1 = row * (i + 1);
                g.drawString(String.valueOf(i + 1), i < 9 ? 31 : 24, y1);

                Font font = new Font(fontName, syntaxColour.fontStyle, size);
                g.setFont(font);
                g.setColor(syntaxColour.color);

                if (syntaxColour.isBraceMatch()) {

                    g.fillRect(gutterWidth + 4, (row * i) + 3, width, row);
                    g.setColor(Color.BLACK);

                } else if (syntaxColour.hasBackgroundColour()) {

                    g.setColor(syntaxColour.background);
                    g.fillRect(gutterWidth + 4, (row * i) + 3, width, row);
                    g.setColor(syntaxColour.color);
                }

                String text = bundledString(getLabelText(i));
                g.drawString(text, gutterWidth + 5, y1);

                if (showCaret) {

                    FontMetrics fm = g.getFontMetrics(font);
                    int lineHeight = fm.getHeight() + 5;

                    g.setColor(editorColoursPreferencesTableModel.getColorForKey("editor.caret.colour"));
                    int carotX = fm.stringWidth(text) + gutterWidth + 6;
                    g.drawLine(carotX, y1 - lineHeight + 6, carotX, y1 + 2);
                }
            }
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), 3 + ((size + 5) * (syntaxColoursTableModel.getRowCount() + 2)));
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return size + 5;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return size + 5;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    }


    class SyntaxColorTableModel extends AbstractTableModel {

        private final Vector<SyntaxColour> syntaxColours;
        private final String[] columnHeaders = {
                bundledString("SyntaxStyle"),
                bundledString("Colour"),
                bundledString("FontStyle")
        };

        SyntaxColorTableModel() {

            syntaxColours = new Vector<>(SYNTAX_TYPES.length);

            for (int i = 0; i < SYNTAX_TYPES.length; i++) {
                addSyntaxColour(
                        bundledString(getTableValueText(i)),
                        SystemProperties.getColourProperty("user", STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]),
                        SystemProperties.getIntProperty("user", STYLE_NAME_PREFIX + SYNTAX_TYPES[i]),
                        SYNTAX_TYPES[i]
                );
            }
        }

        public void restoreAllDefaults() {

            Properties defaults = defaultsForTheme();
            for (int i = 0; i < SYNTAX_TYPES.length; i++) {

                syntaxColoursTableModel.setValueAt(asColour(
                        defaults.getProperty(STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i])), i, 1);
                syntaxColoursTableModel.setValueAt(styleNameForValue(
                        Integer.parseInt(defaults.getProperty(STYLE_NAME_PREFIX + SYNTAX_TYPES[i]))), i, 2);
            }

        }

        public void restoreSingleDefault(int row) {

            Properties defaults = defaultsForTheme();
            syntaxColoursTableModel.setValueAt(asColour(
                    defaults.getProperty(STYLE_COLOUR_PREFIX + SYNTAX_TYPES[row])), row, 1);
            fireTableDataChanged();
        }

        public Color getColorForKey(String key) {

            for (SyntaxColour syntaxColour : syntaxColours)
                if (key.equals(syntaxColour.property)) return syntaxColour.color;

            return null;
        }

        private String getTableValueText(int styleIndex) {

            switch (styleIndex) {
                case 0:
                    return "NormalText";
                case 1:
                    return "Keywords";
                case 2:
                    return "QuoteString";
                case 3:
                    return "Single-lineComment";
                case 4:
                    return "Multi-lineComment";
                case 5:
                    return "Number";
                case 6:
                    return "Operator";
                case 7:
                    return "Literal";
                case 8:
                    return "ObjectsDB";
                case 9:
                    return "Datatype";
                default:
                    return "Text";
            }
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return syntaxColours.size();
        }

        @Override
        public Object getValueAt(int row, int col) {

            SyntaxColour ch = syntaxColours.elementAt(row);
            switch (col) {
                case 0:
                    return ch.label;
                case 1:
                    return ch.color;
                case 2:
                    return ch.style;
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {

            SyntaxColour ch = syntaxColours.elementAt(row);

            if (col == 1) {
                ch.color = (Color) value;

            } else if (col == 2) {

                ch.style = (String) value;
                switch (ch.style) {
                    case PLAIN:
                        ch.fontStyle = 0;
                        break;
                    case BOLD:
                        ch.fontStyle = 1;
                        break;
                    case ITALIC:
                        ch.fontStyle = 2;
                        break;
                }
            }

            if (col == 1 || col == 2) samplePanel.repaint();

            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int nRow, int nCol) {
            return nCol == 2;
        }

        @Override
        public String getColumnName(int col) {
            return columnHeaders[col];
        }

        public Vector<SyntaxColour> getSyntaxColours() {
            return syntaxColours;
        }

        public void save() {

            for (int i = 0; i < syntaxColours.size(); i++) {
                SyntaxColour ch = syntaxColours.elementAt(i);
                SystemProperties.setColourProperty("user", STYLE_COLOUR_PREFIX + ch.property, ch.color);
                SystemProperties.setIntProperty("user", STYLE_NAME_PREFIX + ch.property, ch.fontStyle);
            }
        }

        private void addSyntaxColour(String label, Color color, int style, String property) {
            syntaxColours.addElement(new SyntaxColour(label, color, style, property));
        }

    } // ColorTableModel

    class SyntaxColour {

        String label;
        int fontStyle;
        String style;
        String property;
        Color color;
        Color background;

        SyntaxColour(String label, Color color, int fontStyle, String property) {
            this(label, color, null, fontStyle, property);
        }

        SyntaxColour(String label, Color color, Color background, int fontStyle, String property) {
            this.label = label;
            this.background = background;
            this.fontStyle = fontStyle;
            this.color = color;
            this.property = property;
            this.style = styleNameForValue(fontStyle);
        }

        public boolean hasBackgroundColour() {
            return (background != null);
        }

        public boolean isBraceMatch() {
            return property.contains("braces.");
        }

        @Override
        public String toString() {
            return label;
        }

    } // SyntaxColour

    static class MouseHandler extends MouseAdapter {

        private final JTable table;

        public MouseHandler(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {

            int row = table.rowAtPoint(evt.getPoint());
            int col = table.columnAtPoint(evt.getPoint());

            if (row == -1)
                return;

            if (evt.getButton() == MouseEvent.BUTTON1)  // left mouse button
                leftButtonAction(col, row);
            else if (evt.getButton() == MouseEvent.BUTTON3 && col == 1) // right mouse button
                rightButtonAction(col, row, evt);

        }

        private void leftButtonAction(int col, int row) {

            TableModel model = table.getModel();
            if (col == 1) {

                Color color = JColorChooser.showDialog(
                        GUIUtilities.getInFocusDialogOrWindow(),
                        "Select Colour",
                        (Color) model.getValueAt(row, 1)
                );

                if (color != null)
                    model.setValueAt(color, row, 1);

            } else if (col == 2)
                model.setValueAt(model.getValueAt(row, col), row, 2);
        }

        private void rightButtonAction(int col, int row, MouseEvent evt) {

            JPopupMenu popupMenu = new JPopupMenu();

            JMenuItem editButton = new JMenuItem("Edit");
            editButton.addActionListener(e -> leftButtonAction(col, row));
            popupMenu.add(editButton);

            JMenuItem resetButton = new JMenuItem("Default");
            resetButton.addActionListener(e -> resetColour(table.getModel(), row));
            popupMenu.add(resetButton);

            popupMenu.show(table, evt.getX(), evt.getY());
        }

        private void resetColour(TableModel model, int row) {

            if (model instanceof EditorColourPreferencesTableModel)
                ((EditorColourPreferencesTableModel) model).restoreSingleDefault(row);
            else if (model instanceof SyntaxColorTableModel)
                ((SyntaxColorTableModel) model).restoreSingleDefault(row);

        }

    } // MouseHandler


    class EditorColourPreferencesTableModel extends AbstractTableModel {

        private final List<UserPreference> editorColoursPreferences;

        public EditorColourPreferencesTableModel(List<UserPreference> editorColoursPreferences) {
            this.editorColoursPreferences = editorColoursPreferences;
        }

        public Color getColorForKey(String key) {

            for (UserPreference userPreference : editorColoursPreferences)
                if (key.equals(userPreference.getKey())) return (Color) userPreference.getValue();

            return null;
        }

        public void setValueAt(Object value, int row, int column) {
            UserPreference preference = editorColoursPreferences.get(row);
            preference.setValue(value);
            samplePanel.repaint();
            fireTableRowsUpdated(row, row);
        }

        @Override
        public int getRowCount() {
            return editorColoursPreferences.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int row, int column) {

            UserPreference preference = editorColoursPreferences.get(row);
            switch (column) {
                case 0:
                    return preference.getDisplayedKey();
                case 1:
                    return preference.getValue();
                default:
                    return Constants.EMPTY;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

        public void restoreAllDefaults() {

            Properties defaults = defaultsForTheme();
            for (UserPreference userPreference : editorColoursPreferences)
                userPreference.setValue(asColour(defaults.getProperty(userPreference.getKey())));
            fireTableDataChanged();
        }

        public void restoreSingleDefault(int row) {

            Properties defaults = defaultsForTheme();
            UserPreference userPreference = editorColoursPreferences.get(row);
            userPreference.setValue(asColour(defaults.getProperty(userPreference.getKey())));
            fireTableDataChanged();
        }

        public void save() {
            for (UserPreference userPreference : editorColoursPreferences)
                SystemProperties.setProperty("user", userPreference.getKey(), userPreference.getSaveValue());
        }

    } // class EditorColourPreferencesTableModel

}
