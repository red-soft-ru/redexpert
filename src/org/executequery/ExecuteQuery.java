/*
 * ExecuteQuery.java
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

package org.executequery;

import org.executequery.gui.HelpWindow;
import org.executequery.log.Log;

import java.io.File;
import java.io.IOException;

/**
 * The entry point for RedExpert application.
 *
 * @author Takis Diakoumis
 */
public final class ExecuteQuery {

    private static ProcessBuilder shutdownHook = null;

    public static void main(String[] args) {

        /*
        // make sure the installed java version is at least 1.7
        if (!MiscUtils.isMinJavaVersion(1, 7)) {

            JOptionPane.showMessageDialog(null,
                    "The minimum required Java version is 1.7.\n" +
                    "The reported version is " +
                    System.getProperty("java.vm.version") +
                    ".\n\nPlease download and install the latest Java " +
                    "version\nfrom http://java.sun.com and try again.\n\n",
                    "Java Version Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
        */

        if (isHelpStartupOnly(args)) {
            HelpWindow.main(args);

        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(ExecuteQuery::shutdownHook));
            ApplicationContext.getInstance().startup(args);
            new ApplicationLauncher().startup();
        }

    }

    public static void restart(String repoArg) {

        try {

            StringBuilder sb = new StringBuilder("./RedExpert");
            if (System.getProperty("os.arch").toLowerCase().contains("64"))
                sb.append("64");
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                sb.append(".exe");
            if (repoArg == null || repoArg.isEmpty())
                repoArg = "-repo=";

            System.out.println("Executing: " + sb);

            ProcessBuilder processBuilder = new ProcessBuilder(sb.toString(), repoArg);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.start();

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        stop();
    }

    public static void stop() {
        System.exit(0);
    }

    public static void setShutdownHook(ProcessBuilder shutdownHook) {
        ExecuteQuery.shutdownHook = shutdownHook;
    }

    private static void shutdownHook() {

        try {
            if (ExecuteQuery.shutdownHook != null)
                ExecuteQuery.shutdownHook.start();

        } catch (IOException e) {
            Log.error("Error starting shutdown hook", e);
        }
    }

    private static boolean isHelpStartupOnly(String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("help");
    }

}
