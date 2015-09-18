/*
 * SQLHistoryDialog.java
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

package org.executequery.gui.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultList;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.SqlCommandHistoryRepository;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.util.MiscUtils;

/** 
 * The History Dialog displays the executed SQL statement history
 * from within the Query Editor. The data represented as a
 * <code>Vector</code> object, is displayed within a <code>JLIst</code>.
 * Selection of a stored statement can be achieved by double-clicking the
 * statement, selecting and pressing the ENTER key or by selecting
 * and clicking the SELECT button.<br>
 * The selected statement is displayed within the Query Editor that
 * initiated the frame.<br>
 * Selecting the CANCEL button closes the dialog.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SQLHistoryDialog extends AbstractBaseDialog
                              implements ActionListener, 
                                         ListSelectionListener {
    
    private JList historyList;

    private Vector<String> data;

    private QueryEditor queryEditor;

    private JTextField searchField; 
    
    private JCheckBox newEditorCheck;
    
    private SQLTextPane textPane;
    
    /** 
     * Creates a new object with history data 
     * to be set within the specified editor.
     *
     * @param - the statement history <code>Vector</code>
     * @param - the editor
     */
    public SQLHistoryDialog(Vector<String> data, QueryEditor queryEditor) {
       
        super(GUIUtilities.getParentFrame(), "SQL Command History", true);
        
        try {
        
            this.data = data;
            this.queryEditor = queryEditor;
            
            initHistoryList(data);

            init();
            pack();

            setLocation(GUIUtilities.getLocationForDialog(getSize()));
            setVisible(true);

        } catch(Exception e) {
          
            e.printStackTrace();
        }
        
    }
    
    /** <p>Initialises the state of this instance
     * and positions all components.
     *
     * @throws Exception
     */
    private void init() throws Exception {

        JButton cancelButton = createButton("Cancel", null);       
        JButton selectButton = createButton("Select", 
                "Pastes the selected queries into the Query Editor");
        JButton copyButton = createButton("Copy", 
                "Copies the selected queries to the system clipboard");
        JButton insertAtCursorButton = createButton("Insert at Cursor", 
                "Inserts the selected queries at the cursor position within the Query Editor");
        JButton clearButton = createButton("Clear", 
                "Clears and resets ALL SQL history");

        newEditorCheck = new JCheckBox("Open in new Query Editor");
        newEditorCheck.setToolTipText(
                "Select to paste the query in a new Query Editor panel");
        
        textPane = new SQLTextPane();
        textPane.setEditable(false);
        
        JSplitPane splitPane = createSplitPane();
        splitPane.setLeftComponent(new JScrollPane(historyList));
        splitPane.setRightComponent(new JScrollPane(textPane));
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        
        searchField = WidgetFactory.createTextField();
        searchField.addActionListener(this);
        JButton searchButton = createButton("Search", null);

        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.insets.right = 5;
        searchPanel.add(new JLabel("Find:"), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchField, gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.insets.right = 0;
        searchPanel.add(searchButton, gbc);
        
        // layout the components
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.bottom = 5;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        c.add(searchPanel, gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(splitPane, gbc);
        gbc.weightx = 0;
        gbc.insets.left = 5;
        gbc.insets.bottom = 7;
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        c.add(cancelButton, gbc);
        gbc.gridx--;
        gbc.insets.right = 0;
        c.add(clearButton, gbc);
        gbc.gridx--;
        gbc.insets.right = 0;
        c.add(copyButton, gbc);
        gbc.gridx--;
        c.add(insertAtCursorButton, gbc);
        gbc.gridx--;
        gbc.weightx = 1.0;
        c.add(selectButton, gbc);
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        c.add(newEditorCheck, gbc);
        
        historyList.addListSelectionListener(this);

        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                historyListMouseClicked(e); }
        });

        historyList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                historyListKeyPressed(e); }
        });
    
        c.setPreferredSize(new Dimension(800, 490));
    }
    
    private void initHistoryList(Vector<String> data) {

        historyList = new DefaultList(data);
    }

    private JSplitPane createSplitPane() {
        
        JSplitPane splitPane = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.7);

        return splitPane;
    }

    private JButton createButton(String text, String toolTip) {
        
        JButton button = new DefaultPanelButton(text);       
        if (toolTip != null) {

            button.setToolTipText(toolTip);
        }
        
        button.addActionListener(this);
        
        return button;
    }
    
    /** 
     * Sets the statement history data to the <code>JList</code>.
     *
     * @param - the statement history <code>Vector</code>
     */
    public void setHistoryData(Vector<String> data) {
        this.data = data;
        historyList.setListData(data);
    }
    
    /** <p>Initiates the action of the "Select" button adding
     * the selected statement to the open Query Editor.
     *
     * @param - the action event
     */
    public void actionPerformed(ActionEvent e) {

        String command = e.getActionCommand();

        if (command.equals("Select")) {
            
            selectSQLCommand();
            
        } else if (command.equals("Copy")) {
            
            copySQLCommand();
                
        } else if (command.equals("Insert at Cursor")) {
            
            insertAtCursorButton();
            
        } else if (command.equals("Search") || e.getSource() == searchField) {

            String text = searchField.getText();
            
            if (MiscUtils.isNull(text)) {

                return;
            }

            int start = historyList.getSelectedIndex();
            if (start == -1 || start == data.size() - 1) {
                
                start = 0;
                
            } else {
              
                start++;
            }

            search(text, start);

        } else if (command.equals("Clear")) {

            sqlCommandHistoryRepository().clearSqlCommandHistory();
            setHistoryData(new Vector<String>(0));

        } else {

            dispose();
        }

    }
    
    private SqlCommandHistoryRepository sqlCommandHistoryRepository() {

        return (SqlCommandHistoryRepository)RepositoryCache.load(
                SqlCommandHistoryRepository.REPOSITORY_ID);        
    }

    private void search(String text, int start) {
        Pattern pattern = Pattern.compile("\\b" + text, 
                                          Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(Constants.EMPTY);

        for (int i = start, k = data.size(); i < k; i++) {

            matcher.reset(data.get(i));
            
            if (matcher.find()) {
                historyList.setSelectedIndex(i);
                scrollToSelection(i);
                return;
            }
        }

        GUIUtilities.displayInformationMessage("Search string not found");
    }
    
    private void scrollToSelection(int i) {

        historyList.ensureIndexIsVisible(i);
    }

    private boolean validSelection() {
        
        if (data.isEmpty()) {
            
            return false;
        }

        if (historyList.isSelectionEmpty()) {

            GUIUtilities.displayErrorMessage("No selection made.");
            return false;
        }

        return true;
    }
    
    private void copySQLCommand() {

        if (validSelection()) {
        
            String query = queryForIndices(historyList.getSelectedIndices());
            GUIUtilities.copyToClipBoard(query);
         
            dispose();
        }
        
    }
    
    private void insertAtCursorButton() {
        
        if (newEditorCheck.isSelected()) {
            
            selectSQLCommand();
        
        } else if (queryEditor != null) {
            
            String query = queryForIndices(historyList.getSelectedIndices());
            queryEditor.insertTextAtCaret(query);
            dispose();
        }
    }
   
    
    private void selectSQLCommand() {

        if (validSelection()) {
        
            String query = queryForIndices(historyList.getSelectedIndices());
            if (newEditorCheck.isSelected()) {

                QueryEditor editor = new QueryEditor(query);
                GUIUtilities.addCentralPane(QueryEditor.TITLE,
                                            QueryEditor.FRAME_ICON, 
                                            editor,
                                            null,
                                            true);

            } else if (queryEditor != null) {
              
                queryEditor.setEditorText(query);
            }

            dispose();
        }
    }

    private String queryForIndices(int[] indices) {

        if (indices.length > 0) {
        
            StringBuilder sb = new StringBuilder();
            for (int index :indices) {
                
                sb.append(queryForIndex(index).trim());
                sb.append("\n\n");
            }

            return sb.toString().trim();
        }

        return "";
    }

    private String queryForIndex(int index) {
        
        if (index != -1) {
            
            return data.get(index);
        }
        return "";
    }
    
    /** <p>Initiates the action on the history list after
     * double clicking a selected statement and propagates
     * the action to the method <code>selectButton_actionPerformed</code>.
     *
     * @param - the mouse event
     */
    private void historyListMouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
            
            selectSQLCommand();
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {

        textPane.setText(queryForIndex(historyList.getSelectedIndex()));
    }
    
    /** <p>Initiates the action on the history list after
     * pressing the ENTER key on a selected statement and propagates
     * the action to the method <code>selectButton_actionPerformed</code>.
     *
     * @param - the key event
     */
    private void historyListKeyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            selectSQLCommand();
        }
    }
    
}







