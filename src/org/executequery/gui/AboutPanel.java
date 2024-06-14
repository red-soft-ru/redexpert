/*
 * AboutPanel.java
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

package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

/**
 * System About panel.
 *
 * @author Takis Diakoumis
 */
public class AboutPanel extends BaseDialog {
    public static final String TITLE = bundledString("Title");

    private Icon icon;
    private JButton closeButton;
    private JButton viewLicenseButton;

    private JLabel versionLabel;
    private JLabel designedByLabel;
    private JLabel discussionChatLabel;
    private JLabel reddatabaseNewsLabel;

    public AboutPanel() {
        super(TITLE, true);
        init();
        arrange();
    }

    private void init() {
        icon = GUIUtilities.loadVectorIcon("ApplicationIcon.svg", 70);

        versionLabel = WidgetFactory.createLabel(
                "RedExpert " + System.getProperty("executequery.minor.version"),
                36
        );

        designedByLabel = WidgetFactory.createLabel(
                String.format(
                        "Designed by Red Soft 2015-%s | red-soft.ru",
                        Calendar.getInstance().get(Calendar.YEAR)
                ),
                18
        );

        reddatabaseNewsLabel = WidgetFactory.createLinkLabel(
                "reddatabaseNewsLabel",
                bundledString("reddatabaseNewsLabel"),
                "https://t.me/reddatabase",
                16
        );
        reddatabaseNewsLabel.setIcon(GUIUtilities.loadIcon("WebComponent16"));

        discussionChatLabel = WidgetFactory.createLinkLabel(
                "discussionChatLabel",
                bundledString("discussionChatLabel"),
                bundledString("discussionChatLink"),
                16
        );
        discussionChatLabel.setIcon(GUIUtilities.loadIcon("WebComponent16"));

        viewLicenseButton = WidgetFactory.createButton(
                "viewLicenseButton",
                bundledString("ViewLicense"),
                e -> displayLicense()
        );

        closeButton = WidgetFactory.createButton(
                "closeButton",
                Bundles.get("common.close.button"),
                e -> dispose()
        );
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- logo panel ---

        JPanel logoPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest();
        logoPanel.add(new JLabel(icon), gbh.setHeight(2).get());
        logoPanel.add(versionLabel, gbh.nextCol().topGap(0).setHeight(1).spanX().get());
        logoPanel.add(designedByLabel, gbh.nextRow().get());

        // --- links panel ---

        JPanel linksPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().bottomGap(5);
        linksPanel.add(reddatabaseNewsLabel, gbh.get());
        linksPanel.add(discussionChatLabel, gbh.nextRow().bottomGap(0).get());

        // --- button panel  ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest();
        buttonPanel.add(viewLicenseButton, gbh.get());
        buttonPanel.add(closeButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest();
        mainPanel.add(logoPanel, gbh.get());
        mainPanel.add(linksPanel, gbh.nextRowFirstCol().topGap(30).leftGap(5).get());
        mainPanel.add(buttonPanel, gbh.nextRowFirstCol().anchorSouthEast().spanY().get());

        addDisplayComponentWithEmptyBorder(mainPanel);
        setResizable(false);
        display();
    }

    private void displayLicense() {
        new InformationDialog(
                bundledString("License"),
                "org/executequery/gpl.license",
                InformationDialog.RESOURCE_PATH_VALUE,
                null
        ).display();
    }

    public static String bundledString(String key) {
        return Bundles.get(AboutPanel.class, key);
    }

}
