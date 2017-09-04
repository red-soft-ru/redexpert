/*
 * ProcedureParameterSorter.java
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

package org.executequery.databasemediators;

import java.io.Serializable;
import java.sql.DatabaseMetaData;
import java.util.Comparator;
import org.executequery.databaseobjects.ProcedureParameter;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ProcedureParameterSorter implements Comparator<ProcedureParameter>, Serializable {

    public int compare(ProcedureParameter value1, ProcedureParameter value2) {
        
        int type1 = value1.getType();
        int type2 = value2.getType();

        if (type1 == type2) {

            return 0;

        } else if (type1 == DatabaseMetaData.procedureColumnIn ||
              type1 == DatabaseMetaData.procedureColumnInOut) {

            return 1;

        } else {
          
            return -1;
        }

    }

}











