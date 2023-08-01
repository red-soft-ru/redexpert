package org.executequery.gui.browser.profiler;

import org.executequery.gui.BaseDialog;
import org.executequery.localization.Bundles;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfilerTabPopupMenu extends JPopupMenu implements ActionListener {

    private final JMenuItem copy;
    private final JMenuItem show;
    private final JTree tree;

    public ProfilerTabPopupMenu(JTree tree) {

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

            if (source.equals(copy)) copy(path);
            else if (source.equals(show)) show(path);

        }
    }

    private void copy(TreePath path) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(path.getLastPathComponent().toString()), null);
    }

    private void show(TreePath path) {

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
