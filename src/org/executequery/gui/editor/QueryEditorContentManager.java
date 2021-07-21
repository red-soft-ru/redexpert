package org.executequery.gui.editor;

import java.util.List;

import org.executequery.GUIUtilities;
import org.executequery.gui.SaveFunction;

public class QueryEditorContentManager {

    
    public void save() {
        
        StringBuilder sb = new StringBuilder();
        List<SaveFunction> panels = GUIUtilities.getOpenSaveFunctionPanels();
        for (SaveFunction saveFunction : panels) {
            
            if (QueryEditor.class.isAssignableFrom(saveFunction.getClass())) {
                
                QueryEditor queryEditor = (QueryEditor) saveFunction;
                
                String name = queryEditor.getDisplayName();
                String text = queryEditor.getEditorText();
                
                
                
                
            }
            
        }
        
    }
    
    
    
}
