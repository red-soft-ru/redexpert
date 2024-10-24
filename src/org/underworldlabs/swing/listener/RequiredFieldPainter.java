package org.underworldlabs.swing.listener;

import org.underworldlabs.swing.ViewablePasswordField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.Serializable;

/**
 * JComponent required painting class.
 *
 * @author Alexey Kozlov
 */
public class RequiredFieldPainter implements Serializable {
    private static final long serialVersionUID = -6536280488360814432L;

    private static final Color REQUIRED_COLOR = new Color(205, 61, 60); // #CD3D3C

    private boolean enable;
    private transient DocumentPainter painter;
    private final transient Border defaultBorder;
    private final transient Border reqiuredBorder;

    private RequiredFieldPainter(JComponent component) {
        this.enable = true;
        this.defaultBorder = component.getBorder();
        this.reqiuredBorder = BorderFactory.createLineBorder(REQUIRED_COLOR);

        if (component instanceof JTextField) {
            init((JTextField) component);
        } else if (component instanceof ViewablePasswordField) {
            init((ViewablePasswordField) component);
        }
    }

    private void init(JTextField textField) {
        painter = new DocumentPainter(textField);
    }

    private void init(ViewablePasswordField passwordField) {
        painter = new DocumentPainter(passwordField.getPasswordField(), passwordField);
    }

    // ---

    public static RequiredFieldPainter initialize(JComponent component) {
        return new RequiredFieldPainter(component);
    }

    public void enable() {
        this.enable = true;
        this.painter.paintComponent();
    }

    public void disable() {
        this.enable = false;
        this.painter.paintComponent();
    }

    public boolean check() {
        return !painter.required();
    }

    // ---

    private class DocumentPainter implements DocumentListener {
        private final JComponent component;
        private final JTextField textField;

        public DocumentPainter(JTextField textField) {
            this(textField, textField);
        }

        public DocumentPainter(JTextField textField, JComponent component) {
            this.component = component;
            this.textField = textField;

            textField.getDocument().addDocumentListener(this);
            paintComponent();
        }

        private void paintComponent() {
            component.setBorder(required() ? reqiuredBorder : defaultBorder);
        }

        public boolean required() {
            return enable && MiscUtils.isNull(textField.getText());
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
