package org.executequery.gui.browser;

//import com.sun.glass.ui.Application;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseRole;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.components.table.RoleTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.print.Printable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by mikhan808 on 02.04.2017.
 */
public class BrowserRolePanel  extends AbstractFormObjectViewPanel  {
    public static final String NAME = "BrowserRolePanel";
    public BrowserController controller;
    Action act;
    StatementExecutor querySender;
    enum Action
    {
        NO_ALL_GRANTS_TO_OBJECT,
        ALL_GRANTS_TO_OBJECT,
        ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION,
        NO_GRANT_TO_ALL_OBJECTS,
        GRANT_TO_ALL_OBJECTS,
        GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION,
        NO_ALL_GRANTS_TO_ALL_OBJECTS,
        ALL_GRANTS_TO_ALL_OBJECTS,
        ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION,
        CREATE_TABLE

    }
    public BrowserRolePanel(BrowserController contr)
    {   //super();
        controller=contr;
        gr=GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        no=GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adm=GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        enableGrant=true;
        initComponents();



    }
    void setValues(DefaultDatabaseRole ddr,BrowserController contr)
    {


        /*if (con!=null)
        {
            ConnectionManager.close(controller.getDatabaseConnection(),con);
        }*/
        controller=contr;
       // ConnectionManager.
        try {
            //con = ConnectionManager.getConnection(controller.getDatabaseConnection());
            querySender=new DefaultStatementExecutor(controller.getDatabaseConnection(),true);
        }
        catch(Exception e)
        {
                GUIUtilities.displayErrorMessage(e.getMessage());

        }
        create_roles_list(ddr.getName());
    }

    //Connection con;
    Statement state;
    ResultSet rs;
    Boolean enableGrant;
    void initComponents()
    {
        try {
            //con = ConnectionManager.getConnection(controller.getDatabaseConnection());
            querySender=new DefaultStatementExecutor(controller.getDatabaseConnection(),true);
        }
        catch(Exception e)
        {


        }

        jScrollPane1 = new javax.swing.JScrollPane();
        rolesTable = new javax.swing.JTable();
        jComboBox1 = new javax.swing.JComboBox<>();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        objectBox = new javax.swing.JComboBox<>();
        allUsersGrantButton = new javax.swing.JButton();
        allUsersAdminOptionButton = new javax.swing.JButton();
        allRolesNoGrantButton = new javax.swing.JButton();
        allGrantsButton = new javax.swing.JButton();
        allAdminOptionButton = new javax.swing.JButton();
        noAllGrantsButton = new javax.swing.JButton();
        cancelWait = new JButton();
        progBar=new JProgressBar();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        rolesTable.setDefaultRenderer(Object.class,bctr);
        jScrollPane1.setViewportView(rolesTable);
        rolesTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });

       //create_roles_list();
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        jComboBox1.setToolTipText("List of roles");
        progBar.setMinimum(0);
        jCheckBox1.setText("Show system tables");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton1.setIcon(GUIUtilities.loadIcon("grant_all.png"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton1.setToolTipText("GRANT ALL TO ALL");

        jButton2.setIcon(GUIUtilities.loadIcon("admin_option_all.png"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jButton2.setToolTipText("GRANT ALL TO ALL WITH GRANT_OPTION");

        jButton3.setIcon(GUIUtilities.loadIcon("no_grant_all.png"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jButton3.setToolTipText("REVOKE ALL FROM ALL");

        objectBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All objects","Tables","Procedures","Views" }));
        objectBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                objectBoxActionPerformed(evt);
            }
        });
        objectBox.setToolTipText("Select type of objects");

        allUsersGrantButton.setIcon(GUIUtilities.loadIcon("grant_vertical.png"));
        allUsersGrantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allUsersGrantButtonActionPerformed(evt);
            }
        });
        allUsersGrantButton.setToolTipText("GRANT TO ALL OBJECTS");

        allUsersAdminOptionButton.setIcon(GUIUtilities.loadIcon("admin_option_vertical.png"));
        allUsersAdminOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allUsersAdminOptionButtonActionPerformed(evt);
            }
        });
        allAdminOptionButton.setToolTipText("GRANT TO ALL OBJECTS WITH GRANT OPTION");

        allRolesNoGrantButton.setIcon(GUIUtilities.loadIcon("no_grant_vertical.png"));
        allRolesNoGrantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allRolesNoGrantButtonActionPerformed(evt);
            }
        });
        allRolesNoGrantButton.setToolTipText("REVOKE FROM ALL OBJECTS");

        allGrantsButton.setIcon(GUIUtilities.loadIcon("grant_gorisont.png"));
        allGrantsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allGrantsButtonActionPerformed(evt);
            }
        });
        allGrantsButton.setToolTipText("ALL GRANTS TO OBJECT");

        allAdminOptionButton.setIcon(GUIUtilities.loadIcon("admin_option_gorisont.png"));
        allAdminOptionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allAdminOptionButtonActionPerformed(evt);
            }
        });
        allAdminOptionButton.setToolTipText("ALL GRANTS TO OBJECT WITH GRANT OPTION");

        noAllGrantsButton.setIcon(GUIUtilities.loadIcon("no_grant_gorisont.png"));
        noAllGrantsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noAllGrantsButtonActionPerformed(evt);
            }
        });
        noAllGrantsButton.setToolTipText("REVOKE ALL FROM OBJECT");

        cancelWait.setText("Cancel waiting fill");
        cancelWait.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelWaitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jCheckBox1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(progBar))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(objectBox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allGrantsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allAdminOptionButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(noAllGrantsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allUsersGrantButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allUsersAdminOptionButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allRolesNoGrantButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelWait)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(objectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(allGrantsButton)
                                        .addComponent(allAdminOptionButton)
                                        .addComponent(noAllGrantsButton)
                                        .addComponent(jButton1)
                                        .addComponent(jButton2)
                                        .addComponent(jButton3)
                                        .addComponent(allUsersGrantButton)
                                        .addComponent(allUsersAdminOptionButton)
                                        .addComponent(allRolesNoGrantButton)
                                        .addComponent(cancelWait))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jCheckBox1)
                                        .addComponent(progBar))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
        );
//create_table();
    }
    Vector<String> roles;
    void create_roles_list()
    {
        try {
            //Statement st = con.createStatement();
            String query="SELECT RDB$ROLE_NAME FROM RDB$ROLES";
            ResultSet result = querySender.execute(query,true).getResultSet();
            roles=new Vector<>();
            while (result.next())
            {
                String role=result.getString(1);
                roles.add(role);


            }
            querySender.releaseResources();
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(roles));
            jComboBox1.setSelectedIndex(0);

        }
        catch(Exception e)
        {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }
    void create_roles_list(String selectedRole)
    {
        enableGrant=false;
        try {
            //Statement st = con.createStatement();
            String query="SELECT RDB$ROLE_NAME FROM RDB$ROLES";
            ResultSet result = querySender.execute(query,true).getResultSet();
            roles=new Vector<>();
            while (result.next())
            {
                String role=result.getString(1);
                roles.add(role);


            }
            querySender.releaseResources();
            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(roles));
            jComboBox1.setSelectedItem(selectedRole);

        }
        catch(Exception e)
        {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        enableGrant=true;
    }
    String grants = "SUDIXR";
    String [] headers={"Object", "Select", "Update", "Delete","Insert","Execute","References"};
    Vector<String> relName;
    Vector<String> relType;
    Icon gr,no,adm;
    void create_table()
    {
        setEnableGrant(false);
        relName=new Vector<>();
        relType=new Vector<>();
        rolesTable.setModel(new RoleTableModel


                (headers,0));
        DefaultTableCellRenderer renderer=new DefaultTableCellRenderer();

        try {
            if(objectBox.getSelectedIndex()==0 || objectBox.getSelectedIndex()==1 )
            {
                //state = con.createStatement();
                String query="Select RDB$RELATION_NAME from RDB$RELATIONS WHERE RDB$RELATION_TYPE != 1";
                rs = querySender.execute(query,true).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (jCheckBox1.isSelected()) {
                        relName.add(name);
                        relType.add(objectBox.getItemAt(1));
                    } else {
                        if (!name.contains("$")) {
                            relName.add(name);
                            relType.add(objectBox.getItemAt(1));
                        }
                    }


                }
                querySender.releaseResources();
            }
            if(objectBox.getSelectedIndex()==0 || objectBox.getSelectedIndex()==3 )
            {
                //state = con.createStatement();
                String query="Select DISTINCT RDB$VIEW_NAME from RDB$VIEW_RELATIONS";
                rs = querySender.execute(query,true).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (jCheckBox1.isSelected()) {
                        relName.add(name);
                        relType.add(objectBox.getItemAt(3));
                    } else {
                        if (!name.contains("$")) {
                            relName.add(name);
                            relType.add(objectBox.getItemAt(3));
                        }
                    }


                }
                querySender.releaseResources();
            }
            if(objectBox.getSelectedIndex()==0 || objectBox.getSelectedIndex()==2 )
            {
                //state = con.createStatement();
                String query="Select RDB$PROCEDURE_NAME from RDB$PROCEDURES";
                rs = querySender.execute(query,true).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (jCheckBox1.isSelected()) {
                        relName.add(name);
                        relType.add(objectBox.getItemAt(2));
                    } else {
                        if (!name.contains("$")) {
                            relName.add(name);
                            relType.add(objectBox.getItemAt(2));
                        }
                    }


                }
                querySender.releaseResources();
            }
            progBar.setMaximum(relName.size());
            for(int i=0;i<relName.size()&&!enableGrant;i++)
            {
                progBar.setValue(i);
                try {


                    //Statement st = con.createStatement();
                    String s = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION from RDB$USER_PRIVILEGES\n" +
                            "where (rdb$grant_option is not null) and (rdb$Relation_name='"+relName.elementAt(i)+"') and (rdb$user='"+jComboBox1.getSelectedItem()+"')";
                    ResultSet rs1=querySender.execute(s,true).getResultSet();
                    Vector<Object> roleData= new Vector<Object>();

                    roleData.add(relName.elementAt(i));
                    for(int k=0;k<6;k++)
                      roleData.add(no) ;
                    ((RoleTableModel) rolesTable.getModel()).addRow(roleData);
                    //rolesTable.setDefaultRenderer(ImageIcon.class,renderer);

                    while (rs1.next())
                    {
                        String grant=rs1.getString(1);
                        grant=grant.substring(0,1);
                        int ind = grants.indexOf(grant);
                        if (rs1.getObject(2).equals(0)) {


                            rolesTable.setValueAt(gr, i, ind + 1);
                        }
                        else
                            ((RoleTableModel) rolesTable.getModel()).setValueAt(adm,i,ind+1);

                    }
                    querySender.releaseResources();
                    //((RoleTableModel) rolesTable.getModel()).addRow(roleData);
                    //((RoleTableModel) rolesTable.getModel())


                }
                catch(Exception e)
                {GUIUtilities.displayErrorMessage(e.getMessage());}
            }
        }
        catch(Exception e)
        {

            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    setEnableGrant(true);
        progBar.setValue(0);
    }

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        act=Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }
    void grant_on_role(int grantt,int row,int col)
    {
        if(row<rolesTable.getRowCount())
        switch (grantt)
        {
            case 0:
                try {

                    //Statement st = con.createStatement();
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(2))) {
                        if (!headers[col].equals("Execute")) {
                            String query="REVOKE " + headers[col] + " ON \"" + relName.elementAt(row) + "\" FROM \"" + jComboBox1.getSelectedItem() + "\";";
                            querySender.execute(query,true);
                            rolesTable.setValueAt(no, row, col);
                            querySender.releaseResources();
                        }
                        } else if (headers[col].equals("Execute")) {
                        String query="REVOKE " + headers[col] + " ON PROCEDURE \"" + relName.elementAt(row) + "\" FROM \"" + jComboBox1.getSelectedItem() + "\";";
                        querySender.execute(query,true);
                        rolesTable.setValueAt(no, row, col);
                        querySender.releaseResources();
                    }

                } catch (Exception e) {
                    GUIUtilities.displayErrorMessage(e.getMessage());
                }
                break;
            case 1:
                if (((Icon) rolesTable.getValueAt(row, col)).equals(adm)) {
                    try {

                        //Statement st = con.createStatement();
                        if (!relType.elementAt(row).equals(objectBox.getItemAt(2))) {
                            if (!headers[col].equals("Execute")) {
                                String query="REVOKE " + headers[col] + " ON \"" + relName.elementAt(row) + "\" FROM \"" + jComboBox1.getSelectedItem() + "\";";
                                querySender.execute(query,true);
                                rolesTable.setValueAt(no, row, col);
                                querySender.releaseResources();
                            }
                        } else if (headers[col].equals("Execute")) {
                            String query="REVOKE " + headers[col] + " ON PROCEDURE \"" + relName.elementAt(row) + "\" FROM \"" + jComboBox1.getSelectedItem() + "\";";
                            querySender.execute(query,true);
                            rolesTable.setValueAt(no, row, col);
                            querySender.releaseResources();
                        }

                    } catch (Exception e) {
                        GUIUtilities.displayErrorMessage(e.getMessage());
                    }

                    try {

                        //Statement st = con.createStatement();
                        if (!relType.elementAt(row).equals(objectBox.getItemAt(2))) {
                            if (!headers[col].equals("Execute")) {
                                String query="GRANT " + headers[col] + " ON \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\";";
                                querySender.execute(query,true);
                                querySender.releaseResources();
                                rolesTable.setValueAt(gr, row, col);
                            }
                        } else if (headers[col].equals("Execute")) {
                            String query="GRANT " + headers[col] + " ON PROCEDURE \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\";";
                            querySender.execute(query,true);
                            querySender.releaseResources();
                            rolesTable.setValueAt(gr, row, col);
                        }

                    } catch (Exception e) {
                        GUIUtilities.displayErrorMessage(e.getMessage());
                    }

                }else
                    try {

                        //Statement st = con.createStatement();
                        if (!relType.elementAt(row).equals(objectBox.getItemAt(2))) {
                            if (!headers[col].equals("Execute")) {
                                String query="GRANT " + headers[col] + " ON \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\";";
                                querySender.execute(query,true);
                                querySender.releaseResources();
                                rolesTable.setValueAt(gr, row, col);
                            }
                        } else if (headers[col].equals("Execute")) {
                            String query = "GRANT " + headers[col] + " ON PROCEDURE \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\";";
                            querySender.execute(query, true);
                            querySender.releaseResources();
                            rolesTable.setValueAt(gr, row, col);
                        }
                    } catch (Exception e) {
                        GUIUtilities.displayErrorMessage(e.getMessage());
                    }
                break;
            case 2:try {

                //Statement st = con.createStatement();
                if (!relType.elementAt(row).equals(objectBox.getItemAt(2))) {
                    if (!headers[col].equals("Execute")) {
                        String query="GRANT " + headers[col] + " ON \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\" WITH GRANT OPTION;";
                        querySender.execute(query, true);
                        querySender.releaseResources();
                        rolesTable.setValueAt(adm, row, col);
                    }
                    } else if (headers[col].equals("Execute")) {
                    String query="GRANT " + headers[col] + " ON PROCEDURE \"" + relName.elementAt(row) + "\" TO \"" + jComboBox1.getSelectedItem() + "\" WITH GRANT OPTION;";
                    querySender.execute(query, true);
                    querySender.releaseResources();
                    rolesTable.setValueAt(adm, row, col);
                }

            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            }
            break;
        }
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (enableGrant) {
            act=Action.ALL_GRANTS_TO_ALL_OBJECTS;
            setEnableGrant(false);
            Runnable r= new ThreadOfRole(this);
            Thread t = new Thread(r);
            t.start();
        }
       else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
       //create_table();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:

            act=Action.ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
            setEnableGrant(false);
            Runnable r= new ThreadOfRole(this);
            Thread t = new Thread(r);
            t.start();



        //create_table();
    }

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.NO_ALL_GRANTS_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();

        //create_table();
    }
    private void allGrantsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (enableGrant)
        {
        int row=rolesTable.getSelectedRow();
        if(row>=0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(1, row, col);
            }
            //create_table();
        }}
        else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void allAdminOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (enableGrant)
        {
        int row=rolesTable.getSelectedRow();
        if(row>=0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(2, row, col);
            }
            //create_table();
        }}
        else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void noAllGrantsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if(enableGrant) {
            int row = rolesTable.getSelectedRow();
            if (row >= 0) {
                for (int col = 1; col < headers.length; col++) {
                    grant_on_role(0, row, col);
                }
                //create_table();
            }
        }else {
            GUIUtilities.displayInformationMessage("Please wait");
        }

    }

    private void allUsersGrantButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.GRANT_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();


    }

    private void allUsersAdminOptionButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();

        //create_table();
    }

    private void allRolesNoGrantButtonActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.NO_GRANT_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();


        //create_table();
    }
    private void objectBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        act=Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r= new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }
    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
        // TODO add your handling code here:
        if (enableGrant)
        if (evt.getClickCount()>1) {
            int row = rolesTable.getSelectedRow();
            int col = rolesTable.getSelectedColumn();
            if (col > 0) {
                if (((Icon) rolesTable.getValueAt(row, col)).equals(gr)) {


                        grant_on_role(2,row,col);


                } else if (((Icon) rolesTable.getValueAt(row, col)).equals(adm)) {

                        grant_on_role(0,row,col);

                } else {

                        grant_on_role(1,row,col);

                }
                //create_table();
            }
        }
    }
    private javax.swing.JButton noAllGrantsButton;
    private javax.swing.JComboBox<String> objectBox;
    private javax.swing.JButton allAdminOptionButton;
    private javax.swing.JButton allGrantsButton;
    private javax.swing.JButton allRolesNoGrantButton;
    private javax.swing.JButton allUsersAdminOptionButton;
    private javax.swing.JButton allUsersGrantButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JTable rolesTable;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JScrollPane jScrollPane1;
    JButton cancelWait;

    public JProgressBar progBar;

    void setEnableGrant(boolean enable)
    {
        enableGrant=enable;
        jComboBox1.setEnabled(enable);
        objectBox.setEnabled(enable);
        jCheckBox1.setEnabled(enable);
        jButton1.setEnabled(enable);
        jButton2.setEnabled(enable);
        jButton3.setEnabled(enable);
        noAllGrantsButton.setEnabled(enable);
        allAdminOptionButton.setEnabled(enable);
        allGrantsButton.setEnabled(enable);
        allRolesNoGrantButton.setEnabled(enable);
        allUsersAdminOptionButton.setEnabled(enable);
        allUsersGrantButton.setEnabled(enable);
        cancelWait.setVisible(!enable);
        progBar.setVisible(!enable);
        if (enable)
            progBar.setValue(0);
    }
    public void run(){
        int col;
        switch (act)
        {
            case CREATE_TABLE:create_table();
            break;
            case ALL_GRANTS_TO_ALL_OBJECTS:
                {
                    progBar.setMaximum(relName.size());
                for (int row=0; row < relName.size()&&!enableGrant; row++) {
                    progBar.setValue(row);
                    for ( col = 1; col < headers.length; col++)
                        grant_on_role(1, row, col);
                }
                progBar.setValue(0);
                setEnableGrant(true);
                }
                break;
            case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION: progBar.setMaximum(relName.size());
                for (int row = 0; row < relName.size()&&!enableGrant; row++) {
                    progBar.setValue(row);
                    for ( col = 1; col < headers.length; col++)
                        grant_on_role(2, row, col);
                }progBar.setValue(0);
                setEnableGrant(true);
                break;
            case NO_ALL_GRANTS_TO_ALL_OBJECTS:progBar.setMaximum(relName.size());
                for (int row = 0; row < relName.size()&&!enableGrant; row++) {
                    progBar.setValue(row);
                    for ( col = 1; col < headers.length; col++)
                        grant_on_role(0, row, col);
                }progBar.setValue(0);
                setEnableGrant(true);
                break;
            case GRANT_TO_ALL_OBJECTS: col = rolesTable.getSelectedColumn();
                progBar.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size()&&!enableGrant; row++) {
                        progBar.setValue(row);
                        grant_on_role(1, row, col);
                    }
                progBar.setValue(0);
                setEnableGrant(true);
                break;
            case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                col = rolesTable.getSelectedColumn();
                progBar.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size()&&!enableGrant; row++) {
                        progBar.setValue(row);
                        grant_on_role(2, row, col);
                    }
                progBar.setValue(0);
                setEnableGrant(true);
                break;
            case NO_GRANT_TO_ALL_OBJECTS:
                col = rolesTable.getSelectedColumn();
                progBar.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size()&&!enableGrant; row++) {
                        progBar.setValue(row);
                        grant_on_role(0, row, col);
                    }
                progBar.setValue(0);
                setEnableGrant(true);
                break;
        }
        }
    void cancelWaitActionPerformed(java.awt.event.ActionEvent evt)
    {
        setEnableGrant(true);
    }
    @Override
    public void cleanup() {

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    @Override
    public String getLayoutName() {
        return null;
    }
}

