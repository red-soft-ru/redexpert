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
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
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
public class PropertiesEditorColours extends AbstractPropertiesColours
        implements Constants {

    private static final String[] EDITOR_OUTPUT_COLORS_KEYS = new String[]{
            "editor.output.background",
            "editor.output.plain.color",
            "editor.output.error.color",
            "editor.output.warning.color",
            "editor.output.action.color",
    };

    private static final String[] SAMPLE_TEXT_LABELS = new String[]{
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

    // --- GUI components ---

    private SampleTextPanel sampleTextPanel;
    private JTable syntaxColoursTable;
    private JTable editorColoursTable;

    private SyntaxColorTableModel syntaxColoursModel;
    private EditorColorTableModel editorColoursModel;

    // ---

    public PropertiesEditorColours(PropertiesPanel parent) {
        super(parent);

        init();
        arrange();
    }

    private void init() {

        ColourTableCellRenderer colourRenderer = new ColourTableCellRenderer();
        colourRenderer.setFont(AbstractPropertiesBasePanel.getDefaultFont());

        ComboBoxCellEditor comboEditor = new ComboBoxCellEditor(new String[]{PLAIN, ITALIC, BOLD});
        comboEditor.setFont(AbstractPropertiesBasePanel.getDefaultFont());

        // --- init editorColoursTable ---

        editorColoursModel = new EditorColorTableModel(getColorPreferences());
        editorColoursTable = createTableWithModel("editorColoursModel", editorColoursModel);
        editorColoursTable.setTableHeader(null);
        editorColoursTable.addMouseListener(new MouseHandler(editorColoursTable));

        TableColumnModel tcm = editorColoursTable.getColumnModel();
        TableColumn column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(200);
        column.setMaxWidth(200);
        column.setMinWidth(200);

        // --- configure syntaxColoursTable ---

        syntaxColoursModel = new SyntaxColorTableModel();
        syntaxColoursTable = createTableWithModel("syntaxColoursModel", syntaxColoursModel);
        syntaxColoursTable.addMouseListener(new MouseHandler(syntaxColoursTable));

        tcm = syntaxColoursTable.getColumnModel();
        column = tcm.getColumn(0);
        column.setPreferredWidth(150);

        column = tcm.getColumn(1);
        column.setCellRenderer(colourRenderer);
        column.setPreferredWidth(120);

        column = tcm.getColumn(2);
        column.setCellRenderer(new ComboBoxCellRenderer());
        column.setCellEditor(comboEditor);
        column.setPreferredWidth(70);

        // ---

        sampleTextPanel = new SampleTextPanel();
    }

    private void arrange() {

        JScrollPane scrollPane = new JScrollPane(sampleTextPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        GridBagHelper gbh = new GridBagHelper().bottomGap(5).anchorNorthWest().fillBoth().spanX();
        mainPanel.add(WidgetFactory.createLabel(bundledStaticString("QueryEditorColours")), gbh.setMinWeightY().get());
        mainPanel.add(new JScrollPane(editorColoursTable), gbh.nextRow().setWeightY(0.4).get());
        mainPanel.add(WidgetFactory.createLabel(bundledStaticString("SyntaxStyles")), gbh.nextRow().topGap(5).setMinWeightY().get());
        mainPanel.add(new JScrollPane(syntaxColoursTable), gbh.nextRow().topGap(0).setWeightY(0.5).get());
        mainPanel.add(WidgetFactory.createLabel(bundledStaticString("EditorSample")), gbh.nextRow().topGap(5).setMinWeightY().get());
        mainPanel.add(scrollPane, gbh.nextRow().topGap(0).setWeightY(0.4).get());

        // --- base ---

        addContent(mainPanel);
    }

    // ---

    private static List<UserPreference> getColorPreferences() {

        Map<String, String> colorKeys = new HashMap<>();
        colorKeys.put("editor.caret.colour", bundledStaticString("CaretColour"));
        colorKeys.put("editor.linenumber.background", bundledStaticString("GutterBackground"));
        colorKeys.put("editor.linenumber.foreground", bundledStaticString("GutterForeground"));
        colorKeys.put("editor.text.background.colour", bundledStaticString("EditorBackground"));
        colorKeys.put("editor.text.background.alternate.color", bundledStaticString("EditorAlternativeBackground"));
        colorKeys.put("editor.text.foreground.colour", bundledStaticString("EditorForeground"));
        colorKeys.put("editor.output.background", bundledStaticString("ResultsPanelBackground"));
        colorKeys.put("editor.text.selection.foreground", bundledStaticString("TextSelectionForeground"));
        colorKeys.put("editor.text.selection.background", bundledStaticString("TextSelectionBackground"));
        colorKeys.put("editor.text.selection.background.alternative", bundledStaticString("TextSelectionAlternativeBackground"));
        colorKeys.put("editor.display.linehighlight.colour", bundledStaticString("CurrentLineHighlight"));

        List<UserPreference> coloursPreferences = new ArrayList<>();
        for (Map.Entry<String, String> entry : colorKeys.entrySet()) {
            coloursPreferences.add(new UserPreference(
                    UserPreference.COLOUR_TYPE,
                    entry.getKey(),
                    entry.getValue(),
                    SystemProperties.getColourProperty("user", entry.getKey())
            ));
        }

        return coloursPreferences;
    }

    private JTable createTableWithModel(String name, TableModel tableModel) {

        JTable table = WidgetFactory.createTable(name, tableModel);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(false);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(AbstractPropertiesBasePanel.getDefaultFont());

        return table;
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

    // --- UserPreferenceFunction impl ---

    @Override
    public void restoreDefaults() {
        editorColoursModel.restoreAllDefaults();
        syntaxColoursModel.restoreAllDefaults();

        Properties themeDefaultsProperty = defaultsForTheme();
        for (String key : EDITOR_OUTPUT_COLORS_KEYS)
            SystemProperties.setColourProperty("user", key, asColour(themeDefaultsProperty.getProperty(key)));
    }

    @Override
    public void save() {
        syntaxColoursModel.save();
        editorColoursModel.save();
    }

    // --- inner classes ---

    private class SampleTextPanel extends JPanel implements Scrollable {

        private static final int FONT_SIZE = 14;
        private static final String FONT_NAME = "monospaced";

        private boolean showCaret;

        public SampleTextPanel() {
            runRepaintTimer();
        }

        private void runRepaintTimer() {

            Runnable caret = () -> {
                showCaret = !showCaret;
                repaint();
            };

            TimerTask caretTimer = new TimerTask() {
                @Override
                public void run() {
                    EventQueue.invokeLater(caret);
                }
            };

            new Timer().schedule(caretTimer, 0, 500);
        }

        // --- JComponent impl ---

        @Override
        public void paintComponent(Graphics g) {
            UIUtils.antialias(g);

            List<SyntaxColour> labels = new ArrayList<>(syntaxColoursModel.getSyntaxColours());
            labels.add(1, new SyntaxColour("Sample selected text",
                    editorColoursModel.getColorForKey("editor.text.selection.foreground"),
                    editorColoursModel.getColorForKey("editor.text.selection.background"), 0, ""));

            labels.add(2, new SyntaxColour("Sample current line highlight",
                    syntaxColoursModel.getColorForKey("normal"),
                    editorColoursModel.getColorForKey("editor.display.linehighlight.colour"), 0, ""));

            int row = FONT_SIZE + 5;
            int width = getWidth();
            int gutterWidth = 40;

            // ---

            g.setColor(editorColoursModel.getColorForKey("editor.text.background.colour"));
            g.fillRect(0, 0, width, (FONT_SIZE + 5) * labels.size());

            g.setColor(editorColoursModel.getColorForKey("editor.linenumber.background"));
            g.fillRect(0, 0, gutterWidth, (FONT_SIZE + 5) * (labels.size() + 2));

            g.setColor(GUIUtilities.getDefaultBorderColour().darker());
            g.drawLine(gutterWidth, 0, gutterWidth, getHeight() - 1);

            Color gutterForeground = editorColoursModel.getColorForKey("editor.linenumber.foreground");
            for (int i = 0, k = labels.size(); i < k; i++) {

                int y1 = row * (i + 1);
                SyntaxColour syntaxColour = labels.get(i);
                String text = bundledStaticString(SAMPLE_TEXT_LABELS[i]);
                Font font = new Font(FONT_NAME, syntaxColour.fontStyle, FONT_SIZE);

                // ---

                g.setColor(gutterForeground);
                g.drawString(String.valueOf(i + 1), i < 9 ? 31 : 24, y1);

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

                g.drawString(text, gutterWidth + 5, y1);

                if (showCaret) {
                    FontMetrics fontMetrics = g.getFontMetrics(font);
                    int lineHeight = fontMetrics.getHeight() + 5;

                    g.setColor(editorColoursModel.getColorForKey("editor.caret.colour"));
                    int caretX = fontMetrics.stringWidth(text) + gutterWidth + 6;
                    g.drawLine(caretX, y1 - lineHeight + 6, caretX, y1 + 2);
                }
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), 3 + ((FONT_SIZE + 5) * (syntaxColoursModel.getRowCount() + 2)));
        }

        // --- Scrollable impl ---

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return FONT_SIZE + 5;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return FONT_SIZE + 5;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    } // SampleTextPanel class

    private class SyntaxColorTableModel extends AbstractTableModel {

        private final transient List<SyntaxColour> syntaxColours;
        private final String[] columnHeaders = {
                bundledStaticString("SyntaxStyle"),
                bundledStaticString("Colour"),
                bundledStaticString("FontStyle")
        };

        public SyntaxColorTableModel() {
            syntaxColours = new ArrayList<>();

            for (int i = 0; i < SYNTAX_TYPES.length; i++) {
                syntaxColours.add(new SyntaxColour(
                        bundledStaticString(getTableValueText(i)),
                        SystemProperties.getColourProperty("user", STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]),
                        SystemProperties.getIntProperty("user", STYLE_NAME_PREFIX + SYNTAX_TYPES[i]),
                        SYNTAX_TYPES[i]
                ));
            }
        }

        public void restoreAllDefaults() {
            Properties defaults = defaultsForTheme();

            for (int i = 0; i < SYNTAX_TYPES.length; i++) {
                Color color = asColour(defaults.getProperty(STYLE_COLOUR_PREFIX + SYNTAX_TYPES[i]));
                String style = styleNameForValue(Integer.parseInt(defaults.getProperty(STYLE_NAME_PREFIX + SYNTAX_TYPES[i])));

                syntaxColoursModel.setValueAt(color, i, 1);
                syntaxColoursModel.setValueAt(style, i, 2);
            }
        }

        public void restoreSingleDefault(int row) {
            String property = defaultsForTheme().getProperty(STYLE_COLOUR_PREFIX + SYNTAX_TYPES[row]);

            try {
                syntaxColoursModel.setValueAt(asColour(property), row, 1);

            } catch (NumberFormatException e) {
                Log.error("Unable to set up default color, loaded property [" + property + "] could not convert to Integer");
            }

            fireTableDataChanged();
        }

        public Color getColorForKey(String key) {

            for (SyntaxColour syntaxColour : syntaxColours)
                if (key.equals(syntaxColour.property))
                    return syntaxColour.color;

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

        public void save() {
            for (SyntaxColour syntaxColour : syntaxColours) {
                SystemProperties.setIntProperty("user", STYLE_NAME_PREFIX + syntaxColour.property, syntaxColour.fontStyle);
                SystemProperties.setColourProperty("user", STYLE_COLOUR_PREFIX + syntaxColour.property, syntaxColour.color);
            }
        }

        public List<SyntaxColour> getSyntaxColours() {
            return syntaxColours;
        }

        // --- TableModel impl ---

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
            switch (col) {
                case 0:
                    return syntaxColours.get(row).label;
                case 1:
                    return syntaxColours.get(row).color;
                case 2:
                    return syntaxColours.get(row).style;
                default:
                    return null;
            }
        }

        // --- AbstractTableModel impl ---

        @Override
        public void setValueAt(Object value, int row, int col) {
            SyntaxColour syntaxColour = syntaxColours.get(row);

            if (col == 1) {
                syntaxColour.color = (Color) value;

            } else if (col == 2) {
                syntaxColour.style = (String) value;

                switch (syntaxColour.style) {
                    case BOLD:
                        syntaxColour.fontStyle = 1;
                        break;
                    case ITALIC:
                        syntaxColour.fontStyle = 2;
                        break;
                    case PLAIN:
                    default:
                        syntaxColour.fontStyle = 0;
                        break;
                }
            }

            if (col == 1 || col == 2)
                sampleTextPanel.repaint();

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

    } // ColorTableModel class

    private class EditorColorTableModel extends AbstractTableModel {
        private final transient List<UserPreference> coloursPreferences;

        public EditorColorTableModel(List<UserPreference> coloursPreferences) {
            this.coloursPreferences = coloursPreferences;
        }

        public void restoreAllDefaults() {

            Properties defaults = defaultsForTheme();
            for (UserPreference userPreference : coloursPreferences)
                userPreference.setValue(asColour(defaults.getProperty(userPreference.getKey())), PropertiesEditorColours.class);
            fireTableDataChanged();
        }

        public void restoreSingleDefault(int row) {

            Properties defaults = defaultsForTheme();
            UserPreference userPreference = coloursPreferences.get(row);
            String property = defaults.getProperty(userPreference.getKey());

            try {
                userPreference.setValue(asColour(property), PropertiesEditorColours.class);

            } catch (NumberFormatException e) {
                Log.error("Unable to set up default color, loaded property [" + property + "] could not convert to Integer");
            }
            fireTableDataChanged();
        }

        public Color getColorForKey(String key) {

            for (UserPreference userPreference : coloursPreferences)
                if (key.equals(userPreference.getKey()))
                    return (Color) userPreference.getValue();

            return null;
        }

        public void save() {
            for (UserPreference userPreference : coloursPreferences)
                SystemProperties.setProperty("user", userPreference.getKey(), userPreference.getSaveValue());
        }

        // --- TableModel impl ---

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return coloursPreferences.size();
        }

        @Override
        public Object getValueAt(int row, int column) {

            UserPreference preference = coloursPreferences.get(row);
            switch (column) {
                case 0:
                    return preference.getDisplayedKey();
                case 1:
                    return preference.getValue();
                default:
                    return Constants.EMPTY;
            }
        }

        // --- AbstractTableModel impl ---

        @Override
        public void setValueAt(Object value, int row, int column) {
            UserPreference preference = coloursPreferences.get(row);
            preference.setValue(value, PropertiesEditorColours.class);
            sampleTextPanel.repaint();
            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1;
        }

    } // class EditorColourPreferencesTableModel

    private class SyntaxColour {

        Color color;
        String label;
        String style;
        int fontStyle;
        String property;
        Color background;

        public SyntaxColour(String label, Color color, int fontStyle, String property) {
            this(label, color, null, fontStyle, property);
        }

        public SyntaxColour(String label, Color color, Color background, int fontStyle, String property) {
            this.label = label;
            this.color = color;
            this.property = property;
            this.fontStyle = fontStyle;
            this.background = background;
            this.style = styleNameForValue(fontStyle);
        }

        public boolean hasBackgroundColour() {
            return background != null;
        }

        public boolean isBraceMatch() {
            return property.contains("braces.");
        }

        @Override
        public String toString() {
            return label;
        }

    } // SyntaxColour class

    private static class MouseHandler extends MouseAdapter {
        private final JTable table;

        public MouseHandler(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent evt) {

            int row = table.rowAtPoint(evt.getPoint());
            if (row == -1)
                return;

            int col = table.columnAtPoint(evt.getPoint());
            if (evt.getButton() == MouseEvent.BUTTON1) {  // left mouse button
                leftButtonAction(col, row);

            } else if (evt.getButton() == MouseEvent.BUTTON3 && col == 1) // right mouse button
                rightButtonAction(col, row, evt);
        }

        private void leftButtonAction(int col, int row) {

            TableModel model = table.getModel();
            if (col == 1) {

                Color color = JColorChooser.showDialog(
                        GUIUtilities.getInFocusDialogOrWindow(),
                        Bundles.get("LocaleManager.ColorChooser.title"),
                        (Color) model.getValueAt(row, 1)
                );

                if (color != null)
                    model.setValueAt(color, row, 1);

            } else if (col == 2)
                model.setValueAt(model.getValueAt(row, col), row, 2);
        }

        private void rightButtonAction(int col, int row, MouseEvent evt) {

            JMenuItem resetButton = new JMenuItem(Bundles.get("common.default"));
            resetButton.addActionListener(e -> resetColour(table.getModel(), row));

            JMenuItem editButton = new JMenuItem(Bundles.get("common.edit"));
            editButton.addActionListener(e -> leftButtonAction(col, row));

            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(editButton);
            popupMenu.add(resetButton);

            popupMenu.show(table, evt.getX(), evt.getY());
        }

        private void resetColour(TableModel model, int row) {
            if (model instanceof EditorColorTableModel) {
                ((EditorColorTableModel) model).restoreSingleDefault(row);

            } else if (model instanceof SyntaxColorTableModel)
                ((SyntaxColorTableModel) model).restoreSingleDefault(row);
        }

    } // MouseHandler class

}
