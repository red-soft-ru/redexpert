/*
 * SwingWorker.java
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

package org.underworldlabs.swing.util;

import org.executequery.Constants;

import javax.swing.*;

/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 * <p>
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * <p>
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 */
public abstract class SwingWorker {

    private Object value;  // see getValue(), setValue()
    private String name;

    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private InterruptibleThread thread;

        ThreadVar(InterruptibleThread t) {
            thread = t;
        }

        synchronized InterruptibleThread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }

    private ThreadVar threadVar;

    /**
     * Get the value produced by the worker thread, or null if it
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() {
        return value;
    }

    /**
     * Set the value produced by worker thread
     */
    private synchronized void setValue(Object x) {
        value = x;
    }

    /**
     * Compute the value to be returned by the <code>get</code> method.
     */
    public abstract Object construct();

    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    public void finished() {
    }

    /**
     * Sets canceled flag for thread
     */
    public void setCancel(boolean canceled) {
        threadVar.thread.setCanceled(canceled);
    }

    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }

    /**
     * Return the value created by the <code>construct</code> method.
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     *
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {
            Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }


    /**
     * Start a thread that will call the <code>construct</code> method
     * and then exit.
     */
    public SwingWorker(String name) {

        this.name = name;
        final Runnable doFinished = this::finished;

        Runnable doConstruct = () -> {
            try {
                setValue(construct());
            } finally {
                threadVar.clear();
            }
            SwingUtilities.invokeLater(doFinished);
        };

        InterruptibleThread t = new InterruptibleThread(doConstruct);
        t.setName(this.name);
        threadVar = new ThreadVar(t);

    }

    public SwingWorker (String name, Object userObject) {

        this.name = name;
        final Runnable doFinished = this::finished;

        Runnable doConstruct = () -> {
            try {
                setValue(construct());
            } finally {
                threadVar.clear();
            }
            SwingUtilities.invokeLater(doFinished);
        };

        InterruptibleThread t = new InterruptibleThread(doConstruct, userObject);
        t.setName(this.name);
        threadVar = new ThreadVar(t);

    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }

    public static void run(String name, Runnable runnable) {
        SwingWorker worker = new SwingWorker(name) {

            @Override
            public Object construct() {
                runnable.run();
                return Constants.WORKER_SUCCESS;
            }
        };

        worker.start();
    }

}
