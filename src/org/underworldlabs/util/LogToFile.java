package org.underworldlabs.util;

import java.io.IOException;
import java.time.LocalTime;

public class LogToFile {
    public static void log(String path, String text, boolean printStackTrace) {
        try {
            text = "[" + LocalTime.now() + "]" + " " + text;
            if (printStackTrace) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    text += "\n\t" + stack[i];
                }
            }
            FileUtils.writeFile(path, text, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
