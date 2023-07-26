package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    private final Component component;

    public ProfilerTreeCellRenderer(Component component) {
        this.component = component;
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        ProfilerTreeTableNode node = (ProfilerTreeTableNode) value;
        setText((String) node.getProcessName());

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    new TabPopupMenu(tree).show(component, e.getX(), e.getY());
            }
        });

        switch ((String) node.getProcessType()) {
            case "BLOCK":
                setIcon(GUIUtilities.loadIcon("CreateScripts16.png"));
                break;
            case "FUNCTION":
                setIcon(GUIUtilities.loadIcon("Function16.png"));
                break;
            case "PROCEDURE":
                setIcon(GUIUtilities.loadIcon("Procedure16.png"));
                break;
            case "SELF_TIME":
                setIcon(GUIUtilities.loadIcon("Information16.png"));
                break;
            case "ROOT":
                setIcon(GUIUtilities.loadIcon("JDBCDriver16.png"));
                break;
            default:
                setIcon(GUIUtilities.loadIcon("DefaultFile16.png"));
                break;
        }

        return this;
    }

    private static class TabPopupMenu extends JPopupMenu implements ActionListener {

        private final JMenuItem copy;
        private final JMenuItem show;

        private final JTree tree;

        public TabPopupMenu(JTree tree) {

            this.tree = tree;

            copy = new JMenuItem(Bundles.get("common.copy"));
            show = new JMenuItem(Bundles.get("common.show"));

            copy.addActionListener(this);
            show.addActionListener(this);

            add(copy);
            add(show);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            TreePath path = tree.getSelectionPath();
            if (path != null) {

                Object source = e.getSource();
                if (source.equals(copy)) {

                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(path.getLastPathComponent().toString()), null);

                } else if (source.equals(show)) {

                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    textArea.setText(path.getLastPathComponent().toString());

                    JPanel basePanel = new JPanel(new BorderLayout());
                    basePanel.setPreferredSize(new Dimension(500, 200));
                    basePanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

                    BaseDialog dialog = new BaseDialog(Bundles.get("ResultSetTablePopupMenu.RecordDataItemViewer"), true);
                    dialog.setMinimumSize(basePanel.getPreferredSize());
                    dialog.addDisplayComponentWithEmptyBorder(basePanel);
                    dialog.display();

                }

            }
        }

    } // class TabPopupMenu


}
