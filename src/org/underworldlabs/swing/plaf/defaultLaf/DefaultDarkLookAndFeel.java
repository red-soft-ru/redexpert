package org.underworldlabs.swing.plaf.defaultLaf;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

public class DefaultDarkLookAndFeel extends FlatDarkLaf {

    @Override
    public UIDefaults getDefaults() {
        return DefaultLookAndFeel.baseDefaults(super.getDefaults());
    }

    @Override
    public void initialize() {
        super.initialize();
        DefaultLookAndFeel.baseInitialize();
    }

}
