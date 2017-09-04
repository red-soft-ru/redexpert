/*
 * TablePrivilege.java
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

package org.executequery.databaseobjects;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TablePrivilege {
    
    private String grantor;
    private String grantee;
    private String privilege;
    private String grantable;
    
    public TablePrivilege() {}
    
    public TablePrivilege(String grantor, 
                          String grantee,
                          String privilege, 
                          String grantable) {
        this.grantor = grantor;
        this.grantee = grantee;
        this.privilege = privilege;
        this.grantable = grantable;
    }
    
    public String getGrantor() {
        return grantor;
    }
    
    public void setGrantor(String grantor) {
        this.grantor = grantor;
    }
    
    public String getGrantee() {
        return grantee;
    }
    
    public void setGrantee(String grantee) {
        this.grantee = grantee;
    }
    
    public String getPrivilege() {
        return privilege;
    }
    
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }
    
    public String getGrantable() {
        return grantable;
    }
    
    public void setGrantable(String grantable) {
        this.grantable = grantable;
    }
    
}















