/*
 * ConnectionFoldersRepository.java
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

package org.executequery.repository;

import java.util.List;

import org.executequery.gui.browser.ConnectionsFolder;

public interface ConnectionFoldersRepository extends Repository {

    String REPOSITORY_ID = "connection-folders";

    void save();

    String getId();

    List<ConnectionsFolder> findAll();

    ConnectionsFolder add(ConnectionsFolder connectionsFolder);
    
    ConnectionsFolder findByName(String name);
    
    ConnectionsFolder findById(String id);

    boolean nameExists(ConnectionsFolder exclude, String name);

    void save(String path, List<ConnectionsFolder> connectionFolders);

    List<ConnectionsFolder> open(String filePath);

}










