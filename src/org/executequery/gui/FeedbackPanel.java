/*
 * FeedbackPanel.java
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

package org.executequery.gui;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.repository.UserFeedback;
import org.executequery.repository.UserFeedbackRepository;
import org.executequery.repository.spi.UserFeedbackRepositoryImpl;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Base feedback panel for comments, requests and bugs.
 *
 * @author Takis Diakoumis
 */
public class FeedbackPanel extends DefaultActionButtonsPanel
        implements FocusComponentPanel,
        Interruptible {

    private static final String SYSTEM_PROPERTIES = "\n------------ SYSTEM  PROPERTIES -----------";
    private static final String USER_DESCRIPTION = "\n--------------- USER MESSAGE --------------";
    private static final String AUTO_GENERATED_REPORT = "\n---------- AUTO GENERATED REPORT ----------";

    public static final String DEFAULT_TITLE = bundledString("title");
    public static final String BUG_REPORT_TITLE = bundledString("reportBug");

    public static final int USER_COMMENTS = 0;
    public static final int BUG_REPORT = USER_COMMENTS + 1;
    public static final int FEATURE_REQUEST = BUG_REPORT + 1;

    private final Map<Integer, String> feedbackTypes;
    private final ActionContainer parent;

    // --- GUI components ---

    private JButton sendButton;
    private JButton cancelButton;
    private JTextField nameField;
    private JTextField emailField;
    private JTextArea commentsField;
    private JComboBox<?> feedbackTypeCombo;

    // ---

    private boolean cancelled;
    private SwingWorker worker;
    private UserFeedbackRepository repository;
    private InterruptibleProgressDialog progressDialog;

    public FeedbackPanel(ActionContainer parent) {
        this.parent = parent;

        feedbackTypes = new LinkedHashMap<>();
        feedbackTypes.put(USER_COMMENTS, bundledString("userComments"));
        feedbackTypes.put(BUG_REPORT, bundledString("reportBug"));
        feedbackTypes.put(FEATURE_REQUEST, bundledString("featureRequest"));

        init();
        arrange();
        addSystemProperties();
    }

    public FeedbackPanel(ActionContainer parent, String message, Vector<Throwable> throwableVector, Class<?> sourceClass) {
        this(parent);

        feedbackTypeCombo.setSelectedIndex(BUG_REPORT);
        feedbackTypeCombo.setEnabled(false);
        createBugReport(message, throwableVector, sourceClass);
    }

    private void init() {

        commentsField = new JTextArea();
        commentsField.setFont(UIManager.getDefaults().getFont("Label.font"));
        commentsField.setMargin(new Insets(2, 2, 2, 2));

        nameField = WidgetFactory.createTextField("nameField", getUserName());
        emailField = WidgetFactory.createTextField("emailField", getUserEmail());

        feedbackTypeCombo = WidgetFactory.createComboBox(
                "feedbackTypeCombo",
                feedbackTypes.values().toArray()
        );

        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> parent.finished()
        );

        sendButton = WidgetFactory.createButton(
                "sendButton",
                Bundles.get("common.send.button"),
                e -> send()
        );
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        mainPanel.add(
                new JLabel(generateLabelText()),
                gbh.fillHorizontally().setMinWeightY().spanX().get()
        );
        mainPanel.add(
                new JSeparator(JSeparator.HORIZONTAL),
                gbh.nextRow().bottomGap(5).get()
        );
        mainPanel.add(
                new JLabel(Bundles.getCommon("name")),
                gbh.nextRowFirstCol().setWidth(1).bottomGap(0).rightGap(0).setMinWeightX().get()
        );
        mainPanel.add(
                nameField,
                gbh.nextCol().setMaxWeightX().spanX().rightGap(5).get()
        );
        mainPanel.add(
                new JLabel("Email"),
                gbh.nextRowFirstCol().setWidth(1).rightGap(0).setMinWeightX().get()
        );
        mainPanel.add(
                emailField,
                gbh.nextCol().setMaxWeightX().rightGap(5).bottomGap(5).spanX().get()
        );
        mainPanel.add(
                new JSeparator(JSeparator.HORIZONTAL),
                gbh.nextRowFirstCol().get()
        );
        mainPanel.add(
                feedbackTypeCombo,
                gbh.nextRowFirstCol().bottomGap(0).get()
        );
        mainPanel.add(
                new JScrollPane(commentsField),
                gbh.nextRowFirstCol().setMaxWeightY().spanY().fillBoth().topGap(0).bottomGap(5).get()
        );

        // --- base ---

        addActionButton(sendButton);
        addActionButton(cancelButton);

        setPreferredSize(new Dimension(700, 480));
        addContentPanel(mainPanel);
    }

    private void addSystemProperties() {
        commentsField.append(SYSTEM_PROPERTIES);
        commentsField.append(String.format(
                "\n\nVersion: %s [ build %s ]\nJava VM: %s\nOS: %s [ %s : %s ]\n",
                System.getProperty("executequery.minor.version"),
                System.getProperty("executequery.build"),
                System.getProperty("java.runtime.version"),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch")
        ));
        commentsField.append(USER_DESCRIPTION);
        commentsField.append("\n\n");
    }

    private void createBugReport(String message, Vector<Throwable> throwableVector, Class<?> sourceClass) {
        commentsField.setText(Constants.EMPTY);

        commentsField.append(AUTO_GENERATED_REPORT);

        if (sourceClass != null) {
            commentsField.append("\n\n> EXCEPTION SOURCE\n");
            commentsField.append(sourceClass.getName() + " class");
        }

        if (message != null) {
            commentsField.append("\n\n> SYSTEM RETURNED\n");
            commentsField.append(message);
        }

        if (!MiscUtils.isEmpty(throwableVector)) {
            StringWriter writer = new StringWriter();
            throwableVector.forEach(throwable -> throwable.printStackTrace(new PrintWriter(writer)));

            commentsField.append("\n\n> STACK TRACE\n");
            commentsField.append(writer.toString());
        }

        addSystemProperties();
    }

    private String getUserEmail() {
        return SystemProperties.containsKey(Constants.USER_PROPERTIES_KEY, "user.email.address") ?
                SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "user.email.address") :
                Constants.EMPTY;
    }

    private String getUserName() {

        if (SystemProperties.containsKey(Constants.USER_PROPERTIES_KEY, "user.full.name"))
            return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "user.full.name");

        if (SystemProperties.containsKey(Constants.USER_PROPERTIES_KEY, "reddatabase.user"))
            return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "reddatabase.user");

        return System.getProperty("user.name");
    }

    private String generateLabelText() {
        return "<html>" + Constants.TABLE_TAG_START +
                "<tr><td>" +
                bundledString(feedbackTypeCombo.getSelectedIndex() == BUG_REPORT ?
                        "reportBugLabel" :
                        "completeFields"
                ) +
                "</td></tr>" +
                "<tr><td>" + bundledString("infoAttachments") + "</td></tr>" +
                "<tr><td>" + bundledString("warningInternetConnection") + "</td></tr>"
                + Constants.TABLE_TAG_END + "</html>";
    }

    private void send() {

        if (!fieldsValid())
            return;

        worker = new SwingWorker("sendFeedback") {

            @Override
            public Object construct() {
                parent.block();
                return doWork();
            }

            @Override
            public void interrupt() {

                if (parent.isDialog()) {
                    parent.unblock();
                    ((JDialog) parent).setVisible(true);
                    return;
                }
                super.interrupt();
            }

            @Override
            public void finished() {

                Object result = get();
                if (!cancelled && result == Constants.WORKER_SUCCESS) {
                    closeProgressDialog();
                    GUIUtilities.displayInformationMessage(bundledString("messageSuccess"));

                } else if (cancelled || result == Constants.WORKER_FAIL || result == Constants.WORKER_CANCEL) {

                    if (parent.isDialog()) {
                        parent.unblock();
                        ((JDialog) parent).setVisible(true);
                        closeProgressDialog();
                        return;
                    }

                }

                GUIUtilities.showNormalCursor();
                parent.finished();
            }
        };

        progressDialog = new InterruptibleProgressDialog(
                GUIUtilities.getParentFrame(),
                bundledString("postingFeedback"),
                bundledString("postingFeedbackMessage"),
                this
        );

        worker.start();
        progressDialog.run();
    }

    private boolean fieldsValid() {

        if (MiscUtils.isNull(emailField.getText())) {
            GUIUtilities.displayErrorMessage(bundledString("noEmailErrorMessage"));
            return false;
        }

        if (MiscUtils.isNull(commentsField.getText())) {
            GUIUtilities.displayErrorMessage(bundledString("noRemarksErrorMessage"));
            return false;
        }

        if (MiscUtils.isNull(nameField.getText())) {
            GUIUtilities.displayErrorMessage(bundledString("noNameErrorMessage"));
            return false;
        }

        return true;
    }

    private Object doWork() {
        repository = new UserFeedbackRepositoryImpl();
        return resultWork(repository.postFeedback(createUserFeedback()));
    }

    private Object resultWork(int result) {

        if (result == 1)
            return Constants.WORKER_SUCCESS;

        if (result == 401) {
            SystemProperties.setStringProperty("user", "reddatabase.token", "");
            GUIUtilities.loadAuthorisationInfo();

            return resultWork(repository.postFeedback(createUserFeedback()));
        }

        return Constants.WORKER_FAIL;
    }

    private UserFeedback createUserFeedback() {
        return new UserFeedback(
                nameField.getText().trim(),
                emailField.getText().trim(),
                commentsField.getText().trim(),
                String.valueOf(feedbackTypeCombo.getSelectedIndex())
        );
    }

    private void closeProgressDialog() {
        SwingUtilities.invokeLater(() -> {
            if (progressDialog != null && progressDialog.isVisible())
                progressDialog.dispose();
            progressDialog = null;
        });
    }

    protected static String bundledString(String key) {
        return Bundles.get(FeedbackPanel.class, key);
    }

    // --- Interruptible impl ---

    /**
     * Sets the process cancel flag as specified.
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled)
            repository.cancel();
    }

    /**
     * Indicates thatthis process should be interrupted.
     */
    @Override
    public void interrupt() {
        worker.interrupt();
    }

    // --- FocusComponentPanel impl ---

    /**
     * Returns the default focus component of this object.
     */
    @Override
    public Component getDefaultFocusComponent() {
        return nameField;
    }

}
