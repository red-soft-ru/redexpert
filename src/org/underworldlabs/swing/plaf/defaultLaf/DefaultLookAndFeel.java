package org.underworldlabs.swing.plaf.defaultLaf;

import javax.swing.*;
import java.awt.*;

final class DefaultLookAndFeel {

    public static UIDefaults baseDefaults(UIDefaults defaults) {

        defaults.put("Table.showVerticalLines", true);
        defaults.put("Table.showHorizontalLines", true);
        defaults.put("Table.intercellSpacing", new Dimension(1, 1));

        return defaults;
    }

    public static void baseInitialize() {
        PopupFactory.setSharedInstance(new PopupFactory());
    }

}
