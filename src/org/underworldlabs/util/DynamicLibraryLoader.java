/*
 * DynamicLibraryLoader.java
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

package org.underworldlabs.util;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Takis Diakoumis
 */
public class DynamicLibraryLoader extends URLClassLoader {

    private ClassLoader parent = null;

    public DynamicLibraryLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
        parent = ClassLoader.getSystemClassLoader();
    }

    public Class<?> loadLibrary(String clazz)
            throws ClassNotFoundException {
        return loadClass(clazz, true);
    }

    public Class<?> loadLibrary(String clazz, boolean resolve)
            throws ClassNotFoundException {
        return loadClass(clazz, resolve);
    }

    protected synchronized Class<?> loadClass(String classname, boolean resolve)
            throws ClassNotFoundException {

        Class<?> theClass = findLoadedClass(classname);

        if (theClass != null) {
            return theClass;
        }

        try {

            theClass = findBaseClass(classname);

        } catch (ClassNotFoundException cnfe) {

            theClass = findClass(classname);
        }

        if (resolve) {
            resolveClass(theClass);
        }

        return theClass;

    }

    private Class<?> findBaseClass(String name) throws ClassNotFoundException {

        if (parent == null) {
            return findSystemClass(name);
        } else {
            return parent.loadClass(name);
        }

    }

    public static Object loadingObjectFromClassLoader(Object unwrapObject, String shortClassName)
            throws ClassNotFoundException {
        return loadingObjectFromClassLoader(unwrapObject, "biz.redsoft."
                + shortClassName, "./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
    }

    public static Object loadingObjectFromClassLoader(Object unwrapObject, String className, String jarPath)
            throws ClassNotFoundException {

        URL[] urls;
        Class clazzdb;
        Object odb = null;
        try {
            urls = MiscUtils.loadURLs(jarPath);
            ClassLoader cl = new URLClassLoader(urls, unwrapObject.getClass().getClassLoader());
            clazzdb = cl.loadClass(className);
            odb = clazzdb.newInstance();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error loading class ");
            sb.append(className);
            sb.append(" from ");
            sb.append(jarPath);
            throw new ClassNotFoundException(sb.toString(), e.getCause());
        }

        return odb;
    }

    public static Object loadingObjectFromClassLoaderWithParams(Object unwrapObject, String shortClassName, Parameter... params)
            throws ClassNotFoundException {
        return loadingObjectFromClassLoaderWithParams(unwrapObject, "biz.redsoft."
                + shortClassName, "./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar", params);
    }

    public static Object loadingObjectFromClassLoaderWithParams(Object unwrapObject, String className, String jarPath, Parameter... params)
            throws ClassNotFoundException {

        URL[] urls;
        Class clazzdb;
        Object odb = null;
        try {
            urls = MiscUtils.loadURLs(jarPath);
            ClassLoader cl = new URLClassLoader(urls, unwrapObject.getClass().getClassLoader());
            clazzdb = cl.loadClass(className);
            Class<?>[] types = new Class<?>[params.length];
            Object[] parameters = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                types[i] = params[i].type;
                parameters[i] = params[i].parameter;
            }
            odb = clazzdb.getConstructor(types).newInstance(parameters);
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error loading class ");
            sb.append(className);
            sb.append(" from ");
            sb.append(jarPath);
            throw new ClassNotFoundException(sb.toString(), e.getCause());
        }

        return odb;
    }

    public static class Parameter {
        private Class<?> type;
        private Object parameter;

        public Parameter(Class<?> type, Object parameter) {
            this.type = type;
            this.parameter = parameter;
        }

        public Class<?> getType() {
            return type;
        }

        public Object getParameter() {
            return parameter;
        }
    }
}






