package org.executequery.update;

import org.executequery.http.JSONAPI;
import org.executequery.localization.InterfaceLanguage;
import org.executequery.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.underworldlabs.util.SystemProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

/// @author Aleksey Kozlov
final class UpdateSourceWebsite extends UpdateSource {

    private static final String VERSION_CHECK_RC_URL = SystemProperties.getProperty("user", "update.check.rc.url");
    private static final String VERSION_CHECK_URL = SystemProperties.getProperty("user", "update.check.url");
    private static final String ROOT_URL = "https://rdb.red-soft.ru/";

    private static final String FILE_NAME = "FILE_NAME";
    private static final String FILE_PATH = "FILE_PATH";
    private static final String CHANGELOG = "changelog";
    private static final String VERSION = "version";
    private static final String FILES = "files";

    private UpdateSourceWebsite(JSONObject source) {
        init(source);
    }

    private void init(JSONObject source) {

        if (source == null) {
            loaded = false;
            return;
        }

        try {
            extractVersion(source);
            extractDownloadUrl(source);
            extractChangelogs(source);
            loaded = true;

        } catch (JSONException e) {
            Log.error(e.getMessage(), e);
            loaded = false;
        }
    }

    private void extractVersion(JSONObject source) {
        appVersion = new ApplicationVersion(source.getString(VERSION));
    }

    private void extractDownloadUrl(JSONObject source) {
        String zipFileName = "RedExpert-" + appVersion + ".zip";

        JSONArray filesArray = source.getJSONArray(FILES);
        for (int i = 0; i < filesArray.length(); i++) {

            String fileName = filesArray.getJSONObject(i).getString(FILE_NAME);
            if (Objects.equals(fileName, zipFileName)) {
                downloadUrl = ROOT_URL + filesArray.getJSONObject(i).getString(FILE_PATH);
                return;
            }
        }
    }

    private void extractChangelogs(JSONObject source) {
        JSONObject jsonObject = source.getJSONObject(CHANGELOG);

        changelogs = new HashMap<>();
        changelogs.put(InterfaceLanguage.ru.name(), normalize(jsonObject.getString("ru")));
        changelogs.put(InterfaceLanguage.en.name(), normalize(jsonObject.getString("en")));
    }

    // ---

    static UpdateSource load(boolean unstable) {
        setupHttpClient(useHttpsProtocol() ? "https" : "http", 443);

        try {
            JSONObject jsonObject = JSONAPI.getJsonObject(unstable ? VERSION_CHECK_RC_URL : VERSION_CHECK_URL);
            return new UpdateSourceWebsite(jsonObject);

        } catch (IOException | JSONException e) {
            Log.error(e.getMessage(), e);
            return new UpdateSourceWebsite(null);
        }
    }

}
