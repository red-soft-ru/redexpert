package org.executequery.update;

import org.executequery.http.JSONAPI;
import org.executequery.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.underworldlabs.util.SystemProperties;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

/// @author Aleksey Kozlov
final class UpdateSourceReleaseHub extends UpdateSource {

    private static final String VERSION_DOWNLOAD_URL = SystemProperties.getProperty("user", "update.download.rh.url");
    private static final String VERSION_CHECK_URL = SystemProperties.getProperty("user", "update.check.rh.url");
    private static final String BRANCH = SystemProperties.getProperty("system", "branch");
    private static final String ROOT_URL = "http://builds.red-soft.biz/";

    private static final String ARTIFACT_ID = "artifact_id";
    private static final String VERSION = "version";
    private static final String FILE = "file";

    private UpdateSourceReleaseHub(String version, String downloadUrl) {

        super.loaded = version != null && downloadUrl != null;
        if (!loaded)
            return;

        super.appVersion = new ApplicationVersion(version);
        super.changelogs = Collections.emptyMap();
        super.downloadUrl = downloadUrl;
    }

    // ---

    static UpdateSource load() {
        setupHttpClient("http", 80);

        try {
            JSONObject jsonObject = JSONAPI.getJsonObject(VERSION_CHECK_URL + BRANCH);
            String version = extractVersion(jsonObject);

            JSONArray jsonArray = JSONAPI.getJsonArray(VERSION_DOWNLOAD_URL + version);
            String downloadUrl = extractDownloadUrl(jsonArray, version);

            return new UpdateSourceReleaseHub(version, downloadUrl);

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return new UpdateSourceReleaseHub(null, null);
        }
    }

    private static String extractVersion(JSONObject source) {
        return source.getString(VERSION);
    }

    private static String extractDownloadUrl(JSONArray filesArray, String version) {
        String zipFileName = "red_expert:bin:" + version + ":zip";

        for (int i = 0; i < filesArray.length(); i++) {

            String fileName = filesArray.getJSONObject(i).getString(ARTIFACT_ID);
            if (Objects.equals(fileName, zipFileName)) {
                return ROOT_URL + filesArray.getJSONObject(i).getString(FILE);
            }
        }

        return null;
    }

}
