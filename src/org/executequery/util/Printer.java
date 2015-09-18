/*
 * Printer.java
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

import java.awt.print.Printable;

import org.executequery.GUIUtilities;
import org.executequery.print.PrintFunction;
import org.executequery.print.PrintingSupport;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.util.SwingWorker;

public final class Printer {

    public void print(final PrintFunction printFunction) {
        
        SwingWorker worker = new SwingWorker() {
            public Object construct() {

                return doPrint(printFunction);
            }

            public void finished() {

                GUIUtils.scheduleGC();
            }
        };

        worker.start();        
    }

    private Object doPrint(PrintFunction printFunction) {

        printFunction = GUIUtilities.getPrintableInFocus();

        Printable printable = printFunction.getPrintable();
    
        return new PrintingSupport().print(
                printable, printFunction.getPrintJobName());
    }
    
}










