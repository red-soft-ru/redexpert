/*
 * EventMediator.java
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

package org.executequery;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.executequery.event.ApplicationEvent;
import org.executequery.event.ApplicationEventListener;
import org.executequery.log.Log;

/**
 * Event controller class.
 * Global application events are registered and mediated through this class.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public final class EventMediator {
    
    private static List<ApplicationEventListener> listeners;
    
    static {

        listeners = new ArrayList<ApplicationEventListener>();
    }
    
    public static synchronized void fireEvent(ApplicationEvent event) {
        
        try {

            Object[] arguments = new Object[]{event};

            List<ApplicationEventListener> listenersCopy = copyListeners(listeners);
            for (ApplicationEventListener listener : listenersCopy) {

                if (listener.canHandleEvent(event)) {

                    Method method = findMethod(event, listener);
                    method.invoke(listener, arguments);
                }

            }
            
            // listenersCopy.clear();

        } catch (NoSuchMethodException e) {
            
            handleEventExecutionException(e);

        } catch (IllegalAccessException e) {
            
            handleEventExecutionException(e);

        } catch (InvocationTargetException e) {
            
            handleEventExecutionException(e);
        }
    
    }

    public static synchronized void registerListener(ApplicationEventListener listener) {
        
        listeners.add(listener);
    }

    public static synchronized void deregisterListener(ApplicationEventListener listener) {
        
        if (listeners.contains(listener)) {

            listeners.remove(listener);
        }

    }

    private static Method findMethod(ApplicationEvent event,
            ApplicationEventListener listener) throws NoSuchMethodException {

        Class<?> listenerClass = listener.getClass();
        String methodName = event.getMethod();
        
        for (Method method : listenerClass.getMethods()) {

            if (method.getName().compareTo(methodName) == 0) {
                
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length ==1) {

                    Class<?> parameter = parameterTypes[0];
                    if (parameter.isInstance(event)) {

                        return method;
                    }
                    
                }
                
            }

        }

        throw new NoSuchMethodException(
                String.format("Method [ %s ] not available for class [ %s ]", 
                        event.getMethod(), listener.getClass().getName()));
    }

    private static void handleEventExecutionException(Throwable e) {
        
        if (Log.isDebugEnabled()) {
        
            e.printStackTrace();
        }
    }

    private static <T> List<T> copyListeners(final List<T> sourceList) {

        final List<T> destinationList = new ArrayList<T>(sourceList.size());
        for (Iterator<T> iter = sourceList.iterator(); iter.hasNext();) {
            
            destinationList.add((T) iter.next());
        }

        return destinationList;
    }

    /** Prevent instantiation */
    private EventMediator() {}
    
}






