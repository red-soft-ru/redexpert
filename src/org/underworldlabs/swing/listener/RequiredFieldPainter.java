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

    private final Border defaultBorder;
    private final Border reqiuredBorder;

    public static void initialize(JComponent component) {
        new RequiredFieldPainter(component);
    }

    private RequiredFieldPainter(JComponent component) {
        this.defaultBorder = component.getBorder();
        this.reqiuredBorder = BorderFactory.createLineBorder(REQUIRED_COLOR);

        if (component instanceof JTextField) {
            init((JTextField) component);
        }
    }

    private void init(JTextField textField) {
        textField.getDocument().addDocumentListener(new DocumentPainter(textField));
    }

    // ---

    private class DocumentPainter implements DocumentListener {
        private final JTextField textField;

        public DocumentPainter(JTextField textField) {
            this.textField = textField;
        }

        private void paintComponent() {
            textField.setBorder(MiscUtils.isNull(textField.getText()) ? reqiuredBorder : defaultBorder);
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
