/*
 * AbstractPropertiesBasePanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.prefs;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User preferences base panel.
 *
 * @author Takis Diakoumis
 */
abstract class AbstractPropertiesBasePanel extends JPanel
        implements UserPreferenceFunction,
        PreferenceChangeListener,
        PreferenceTableModelListener {

    public static final int TABLE_ROW_HEIGHT = 26;

    private JButton restoreButton;
    private List<PreferenceChangeListener> listeners;

    public AbstractPropertiesBasePanel() {
        super(new GridBagLayout());
        init();
    }

    private void init() {
        listeners = new ArrayList<>();

        restoreButton = WidgetFactory.createButton(
                "restoreButton",
                e -> this.restoreDefaults(),
                Bundles.get("AbstractPropertiesBasePanel.restoreDefaults")
        );

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(restoreButton);

        setBorder(BorderFactory.createLineBorder(GUIUtilities.getDefaultBorderColour()));
        add(bottomPanel, new GridBagHelper()
                .setInsets(0, 5, 5, 5)
                .anchorSouthEast()
                .fillNone()
                .setY(2)
                .get()
        );
    }

    protected final void addContent(JPanel panel) {
        add(panel, new GridBagHelper()
                .setInsets(5, 5, 5, 0)
                .setMaxWeightX()
                .setMaxWeightY()
                .fillBoth()
                .get()
        );

        if (panel instanceof SimplePreferencesPanel)
            ((SimplePreferencesPanel) panel).addPreferenceTableModelListener(this);
    }

    // ---

    protected final void setRestoreButtonVisible(boolean visible) {
        restoreButton.getParent().setVisible(visible);
    }

    protected final String stringUserProperty(String key) {
        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, key);
    }

    protected static Font getDefaultFont() {
        return new Font("dialog", Font.PLAIN, 12);
    }

    protected static String bundledStaticString(String key) {
        return Bundles.get("preferences." + key);
    }

    protected String bundledString(String key) {
        return Bundles.get(getClass(), key);
    }

    // --- UserPreferenceFunction impl ---

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        listeners.add(listener);
    }

    // --- PreferenceChangeListener impl ---

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {
        PropertiesPanel.checkAndSetRestartNeed(e.getKey());
    }

    // --- PreferenceTableModelListener impl ---

    @Override
    public void preferenceTableModelChange(PreferenceTableModelChangeEvent e) {
        for (PreferenceChangeListener listener : listeners)
            listener.preferenceChange(new PreferenceChangeEvent(this, e.getKey(), e.getValue()));
    }

}
