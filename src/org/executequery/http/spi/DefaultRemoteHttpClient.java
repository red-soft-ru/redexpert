/*
 * DefaultRemoteHttpClient.java
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

package org.executequery.http.spi;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.http.RemoteHttpClient;
import org.executequery.http.RemoteHttpClientException;
import org.executequery.http.RemoteHttpResponse;
import org.underworldlabs.util.SystemProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Takis Diakoumis
 */
public class DefaultRemoteHttpClient implements RemoteHttpClient {

    private static String HTTP = "https";

    private static int HTTP_PORT = 443;

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

    public RemoteHttpResponse httpGetRequest(String url) throws MalformedURLException {
        return httpGetRequest(url, (Map<String, String>) null);
    }

    public RemoteHttpResponse httpGetRequest(String url, Map<String, String> headers) throws MalformedURLException {
        URL url_ = new URL(url);
        return httpGetRequest(url_.getHost(), url_.getPath() + (url_.getQuery() != null ? ("?" + url_.getQuery()) : ""), headers);
    }


    public RemoteHttpResponse httpGetRequest(String host, String path) {
        return httpGetRequest(host, path, null);
    }

    public RemoteHttpResponse httpGetRequest(String host, String path, Map<String, String> headers) {

        HttpMethod method = null;

        HttpConnectionManager httpConnectionManager = createConnectionManager();

        try {

            HttpClient client = createHttpClientForManager(host, httpConnectionManager);

            method = new GetMethod(path);
            if (headers != null)
                for (String key : headers.keySet()) {
                    method.addRequestHeader(key, headers.get(key));
                }

            return executeMethod(method, client);

        } finally {

            releaseMethod(method);

            releaseConnectionManager(httpConnectionManager);
        }

    }

    public RemoteHttpResponse httpPostRequest(String url, Map<String, String> params) throws MalformedURLException {

        return httpPostRequest(url, params, null);
    }

    public RemoteHttpResponse httpPostRequest(String url, Map<String, String> params, Map<String, String> heads) throws MalformedURLException {
        URL url_ = new URL(url);
        return httpPostRequest(url_.getHost(), url_.getPath() + (url_.getQuery() != null ? ("?" + url_.getQuery()) : ""), params, heads);
    }

    public RemoteHttpResponse httpPostRequest(String host, String path, Map<String, String> params) {
        return httpPostRequest(host, path, params, null);
    }


    public RemoteHttpResponse httpPostRequest(String host, String path, Map<String, String> params, Map<String, String> heads) {

        PostMethod method = null;

        HttpConnectionManager httpConnectionManager = createConnectionManager();

        try {

            HttpClient client = createHttpClientForManager(host, httpConnectionManager);
            HostConfiguration config = client.getHostConfiguration();
            HostParams hostParams = config.getParams();
            hostParams.setParameter("http.protocol.content-charset", "UTF8");
            method = new PostMethod(path);

            for (Entry<String, String> entry : params.entrySet()) {

                method.addParameter(entry.getKey(), entry.getValue());
            }
            if (heads != null)
                for (String key : heads.keySet()) {
                    method.addRequestHeader(key, heads.get(key));
                }

            RemoteHttpResponse remoteHttpResponse = executeMethod(method, client);

            if (isRedirection(remoteHttpResponse.getResponseCode())) {

                return handlePostRedirection(method, params);

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

            } catch (Exception e) {
            }

        }
    }

    private HttpConnectionManager createConnectionManager() {

        return new SimpleHttpConnectionManager(true);
    }

    private RemoteHttpResponse handlePostRedirection(HttpMethod method, Map<String, String> params) {

        Header locationHeader = method.getResponseHeader("location");
        if (locationHeader != null) {

            try {

                URL url = new URL(locationHeader.getValue());
                Map<String, String> heads = new HashMap<>();
                for (Header header : method.getRequestHeaders()) {
                    heads.put(header.getName(), header.getValue());
                }
                return httpPostRequest(url.getHost(), url.getPath(), params, heads);

            } catch (MalformedURLException e) {

                throw new RemoteHttpClientException(e);
            }

        } else {

            throw new RemoteHttpClientException("Invalid redirection after method");
        }
    }

    private RemoteHttpResponse handleGetRedirection(HttpMethod method) {

        Header locationHeader = method.getResponseHeader("location");
        if (locationHeader != null) {

            try {

                URL url = new URL(locationHeader.getValue());
                Map<String, String> heads = new HashMap<>();
                for (Header header : method.getRequestHeaders()) {
                    heads.put(header.getName(), header.getValue());
                }
                return httpGetRequest(url.getHost(), url.getPath(), heads);

            } catch (MalformedURLException e) {

                throw new RemoteHttpClientException(e);
            }

        } else {

            throw new RemoteHttpClientException("Invalid redirection after method");
        }
    }

    private boolean isRedirection(int responseCode) {

        return responseCode >= 300 && responseCode < 400;
    }

    private RemoteHttpResponse executeMethod(HttpMethod method, HttpClient client) {

        try {

            int statusCode = client.executeMethod(method);
            StringBuilder text = new StringBuilder();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(method.getResponseBodyAsStream(), StandardCharsets.UTF_8));

            String inputLine;


            while ((inputLine = br.readLine()) != null) {
                text.append(inputLine).append("\n");
            }

            br.close();

            return new RemoteHttpResponse(statusCode, text.toString());

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


    public boolean hasProxyAuthentication() {

        return StringUtils.isNotBlank(getProxyUser()) && StringUtils.isNotBlank(getProxyPassword());
    }

    public boolean isUsingProxy() {

        return (getProxyHost() != null && getProxyPort() != null);
    }

    public Integer getProxyPort() {

        if (System.getProperty("http.proxyPort") != null) {

            return Integer.valueOf(System.getProperty("http.proxyPort"));
        }

        return null;
    }

    public String getProxyHost() {

        return System.getProperty("http.proxyHost");
    }

    public String getProxyUser() {

        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "internet.proxy.user");
    }

    public String getProxyPassword() {

        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, "internet.proxy.password");
    }

    public void setHttp(String http) {
        HTTP = http;
    }

    public void setHttpPort(int port) {
        HTTP_PORT = port;
    }

}







