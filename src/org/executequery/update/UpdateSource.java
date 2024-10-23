package org.executequery.update;

import org.apache.commons.lang.StringEscapeUtils;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.util.UserProperties;

import java.util.Map;

/// @author Aleksey Kozlov
abstract class UpdateSource {

    protected boolean loaded;
    protected String downloadUrl;
    protected ApplicationVersion appVersion;
    protected Map<String, String> changelogs;

    // ---

    public static UpdateSource load(boolean releaseHub, boolean unstable) {
        return releaseHub ? UpdateSourceReleaseHub.load() : UpdateSourceWebsite.load(unstable);
    }

    // ---

    public boolean loaded() {
        return loaded;
    }

    public boolean canUpdate() {
        return loaded() && getVersion().hasUpdate();
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public ApplicationVersion getVersion() {
        return appVersion;
    }

    public String getChangelog(String language) {
        return changelogs.containsKey(language) ? changelogs.get(language) : changelogs.get("en");
    }

    // ---

    @SuppressWarnings("WriteOnlyObject")
    protected static void setupHttpClient(String protocol, int port) {
        new DefaultRemoteHttpClient().setHttp(protocol);
        new DefaultRemoteHttpClient().setHttpPort(port);
    }

    protected static boolean useHttpsProtocol() {
        return UserProperties.getInstance().getBooleanProperty("update.use.https");
    }

    protected String normalize(String text) {
        return StringEscapeUtils.unescapeHtml(text);
    }

}
