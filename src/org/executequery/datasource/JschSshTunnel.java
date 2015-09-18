/*
 * JschSshTunnel.java
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

package org.executequery.datasource;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class JschSshTunnel implements SshTunnel {

    private Session session;
    private int tunnelPort;
    
    @Override
    public void connect(DatabaseConnection databaseConnection) {

        if (!databaseConnection.isSshTunnel()) {
            
            return;
        }
        
        session = createSession(databaseConnection);
    }

    @Override
    public void disconnect(DatabaseConnection databaseConnection) {

        if (!databaseConnection.isSshTunnel()) {
            
            return;
        }

        if (session != null) {
            
            Log.info("Disconnecting SSH tunnel to host [ " + databaseConnection.getHost() + 
                    " ] through port [ " + databaseConnection.getSshPort() + " ] ... ");
            
            session.disconnect();
            session = null;

            Log.info("SSH tunnel to host [ " + databaseConnection.getHost() + 
                    " ] through port [ " + databaseConnection.getSshPort() + " ] closed");
        }
        
    }
    
    public int getTunnelPort() {
        
        return tunnelPort;
    }
    
    private Session createSession(DatabaseConnection databaseConnection) {
        
        Log.info("Creating SSH tunnel to host [ " + databaseConnection.getHost() + 
                " ] through port [ " + databaseConnection.getSshPort() + " ]");
        
        try {

            tunnelPort = findUnusedPort();
            
            JSch jsch = new JSch();
            session = jsch.getSession(databaseConnection.getSshUserName(), databaseConnection.getHost(), databaseConnection.getSshPort());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            session.setConfig(config);
            session.setPassword(databaseConnection.getUnencryptedSshPassword());

            session.connect();

            int assignedPort = session.setPortForwardingL(tunnelPort, databaseConnection.getHost(), databaseConnection.getPortInt());
            
            Log.info("Created SSH tunnel :: localhost:" + assignedPort + " --> " + databaseConnection.getHost() + ":" + databaseConnection.getPort());            

            return session;
            
        } catch (Exception e) {

            throw new DataSourceException("Error opening SSH tunnel: " + e.getMessage(), e);
        }

    }

    private int findUnusedPort() {

        int start = 10000;
        int end = 11000;

        Log.info("Scanning for unused local ports between [ " + start + " -- " + end + " ]");

        for (int i = start; i <= end; i++) {

            Log.info("Scanning for unused port at [ " + i + " ]");
            
            ServerSocket serverSocket = null;

            try {

                serverSocket = new ServerSocket(i);
                Log.info("Found unused port at [ " + i + " ]");
                return i;

            } catch (IOException e) {

                Log.info("Port " + i + "is currently in use, retrying port " + i + 1);

            } finally {

                if (serverSocket != null) {
                 
                    try {
                    
                        serverSocket.close();

                    } catch (IOException e) {}

                    serverSocket = null;
                }
            }
        }

        throw new DataSourceException("Unable to find open port between " + start + " and " + end);
    }
    
    
}

