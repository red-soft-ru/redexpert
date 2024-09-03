package org.underworldlabs.swing.listener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JComponent on hover action painting class.
 *
 * @author Alexey Kozlov
 */
public final class PaintMouseHandler extends MouseAdapter {

    public enum Preset {
        COLORED_FOREGROUND,
        COLORED_BACKGROUND,
        COLORED_UNDERLINE
    }

    private static final Color FOREGROUND = UIManager.getColor("Tree.textForeground");
    private static final Color BACKGROUND = UIManager.getColor("Tree.textBackground");
    private static final Color FOREGROUND_SELECTED = UIManager.getColor("Tree.selectionForeground");
    private static final Color BACKGROUND_SELECTED = UIManager.getColor("Tree.selectionBackground");

    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder();
    private static final Border UNDERLINE_DEFAULT_BORDER = BorderFactory.createEmptyBorder(0, 0, 1, 0);
    private static final Border UNDERLINE_SELECTED_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, BACKGROUND_SELECTED);

    private Border hoverBorder;
    private Border defaultBorder;
    private Color hoverForeground;
    private Color hoverBackground;
    private Color defaultForeground;
    private Color defaultBackground;
    private final JComponent component;

    public PaintMouseHandler(JComponent component, Preset preset) {
        this.component = component;

        init(preset);
        paintDefault();
    }

    private void init(Preset preset) {
        switch (preset) {

            case COLORED_FOREGROUND: {
                hoverBorder = EMPTY_BORDER;
                defaultBorder = EMPTY_BORDER;
                hoverBackground = BACKGROUND;
                defaultBackground = BACKGROUND;
                defaultForeground = FOREGROUND;
                hoverForeground = BACKGROUND_SELECTED;
                break;
            }

            case COLORED_BACKGROUND: {
                hoverBorder = EMPTY_BORDER;
                defaultBorder = EMPTY_BORDER;
                defaultBackground = BACKGROUND;
                defaultForeground = FOREGROUND;
                hoverBackground = BACKGROUND_SELECTED;
                hoverForeground = FOREGROUND_SELECTED;
                break;
            }

            case COLORED_UNDERLINE: {
                hoverBackground = BACKGROUND;
                defaultBackground = BACKGROUND;
                hoverForeground = BACKGROUND_SELECTED;
                defaultForeground = BACKGROUND_SELECTED;
                hoverBorder = UNDERLINE_SELECTED_BORDER;
                defaultBorder = UNDERLINE_DEFAULT_BORDER;
                break;
            }
        }
    }

    private void paintDefault() {
        if (component != null) {
            component.setBorder(defaultBorder);
            component.setBackground(defaultBackground);
            component.setForeground(defaultForeground);
        }
    }

    private void paintHovered() {
        if (component != null) {
            component.setBorder(hoverBorder);
            component.setBackground(hoverBackground);
            component.setForeground(hoverForeground);
        }
    }

    // --- MouseAdapter impl ---

    @Override
    public void mouseEntered(MouseEvent e) {
        paintHovered();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        paintHovered();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        paintDefault();
    }

}
