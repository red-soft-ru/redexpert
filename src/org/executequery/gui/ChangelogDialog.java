package org.executequery.gui;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChangelogDialog extends InformationDialog {

    private final JPanel searchPanel;
    private final JTree navigationTree;
    private final String formattedText;
    private final JTextField searchField;
    private final ChangelogSearch searcher;

    public ChangelogDialog(String name, String value, int valueType, String charSet) {
        super(name, value, valueType, charSet, "text/html");

        formattedText = loadedText
                .replaceAll("<[^>]*>", "")
                .replaceAll(" {2,}", "")
                .replaceAll("\n{2,}", "\n")
                .toLowerCase();
        searcher = new ChangelogSearch();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        Matcher matcher = Pattern.compile("<h2>.*</h2>").matcher(loadedText);
        while (matcher.find()) {
            String machedString = matcher.group();
            root.add(new NavigationTreeNode(machedString));
        }

        searchPanel = new JPanel(new GridBagLayout());

        searchField = WidgetFactory.createTextField("searchField");
        searchField.getDocument().addDocumentListener(new SearchFieldDocumentListener());
        searchField.addKeyListener(new SearchFieldKeyAdapter());

        navigationTree = new JTree(root);
        navigationTree.setRootVisible(false);
        navigationTree.setCellRenderer(new NavigationTreeRenderer());
        navigationTree.addKeyListener(new InitialSearchKeyAdapter());
        navigationTree.addMouseListener(new NavigationTreeMouseAdapter());

        // --- bind search tool ---

        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "search");
        getRootPane().getActionMap().put("search", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSearchPanelVisible(true);
            }
        });

        editorPane.addKeyListener(new InitialSearchKeyAdapter());
        setSearchPanelVisible(false);
    }

    @Override
    protected JPanel buildDisplayComponent() {
        GridBagHelper gbh;

        // --- search panel ---

        JButton hideSearchButton = WidgetFactory.createRolloverButton(
                "hideSearchButton",
                Constants.EMPTY,
                "icon_close",
                e -> setSearchPanelVisible(false)
        );

        gbh = new GridBagHelper().fillHorizontally();
        searchPanel.add(searchField, gbh.setMaxWeightX().get());
        searchPanel.add(hideSearchButton, gbh.nextCol().setMinWeightX().get());

        // --- view panel ---

        JPanel viewPanel = new JPanel(new GridBagLayout());
        viewPanel.setPreferredSize(new Dimension(800, 500));

        gbh = new GridBagHelper().setInsets(5, 5, 0, 5).fillBoth().spanY();
        viewPanel.add(new JScrollPane(navigationTree), gbh.setWeightX(0.15).get());
        viewPanel.add(searchPanel, gbh.nextCol().setHeight(1).setMinWeightY().setMaxWeightX().rightGap(5).bottomGap(0).spanX().get());
        viewPanel.add(new JScrollPane(editorPane), gbh.nextRow().setMaxWeightY().bottomGap(5).get());

        return viewPanel;
    }

    private void setSearchPanelVisible(boolean visible) {

        searcher.setFindText(null);
        searchPanel.setVisible(visible);

        if (visible) {
            searchField.setText(Constants.EMPTY);
            searchField.requestFocus();
        } else
            editorPane.requestFocus();
    }

    private final class ChangelogSearch {

        private final DefaultHighlighter.DefaultHighlightPainter HIGHLIGHT_PAINTER =
                new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

        private Matcher matcher;
        private int searchIndex;

        public void setFindText(String findText) {
            searchIndex = 0;
            matcher = !MiscUtils.isNull(findText) ?
                    Pattern.compile(findText.toLowerCase()).matcher(formattedText) :
                    null;
        }

        public void findNext() {
            if (matcher != null && matcher.find(searchIndex)) {
                searchIndex = matcher.end();
                setSelection(matcher.start(), matcher.end());
            }
        }

        private void setSelection(int start, int end) {
            try {
                editorPane.setCaretPosition(start);
                editorPane.getHighlighter().removeAllHighlights();
                editorPane.getHighlighter().addHighlight(start, end, HIGHLIGHT_PAINTER);

            } catch (BadLocationException ignored) {
            }
        }

    } // ChangelogSearch class

    private static final class NavigationTreeNode extends DefaultMutableTreeNode {

        private String nodeText;

        NavigationTreeNode(String nodeText) {
            super();

            Matcher matcher = Pattern.compile("v[\\d.]*").matcher(nodeText);
            if (matcher.find())
                this.nodeText = matcher.group();
        }

        @Override
        public String toString() {
            return nodeText;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

    } // NavigationTreeNode class

    private static final class NavigationTreeRenderer extends AbstractTreeCellRenderer {

        private final Color textBackground;
        private final Color textForeground;
        private final Color selectionBackground;
        private final Color selectionForeground;

        public NavigationTreeRenderer() {

            textBackground = UIManager.getColor("Tree.textBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");

            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            this.selected = selected;
            this.hasFocus = hasFocus;

            if (value instanceof NavigationTreeNode) {
                NavigationTreeNode treeNode = (NavigationTreeNode) value;

                setBackground(selected ? selectionBackground : textBackground);
                setForeground(selected ? selectionForeground : textForeground);

                setText(treeNode.nodeText);
                setIcon(GUIUtilities.loadIcon("icon_information"));
            }

            return this;
        }

    } // NavigationTreeRenderer class

    private final class NavigationTreeMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {

            TreePath path = navigationTree.getPathForLocation(e.getX(), e.getY());
            if (path == null || path != navigationTree.getSelectionPath())
                return;

            Object selectedComponent = navigationTree.getSelectionPath().getLastPathComponent();
            if (selectedComponent instanceof NavigationTreeNode) {
                NavigationTreeNode treeNode = (NavigationTreeNode) selectedComponent;
                editorPane.scrollToReference(treeNode.nodeText);
            }
        }

    } // NavigationTreeMouseAdapter class

    private final class InitialSearchKeyAdapter extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            setSearchPanelVisible(true);
            editorPane.getHighlighter().removeAllHighlights();
            searchField.setText(String.valueOf(e.getKeyChar()));
        }

    } // InitialSearchKeyAdapter class

    private final class SearchFieldKeyAdapter extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER)
                searcher.findNext();
        }

    } // SearchFieldKeyAdapter class

    private class SearchFieldDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            search();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            search();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            search();
        }

        private void search() {
            searcher.setFindText(searchField.getText().trim());
            editorPane.setCaretPosition(0);
            searcher.findNext();
        }

    } // SearchFieldDocumentListener class

}
