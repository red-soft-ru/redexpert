package org.underworldlabs.swing.hexeditor;
import  javax.swing.*;
import  java.awt.event.*;

public class BinaryOperationsToolBar extends JToolBar {

  private JButton newButton;      
  private JButton openButton;
  private JButton saveButton;

  public BinaryOperationsToolBar() {
    super("Binary Operations Tool Bar");
    
    add(new JButton("<<"));
    add(new JButton(">>"));
    add(new JButton("&"));
    add(new JButton("|"));
    add(new JButton("!"));

    setFloatable(true);
    setRollover(true);
    setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createEtchedBorder(), getBorder() ));
  }
}

