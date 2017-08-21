package org.executequery.gui.connections;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.ProgressBar;
import org.underworldlabs.swing.ProgressBarFactory;

public class ImportConnectionsPanelTwo extends ActionPanel {
    
    private ProgressBar progressBar;

    private JTextArea textArea;
    
    public ImportConnectionsPanelTwo() {

        super(new GridBagLayout());
        try  {

            init();

        } catch (Exception e) {
          
            e.printStackTrace();
        }

    }
    
    private void init() throws Exception {

        textArea = new JTextArea();
        textArea.setMargin(new Insets(5,5,5,5));
        textArea.setEditable(false);

        progressBar = ProgressBarFactory.create(true);
        ((JComponent) progressBar).setPreferredSize(new Dimension(1, 22));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 7;
        gbc.insets.bottom = 15;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(new JLabel("Exporting selections..."), gbc);
        gbc.gridy++;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.weightx = 1.0;
        add((JComponent) progressBar, gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.top = 0;
        gbc.insets.right = 0;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(textArea), gbc);
        
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    public void append(final String text) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.append(text);
                textArea.append("\n");
            }
        });
        
    }

    public void stop() {

        progressBar.stop();
    }
 
    public void start() {

        progressBar.start();
        append("Importing ... ");
    }
    
}
