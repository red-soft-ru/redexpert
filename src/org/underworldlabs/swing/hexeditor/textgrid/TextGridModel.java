package org.underworldlabs.swing.hexeditor.textgrid;

import java.awt.*;

public interface TextGridModel {
    public int getColumnCount();

    public int getRowCount();

    public char getCharAt(int row, int col);

    public Color getCharColor(int row, int col);

    public Color getCharBackground(int row, int col);

    public int getCharStyle(int row, int col);

    public void addTextGridModelListener(TextGridModelListener l);

    public void removeTextGridModelListener(TextGridModelListener l);
}
  
