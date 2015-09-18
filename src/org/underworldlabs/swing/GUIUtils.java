/*
 * GUIUtils.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.underworldlabs.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.util.SwingWorker;

/**
 * Simple of collection of GUI utility methods.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class GUIUtils {

    /** Prevent instantiation */
    private GUIUtils() {}

    /**
     * Convenience method for consistent border colour.
     * Actually aims to return the value from <code>
     * UIManager.getColor("controlShadow")</code>.
     *
     * @return the system default border colour
     */
    public static Color getDefaultBorderColour() {
        return UIManager.getColor("controlShadow");
    }

    /**
     * Displays the error dialog displaying the stack trace from a
     * throws/caught exception.
     *
     * @param owner - the owner of the dialog
     * @param message - the error message to display
     * @param e - the throwable
     */
    public static void displayExceptionErrorDialog(Frame owner, String message, Throwable e) {
        new ExceptionErrorDialog(owner, message, e);
    }

    /**
     * Returns the specified component's visible bounds within the screen.
     *
     *  @return the component's visible bounds as a <code>Rectangle</code>
     */
    public static Rectangle getVisibleBoundsOnScreen(JComponent component) {
        Rectangle visibleRect = component.getVisibleRect();
        Point onScreen = visibleRect.getLocation();
        SwingUtilities.convertPointToScreen(onScreen, component);
        visibleRect.setLocation(onScreen);
        return visibleRect;
    }

    /**
     * Calculates and returns the centered position of a dialog with
     * the specified size to be added to the desktop area.
     *
     * @param the component to center to
     * @param the size of the componennt to be added as a
     *        <code>Dimension</code> object
     * @return the <code>Point</code> at which to add the dialog
     */
    public static Point getPointToCenter(Component component, Dimension dimension) {

        Dimension screenSize = getDefaultDeviceScreenSize();

        if (component == null) {

            if (dimension.height > screenSize.height) {
                dimension.height = screenSize.height;
            }

            if (dimension.width > screenSize.width) {
                dimension.width = screenSize.width;
            }

            return new Point((screenSize.width - dimension.width) / 2,
                             (screenSize.height - dimension.height) / 2);
        }

        Dimension frameDim = component.getSize();
        Rectangle dRec = new Rectangle(component.getX(),
                                       component.getY(),
                                       (int)frameDim.getWidth(),
                                       (int)frameDim.getHeight());

        int dialogX = dRec.x + ((dRec.width - dimension.width) / 2);
        int dialogY = dRec.y + ((dRec.height - dimension.height) / 2);

        if (dialogX <= 0 || dialogY <= 0) {

            if (dimension.height > screenSize.height) {
                dimension.height = screenSize.height;
            }

            if (dimension.width > screenSize.width) {
                dimension.width = screenSize.width;
            }

            dialogX = (screenSize.width - dimension.width) / 2;
            dialogY = (screenSize.height - dimension.height) / 2;
        }

        return new Point(dialogX, dialogY);
    }

    public static Dimension getDefaultDeviceScreenSize() {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getScreenDevices()[0];
        Dimension screenSize = gs.getDefaultConfiguration().getBounds().getSize();

        return screenSize;
    }

    /**
     * Returns the system font names within a collection.
     *
     * @return the system fonts names within a <code>Vector</code> object
     */
    public static Vector<String> getSystemFonts() {
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] tempFonts = gEnv.getAllFonts();

        char dot = '.';
        int dotIndex = 0;

        char[] fontNameChars = null;
        String fontName = null;
        Vector<String> fontNames = new Vector<String>();

        for (int i = 0; i < tempFonts.length; i++) {

            fontName = tempFonts[i].getFontName();
            dotIndex = fontName.indexOf(dot);

            if (dotIndex == -1) {
                fontNames.add(fontName);
            }
            else {
                fontNameChars = fontName.substring(0, dotIndex).toCharArray();
                fontNameChars[0] = Character.toUpperCase(fontNameChars[0]);

                fontName = new String(fontNameChars);

                if (!fontNames.contains(fontName)) {
                    fontNames.add(fontName);
                }

            }

        }

        Collections.sort(fontNames);
        return fontNames;
    }

    /**
     * Executes requestFocusInWindow on the specified component
     * using invokeLater.
     *
     * @param c - the component
     */
    public static void requestFocusInWindow(final Component c) {
        invokeAndWait(new Runnable() {
            public void run() {
                c.requestFocusInWindow();
            }
        });
    }

    /**
     * Sets the specified cursor on the primary frame.
     *
     * @param the cursor to set
     */
    private static void setCursor(Cursor cursor, Component component) {
        if (component != null) {
            component.setCursor(cursor);
        }
    }

    /**
     * Sets the application cursor to the system normal cursor
     * the specified component.
     *
     * @param component - the component to set the cursor onto
     */
    public static void showNormalCursor(Component component) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR), component);
    }

    /**
     * Executes the specified runnable using the
     * <code>SwingWorker</code>.
     *
     * @param runnable - the runnable to be executed
     */
     public static void startWorker(final Runnable runnable) {
         SwingWorker worker = new SwingWorker() {
             public Object construct() {
                 try {

                     runnable.run();

                 } catch (final Exception e) {

                     invokeAndWait(new Runnable() {
                         public void run() {
                            displayExceptionErrorDialog(null,
                                    "Error in EDT thread execution: " + e.getMessage(), e);
                         }
                     });

                 }
                 return null;
             }
         };
         worker.start();
     }

    /**
     * Runs the specified runnable in the EDT using
     * <code>SwingUtilities.invokeLater(...)</code>.
     *
     * @param runnable - the runnable to be executed
     */
    public static void invokeLater(Runnable runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Runs the specified runnable in the EDT using
     * <code>SwingUtilities.invokeAndWait(...)</code>.
     * Note: This method 'supresses' the method's
     * thrown exceptions - InvocationTargetException and
     * InterruptedException.
     *
     * @param runnable - the runnable to be executed
     */
    public static void invokeAndWait(Runnable runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
//                System.err.println("Not EDT");
                SwingUtilities.invokeAndWait(runnable);
            }
            catch (InterruptedException e) {}
            catch (InvocationTargetException e) {}
        } else {
            runnable.run();
        }
    }

    /**
     * Sets the application cursor to the system wait cursor on
     * the specified component.
     *
     * @param component - the component to set the cursor onto
     */
    public static void showWaitCursor(Component component) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR), component);
    }

    // -------------------------------------------------------
    // ------ Helper methods for various option dialogs ------
    // -------------------------------------------------------

    // These have been revised to use JDialog as the wrapper to
    // ensure the dialog is centered within the dektop pane and not
    // within the entire screen as you get with JOptionPane.showXXX()

    public static final void displayInformationMessage(Component parent, Object message) {
        displayDialog(parent,
                      JOptionPane.DEFAULT_OPTION,
                      JOptionPane.INFORMATION_MESSAGE,
                      false,
                      "OptionPane.informationIcon",
                      "Message",
                      message);
    }

    public static final String displayInputMessage(Component parent, String title, Object message) {
        return displayDialog(parent,
                             JOptionPane.OK_CANCEL_OPTION,
                             JOptionPane.QUESTION_MESSAGE,
                             true,
                             "OptionPane.questionIcon",
                             title,
                             message).toString();
    }

    public static final void displayWarningMessage(Component parent, Object message) {
        displayDialog(parent,
                      JOptionPane.DEFAULT_OPTION,
                      JOptionPane.WARNING_MESSAGE,
                      false,
                      "OptionPane.warningIcon",
                      "Warning",
                      message);
    }

    /** The dialog return value - where applicable */
    private static Object dialogReturnValue;

    private static Object displayDialog(final Component parent,
                                        final int optionType,
                                        final int messageType,
                                        final boolean wantsInput,
                                        final String icon,
                                        final String title,
                                        final Object message) {

        dialogReturnValue = null;

        Runnable runnable = new Runnable() {
            public void run() {
                showNormalCursor(parent);
                JOptionPane pane = new JOptionPane(message, messageType,
                                            optionType, UIManager.getIcon(icon));
                pane.setWantsInput(wantsInput);

                JDialog dialog = pane.createDialog(parent, title);

                if (message instanceof DialogMessageContent) {

                    ((DialogMessageContent) message).setDialog(dialog);
                }

                dialog.setLocation(getPointToCenter(parent, dialog.getSize()));
                dialog.setVisible(true);
                dialog.dispose();

                if (wantsInput) {
                    dialogReturnValue = pane.getInputValue();
                } else {
                    dialogReturnValue = pane.getValue();
                }

            }
        };
        invokeAndWait(runnable);

        return dialogReturnValue;
    }

    public static final void displayErrorMessage(Component parent, Object message) {
        displayDialog(parent,
                      JOptionPane.DEFAULT_OPTION,
                      JOptionPane.ERROR_MESSAGE,
                      false,
                      "OptionPane.errorIcon",
                      "Error Message",
                      message);
    }

    public static final int displayConfirmCancelErrorMessage(Component parent, Object message) {
        return formatDialogReturnValue(displayDialog(
                                parent,
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.ERROR_MESSAGE,
                                false,
                                "OptionPane.errorIcon",
                                "Error Message",
                                message));
    }

    public static final int displayYesNoDialog(Component parent, Object message, String title) {
        return formatDialogReturnValue(displayDialog(parent,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                false,
                                "OptionPane.questionIcon",
                                title,
                                message));
    }

    public static final int displayConfirmCancelDialog(Component parent, Object message) {
        return formatDialogReturnValue(displayDialog(parent,
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                false,
                                "OptionPane.questionIcon",
                                "Confirmation",
                                message));
    }

    public static final int displayConfirmDialog(Component parent, Object message) {
        return formatDialogReturnValue(displayDialog(parent,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                false,
                                "OptionPane.questionIcon",
                                "Confirmation",
                                message));
    }

    private static int formatDialogReturnValue(Object returnValue) {

        if (returnValue instanceof Integer) {

            return ((Integer) returnValue).intValue();
        }

        return -1;
    }

    /**
     * Schedules the garbage collector to run
     */
    public static void scheduleGC() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.gc();
            }
        });
    }

    /**
     * Returns whether the current applied look and feel is
     * the EQ default look and feel (or the metal look with ocean theme).
     *
     * @return true | false
     */
    public static boolean isDefaultLookAndFeel() {
        return UIUtils.isDefaultLookAndFeel() || UIUtils.usingOcean();
    }

    /**
     * Returns true if we're using the Ocean Theme under the
     * MetalLookAndFeel.
     */
    public static boolean usingOcean() {
        return UIUtils.usingOcean();
    }

    /**
     * Returns whether the current applied look and feel is
     * the MetalLookAndFeel;
     *
     * @return true | false
     */
    public static boolean isMetalLookAndFeel() {
        return UIUtils.isMetalLookAndFeel();
    }

    public static Color getSlightlyBrighter(Color color, float factor) {
        float[] hsbValues = new float[3];
        Color.RGBtoHSB(
            color.getRed(),
            color.getGreen(),
            color.getBlue(),
            hsbValues);
        float hue = hsbValues[0];
        float saturation = hsbValues[1];
        float brightness = hsbValues[2];
        float newBrightness = Math.min(brightness * factor, 1.0f);
        return Color.getHSBColor(hue, saturation, newBrightness);
    }

}






