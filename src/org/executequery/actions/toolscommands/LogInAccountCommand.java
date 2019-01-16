package org.executequery.actions.toolscommands;

import org.executequery.actions.OpenFrameCommand;
import org.executequery.http.ReddatabaseAPI;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

public class LogInAccountCommand extends OpenFrameCommand implements BaseCommand {
    @Override
    public void execute(ActionEvent e) {
        ReddatabaseAPI.getToken();
    }
}
