package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.stream.Collectors;

public class ProfilerTabPopupMenu extends JPopupMenu implements ActionListener {

    private final JMenuItem copy;
    private final JMenuItem show;
    private final JMenuItem info;
    private final JTree tree;

    public ProfilerTabPopupMenu(JTree tree) {

        this.tree = tree;

        copy = new JMenuItem(Bundles.get("common.copy"));
        show = new JMenuItem(Bundles.get("common.show"));
        info = new JMenuItem(Bundles.get("common.info"));

        copy.addActionListener(this);
        show.addActionListener(this);
        info.addActionListener(this);

        add(copy);
        add(show);
        add(info);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        TreePath path = tree.getSelectionPath();
        if (path != null) {
            Object source = e.getSource();

            if (source.equals(copy)) copy(path);
            else if (source.equals(show)) show(path);
            else if (source.equals(info)) info(path);

        }
    }

    private void copy(TreePath path) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(path.getLastPathComponent().toString()), null);
    }

    private void show(TreePath path) {

        SimpleSqlTextPanel textArea = new SimpleSqlTextPanel();
        textArea.getTextPane().setEditable(false);
        textArea.getTextPane().setText(path.getLastPathComponent().toString());

        if (path.getLastPathComponent() instanceof ProfilerTreeTableNode) {
            ProfilerTreeTableNode node = (ProfilerTreeTableNode) path.getLastPathComponent();
            if (node.getData().getSourceCode() != null)
                textArea.getTextPane().setText(node.getData().getSourceCode());
        }

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setPreferredSize(new Dimension(700, 500));
        basePanel.add(textArea, BorderLayout.CENTER);

        BaseDialog dialog = new BaseDialog(Bundles.get("ProfilerTabPopupMenu.RecordDataItemViewer"), true);
        dialog.setMinimumSize(basePanel.getPreferredSize());
        dialog.addDisplayComponentWithEmptyBorder(basePanel);
        dialog.display();
    }

    private void info(TreePath path) {

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText(path.getLastPathComponent().toString());

        if (path.getLastPathComponent() instanceof ProfilerTreeTableNode) {

            ProfilerTreeTableNode node = (ProfilerTreeTableNode) path.getLastPathComponent();
            if (node.getProcessType().equals(ProfilerData.SELF_TIME) || node.getProcessType().equals(ProfilerData.ROOT)) {
                GUIUtilities.displayWarningMessage(Bundles.get("ProfilerTabPopupMenu.NoInformationForSelectedNode"));
                return;
            }

            textArea.setText("ID: " + node.getData().getId() +
                    "\nCaller ID: " + node.getData().getCallerId() +
                    "\nType: " + node.getData().getProcessType() +
                    "\nProcess: " + node.getData().getProcessName().trim() +
                    "\nCalls count: " + node.getData().getCallCount() +
                    "\nTotal time: " + node.getData().getTotalTime() +
                    "\nAvg time: " + node.getData().getAvgTime() +
                    (node.getData().getSourceCode() == null ? "" :
                            "\nSource code:\n---\n" + node.getData().getSourceCode() + "\n---") +
                    (node.getData().getPsqlStats() == null ? "" :
                            "\nPSQL: " + node.getData().getPsqlStats().stream()
                                    .map(i -> "\n\tline: " + i.getNumber() +
                                            "\n\tstr: " + i.getString() +
                                            "\n\tcalls count: " + i.getCallCount() +
                                            "\n\ttotal time: " + i.getTotalTime() +
                                            "\n\tavg time: " + i.getAvgTime()
                                    ).collect(Collectors.joining("\n", "{", "\n}")))
            );
        }

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setPreferredSize(new Dimension(700, 500));
        basePanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        BaseDialog dialog = new BaseDialog(Bundles.get("ProfilerTabPopupMenu.RecordDataItemViewer"), true);
        dialog.setMinimumSize(basePanel.getPreferredSize());
        dialog.addDisplayComponentWithEmptyBorder(basePanel);
        dialog.display();
    }

}
