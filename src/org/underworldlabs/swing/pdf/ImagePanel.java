package org.underworldlabs.swing.pdf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ImagePanel extends JPanel {

    private static final long serialVersionUID = -8483797305070521030L;

    private final Image image;

    public ImagePanel(Image image) {
        this.image = image;

        setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(image, 0, 0, null);
    }

}
