package org.executequery.http;

import org.executequery.GUIUtilities;
import org.executequery.UserPreferencesManager;
import org.executequery.gui.LoginPasswordDialog;
import org.executequery.localization.Bundles;
import org.executequery.util.UserProperties;
import org.json.JSONException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReddatabaseAPI {

    public static final String URL_OF_REGISTRATION = "https://reddatabase.ru/user/register/";


    public static boolean getToken() {
        String user = SystemProperties.getStringProperty("user", "reddatabase.user");
        LoginPasswordDialog dialog = new LoginPasswordDialog(Bundles.get(ReddatabaseAPI.class, "Authorization"), Bundles.get(ReddatabaseAPI.class, "message"), URL_OF_REGISTRATION, user);
        dialog.display();
        if (dialog.isClosedDialog())
            return false;
        SystemProperties.setProperty("user", "reddatabase.user", dialog.getUsername());
        Map<String, String> params = new HashMap<>();
        params.put("username", dialog.getUsername());
        params.put("password", dialog.getPassword());
        try {
            String token = JSONAPI.postJsonPropertyFromUrl(UserProperties.getInstance().getStringProperty("reddatabase.get-token.url"), "token", params, null);
            if (token != null) {
                SystemProperties.setProperty("user", "reddatabase.token", token);
                GUIUtilities.loadAuthorisationInfo();
                UserPreferencesManager.fireUserPreferencesChanged();
                return true;
            }
            return false;
        } catch (JSONException e) {
            if (GUIUtilities.displayConfirmDialog("Unknown Login or Password. Try again?") == JOptionPane.YES_OPTION)
                return getToken();
            else return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static Map<String, String> getHeadersWithToken() {
        if (MiscUtils.isNull(SystemProperties.getStringProperty("user", "reddatabase.token"))) {
            if (!ReddatabaseAPI.getToken())
                return null;
        }
        String token = SystemProperties.getStringProperty("user", "reddatabase.token");
        if (!MiscUtils.isNull(token)) {
            Map<String, String> heads = new HashMap<>();
            heads.put("Authorization", "Token " + token);
            return heads;
        } else return null;
    }
}
