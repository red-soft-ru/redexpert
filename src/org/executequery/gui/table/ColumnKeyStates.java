package org.executequery.gui.table;

import org.executequery.gui.IconManager;
import org.executequery.localization.Bundles;

import javax.swing.*;

public interface ColumnKeyStates {

    ImageIcon newImage = IconManager.getIcon("icon_mark_new");
    ImageIcon deleteImage = IconManager.getIcon("icon_mark_delete");
    ImageIcon primaryImage = IconManager.getIcon("icon_key_primary");
    ImageIcon foreignImage = IconManager.getIcon("icon_key_foreign");
    ImageIcon primaryForeignImage = IconManager.getIcon("icon_key_mixed");

    String newTooltip = Bundles.get(ColumnKeyStates.class, "newTooltip");
    String deleteTooltip = Bundles.get(ColumnKeyStates.class, "deleteTooltip");
    String primaryTooltip = Bundles.get(ColumnKeyStates.class, "primaryTooltip");
    String foreignTooltip = Bundles.get(ColumnKeyStates.class, "foreignTooltip");
    String primaryForeignTooltip = Bundles.get(ColumnKeyStates.class, "primaryForeignTooltip");

}
