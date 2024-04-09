package org.executequery.http;

import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

public class JSONAPI {

    public static JSONObject getJsonObject(String Url) throws IOException {
        DefaultRemoteHttpClient client = new DefaultRemoteHttpClient();
        RemoteHttpResponse rhr = client.httpGetRequest(Url);
        String text = rhr.getResponse();

        if (text.length() == 0) {
            text = "{\n" +
                    "    \"version\": \"0.0.0.0\"\n" +
                    "}";
        }

        return new JSONObject(text);
    }

    public static JSONObject getJsonObject(String Url, Map<String, String> headers) throws IOException {
        DefaultRemoteHttpClient client = new DefaultRemoteHttpClient();
        RemoteHttpResponse rhr = client.httpGetRequest(Url, headers);
        String text = rhr.getResponse();
        return new JSONObject(text);
    }

    public static JSONObject postJsonObjectAsJSON(String Url, Map<String, String> parameters, Map<String, String> headers) throws IOException {
        return new JSONObject(postJsonObject(Url, parameters, headers));
    }

    public static String postJsonObject(String Url, Map<String, String> parameters, Map<String, String> headers) throws IOException {
        DefaultRemoteHttpClient client = new DefaultRemoteHttpClient();
        RemoteHttpResponse rhr = client.httpPostRequest(Url, parameters, headers);
        StringBuilder text = new StringBuilder();
        text.append(rhr.getResponse());
        int cod = rhr.getResponseCode();

        if (cod < 200 || cod > 300) {
            text.insert(0, "Server return error:\n" + cod + "\n");
            Log.error(text);

        }
        return text.toString();
    }

    public static JSONArray getJsonArray(String Url, Map<String, String> headers) throws IOException {
        DefaultRemoteHttpClient client = new DefaultRemoteHttpClient();
        RemoteHttpResponse rhr = client.httpGetRequest(Url, headers);
        String text = rhr.getResponse();

        return new JSONArray(text);
    }

    public static JSONArray getJsonArray(String Url) throws IOException {

        DefaultRemoteHttpClient client = new DefaultRemoteHttpClient();
        RemoteHttpResponse rhr = client.httpGetRequest(Url);
        String text = rhr.getResponse();

        return new JSONArray(text);
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
