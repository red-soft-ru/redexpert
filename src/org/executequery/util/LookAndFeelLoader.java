/*
 * LookAndFeelLoader.java
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

package org.executequery.util;

import com.formdev.flatlaf.FlatLaf;
import org.executequery.ApplicationException;
import org.executequery.ApplicationLauncher;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.swing.plaf.*;
import org.underworldlabs.swing.plaf.base.CustomTextAreaUI;
import org.underworldlabs.swing.plaf.base.CustomTextPaneUI;
import org.underworldlabs.swing.plaf.defaultLaf.DefaultDarkLookAndFeel;
import org.underworldlabs.swing.plaf.defaultLaf.DefaultLightLookAndFeel;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.KeyEvent;

public final class LookAndFeelLoader {

    public LookAndFeelType loadLookAndFeel(String lookAndFeelType) {
        try {
            return loadLookAndFeel(LookAndFeelType.valueOf(lookAndFeelType));

        } catch (IllegalArgumentException e) {
            ApplicationLauncher.setNeedUpdateColorsAndFonts(true);
            return loadLookAndFeel(LookAndFeelType.DEFAULT_LIGHT);
        }
    }

    public LookAndFeelType loadLookAndFeel(LookAndFeelType lookAndFeelType) {
        FlatLaf.registerCustomDefaultsSource("org.executequery.plaf.defaultLaf");

        try {

            switch (lookAndFeelType) {
                case DEFAULT_DARK:
                    loadDefaultDarkLookAndFeel();
                    break;
                case CLASSIC_LIGHT:
                    loadClassicLightLookAndFeel();
                    break;
                case CLASSIC_DARK:
                    loadClassicDarkLookAndFeel();
                    break;
                case NATIVE:
                    loadSystemLookAndFeel();
                    break;
                case PLUGIN:
                    loadCustomLookAndFeel();
                    break;
                case LACKEY:
                    loadLackeyLookAndFeel();
                    break;
                default:
                    loadDefaultLightLookAndFeel();
                    break;
            }

        } catch (Exception e) {
            throw new ApplicationException(e);
        }

        if (!UIUtils.isNativeMacLookAndFeel()) {
            CustomTextAreaUI.initialize();
            CustomTextPaneUI.initialize();
        }

        applyMacSettings();
        return lookAndFeelType;
    }

    private void applyMacSettings() {

        if (!UIUtils.isMac())
            return;

        String[] textComponents = {"TextField", "TextPane", "TextArea", "EditorPane", "PasswordField"};
        for (String textComponent : textComponents) {

            InputMap im = (InputMap) UIManager.get(textComponent + ".focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }

        if (UIUtils.isNativeMacLookAndFeel())
            UIManager.put("Table.gridColor", UIUtils.getDefaultBorderColour());
    }

    private void loadCustomLookAndFeel() {
        try {
            new PluginLookAndFeelManager().loadLookAndFeel();

        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    private void loadDefaultLightLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new DefaultLightLookAndFeel());
            setDecorating(true, true);

        } catch (UnsupportedLookAndFeelException e) {
            throw new ApplicationException(e);
        }
    }

    private void loadDefaultDarkLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new DefaultDarkLookAndFeel());
            setDecorating(true, true);

        } catch (UnsupportedLookAndFeelException e) {
            throw new ApplicationException(e);
        }
    }

    private void loadClassicLightLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new UnderworldLabsFlatLookAndFeel());
            setDecorating(false, false);

        } catch (UnsupportedLookAndFeelException e) {
            throw new ApplicationException(e);
        }
    }

    private void loadClassicDarkLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new UnderworldLabsDarkFlatLookAndFeel());
            setDecorating(false, false);

        } catch (UnsupportedLookAndFeelException e) {
            throw new ApplicationException(e);
        }
    }

    private void loadLackeyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new LackeyLookAndFeel());
            setDecorating(false, false);

        } catch (UnsupportedLookAndFeelException e) {
            throw new ApplicationException(e);
        }
    }

    public void loadSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setDecorating(false, false);

        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    public void loadCrossPlatformLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            setDecorating(false, false);

        } catch (Exception e) {
            throw new ApplicationException(e);
        }
    }

    private void setDecorating(boolean decorateDialogs, boolean decorateFrames) {
        UserProperties.getInstance().setBooleanProperty("decorate.frame.look", decorateFrames);
        UserProperties.getInstance().setBooleanProperty("decorate.dialog.look", decorateDialogs);
    }

    public void decorateDialogsAndFrames(boolean decorateDialogs, boolean decorateFrames) {
        JDialog.setDefaultLookAndFeelDecorated(decorateDialogs);
        JFrame.setDefaultLookAndFeelDecorated(decorateFrames);
    }

}
