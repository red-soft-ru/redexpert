package org.underworldlabs.swing.listener;

import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * JComponent required painting class.
 *
 * @author Alexey Kozlov
 */
public class RequiredFieldPainter {
    private static final Color REQUIRED_COLOR = new Color(205, 61, 60); // #CD3D3C

    private boolean enable;
    private final JComponent component;
    private final Border defaultBorder;
    private final Border reqiuredBorder;

    private RequiredFieldPainter(JComponent component) {
        this.enable = true;
        this.component = component;
        this.defaultBorder = component.getBorder();
        this.reqiuredBorder = BorderFactory.createLineBorder(REQUIRED_COLOR);

        if (component instanceof JTextField) {
            init((JTextField) component);
        }
    }

    private void init(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentPainter(textField));
    }

    public static RequiredFieldPainter initialize(JComponent component) {
        return new RequiredFieldPainter(component);
    }

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        this.enable = false;
        this.component.setBorder(defaultBorder);
    }

    // ---

    private class DocumentPainter implements DocumentListener {
        private final JTextField textField;

        public DocumentPainter(JTextField textField) {
            this.textField = textField;
        }

        private void paintComponent() {
            boolean required = enable && MiscUtils.isNull(textField.getText());
            textField.setBorder(required ? reqiuredBorder : defaultBorder);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            paintComponent();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            paintComponent();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            paintComponent();
        }

    } // DocumentPainter class

}
