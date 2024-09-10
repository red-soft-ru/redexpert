package org.executequery.localization;

public enum InterfaceLanguage {

    en("en"),
    ru("ru"),
    pt_br("pt_br");

    private final String key;
    private final String label;

    InterfaceLanguage(String key) {
        this.key = key;
        this.label = Bundles.get("preferences.language." + key);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

}
