package org.executequery.http;

import org.executequery.GUIUtilities;
import org.executequery.UserPreferencesManager;
import org.executequery.gui.LoginPasswordDialog;
import org.executequery.util.UserProperties;
import org.json.JSONException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReddatabaseAPI {
    public static void getToken() {
        LoginPasswordDialog dialog = new LoginPasswordDialog("Autorisation", "To continue the update you need to enter\nyour login and password from the site reddatabase.ru");
        dialog.display();
        Map<String, String> params = new HashMap<>();
        params.put("username", dialog.getUsername());
        params.put("password", dialog.getPassword());
        try {
            String token = JSONAPI.postJsonPropertyFromUrl(UserProperties.getInstance().getStringProperty("reddatabase.get-token.url"), "token", params, null);
            if (token != null) {
                SystemProperties.setProperty("user", "reddatabase.token", token);
                UserPreferencesManager.fireUserPreferencesChanged();
            }
        } catch (JSONException e) {
            if (GUIUtilities.displayConfirmCancelDialog("Unknown Login or Password. Try again?") == JOptionPane.YES_OPTION)
                getToken();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, String> getHeadersWithToken() {
        if (MiscUtils.isNull(SystemProperties.getStringProperty("user", "reddatabase.token"))) {
            ReddatabaseAPI.getToken();
        }
        String token = SystemProperties.getStringProperty("user", "reddatabase.token");
        if (!MiscUtils.isNull(token)) {
            Map<String, String> heads = new HashMap<>();
            heads.put("Authorization", "Token " + token);
            return heads;
        } else return null;
    }
}
