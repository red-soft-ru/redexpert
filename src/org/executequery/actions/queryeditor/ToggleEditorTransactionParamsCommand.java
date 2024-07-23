package org.executequery.actions.queryeditor;

import org.executequery.EventMediator;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.util.UserProperties;

import java.awt.event.ActionEvent;

public class ToggleEditorTransactionParamsCommand extends AbstractQueryEditorCommand {

    public static final String PROPERTY_KEY = "editor.display.transaction.params";

    @Override
    public void execute(ActionEvent e) {
        boolean oldValue = UserProperties.getInstance().getBooleanProperty(PROPERTY_KEY);
        UserProperties.getInstance().setBooleanProperty(PROPERTY_KEY, !oldValue);
        EventMediator.fireEvent(new DefaultUserPreferenceEvent(this, PROPERTY_KEY, UserPreferenceEvent.QUERY_EDITOR));
    }
}
