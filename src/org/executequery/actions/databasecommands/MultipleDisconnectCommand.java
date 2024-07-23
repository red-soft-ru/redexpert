package org.executequery.actions.databasecommands;

import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;
import java.util.List;

public class MultipleDisconnectCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseConnectionRepository) {
            List<DatabaseConnection> connections = ((DatabaseConnectionRepository) repo).findAll();
            connections.forEach(connection -> ConnectionMediator.getInstance().disconnect(connection));
        }
    }
}
