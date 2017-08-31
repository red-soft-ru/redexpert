package org.executequery.localization;

import java.util.Locale;
import java.util.ResourceBundle;

import org.executequery.util.StringBundle;

public final class Bundles {

    private static StringBundle stringBundle;
    
    public static String get(String key) {

        return bundle().getString(key);
    }
    
    public static String get(Class<?> clazz, String key) {
        
        return bundle().getString(keyForClazz(clazz, key));
    }
    
    private static StringBundle bundle() {

        if (stringBundle == null) {

            String packageName = Bundles.class.getPackage().getName();
            String path = packageName.replaceAll("\\.", "/") + "/resources";
            
            ResourceBundle bundle = ResourceBundle.getBundle(path, Locale.getDefault());
            stringBundle = new StringBundle(bundle);
        }

        return stringBundle;
    }

    private static String keyForClazz(Class<?> clazz, String key) {

        return clazz.getSimpleName() + "." + key;
    }

}
