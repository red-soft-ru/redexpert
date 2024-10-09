package org.underworldlabs.util;

import org.executequery.Constants;
import org.executequery.log.Log;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksey Kozlov
 */
public final class ParameterSaver {

    private final Map<String, Component> components;
    private final PanelsStateProperties stateProperties;

    public ParameterSaver(String className) {
        stateProperties = new PanelsStateProperties(className);
        components = new HashMap<>();
    }

    public void set(Map<String, Component> components) {
        this.components.clear();
        add(components);
    }

    public void add(Map<String, Component> components) {
        this.components.putAll(components);
    }

    public PanelsStateProperties getProperties() {
        return stateProperties;
    }

    public void save() {

        for (String key : components.keySet()) {
            Object value = null;

            Component component = components.get(key);
            if (component instanceof JCheckBox) {
                value = ((JCheckBox) component).isSelected();

            } else if (component instanceof JTextField) {
                value = ((JTextField) component).getText().trim();

            } else if (component instanceof JComboBox) {
                value = ((JComboBox<?>) component).getSelectedItem();

            } else if (component instanceof JSpinner) {
                value = ((JSpinner) component).getValue();
            }

            if (value == null)
                value = Constants.EMPTY;

            stateProperties.put(key, value.toString());
        }

        stateProperties.save();
    }

    public void restore() {

        for (String key : components.keySet()) {
            Component component = components.get(key);

            String value = stateProperties.get(key);
            if (MiscUtils.isNull(value))
                continue;

            try {
                if (component instanceof JCheckBox) {
                    ((JCheckBox) component).setSelected(value.equalsIgnoreCase("true"));

                } else if (component instanceof JTextField) {
                    ((JTextField) component).setText(value);

                } else if (component instanceof JComboBox) {
                    ((JComboBox<?>) component).setSelectedItem(value);

                } else if (component instanceof JSpinner) {
                    ((JSpinner) component).setValue(Integer.valueOf(value));
                }

            } catch (Throwable e) {
                Log.debug(e.getMessage());
            }
        }
    }

}
