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
import org.executequery.repository.RepositoryException;
import org.executequery.repository.UserFeedback;
import org.executequery.repository.UserFeedbackRepository;
import org.executequery.repository.spi.UserFeedbackRepositoryImpl;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base feedback panel for comments, requests and bugs.
 *
 * @author Takis Diakoumis
 */
public class FeedbackPanel extends DefaultActionButtonsPanel
        implements ActionListener,
        FocusComponentPanel,
        Interruptible {

    /**
     * user comments feedback indicator
     */
    public static final int USER_COMMENTS = 0;

    /**
     * feature request feedback indicator
     */
    public static final int FEATURE_REQUEST = 2;

    /**
     * bug report feedback indicator
     */
    public static final int BUG_REPORT = 1;

    /**
     * the feedback type for the instance
     */
    private int feedbackType;

    /**
     * user's name field
     */
    private JTextField nameField;

    /**
     * user's email field
     */
    private JTextField emailField;

    /**
     * user's comments field
     */

    private JTextArea commentsField;

    /**
     * the parent container
     */
    private ActionContainer parent;

    /**
     * Thread worker object
     */
    private SwingWorker worker;

    /**
     * The progress dialog
     */
    private InterruptibleProgressDialog progressDialog;

    /**
     * Creates a new instance of FeedbackPanel
     */
    public FeedbackPanel(ActionContainer parent, int feedbackType) {

        this.parent = parent;
        this.feedbackType = feedbackType;

        init();
    }

    private void init() {

        String labelText = generateLabelText();

        commentsField = new JTextArea(createHeader());
        commentsField.setFont(UIManager.getDefaults().getFont("Label.font"));
        commentsField.setMargin(new Insets(2, 2, 2, 2));
        commentsField.setLineWrap(true);
        commentsField.setWrapStyleWord(true);

        nameField = WidgetFactory.createTextField();
        emailField = WidgetFactory.createTextField();
        initFieldValues();

        JPanel basePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy++;
        gbc.gridx++;
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        basePanel.add(new JLabel(labelText), gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.top = 7;
        gbc.insets.right = 10;
        gbc.fill = GridBagConstraints.NONE;
        basePanel.add(new JLabel(Bundles.getCommon("name")), gbc);
        gbc.gridx++;
        gbc.insets.left = 0;
        gbc.weightx = 1.0;
        gbc.insets.top = 5;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePanel.add(nameField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets.top = 2;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.right = 10;
        basePanel.add(new JLabel("Email"), gbc);
        gbc.gridx++;
        gbc.insets.left = 0;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        basePanel.add(emailField, gbc);


        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 0;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        basePanel.add(new JScrollPane(commentsField), gbc);

        JButton cancelButton = WidgetFactory.createButton(Bundles.get("common.cancel.button"));
        cancelButton.setActionCommand("cancel");
        JButton sendButton = WidgetFactory.createButton(Bundles.get("common.send.button"));
        sendButton.setActionCommand("Send");

        sendButton.addActionListener(this);
        cancelButton.addActionListener(this);

        addActionButton(sendButton);
        addActionButton(cancelButton);

        setPreferredSize(new Dimension(700, 480));

        addContentPanel(basePanel);
    }

    private void initFieldValues() {
        if (hasUserFullName()) {

            nameField.setText(getUserFullName());

        } else {

            nameField.setText(System.getProperty("user.name"));
        }
        if (hasUserEmail()) {

            emailField.setText(getUserEmail());
        }
    }

    private String getUserEmail() {

        return SystemProperties.getProperty(
                Constants.USER_PROPERTIES_KEY, "user.email.address");
    }

    private boolean hasUserEmail() {

        return SystemProperties.containsKey(
                Constants.USER_PROPERTIES_KEY, "user.email.address");
    }

    private String getUserFullName() {

        return SystemProperties.getProperty(
                Constants.USER_PROPERTIES_KEY, "user.full.name");
    }

    private boolean hasUserFullName() {

        return SystemProperties.containsKey(
                Constants.USER_PROPERTIES_KEY, "user.full.name");
    }

    private String createHeader() {

        StringBuilder sb = new StringBuilder();

        sb.append("---\nVersion: ");
        sb.append(System.getProperty("executequery.minor.version"));
        sb.append(" [ Build ");
        sb.append(System.getProperty("executequery.build"));
        sb.append(" ]\nJava VM: ");
        sb.append(System.getProperty("java.runtime.version"));
        sb.append("\nOperating System: ");
        sb.append(System.getProperty("os.name"));
        sb.append(" [ ");
        sb.append(System.getProperty("os.version"));
        sb.append(" ]\n---\n\n");

        return sb.toString();
    }

    private String generateLabelText() {

        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td>");

        switch (feedbackType) {
            case BUG_REPORT:
                sb.append(bundledString("bugReport"));
                break;
            case FEATURE_REQUEST:
            case USER_COMMENTS:
            default:
                sb.append(bundledString("completeFields"));
                break;
        }

        sb.append("</td></tr><tr><td>");
        sb.append(bundledString("infoAttachments"));
        sb.append("</td></tr><tr><td>");
        sb.append(bundledString("warningInternetConnection"));
        sb.append("</td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("</html>");

        return sb.toString();
    }

    private void send() {

        if (!fieldsValid()) {

            return;
        }

        worker = new SwingWorker() {

            public Object construct() {

                parent.block();

                return doWork();
            }

            @Override
            public void interrupt() {

                if (parent.isDialog()) {

                    parent.unblock();

                    JDialog dialog = (JDialog) parent;
                    dialog.setVisible(true);
                    return;
                }
                super.interrupt();
            }

            public void finished() {

                Object result = get();

                if (!cancelled && result == Constants.WORKER_SUCCESS) {

                    closeProgressDialog();

                    GUIUtilities.displayInformationMessage(
                            bundledString("messageSuccess"));

                } else if (cancelled || result == Constants.WORKER_FAIL ||
                        result == Constants.WORKER_CANCEL) {

                    if (parent.isDialog()) {

                        parent.unblock();

                        JDialog dialog = (JDialog) parent;
                        dialog.setVisible(true);
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
                this);

        worker.start();
        progressDialog.run();
    }

    private boolean fieldsValid() {
        String email = emailField.getText();

        if (MiscUtils.isNull(email)) {

            GUIUtilities.displayErrorMessage(
                    noEmailErrorMessage());
            return false;


        }
        String comments = commentsField.getText();

        if (MiscUtils.isNull(comments)) {

            GUIUtilities.displayErrorMessage(noRemarksErrorMessage());

            return false;
        }
        String name = nameField.getText();

        if (MiscUtils.isNull(name)) {

            GUIUtilities.displayErrorMessage(noNameErrorMessage());

            return false;
        }

        return true;
    }

    private String noRemarksErrorMessage() {
        return bundledString("noRemarksErrorMessage");
    }

    private String noNameErrorMessage() {
        return bundledString("noNameErrorMessage");
    }

    private String noEmailErrorMessage() {
        return bundledString("noEmailErrorMessage");
    }

    private Object doWork() {

        repository = createFeedbackRepository();

        try {

            UserFeedback userFeedback = createUserFeedback();
            return resultWork(repository.postFeedback(userFeedback));
        } catch (RepositoryException e) {

            if (cancelled) {

                return Constants.WORKER_CANCEL;
            }

            showError(e.getMessage());

            return Constants.WORKER_FAIL;
        }

    }

    private Object resultWork(int res) {
        if (res == 1)
            return Constants.WORKER_SUCCESS;
        else {
            if (res == 401) {
                SystemProperties.setStringProperty("user", "reddatabase.token", "");
                GUIUtilities.loadAuthorisationInfo();
                UserFeedback userFeedback = createUserFeedback();
                return resultWork(repository.postFeedback(userFeedback));
            }
            return Constants.WORKER_FAIL;
        }
    }

    private UserFeedbackRepository createFeedbackRepository() {

        return new UserFeedbackRepositoryImpl();
    }

    private UserFeedback createUserFeedback() {

        String typeString = "" + feedbackType;


        return new UserFeedback(nameField.getText(), emailField.getText(),
                commentsField.getText(),
                typeString);
    }

    private void showError(String message) {

        closeProgressDialog();

        GUIUtilities.showNormalCursor();
        GUIUtilities.displayErrorMessage(message);
    }

    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();

        if (command.equals("Send")) {

            send();

        } else {

            parent.finished();
        }

    }

    private void closeProgressDialog() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (progressDialog != null && progressDialog.isVisible()) {
                    progressDialog.dispose();
                }
                progressDialog = null;
            }
        });
    }

    /**
     * process cancelled flag
     */
    private boolean cancelled;

    private UserFeedbackRepository repository;

    /**
     * Sets the process cancel flag as specified.
     */
    public void setCancelled(boolean cancelled) {

        this.cancelled = cancelled;

        if (cancelled) {

            repository.cancel();
        }

    }

    /**
     * Indicates thatthis process should be interrupted.
     */
    public void interrupt() {
        worker.interrupt();
    }

    /**
     * Returns the default focus component of this object.
     */
    public Component getDefaultFocusComponent() {
        return nameField;
    }

    protected String bundledString(String key) {
        return Bundles.get(this.getClass(), key);
    }


}





