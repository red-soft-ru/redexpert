package org.executequery.gui.connections;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.repository.ConnectionImport;
import org.executequery.repository.ConnectionImporter;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;
import org.underworldlabs.swing.wizard.WizardProcessPanel;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ImportConnectionsPanel extends WizardProcessPanel implements ActiveComponent, ImportProcessMonitor {

    public static final String TITLE = "Import Connections";
    public static final String FRAME_ICON = "ImportConnections16.png";

    private static final String[] STEPS = {
            "Select the import file",
            "Import from file"
    };

    private static final String[] TITLES = {
            "Import file",
            "Import"
    };

    private ImportConnectionsPanelOne firstPanel;

    private ImportConnectionsPanelTwo secondPanel;

    private ImportConnectionsModel model;
    
    private ActionContainer parent;

    public ImportConnectionsPanel(ActionContainer parent) {
        
        this.parent = parent;

        firstPanel = new ImportConnectionsPanelOne();
        secondPanel = new ImportConnectionsPanelTwo();
        
        List<JPanel> panels = new ArrayList<JPanel>();
        panels.add(firstPanel);
        panels.add(secondPanel);
        
        model = new ImportConnectionsModel(panels);
        setModel(model);
        
        prepare();
    }
    
    private boolean doNext() {
        
        int index = model.getSelectedIndex();        
        if (index == 0) {

            if (!firstPanel.canProceed()) {
                
                return false;
            }
            
            secondPanel.start();
            doImport();
        }
        return true;
    }
    
    @Override
    public void cleanup() {}

    @Override
    public void cancel() {
        
        parent.finished();
    }

    private void doImport() {
        
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    
                    System.out.println("Importing connections from file...");
                    
                    setButtonsEnabled(false);

                    parent.block();
                    boolean result = save();

                    if (result) {

                        setButtonsEnabled(true);
                        setNextButtonEnabled(false);
                        setBackButtonEnabled(true);
                        setCancelButtonEnabled(true);
                        setCancelButtonText("Finish");
                        
                        secondPanel.stop();
                        
                        secondPanel.append("\nDone.");
                        secondPanel.append("The import file was processed successfully");
                        
                        System.out.println("Finished importing connections from file.");
                    }
                        
                } finally {

                    parent.unblock();
                }
            }
        });

    }
    
    @Override
    public void progress(String message) {
        
        secondPanel.append(message);
    }
    
    private boolean save() {
        
        try {

            String importFile = firstPanel.getImportPath();
        
            ConnectionImport connectionImport = new ConnectionImporter().read(importFile, this);

            EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                            this, ConnectionRepositoryEvent.CONNECTION_ADDED, connectionImport.getConnections()));

            secondPanel.append("\nProcessed " + connectionImport.getFolderCount() +" folders and " 
                    + connectionImport.getConnectionCount() + " connections");
            
            return true;
            
        } catch (RepositoryException e) {
            
            GUIUtilities.displayExceptionErrorDialog(
                    "Error reading connections from file.\n\nThe system returned:\n" + e.getMessage(), e);
            return false;
        }

    }    

    
    class ImportConnectionsModel extends DefaultWizardProcessModel {

        ImportConnectionsModel(List<JPanel> panels) {

            super(panels, STEPS, TITLES);
        }

        @Override
        public boolean next() {

            if (doNext()) {
                
                return super.next();
            }
            return false;
        }
        
    }
    
}
