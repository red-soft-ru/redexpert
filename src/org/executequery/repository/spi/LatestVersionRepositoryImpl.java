/*
 * LatestVersionRepositoryImpl.java
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

import org.executequery.ApplicationException;
import org.executequery.ApplicationVersion;
import org.executequery.Constants;
import org.executequery.http.RemoteHttpClient;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.RemoteHttpResponse;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.log.Log;
import org.executequery.repository.LatestVersionRepository;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class LatestVersionRepositoryImpl implements LatestVersionRepository {

    private static final String ADDRESS = "www.executequery.org";
    
    public String getId() {

        return REPOSITORY_ID;
    }
    
    public ApplicationVersion getLatestVersion() {

        if (siteAvailable()) {

            try {

                String version = null;
                String build = null;

                RemoteHttpClient httpClient = remoteHttpClient();
                RemoteHttpResponse response = httpClient.httpGetRequest(ADDRESS, versionUrl().getPath());

                String[] responseTextLines = response.getResponse().split("\n");
                for (String line : responseTextLines) {
                    
                    if (line.startsWith("version")) {

                        version = line.substring(8).trim();

                    } else if(line.startsWith("build")) {

                        build = line.substring(6).trim();
                    }

                } 

                return new ApplicationVersion(version, build);
                
            } catch (MalformedURLException e) {

                handleException(e);
                
            } catch (RemoteHttpClientException e) {
                
                logError(e);
            }

        }
        
        throw new ApplicationException(ioErrorMessage());
    }

    private RemoteHttpClient remoteHttpClient() {
        
        return new DefaultRemoteHttpClient();
    }
    
    private String ioErrorMessage() {

        return "The version file at http://executequery.org " +
            "could not be opened.\nThis feature requires an " +
            "active internet connection.\nIf using a proxy server, " +
            "please configure this through the user preferences " +
            "> general selection.";
    }

    private void handleException(Throwable e) {

        logError(e);
        throw new ApplicationException(e);
    }

    private void logError(Throwable e) {

        if (Log.isDebugEnabled()) {
            
            Log.debug("Error during version check from remote site.", e);
        }

    }

    private URL versionUrl() throws MalformedURLException {

        return new URL(SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "check.version.url"));
    }

    private boolean siteAvailable() {

        try {
        
            return remoteHttpClient().hostReachable(ADDRESS);
            
        } catch (Exception e) {
            
            Log.error(e.getMessage(), e);
            throw new ApplicationException(ioErrorMessage());
        }
    }

    public String getReleaseNotes() {

        if (siteAvailable()) {

            Log.info("Downloading latest release notes from http://executequery.org");
            
            try {

                RemoteHttpClient httpClient = remoteHttpClient();
                RemoteHttpResponse response = httpClient.httpGetRequest(ADDRESS, releaseNotesUrl().getPath());

                return response.getResponse();

            } catch (MalformedURLException e) {

                handleException(e);

            } catch (RemoteHttpClientException e) {
                
                logError(e);
            }

        }
        
        throw new ApplicationException(ioErrorMessage());
    }

    private URL releaseNotesUrl() throws MalformedURLException {

        return new URL(SystemProperties.getProperty(Constants.SYSTEM_PROPERTIES_KEY, "check.version.notes.url"));
    }
    
}

