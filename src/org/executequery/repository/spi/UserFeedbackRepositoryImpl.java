/*
 * UserFeedbackRepositoryImpl.java
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

package org.executequery.repository.spi;

import java.net.MalformedURLException;
import java.net.URL;

import org.executequery.http.RemoteHttpClient;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.RemoteHttpResponse;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.log.Log;
import org.executequery.repository.RepositoryException;
import org.executequery.repository.UserFeedback;
import org.executequery.repository.UserFeedbackRepository;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1488 $
 * @date     $Date: 2015-08-23 22:25:06 +1000 (Sun, 23 Aug 2015) $
 */
public class UserFeedbackRepositoryImpl implements UserFeedbackRepository {

    private static final String FEEDBACK_POST_ADDRESS = "http://executequery.org/feedback-submit";
    
    private static final String ADDRESS = "executequery.org";
    
    public void postFeedback(UserFeedback userFeedback) throws RepositoryException {

        try {
        
            Log.info("Posting feedback to http://executequery.org");

            saveEntriesToPreferences(userFeedback);
            
            if (siteAvailable()) {
                
                URL url = new URL(FEEDBACK_POST_ADDRESS);

                Log.trace("Sending user feedback to path [ " + ADDRESS + url.getPath() + " ]");
                
                RemoteHttpClient httpClient = remoteHttpClient();
                RemoteHttpResponse httpPostResponse = 
                    httpClient.httpPostRequest(ADDRESS, url.getPath(), userFeedback.asMap());

                Log.trace("User feedback submitted - response code [ " + httpPostResponse.getResponseCode() + " ]");
                
                if (httpPostResponse.getResponseCode() != 200) {
                    
                    throw new RepositoryException(genericExceptionMessage());
                }
                
            }
            
        }
        catch (MalformedURLException e) {

            handleException(e);
            
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

        return remoteHttpClient().hostReachable(ADDRESS);
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
            "http://executequery.org. Please try again later.";
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
