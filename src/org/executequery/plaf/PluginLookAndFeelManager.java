/*
 * PluginLookAndFeelManager.java
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

package org.executequery.plaf;

import java.io.File;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.net.URL;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.underworldlabs.util.DynamicLibraryLoader;
import org.executequery.GUIUtilities;
import org.executequery.repository.LookAndFeelProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PluginLookAndFeelManager  {
    
    /** The look and feel to install */
    private LookAndFeelDefinition lfd;
    
    /** Whether the look and feel has been installed */
    private boolean installed;
    
    public PluginLookAndFeelManager() {
        installed = false;
    }
    
    public PluginLookAndFeelManager(LookAndFeelDefinition lfd) {
        this();
        this.lfd = lfd;
    }
    
    public void loadLookAndFeel() throws Exception {
        
        if (lfd == null) {
            LookAndFeelProperties.newInstance();
            LookAndFeelProperties.loadLookAndFeels();
            lfd = LookAndFeelProperties.getInstalledCustomLook();
        } 
        
        String paths = lfd.getLibraryPath();
        
        String token = ";";
        Vector pathsVector = new Vector();
        
        if (paths.indexOf(token) != -1) {
            StringTokenizer st = new StringTokenizer(paths, token);
            while (st.hasMoreTokens()) {
                pathsVector.add(st.nextToken());
            } 
        } else {
            pathsVector.add(paths);
        } 
        
        URL[] urls = new URL[pathsVector.size()];
        int v_length = pathsVector.size();
        
        for (int i = 0; i < v_length; i++) {
            File f = new File((String)pathsVector.elementAt(i));
            urls[i] = f.toURL();
            f = null;
        } 
        
        pathsVector = null;
        
        if (lfd.isSkinLookAndFeel())
            loadSkinLookAndFeel(urls);
        else
            loadCustomLookAndFeel(urls);
        
    }
    
    private void loadCustomLookAndFeel(URL[] urls)
    throws Exception {
        
        try {
            DynamicLibraryLoader d_loader = new DynamicLibraryLoader(urls);
            Class c = d_loader.loadLibrary(lfd.getClassName());
            
            LookAndFeel laf = (LookAndFeel)c.newInstance();
            
            if (!laf.isSupportedLookAndFeel()) {
                GUIUtilities.displayErrorMessage(
                "The selected Look and Feel is not supported");
                return;
            } 
            
            LookAndFeelInfo info = new LookAndFeelInfo(laf.getName(), c.getName());
            UIManager.installLookAndFeel(info);
            UIManager.setLookAndFeel(laf);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", d_loader);
            
            installed = true;
            
        } catch (ClassNotFoundException cExc) {
            GUIUtilities.displayErrorMessage(
            "The specified Look and Feel class was not found");
        } 
        
        catch (UnsupportedLookAndFeelException ulfExc) {
            GUIUtilities.displayErrorMessage(
            "The selected Look and Feel is not supported");
        } 
        
    }
    
    private void loadSkinLookAndFeel(URL[] urls)
    throws Exception {
        try {
            DynamicLibraryLoader d_loader = new DynamicLibraryLoader(urls);
            
            String skinLFClassName = "com.l2fprod.gui.plaf.skin.SkinLookAndFeel";
            
            Class skinLfClass = d_loader.loadLibrary(skinLFClassName);
            Class skinClass = d_loader.loadLibrary("com.l2fprod.gui.plaf.skin.Skin");
            
            Method loadThemePack = skinLfClass.getMethod("loadThemePack",
            new Class[]{String.class});
            
            Method setSkin = skinLfClass.getMethod("setSkin", new Class[]{skinClass});
            
            Object[] params = new String[]{lfd.getThemePack()};
            Object skin = loadThemePack.invoke(skinLfClass, params);
            setSkin.invoke(skinLfClass, new Object[]{skin});
            
            LookAndFeel laf = (LookAndFeel)skinLfClass.newInstance();
            
            if (!laf.isSupportedLookAndFeel()) {
                GUIUtilities.displayErrorMessage(
                "The selected Look and Feel is not supported");
                return;
            } 
            
            UIManager.setLookAndFeel(laf);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", d_loader);
            
            installed = true;
            
        } catch (ClassNotFoundException cExc) {
            GUIUtilities.displayErrorMessage(
            "The specified Look and Feel class was not found");
        } 
        
        catch (InvocationTargetException invExc) { invExc.printStackTrace();
        GUIUtilities.displayErrorMessage(
        "An error occured loading the Skin L&F library.\n" +
        "Loading default look and feel.");
        } 
        
        catch (UnsupportedLookAndFeelException ulfExc) {
            GUIUtilities.displayErrorMessage(
            "The selected Look and Feel is not supported");
        } 
        
    }
    
    public boolean isInstalled() {
        return installed;
    }
    
    public LookAndFeelDefinition getLookAndFeelDefinition() {
        return lfd;
    }
    
}


















