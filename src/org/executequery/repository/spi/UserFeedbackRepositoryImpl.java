/*
 * UserFeedbackRepositoryImpl.java
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

package org.executequery.repository.spi;

import org.executequery.http.RemoteHttpClient;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.log.Log;
import org.executequery.repository.RepositoryException;
import org.executequery.repository.UserFeedback;
import org.executequery.repository.UserFeedbackRepository;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 *
 * @author   Takis Diakoumis
 */
public class UserFeedbackRepositoryImpl implements UserFeedbackRepository {

    private static final String FEEDBACK_POST_ADDRESS = "rdb.support@red-soft.biz";
    
    private static final String ADDRESS = "red-soft.biz";

    private static final String MAIL_SERVER = "mail.red-soft.biz";
    
    public void postFeedback(UserFeedback userFeedback) throws RepositoryException {

        try {
        
            Log.info("Sending feedback to rdb.support@red-soft.biz");

            saveEntriesToPreferences(userFeedback);
            
            if (siteAvailable()) {

                try{
                    // Get system properties
                    Properties properties = System.getProperties();

                    // Setup mail server
                    properties.setProperty("mail.smtp.host", MAIL_SERVER);

                    // Get the default Session object.
                    Session session = Session.getDefaultInstance(properties);

                    // Create a default MimeMessage object.
                    MimeMessage message = new MimeMessage(session);

                    // Set From: header field of the header.
                    message.setFrom(new InternetAddress(userFeedback.getEmail()));

                    // Set To: header field of the header.
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(FEEDBACK_POST_ADDRESS));

                    // Set Subject: header field
                    message.setSubject(userFeedback.getType());

                    // Now set the actual message
                    message.setText("From: " + userFeedback.getName() + "\n\n" + userFeedback.getRemarks());

                    // Send message
                    Transport.send(message);
                    System.out.println("Sent message successfully....");
                }catch (MessagingException mex) {
                    mex.printStackTrace();
                }

            }
            
        } catch (RemoteHttpClientException e) {
            
            handleException(e);            
        }

    }

    private void handleException(Throwable e) {

        logError(e);
        throw new RepositoryException(ioExceptionMessage());
    }

    private void logError(Throwable e) {

        if (Log.isDebugEnabled()) {

            Log.error("Error posting user feedback", e);
        }

    }

    private boolean siteAvailable() {
        RemoteHttpClient remoteHttpClient = remoteHttpClient();
        remoteHttpClient.setHttp("http");
        remoteHttpClient.setHttpPort(80);
        return remoteHttpClient.hostReachable(ADDRESS);
    }

    public void cancel() {
//        cancelled = true;
    }
    
    private String ioExceptionMessage() {

        return "An error occured posting the feedback report.\n" +
            "This feature requires an active internet connection.\n" +
            "If using a proxy server, please configure this through " +
            "the user preferences > general selection.";
    }

    private String genericExceptionMessage() {

        return "An error occured posting the feedback report to\n" +
            "http://red-soft.biz. Please try again later.";
    }

    private RemoteHttpClient remoteHttpClient() {
        
        return new DefaultRemoteHttpClient();
    }

    private void saveEntriesToPreferences(UserFeedback userFeedback) {
        boolean savePrefs = false;

        if (!MiscUtils.isNull(userFeedback.getName())) {
            savePrefs = true;
            SystemProperties.setStringProperty(
                    "user", "user.full.name", userFeedback.getName());
        }

        if (!MiscUtils.isNull(userFeedback.getEmail())) {
            savePrefs = true;
            SystemProperties.setStringProperty(
                    "user", "user.email.address", userFeedback.getEmail());
        }

        if (savePrefs) {
            SystemResources.setUserPreferences(
                                SystemProperties.getProperties("user"));
        }
    }

}

