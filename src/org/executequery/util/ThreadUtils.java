/*
 * ThreadUtils.java
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

package org.executequery.util;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.underworldlabs.swing.GUIUtils;

public final class ThreadUtils {

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
     * Note: This method 'supresses' the method's thrown exceptions 
     * - InvocationTargetException and InterruptedException.
     *
     * @param runnable - the runnable to be executed
     */
    public static void invokeAndWait(Runnable runnable) {
       
        if (!SwingUtilities.isEventDispatchThread()) {
        
            try {
            
                //System.err.println("Not EDT");
                SwingUtilities.invokeAndWait(runnable);

            } catch (InterruptedException e) {
                
                // nothing to do here

            } catch (InvocationTargetException e) {
                
                // nothing to do here
            }

        } else {

            runnable.run();
        }

    }
    
    /**
     * Executes the specified runnable using a worker thread.
     *
     * @param runnable - the runnable to be executed
     */
     public static void startWorker(final Runnable runnable) {

         GUIUtils.startWorker(runnable);
     }
    
    private ThreadUtils() {}

}





