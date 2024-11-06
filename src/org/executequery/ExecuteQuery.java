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
import java.util.HashMap;
import java.util.Map;

/**
 * The entry point for RedExpert application.
 *
 * @author Takis Diakoumis
 */
public final class ExecuteQuery {
    private static Map<String, Thread> shutdownHooks;

    public static void main(String[] args) {

        if (isHelpStartupOnly(args)) {
            HelpWindow.main(args);
            return;
        }

        ApplicationContext.getInstance().startup(args);
        new ApplicationLauncher().startup();
    }

    public static void restart(String repoArg, boolean updateEnv) {
        try {

            StringBuilder sb = new StringBuilder("./RedExpert");
            if (System.getProperty("os.arch").toLowerCase().contains("64"))
                sb.append("64");
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                sb.append(".exe");

            String updateEnvStr = updateEnv ? "--update-env" : Constants.EMPTY;

            if (repoArg == null || repoArg.isEmpty())
                repoArg = "-repo=";

            ProcessBuilder processBuilder = new ProcessBuilder(sb.toString(), updateEnvStr, repoArg);
            processBuilder.directory(new File(System.getProperty("user.dir")));

            System.out.println("Executing: " + String.join(" ", processBuilder.command()));
            processBuilder.start();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }

        Application.exitProgram();
    }

    public static void addShutdownHook(String id, Runnable runnable) {
        Thread thread = new Thread(runnable, id);

        shutdownHooks().put(id, thread);
        Runtime.getRuntime().addShutdownHook(thread);
        Log.debug("Added shutdown hook with the id [" + id + "]");
    }

    public static void removeShutdownHook(String id) {
        if (shutdownHooks().containsKey(id)) {
            Runtime.getRuntime().removeShutdownHook(shutdownHooks().get(id));
            shutdownHooks().remove(id);

            Log.debug("Removed shutdown hook with the id [" + id + "]");
        }
    }

    private static Map<String, Thread> shutdownHooks() {
        if (shutdownHooks == null)
            shutdownHooks = new HashMap<>();
        return shutdownHooks;
    }

    private static boolean isHelpStartupOnly(String[] args) {
        return args.length > 0 && args[0].equalsIgnoreCase("help");
    }

}
