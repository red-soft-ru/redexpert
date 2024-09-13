package org.executequery.gui.editor;

import com.github.lgooddatepicker.components.DatePicker;
import org.executequery.databaseobjects.Types;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.celleditor.picker.TimestampPicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimestampPicker;
import org.underworldlabs.swing.celleditor.picker.TimePicker;
import org.underworldlabs.swing.celleditor.picker.ZonedTimePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class InputParametersDialog extends BaseDialog {

    private JPanel panel;
    private GridBagHelper gbh;
    private JButton applyButton;
    private JButton cancelButton;

    private boolean canceled;
    private final List<Parameter> parameters;
    private final List<JComponent> componentList;

    public InputParametersDialog(List<Parameter> parameters) {
        super(Bundles.get("common.input-parameters"), true, true);
        this.componentList = new ArrayList<>();
        this.parameters = parameters;

        init();
        arrange();
        setCanceled(false);
    }

    private void init() {

        panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        applyButton = WidgetFactory.createButton(
                "applyButton",
                Bundles.get("common.ok.button"),
                e -> apply()
        );

        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> cancel()
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setCanceled(true);
                super.windowClosing(e);
            }
        });
    }

    private void arrange() {

        // --- button panel ---

        JPanel buttonPanel = WidgetFactory.createPanel("buttonPanel");

        gbh = new GridBagHelper().fillHorizontally();
        buttonPanel.add(applyButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");
        mainPanel.registerKeyboardAction(
                e -> cancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        mainPanel.add(new JScrollPane(panel), gbh.spanX().setMaxWeightY().get());
        mainPanel.add(buttonPanel, gbh.nextRow().setMinWeightY().fillHorizontally().topGap(0).get());

        // ---

        gbh = new GridBagHelper().setInsets(0, 5, 5, 5).anchorNorthWest().fillHorizontally();
        parameters.forEach(this::addParameter);
        addDisplayComponent(mainPanel);
    }

    private void addParameter(Parameter parameter) {

        JComponent component;
        switch (parameter.getType()) {
            case Types.DATE:
                component = new DatePicker();
                break;

            case Types.TIMESTAMP:
                component = new TimestampPicker();
                break;

            case Types.TIMESTAMP_WITH_TIMEZONE:
                component = new ZonedTimestampPicker();
                break;

            case Types.TIME:
                component = new TimePicker();
                break;

            case Types.TIME_WITH_TIMEZONE:
                component = new ZonedTimePicker();
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

        panel.add(new JLabel(parameter.getName()), gbh.nextRowFirstCol().setMinWeightX().leftGap(5).topGap(8).get());
        panel.add(new JLabel(parameter.getTypeName()), gbh.nextCol().leftGap(0).get());
        panel.add(component, gbh.nextCol().topGap(5).setMaxWeightX().get());
    }

    private void setValueToComponent(Parameter parameter, JComponent component) {

        if (parameter.getValue() == null)
            return;

        switch (parameter.getType()) {
            case Types.DATE:
                ((DatePicker) component).setDate((LocalDate) parameter.getValue());
                break;

            case Types.TIMESTAMP:
                ((TimestampPicker) component).setDateTime((LocalDateTime) parameter.getValue());
                break;

            case Types.TIMESTAMP_WITH_TIMEZONE:
                ((ZonedTimestampPicker) component).setDateTime((OffsetDateTime) parameter.getValue());
                break;

            case Types.TIME:
                ((TimePicker) component).setTime((LocalTime) parameter.getValue());
                break;

            case Types.TIME_WITH_TIMEZONE:
                ((ZonedTimePicker) component).setTime((OffsetTime) parameter.getValue());
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

    private void apply() {

        for (int i = 0; i < parameters.size(); i++) {

            Parameter parameter = parameters.get(i);
            JComponent component = componentList.get(i);

            switch (parameter.getType()) {
                case Types.DATE:
                    parameter.setValue(((DatePicker) component).getDate());
                    break;

                case Types.TIMESTAMP:
                    parameter.setValue(((TimestampPicker) component).getDateTime());
                    break;

                case Types.TIMESTAMP_WITH_TIMEZONE:
                    parameter.setValue(((ZonedTimestampPicker) component).getOffsetDateTime());
                    break;

                case Types.TIME:
                    parameter.setValue(((TimePicker) component).getLocalTime());
                    break;

                case Types.TIME_WITH_TIMEZONE:
                    parameter.setValue(((ZonedTimePicker) component).getOffsetTime());
                    break;

                case Types.BOOLEAN:
                    parameter.setValue(((RDBCheckBox) component).getStringValue());
                    break;

                case Types.BINARY:
                case Types.BLOB:
                case Types.LONGVARBINARY:
                case Types.LONGVARCHAR:
                    parameter.setValue(((RDBFieldFileChooser) component).getFile());
                    break;

                default:
                    parameter.setValue(((ValueOrNullParameterField) component).getValue());
                    break;
            }
        }

        finished();
    }

    private void cancel() {
        setCanceled(true);
        dispose();
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

}
