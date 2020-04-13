package org.executequery.http;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HostParams;
import org.executequery.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class JSONAPI {

    public static JSONObject getJsonObject(String Url) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(Url);
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();

        if (text.length() == 0) {
            text.append("{\n" +
                    "    \"version\": \"0.0.0.0\"\n" +
                    "}");
        }

        return new JSONObject(text.toString());
    }

    public static JSONObject getJsonObject(String Url, Map<String, String> headers) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(Url);
        for (String key : headers.keySet()) {
            get.addRequestHeader(key, headers.get(key));
        }
        client.executeMethod(get);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();


        return new JSONObject(text.toString());
    }

    public static JSONObject postJsonObjectAsJSON(String Url, Map<String, String> parameters, Map<String, String> headers) throws IOException {
        return new JSONObject(postJsonObject(Url, parameters, headers));
    }

    public static String postJsonObject(String Url, Map<String, String> parameters, Map<String, String> headers) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        PostMethod get = new PostMethod(Url);
        for (String key : parameters.keySet()) {
            get.addParameter(key, parameters.get(key));
        }
        if (headers != null)
            for (String key : headers.keySet()) {
                get.addRequestHeader(key, headers.get(key));
            }
        HostConfiguration config = client.getHostConfiguration();
        HostParams hostParams = config.getParams();
        hostParams.setParameter("http.protocol.content-charset", "UTF8");
        int cod = client.executeMethod(get);
        if (cod >= 300 && cod < 400) {
            String redirectLocation = null;
            Header locationHeader = get.getResponseHeader("location");
            if (locationHeader != null)
                redirectLocation = locationHeader.getValue();
            return postJsonObject(redirectLocation, parameters, headers);

        }
        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));
        String inputLine;
        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();


        if (cod < 200 || cod > 300) {
            text.insert(0, "Server return error:\n" + cod + "\n");
            Log.error(text);

        }
        return text.toString();
    }

    public static JSONArray getJsonArray(String Url, Map<String, String> headers) throws IOException {
        StringBuilder text = new StringBuilder();

        HttpClient client = new HttpClient();
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        GetMethod get = new GetMethod(Url);
        for (String key : headers.keySet()) {
            get.addRequestHeader(key, headers.get(key));
        }
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();

        return new JSONArray(text.toString());
    }

    public static JSONArray getJsonArray(String Url) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(Url);
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();

        return new JSONArray(text.toString());
    }


    public static JSONObject getJsonObjectFromArray(JSONArray mas, String key, String value) {
        for (int i = 0; i < mas.length(); i++) {
            String prop = mas.getJSONObject(i).getString(key);
            if (prop.contentEquals(value))
                return mas.getJSONObject(i);
        }
        return null;

    }

    public static String getJsonPropertyFromUrl(String Url, String key) throws IOException {

        return getJsonObject(Url).getString(key);
    }

    public static String getJsonPropertyFromUrl(String Url, String key, Map<String, String> headers) throws IOException {

        return getJsonObject(Url, headers).getString(key);
    }

    public static String postJsonPropertyFromUrl(String Url, String key, Map<String, String> parameters, Map<String, String> headers) throws IOException {

        return postJsonObjectAsJSON(Url, parameters, headers).getString(key);
    }
}
