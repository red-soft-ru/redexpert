/*
 * FeedbackPanel.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.repository.RepositoryException;
import org.executequery.repository.UserFeedback;
import org.executequery.repository.UserFeedbackRepository;
import org.executequery.repository.spi.UserFeedbackRepositoryImpl;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.InterruptibleProgressDialog;
import org.underworldlabs.swing.util.Interruptible;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

/**
 * Base feedback panel for comments, requests and bugs.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class FeedbackPanel extends DefaultActionButtonsPanel
                           implements ActionListener,
                                      FocusComponentPanel,
                                      Interruptible {

    /** user comments feedback indicator */
    public static final int USER_COMMENTS = 2;

    /** feature request feedback indicator */
    public static final int FEATURE_REQUEST = 1;

    /** bug report feedback indicator */
    public static final int BUG_REPORT = 0;

    /** the feedback type for the instance */
    private int feedbackType;

    /** user's name field */
    private JTextField nameField;

    /** user's comments field */
    private JTextArea commentsField;

    /** user's email field */
    private JTextField emailField;

    /** the parent container */
    private ActionContainer parent;

    /** Thread worker object */
    private SwingWorker worker;

    /** The progress dialog */
    private InterruptibleProgressDialog progressDialog;

    /** Creates a new instance of FeedbackPanel */
    public FeedbackPanel(ActionContainer parent, int feedbackType) {

        try {

            this.parent = parent;
            this.feedbackType = feedbackType;

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        String labelText = generateLabelText();

        commentsField = new JTextArea(createHeader());
        commentsField.setMargin(new Insets(2,2,2,2));

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
        basePanel.add(new JLabel("Name:"), gbc);
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
        basePanel.add(new JLabel("Email:"), gbc);
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

        JButton cancelButton = new DefaultButton("Cancel");
        JButton sendButton = new DefaultButton("Send");

        sendButton.addActionListener(this);
        cancelButton.addActionListener(this);

        addActionButton(sendButton);
        addActionButton(cancelButton);

        setPreferredSize(new Dimension(700, 450));

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
                sb.append("Please provide as much information on what ");
                sb.append("you were doing when the error occurred as well ");
                sb.append("as what database you are connected to and any ");
                sb.append("exception stack traces that may be available.");
                break;
            case FEATURE_REQUEST:
            case USER_COMMENTS:
            default:
                sb.append("Please complete the fields below.");
                break;
        }

        sb.append("</td></tr><tr><td>");
        sb.append("If you wish to send feedback with ");
        sb.append("attachments, please send an email to ");
        sb.append("feedback@executequery.org instead of completing this form.");
        sb.append("</td></tr><tr><td>This feature requires an active ");
        sb.append("internet connection.</td></tr>");
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

                    JDialog dialog = (JDialog)parent;
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
                            "Your remarks were successfully posted to " +
                            "http://executequery.org.\nThank you for your feedback.");

                } else if (cancelled || result == Constants.WORKER_FAIL ||
                        result == Constants.WORKER_CANCEL) {

                    if (parent.isDialog()) {

                        parent.unblock();

                        JDialog dialog = (JDialog)parent;
                        dialog.setVisible(true);
                        return;
                    }

                }

                GUIUtilities.showNormalCursor();
                parent.finished();
            }
        };

        progressDialog = new InterruptibleProgressDialog(
                            GUIUtilities.getParentFrame(),
                            "Posting Feedback",
                            "Posting feedback report to http://executequery.org",
                            this);

        worker.start();
        progressDialog.run();
    }

    private boolean fieldsValid() {

        String email = emailField.getText();

        if (MiscUtils.isNull(email)) {

            int result = GUIUtilities.displayYesNoDialog(
                    noEmailAddressWarningMessage(), "Feedback");

            if (result == JOptionPane.NO_OPTION ||
                    result == JOptionPane.CANCEL_OPTION) {

                return false;
            }

        }

        String comments = commentsField.getText();

        if (MiscUtils.isNull(comments)) {

            GUIUtilities.displayErrorMessage(noRemarksErrorMessage());

            return false;
        }

        return true;
    }

    private String noRemarksErrorMessage() {
        return "Please enter your remarks in the text area provided.";
    }

    private String noEmailAddressWarningMessage() {
        return "You have not entered your email address.\n" +
            "An email address is required in order for the " +
            "developers to reply to your feedback.\n" +
            "Are you sure you want to submit this report " +
            "without an email address?";
    }

    private Object doWork() {

        repository = createFeedbackRepository();

        try {

            UserFeedback userFeedback = createUserFeedback();
            repository.postFeedback(userFeedback);

            return Constants.WORKER_SUCCESS;

        } catch (RepositoryException e) {

            if (cancelled) {

                return Constants.WORKER_CANCEL;
            }

            showError(e.getMessage());

            return Constants.WORKER_FAIL;
        }

    }

    private UserFeedbackRepository createFeedbackRepository() {

        return new UserFeedbackRepositoryImpl();
    }

    private UserFeedback createUserFeedback() {

        String typeString = null;

        switch (feedbackType) {
            case BUG_REPORT:
                typeString = "1";
                break;
            case FEATURE_REQUEST:
                typeString = "2";
                break;
            case USER_COMMENTS:
            default:
                typeString = "0";
                break;
        }

        return new UserFeedback(nameField.getText(),
                emailField.getText(),
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

    /** process cancelled flag */
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


}




