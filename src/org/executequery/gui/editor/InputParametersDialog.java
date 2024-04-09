package org.executequery.gui.editor;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimezonePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimePicker;
import org.underworldlabs.swing.celleditor.picker.DefaultTimezonePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class InputParametersDialog extends BaseDialog {

    private final List<Parameter> parameters;
    private JScrollPane scrollPanel;
    private JPanel mainPanel;
    private JPanel panel;
    private List<JComponent> componentList;
    private boolean canceled = false;

    public InputParametersDialog(List<Parameter> parameters) {
        super(Bundles.getCommon("input-parameters"), true, true);
        this.parameters = parameters;
        init();
    }

    GridBagHelper gbh;

    private void init() {
        mainPanel = new JPanel();
        scrollPanel = new JScrollPane();
        panel = new JPanel();
        scrollPanel.setViewportView(panel);
        panel.setLayout(new GridBagLayout());
        componentList = new ArrayList<>();
        mainPanel.setLayout(new GridBagLayout());
        gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        mainPanel.add(scrollPanel, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));
        // empty panel for stretch
        mainPanel.add(new JPanel(), new GridBagConstraints(0, 1, 2, 1,
                1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(this.isDialog());
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        bottomButtonPanel.setOkButtonText("OK");
        bottomButtonPanel.setCancelButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canceled = true;
                dispose();

            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;
                super.windowClosing(e);
            }
        });
        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                canceled = true;
                dispose();
            }
        };

        mainPanel.registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        bottomButtonPanel.setCancelButtonText(Bundles.getCommon("cancel.button"));
        mainPanel.add(bottomButtonPanel, new GridBagConstraints(0, 2,
                1, 1, 1, 0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        for (int i = 0; i < parameters.size(); i++)
            addParameter(parameters.get(i));
        addDisplayComponent(mainPanel);
        bottomButtonPanel.setHelpButtonVisible(false);
    }

    private void setValueToComponent(Parameter parameter, JComponent component) {
        if (parameter.getValue() != null)
            switch (parameter.getType()) {
                case Types.DATE:
                    ((DatePicker) component).setDate((LocalDate) parameter.getValue());
                    break;
                case Types.TIMESTAMP:
                    ((DefaultDateTimePicker) component).setDateTime((LocalDateTime) parameter.getValue());
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    ((DefaultDateTimezonePicker) component).setDateTime((OffsetDateTime) parameter.getValue());
                    break;
                case Types.TIME:
                    ((DefaultTimePicker) component).setTime((LocalTime) parameter.getValue());
                    break;
                case Types.TIME_WITH_TIMEZONE:
                    ((DefaultTimezonePicker) component).setTime((OffsetTime) parameter.getValue());
                    break;
                case Types.BOOLEAN:
                    ((RDBCheckBox) component).setStingValue((String) parameter.getValue());
                    break;
                case Types.BINARY:
                case Types.BLOB:
                case Types.LONGVARBINARY:
                case Types.LONGVARCHAR:
                    ((RDBFieldFileChooser) component).setFile(((File) parameter.getValue()));
                    break;
                default:
                    ((ValueOrNullParameterField) component).setValue((String) parameter.getValue());
                    break;
            }
    }

    private void addParameter(Parameter parameter) {

        panel.add(new JLabel(parameter.getName()), gbh.nextRowFirstCol().setLabelDefault().get());
        panel.add(new JLabel(parameter.getTypeName()), gbh.nextCol().get());

        JComponent component;
        switch (parameter.getType()) {
            case Types.DATE:
                component = new DatePicker();
                break;
            case Types.TIMESTAMP:
                component = new DefaultDateTimePicker();
                break;
            case Types.TIMESTAMP_WITH_TIMEZONE:
                component = new DefaultDateTimezonePicker();
                break;
            case Types.TIME:
                component = new DefaultTimePicker();
                break;
            case Types.TIME_WITH_TIMEZONE:
                component = new DefaultTimezonePicker();
                break;
            case Types.BOOLEAN:
                component = new RDBCheckBox();
                break;
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.LONGVARCHAR:
                component = new RDBFieldFileChooser();
                break;
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.INT128:
                component = new ValueOrNullParameterField(new NumberTextField());
                break;
            default:
                component = new ValueOrNullParameterField(new JTextField(14));
                break;
        }

        setValueToComponent(parameter, component);
        componentList.add(component);
        panel.add(component, gbh.nextCol().fillHorizontally().spanX().get());
    }

    private void ok() {
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            JComponent component = componentList.get(i);
            switch (parameter.getType()) {
                case Types.DATE:
                    parameter.setValue(((DatePicker) component).getDate());
                    break;
                case Types.TIMESTAMP:
                    parameter.setValue(((DefaultDateTimePicker) component).getDateTime());
                    break;
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    parameter.setValue(((DefaultDateTimezonePicker) component).getOffsetDateTime());
                    break;
                case Types.TIME:
                    parameter.setValue(((DefaultTimePicker) component).getLocalTime());
                case Types.TIME_WITH_TIMEZONE:
                    parameter.setValue(((DefaultTimezonePicker) component).getOffsetTime());
                    break;
                case Types.BOOLEAN:
                    parameter.setValue(((RDBCheckBox) component).getStringValue());
                    break;
                case Types.BINARY:
                case Types.BLOB:
                case Types.LONGVARBINARY:
                case Types.LONGVARCHAR:
                    File file = ((RDBFieldFileChooser) component).getFile();
                    parameter.setValue(file);
                    break;
                default:
                    parameter.setValue(((ValueOrNullParameterField) component).getValue());
                    break;
            }
        }
        finished();
    }

    public boolean isCanceled() {
        return canceled;
    }
}
