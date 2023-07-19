package org.executequery.localization;

import javax.swing.*;

/**
 * Localization setup class for default Java objects
 * like JColorChooser or JFileChooser.
 *
 * @author Alexey Kozlov
 */
public class LocaleManager {

    public static final int COLOR_CHOOSER = 0;
    public static final int FILE_CHOOSER = COLOR_CHOOSER + 1;

    public static void updateLocaleEverywhere() {
        bundleColorChooser();
        bundleFileChooser();
    }

    @SuppressWarnings("unused")
    public static void updateLocaleFor(int key) {

        switch (key) {

            case COLOR_CHOOSER:
                bundleColorChooser();
                break;

            case FILE_CHOOSER:
                bundleFileChooser();
                break;

        }
    }

    // --------------------------------------------------
    // See UIManager keys by the several link:
    // https://thebadprogrammer.com/swing-uimanager-keys/
    // --------------------------------------------------

    private static void bundleColorChooser() {

        // --- fixed tab ---

        UIManager.put("ColorChooser.previewText", bundleString("ColorChooser.previewText"));
        UIManager.put("ColorChooser.sampleText", bundleString("ColorChooser.sampleText"));
        UIManager.put("ColorChooser.okText", bundleString("ColorChooser.okText"));
        UIManager.put("ColorChooser.resetText", bundleString("ColorChooser.resetText"));
        UIManager.put("ColorChooser.cancelText", bundleString("ColorChooser.cancelText"));

        // --- swatches tab ---

        UIManager.put("ColorChooser.swatchesNameText", bundleString("ColorChooser.swatchesNameText"));
        UIManager.put("ColorChooser.swatchesRecentText", bundleString("ColorChooser.swatchesRecentText"));

        // --- hsv tab ---

        UIManager.put("ColorChooser.hsvHueText", bundleString("ColorChooser.hueText"));
        UIManager.put("ColorChooser.hsvSaturationText", bundleString("ColorChooser.saturationText"));
        UIManager.put("ColorChooser.hsvValueText", bundleString("ColorChooser.valueText"));
        UIManager.put("ColorChooser.hsvTransparencyText", bundleString("ColorChooser.transparencyText"));

        // --- hsl tab ---

        UIManager.put("ColorChooser.hslHueText", bundleString("ColorChooser.hueText"));
        UIManager.put("ColorChooser.hslSaturationText", bundleString("ColorChooser.saturationText"));
        UIManager.put("ColorChooser.hslLightnessText", bundleString("ColorChooser.lightnessText"));
        UIManager.put("ColorChooser.hslTransparencyText", bundleString("ColorChooser.transparencyText"));

        // --- rgb tab ---

        UIManager.put("ColorChooser.rgbRedText", bundleString("ColorChooser.rgbRedText"));
        UIManager.put("ColorChooser.rgbGreenText", bundleString("ColorChooser.rgbGreenText"));
        UIManager.put("ColorChooser.rgbBlueText", bundleString("ColorChooser.rgbBlueText"));
        UIManager.put("ColorChooser.rgbAlphaText", bundleString("ColorChooser.alphaText"));
        UIManager.put("ColorChooser.rgbHexCodeText", bundleString("ColorChooser.rgbHexCodeText"));

        // --- cmyk tab ---

        UIManager.put("ColorChooser.cmykCyanText", bundleString("ColorChooser.cmykCyanText"));
        UIManager.put("ColorChooser.cmykMagentaText", bundleString("ColorChooser.cmykMagentaText"));
        UIManager.put("ColorChooser.cmykYellowText", bundleString("ColorChooser.cmykYellowText"));
        UIManager.put("ColorChooser.cmykBlackText", bundleString("ColorChooser.cmykBlackText"));
        UIManager.put("ColorChooser.cmykAlphaText", bundleString("ColorChooser.alphaText"));

        // ---

    }

    private static void bundleFileChooser() {

        // --- buttons ---

        UIManager.put("FileChooser.openButtonText", bundleString("FileChooser.openButtonText"));
        UIManager.put("FileChooser.directoryOpenButtonText", bundleString("FileChooser.directoryOpenButtonText"));
        UIManager.put("FileChooser.saveButtonText", bundleString("FileChooser.saveButtonText"));
        UIManager.put("FileChooser.cancelButtonText", bundleString("FileChooser.cancelButtonText"));
        UIManager.put("FileChooser.updateButtonText", bundleString("FileChooser.updateButtonText"));
        UIManager.put("FileChooser.helpButtonText", bundleString("FileChooser.helpButtonText"));

        // --- tooltips ---

        UIManager.put("FileChooser.openButtonToolTipText", bundleString("FileChooser.openButtonToolTipText"));
        UIManager.put("FileChooser.directoryOpenButtonToolTipText", bundleString("FileChooser.directoryOpenButtonToolTipText"));
        UIManager.put("FileChooser.saveButtonToolTipText", bundleString("FileChooser.saveButtonToolTipText"));
        UIManager.put("FileChooser.cancelButtonToolTipText", bundleString("FileChooser.cancelButtonToolTipText"));
        UIManager.put("FileChooser.updateButtonToolTipText", bundleString("FileChooser.updateButtonToolTipText"));
        UIManager.put("FileChooser.helpButtonToolTipText", bundleString("FileChooser.helpButtonToolTipText"));

        UIManager.put("FileChooser.upFolderToolTipText", bundleString("FileChooser.upFolderToolTipText"));
        UIManager.put("FileChooser.homeFolderToolTipText", bundleString("FileChooser.homeFolderToolTipText"));
        UIManager.put("FileChooser.newFolderToolTipText", bundleString("FileChooser.newFolderToolTipText"));
        UIManager.put("FileChooser.listViewButtonToolTipText", bundleString("FileChooser.listViewButtonToolTipText"));
        UIManager.put("FileChooser.detailsViewButtonToolTipText", bundleString("FileChooser.detailsViewButtonToolTipText"));
        UIManager.put("FileChooser.viewMenuButtonToolTipText", bundleString("FileChooser.viewMenuButtonToolTipText"));

        // --- detail table headers ---

        UIManager.put("FileChooser.fileNameHeaderText", bundleString("FileChooser.fileNameHeaderText"));
        UIManager.put("FileChooser.fileTypeHeaderText", bundleString("FileChooser.fileTypeHeaderText"));
        UIManager.put("FileChooser.fileSizeHeaderText", bundleString("FileChooser.fileSizeHeaderText"));
        UIManager.put("FileChooser.fileDateHeaderText", bundleString("FileChooser.fileDateHeaderText"));
        UIManager.put("FileChooser.fileAttrHeaderText", bundleString("FileChooser.fileAttrHeaderText"));

        // --- labels ---

        UIManager.put("FileChooser.lookInLabelText", bundleString("FileChooser.lookInLabelText"));
        UIManager.put("FileChooser.saveInLabelText", bundleString("FileChooser.saveInLabelText"));
        UIManager.put("FileChooser.fileNameLabelText", bundleString("FileChooser.fileNameLabelText"));
        UIManager.put("FileChooser.filesOfTypeLabelText", bundleString("FileChooser.filesOfTypeLabelText"));

        // --- action labels ---

        UIManager.put("FileChooser.refreshActionLabelText", bundleString("FileChooser.refreshActionLabelText"));
        UIManager.put("FileChooser.newFolderActionLabelText", bundleString("FileChooser.newFolderActionLabelText"));
        UIManager.put("FileChooser.listViewActionLabelText", bundleString("FileChooser.listViewActionLabelText"));
        UIManager.put("FileChooser.detailsViewActionLabelText", bundleString("FileChooser.detailsViewActionLabelText"));

        // --- accessible names ---

        UIManager.put("FileChooser.upFolderAccessibleName", bundleString("FileChooser.upFolderAccessibleName"));
        UIManager.put("FileChooser.homeFolderAccessibleName", bundleString("FileChooser.homeFolderAccessibleName"));
        UIManager.put("FileChooser.newFolderAccessibleName", bundleString("FileChooser.newFolderAccessibleName"));
        UIManager.put("FileChooser.listViewButtonAccessibleName", bundleString("FileChooser.listViewButtonAccessibleName"));
        UIManager.put("FileChooser.detailsViewButtonAccessibleName", bundleString("FileChooser.detailsViewButtonAccessibleName"));
        UIManager.put("FileChooser.viewMenuButtonAccessibleName", bundleString("FileChooser.viewMenuButtonAccessibleName"));

        // --- others ---

        UIManager.put("FileChooser.openDialogTitleText", bundleString("FileChooser.openDialogTitleText"));
        UIManager.put("FileChooser.saveDialogTitleText", bundleString("FileChooser.saveDialogTitleText"));

        UIManager.put("FileChooser.newFolderErrorText", bundleString("FileChooser.newFolderErrorText"));
        UIManager.put("FileChooser.acceptAllFileFilterText", bundleString("FileChooser.acceptAllFileFilterText"));

        UIManager.put("FileChooser.fileDescriptionText", bundleString("FileChooser.fileDescriptionText"));
        UIManager.put("FileChooser.directoryDescriptionText", bundleString("FileChooser.directoryDescriptionText"));
        UIManager.put("FileChooser.viewMenuLabelText", bundleString("FileChooser.viewMenuLabelText"));

        // ---

    }

    private static String bundleString(String key) {
        return Bundles.get(LocaleManager.class, key);
    }

}
