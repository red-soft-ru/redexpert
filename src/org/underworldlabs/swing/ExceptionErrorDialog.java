/*
 * ExceptionErrorDialog.java
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

package org.underworldlabs.swing;

import org.executequery.actions.helpcommands.FeedbackCommand;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Generic error dialog box displaying the stack trace.
 *
 * @author Takis Diakoumis
 */
public class ExceptionErrorDialog extends AbstractBaseDialog {

    private static final int STACK_HEIGHT = 220;
    private static final int DEFAULT_WIDTH = 700;

    private static final String HTML_ROW = "<tr><td>%s</td></tr>\n";
    private static final String HTML_MESSAGE = "<html><table border=\"0\" cellpadding=\"2\">%s</table></html>";

    private final String message;
    private final Class<?> sourceClass;
    private final Vector<Throwable> exceptions;

    // --- GUI components ---

    private JTextArea textPane;
    private JPanel stackTracePanel;

    private JButton showStackButton;
    private JButton sendReportButton;
    private JButton nextButton;
    private JButton previousButton;
    private JButton copyButton;

    // ---

    private int defaultHeight;
    private int selectedIndex;
    private Throwable noMoreExceptions;

    public ExceptionErrorDialog(Frame owner, String message, Throwable exception) {
        this(owner, message, exception, null);
    }

    public ExceptionErrorDialog(Frame owner, String message, Throwable exception, Class<?> sourceClass) {
        super(owner, Bundles.getCommon("error-message"), true);
        this.message = message;
        this.sourceClass = sourceClass;

        exceptions = new Vector<>();
        exceptions.add(exception);
        selectedIndex = 0;

        init();
        arrange();
    }

    private void init() {

        stackTracePanel = new JPanel(new GridBagLayout());
        stackTracePanel.setVisible(false);

        showStackButton = WidgetFactory.createDefaultButton(
                "showStackButton",
                bundleString("ShowStackTrace"),
                e -> showHideStack()
        );

        sendReportButton = WidgetFactory.createDefaultButton(
                "sendReportButton",
                bundleString("sendReportButton"),
                e -> new FeedbackCommand().bugReport(message, exceptions, sourceClass)
        );

        copyButton = WidgetFactory.createRolloverButton(
                "copyButton",
                bundleString("pasteToClipboard"),
                "icon_paste",
                e -> Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(textPane.getText()), null)
        );

        nextButton = WidgetFactory.createDefaultButton(
                "nextButton",
                bundleString("NextException"),
                e -> showNextStack()
        );

        previousButton = WidgetFactory.createDefaultButton(
                "previousButton",
                bundleString("PreviousException"),
                e -> showPrewStack()
        );
        previousButton.setEnabled(false);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- exception icon ---

        Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
        if (errorIcon == null)
            errorIcon = UIManager.getIcon("OptionPane.warningIcon");

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper();
        buttonPanel.add(showStackButton, gbh.nextCol().fillNone().setMinWeightX().get());
        buttonPanel.add(sendReportButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 20, 0);
        mainPanel.add(new JLabel(errorIcon), gbh.get());
        mainPanel.add(new ExceptionMessageLabel(message), gbh.nextCol().rightGap(5).leftGap(0).setMaxWeightX().fillHorizontally().spanX().get());
        mainPanel.add(buttonPanel, gbh.anchorEast().fillNone().leftGap(5).bottomGap(5).nextRowFirstCol().spanX().get());
        mainPanel.add(stackTracePanel, gbh.nextRowFirstCol().topGap(0).setMaxWeightY().fillBoth().get());

        // --- base ---

        add(mainPanel);
        pack();

        defaultHeight = getHeight();
        int defaultWidth = Math.max(Math.min(getPreferredSize().width, Toolkit.getDefaultToolkit().getScreenSize().width / 2), DEFAULT_WIDTH);
        setMinimumSize(new Dimension(defaultWidth, defaultHeight));
        setSize(getMinimumSize());

        Point location = GUIUtils.getPointToCenter(getOwner(), getSize());
        location.y -= (STACK_HEIGHT / 2);

        setLocation(location);
        setVisible(true);
    }

    /**
     * Builds the stack trace text pane and associated buttons in the case of SQLExceptions.
     */
    private void buildStackTracePanel() {

        if (textPane != null)
            return;

        textPane = new JTextArea();
        textPane.setMargin(new Insets(2, 2, 2, 2));
        textPane.setEditable(false);
        textPane.setWrapStyleWord(false);
        textPane.setBackground(getBackground());
        textPane.setFont(UIManager.getDefaults().getFont("Label.font"));

        GridBagHelper gbh = new GridBagHelper();
        stackTracePanel.add(new JScrollPane(textPane), gbh.fillBoth().setMaxWeightY().spanX().get());
        stackTracePanel.add(copyButton, gbh.nextRow().topGap(5).fillNone().setMinWeightY().get());

        if (exceptions.get(selectedIndex) instanceof SQLException) {
            SQLException exception = (SQLException) exceptions.get(selectedIndex);
            if (exception.getNextException() != null) {

                stackTracePanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().fillHorizontally().get());
                stackTracePanel.add(previousButton, gbh.nextCol().setMinWeightX().get());
                stackTracePanel.add(nextButton, gbh.nextCol().get());
            }
        }
    }

    /**
     * Prints the specified exception's stack trace
     * to the text pane.
     *
     * @param throwable - the exception to be printed
     */
    private void printException(Throwable throwable) {

        if (throwable != null && throwable != noMoreExceptions) {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            textPane.setText(writer.toString());

        } else
            textPane.setText(bundleString("stackNotAvailable"));

        textPane.setCaretPosition(0);
    }

    private void showHideStack() {
        buildStackTracePanel();

        if (stackTracePanel.isVisible()) {
            stackTracePanel.setVisible(false);
            showStackButton.setText(bundleString("ShowStackTrace"));
            setSize(new Dimension(getWidth(), defaultHeight));

        } else {
            stackTracePanel.setVisible(true);
            showStackButton.setText(bundleString("HideStackTrace"));
            setSize(new Dimension(getWidth(), defaultHeight + STACK_HEIGHT + 30));

            printException(exceptions.get(selectedIndex));
        }
    }

    private void showNextStack() {
        selectedIndex++;

        if (exceptions.size() - 1 < selectedIndex) {
            SQLException sqlException = (SQLException) exceptions.get(selectedIndex - 1);
            SQLException nextSQLException = sqlException.getNextException();

            if (nextSQLException == null) {
                if (noMoreExceptions == null) {
                    noMoreExceptions = new Throwable();
                    exceptions.add(noMoreExceptions);
                }

            } else
                exceptions.add(nextSQLException);
        }

        Throwable currentException = exceptions.get(selectedIndex);
        printException(currentException);

        if (currentException == noMoreExceptions || currentException == null)
            nextButton.setEnabled(false);
        previousButton.setEnabled(true);
    }

    private void showPrewStack() {
        selectedIndex--;

        Throwable currentException = exceptions.get(selectedIndex);
        printException(currentException);

        if (selectedIndex == 0)
            previousButton.setEnabled(false);
        nextButton.setEnabled(true);
    }

    private String bundleString(String key) {
        return Bundles.get(ExceptionErrorDialog.class, key);
    }

    // ---

    private class ExceptionMessageLabel extends JLabel implements ComponentListener {
        private final List<ExceptionMessage> messages;

        public ExceptionMessageLabel(String originalMessage) {
            super();
            this.messages = parseMessage(originalMessage);
            addComponentListener(this);
            update();
        }

        public List<ExceptionMessage> parseMessage(String originalMessage) {

            String lineBreak = "\n";
            boolean hasLineBreak = true;
            List<ExceptionMessage> parsedMessage = new LinkedList<>();

            StringTokenizer tokenizer = new StringTokenizer(originalMessage, lineBreak, true);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();

                if (lineBreak.equals(token)) {
                    if (hasLineBreak)
                        parsedMessage.add(new ExceptionMessage());

                    hasLineBreak = true;
                    continue;
                }

                parsedMessage.add(new ExceptionMessage(token.trim()));
                hasLineBreak = false;
            }

            return parsedMessage;
        }

        private void update() {
            ExceptionMessage.setProperties(
                    getFontMetrics(getFont()),
                    ExceptionErrorDialog.this.getWidth() - 100,
                    getInsets().left + getInsets().right
            );

            String preparedMessage = messages.stream()
                    .map(ExceptionMessage::getPrepareMessage)
                    .collect(Collectors.joining());

            setText(String.format(HTML_MESSAGE, preparedMessage));
        }

        // --- ComponentListener impl ---

        @Override
        public void componentResized(ComponentEvent e) {
            update();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

    } // ExceptionMessageLabel class

    private static class ExceptionMessage {
        private static final String ELLIPSIS = "...";

        private static FontMetrics fontMetrics;
        private static int availableWidth;
        private final String message;

        public ExceptionMessage() {
            this(null);
        }

        public ExceptionMessage(String message) {
            this.message = message;
        }

        public static void setProperties(FontMetrics fontMetrics, int availableWidth, int insets) {
            ExceptionMessage.fontMetrics = fontMetrics;
            ExceptionMessage.availableWidth = availableWidth - insets;
        }

        public String getPrepareMessage() {

            if (MiscUtils.isNull(message))
                return String.format(HTML_ROW, "");

            String preparedMessage = message;
            int textWidth = fontMetrics.stringWidth(message);

            if (textWidth > availableWidth) {
                int ellipsisWidth = fontMetrics.stringWidth(ELLIPSIS);

                do {
                    preparedMessage = clipText(preparedMessage);
                    textWidth = fontMetrics.stringWidth(preparedMessage) + ellipsisWidth;

                } while (!preparedMessage.isEmpty() && textWidth >= availableWidth);

                preparedMessage += ELLIPSIS;
            }

            return String.format(HTML_ROW, preparedMessage);
        }

        private String clipText(String text) {
            return text.substring(0, text.length() - 1);
        }

    } // ExceptionMessage class

}
