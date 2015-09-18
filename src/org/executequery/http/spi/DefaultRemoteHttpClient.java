/*
 * DefaultRemoteHttpClient.java
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

package org.executequery.http.spi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.http.RemoteHttpClient;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.RemoteHttpResponse;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultRemoteHttpClient implements RemoteHttpClient {

    private static final String HTTP = "http";

    private static final int HTTP_PORT = 80;

    public boolean hostReachable(String host) {

        /*
        String  urlString = host;
        if (!host.startsWith("http://")) {

            urlString = "http://" + host;
        }

        URLConnection connection = null;
        try {

            URL url = new URL(urlString);
            connection = url.openConnection();
            connection.connect();

            return true;

        } catch (MalformedURLException e) {

            throw new RemoteHttpClientException(e);

        } catch (IOException e) {

            Log.warning("Host not reachable - " + host);
            return false;

        } finally {

            connection = null;
        }
        */

        HttpMethod method = null;
        HttpConnectionManager httpConnectionManager = createConnectionManager();

        try {

            HttpClient client = createHttpClientForManager(host, httpConnectionManager);

            method = new HeadMethod();

            RemoteHttpResponse remoteHttpResponse = executeMethod(method, client);

            return remoteHttpResponse.getResponseCode() == HttpStatus.SC_OK;

        } finally {

            releaseMethod(method);

            releaseConnectionManager(httpConnectionManager);
        }

    }

    public RemoteHttpResponse httpGetRequest(String host, String path) {

        HttpMethod method = null;

        HttpConnectionManager httpConnectionManager = createConnectionManager();

        try {

            HttpClient client = createHttpClientForManager(host, httpConnectionManager);

            method = new GetMethod(path);

            return executeMethod(method, client);

        } finally {

            releaseMethod(method);

            releaseConnectionManager(httpConnectionManager);
        }

    }

    public RemoteHttpResponse httpPostRequest(String host, String path, Map<String, String> params) {

        PostMethod method = null;

        HttpConnectionManager httpConnectionManager = createConnectionManager();

        try {

            HttpClient client = createHttpClientForManager(host, httpConnectionManager);

            method = new PostMethod(path);

            for (Entry<String, String> entry : params.entrySet()) {

                method.addParameter(entry.getKey(), entry.getValue());
            }

            RemoteHttpResponse remoteHttpResponse = executeMethod(method, client);

            if (isRedirection(remoteHttpResponse.getResponseCode())) {

                return handleRedirection(method);

            } else {

                return remoteHttpResponse;
            }

        } finally {

            releaseMethod(method);

            releaseConnectionManager(httpConnectionManager);
        }

    }

    private void releaseMethod(HttpMethod method) {

        if (method != null) {

            method.releaseConnection();
        }
    }

    private void releaseConnectionManager(HttpConnectionManager httpConnectionManager) {

        if (httpConnectionManager instanceof SimpleHttpConnectionManager) {

            try {

                ((SimpleHttpConnectionManager) httpConnectionManager).shutdown();

            } catch (Exception e) {}

        }
    }

    private HttpConnectionManager createConnectionManager() {

        return new SimpleHttpConnectionManager(true);
    }

    private RemoteHttpResponse handleRedirection(HttpMethod method) {

        Header locationHeader = method.getResponseHeader("location");
        if (locationHeader != null) {

            try {

                URL url = new URL(locationHeader.getValue());
                return httpGetRequest(url.getHost(), url.getPath());

            } catch (MalformedURLException e) {

                throw new RemoteHttpClientException(e);
            }

        } else {

            throw new RemoteHttpClientException("Invalid redirection after method");
        }
    }

    private boolean isRedirection(int responseCode) {

        return responseCode == HttpStatus.SC_MOVED_PERMANENTLY
            || responseCode == HttpStatus.SC_MOVED_TEMPORARILY
            || responseCode == HttpStatus.SC_SEE_OTHER
            || responseCode == HttpStatus.SC_TEMPORARY_REDIRECT;
    }

    private RemoteHttpResponse executeMethod(HttpMethod method, HttpClient client) {

        try {

            int statusCode = client.executeMethod(method);

            return new RemoteHttpResponse(statusCode, method.getResponseBodyAsString());

        } catch (HttpException e) {

            throw new RemoteHttpClientException(e);

        } catch (IOException e) {

            throw new RemoteHttpClientException(e);
        }

    }

    private HttpClient createHttpClientForManager(String host, HttpConnectionManager httpConnectionManager) {

        HttpClient client = new HttpClient(httpConnectionManager);
        client.getHostConfiguration().setHost(host, HTTP_PORT, HTTP);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        if (isUsingProxy()) {

            client.getHostConfiguration().setProxy(getProxyHost(), getProxyPort());

            if (hasProxyAuthentication()) {

                client.getState().setProxyCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(getProxyUser(), getProxyPassword()));
            }

        }
        return client;
    }


    private boolean hasProxyAuthentication() {

        return StringUtils.isNotBlank(getProxyUser()) && StringUtils.isNotBlank(getProxyPassword());
    }

    private boolean isUsingProxy() {

        return (getProxyHost() != null && getProxyPort() != null);
    }

    private Integer getProxyPort() {

        if (System.getProperty("http.proxyPort") != null) {

            return Integer.valueOf(System.getProperty("http.proxyPort"));
        }

        return null;
    }

    private String getProxyHost() {

        return System.getProperty("http.proxyHost");
    }

    private String getProxyUser() {

        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "internet.proxy.user");
    }

    private String getProxyPassword() {

        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "internet.proxy.password");
    }

}






