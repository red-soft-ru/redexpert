package org.executequery.gui.browser.managment;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class FrameListener implements WindowListener {
    public FrameListener(JFrame frame)
    {
        fram=frame;
        fram.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    }
    JFrame fram;
    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        fram.dispose();

    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {


    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
