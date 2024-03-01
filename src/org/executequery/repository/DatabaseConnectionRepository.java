/*
 * DatabaseConnectionRepository.java
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

package org.executequery.repository;

import org.executequery.databasemediators.DatabaseConnection;

import java.util.List;

public interface DatabaseConnectionRepository extends Repository {

    String REPOSITORY_ID = "database-connections";

    void save();

    String getId();

    List<DatabaseConnection> findAll();

    DatabaseConnection findById(String id);

    DatabaseConnection findByName(String name);

    DatabaseConnection findBySourceName(String sourceName);

    boolean nameExists(DatabaseConnection exclude, String name, String folderId);

    void save(String path, List<DatabaseConnection> databaseConnections);

    List<DatabaseConnection> open(String filePath);

    DatabaseConnection add(DatabaseConnection databaseConnection);

}











