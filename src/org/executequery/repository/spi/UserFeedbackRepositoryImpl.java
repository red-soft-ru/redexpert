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


import org.executequery.http.JSONAPI;
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

import java.io.IOException;
import java.util.Map;


/**
 * @author Takis Diakoumis
 */
public class UserFeedbackRepositoryImpl implements UserFeedbackRepository {

    private static final String ADDRESS = "reddatabase.ru";


    public int postFeedback(UserFeedback userFeedback) throws RepositoryException {

        try {

            Log.info("Sending feedback to rdb.support@red-soft.ru");

            saveEntriesToPreferences(userFeedback);

            if (siteAvailable()) {
                Map<String, String> params = userFeedback.asMap();
                String res = JSONAPI.postJsonObject("https://reddatabase.ru/api/website/feedbacks/", params, null);
                if (res.startsWith("Server return error"))
                    return Integer.parseInt(res.split("\n")[1]);
                Log.info(res);
                return 1;
            }
            return -1;

        } catch (RemoteHttpClientException e) {
            handleException(e);
            return 0;
        } catch (IOException e) {
            handleException(e);
            return 0;
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

