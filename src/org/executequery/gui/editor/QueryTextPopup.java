/*
 * QueryTextPopup.java
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

package org.executequery.gui.editor;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlMessages;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Popup panel for the results panel tab rollovers.
 *
 * @author Takis Diakoumis
 */
class QueryTextPopup extends JPanel {

    private static final int WIDTH = 450;
    private static final int HEIGHT = 240;

    private final MouseListener mouseListener;
    private final QueryEditorResultsPanel parent;

    // --- GUI components ---

    private RolloverButton previousQueryButton;
    private RolloverButton nextQueryButton;
    private RolloverButton copyQueryButton;
    private RolloverButton goToQueryButton;
    private RolloverButton hidePopupButton;

    private JLabel bottomLabel;
    private SQLTextArea textPane;

    // ---

    private int index;
    private int timeout;
    private int mousePosX;
    private int mousePosY;

    private boolean timerStarted;
    private boolean mouseOverPanel;

    private Timer timer;
    private String query;
    private String displayQuery;

    public QueryTextPopup(QueryEditorResultsPanel parent) {
        super(new BorderLayout());
        this.parent = parent;
        this.mouseListener = new PopupMouseListener();

        init();
        arrange();
    }

    private void init() {

        previousQueryButton = WidgetFactory.createRolloverButton(
                "previousQueryButton",
                bundleString("PreviousExecutedResultSet"),
                "Previous16",
                e -> previousQuery());
        previousQueryButton.addMouseListener(mouseListener);

        nextQueryButton = WidgetFactory.createRolloverButton(
                "nextQueryButton",
                bundleString("NextExecutedResultSet"),
                "Forward16",
                e -> nextQuery());
        nextQueryButton.addMouseListener(mouseListener);

        copyQueryButton = WidgetFactory.createRolloverButton(
                "copyQueryButton",
                bundleString("Copy"),
                "Copy16",
                e -> copyQuery());
        copyQueryButton.addMouseListener(mouseListener);

        goToQueryButton = WidgetFactory.createRolloverButton(
                "goToQueryButton",
                bundleString("GoTo"),
                "GoToResultSetQuery16",
                e -> goToQuery());
        goToQueryButton.addMouseListener(mouseListener);

        hidePopupButton = WidgetFactory.createRolloverButton(
                "hidePopupButton",
                bundleString("hidePopup"),
                "Close16",
                e -> hidePopup());
        hidePopupButton.addMouseListener(mouseListener);

        textPane = new SQLTextArea();
        textPane.setBackground(UIUtils.getColour("executequery.QueryEditor.queryTooltipBackground", new Color(255, 255, 235)));
        textPane.addMouseListener(mouseListener);
        textPane.setEditable(false);

        bottomLabel = new JLabel();
        bottomLabel.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 2));
        bottomLabel.addMouseListener(mouseListener);
    }

    private void arrange() {
        GridBagHelper gbh;
        Color shadow = UIUtils.getColour("controlDkShadow", Color.DARK_GRAY);

        // --- scroll pane ---

        JScrollPane scrollPane = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.addMouseListener(mouseListener);

        // --- buttons panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, shadow));
        buttonPanel.addMouseListener(mouseListener);

        gbh = new GridBagHelper().anchorNorthWest();
        buttonPanel.add(previousQueryButton, gbh.get());
        buttonPanel.add(nextQueryButton, gbh.nextCol().leftGap(5).get());
        buttonPanel.add(copyQueryButton, gbh.nextCol().get());
        buttonPanel.add(goToQueryButton, gbh.nextCol().get());
        buttonPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().get());
        buttonPanel.add(hidePopupButton, gbh.nextCol().setMinWeightX().get());

        // --- label panel ---

        JPanel labelPanel = new JPanel(new GridBagLayout());
        labelPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, shadow));
        labelPanel.addMouseListener(mouseListener);

        gbh = new GridBagHelper().anchorNorthWest();
        labelPanel.add(bottomLabel, gbh.spanX().get());

        // --- base ---

        setVisible(false);
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createLineBorder(shadow));

        gbh = new GridBagHelper().fillBoth().spanX();
        add(buttonPanel, gbh.setMinWeightY().get());
        add(scrollPane, gbh.nextRow().setMaxWeightY().get());
        add(labelPanel, gbh.nextRow().setMinWeightY().get());
    }

    private void enableScrollButtons() {

        if (parent.hasOutputPane()) {
            previousQueryButton.setEnabled(index > 1);
            nextQueryButton.setEnabled(index < parent.getResultSetTabCount());

        } else {
            previousQueryButton.setEnabled(index > 0);
            nextQueryButton.setEnabled(index < parent.getResultSetTabCount() - 1);
        }
    }

    // --- buttons handlers ---

    private void previousQuery() {
        int previousIndex = index - 1;
        showPopup(
                -1, -1,
                parent.getQueryTextAt(previousIndex),
                parent.getTitleAt(previousIndex),
                previousIndex
        );
    }

    private void nextQuery() {
        int nextIndex = index + 1;
        showPopup(
                -1, -1,
                parent.getQueryTextAt(nextIndex),
                parent.getTitleAt(nextIndex),
                nextIndex
        );
    }

    private void copyQuery() {
        SwingUtilities.invokeLater(() -> {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(query), null);
            hidePanel();
        });
    }

    private void goToQuery() {
        SwingUtilities.invokeLater(() -> {
            hidePanel();
            parent.caretToQuery(query);
        });
    }

    private void hidePopup() {
        SwingUtilities.invokeLater(this::hidePanel);
    }

    // ---

    public void showPopup(int xPos, int yPos, String query, String label, int index) {
        stopTimer();

        this.index = index;
        this.query = query.trim();
        this.mouseOverPanel = false;
        this.displayQuery = query.replaceAll(SqlMessages.BLOCK_COMMENT_REGEX, Constants.EMPTY).trim();

        bottomLabel.setText(label);
        enableScrollButtons();

        if (isVisible()) {
            textPane.setText(displayQuery);

            if (xPos > 0 && yPos > 0) {
                timeout = 2500;
                startHideTimer();
            }
            return;
        }

        mousePosX = xPos;
        mousePosY = yPos;
        timeout = 750;
        startShowTimer();
    }

    private void showPanel() {

        textPane.setText(displayQuery);

        Frame frame = GUIUtilities.getParentFrame();
        Point mousePoint = SwingUtilities.convertPoint(parent, mousePosX, mousePosY, frame);

        int xPos = mousePoint.x;
        int yPos = mousePoint.y - HEIGHT - 30;
        setBounds(new Rectangle(xPos, yPos, WIDTH, HEIGHT));

        if (!isVisible()) {
            setVisible(true);
            parent.addPopupComponent(this);
        }

        mouseOverPanel = false;
        timeout = 2500;
        startHideTimer();
    }

    public void dispose() {
        textPane.cleanup();
        if (mouseOverPanel)
            return;

        stopTimer();
        timeout = 500;
        startHideTimer();
    }

    public void forceDispose() {
        if (mouseOverPanel)
            return;

        stopTimer();
        hidePanel();
    }

    private void startShowTimer() {
        TimerTask showPanel = new TimerTask() {

            @Override
            public void run() {
                EventQueue.invokeLater(() -> showPanel());
            }
        };

        timer = new Timer();
        timerStarted = true;
        timer.schedule(showPanel, timeout);
    }

    private void startHideTimer() {
        TimerTask hidePanel = new TimerTask() {

            @Override
            public void run() {
                EventQueue.invokeLater(() -> hidePanel());
            }
        };

        timer = new Timer();
        timerStarted = true;
        timer.schedule(hidePanel, timeout);
    }

    private void stopTimer() {
        if (timer == null || !timerStarted)
            return;
        timer.cancel();
        timerStarted = false;
    }

    private void hidePanel() {
        setVisible(false);
        parent.removePopupComponent(this);
        timerStarted = false;
        mouseOverPanel = false;
    }

    private static String bundleString(String key) {
        return Bundles.get(QueryTextPopup.class, key);
    }

    private final class PopupMouseListener extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent e) {
            stopTimer();
            mouseOverPanel = true;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            mouseOverPanel = false;
            startHideTimer();
        }

    } // PopupMouseListener class

}
