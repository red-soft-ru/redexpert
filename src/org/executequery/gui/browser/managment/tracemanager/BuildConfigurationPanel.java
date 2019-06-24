package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.browser.TraceManagerPanel;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BuildConfigurationPanel extends JPanel {
    String[] checkDatabaseStrs = {
            "print_plan",
            "print_perf",
            "print_blr",
            "print_dyn",
            "print_stack_trace",
            "log_errors_only",
            "log_changes_only",
            "log_security_incidents",
            "log_privilege_changes",
            "log_auth_factors",
            "log_init",
            "log_connections",
            "log_transactions",
            "log_statement_prepare",
            "log_statement_free",
            "log_statement_start",
            "log_statement_finish",
            "log_procedure_start",
            "log_procedure_finish",
            "log_trigger_start",
            "log_trigger_finish",
            "log_context",
            "log_warnings",
            "log_mandatory_access",
            "log_record_mandatory_access",
            "log_object_relabeling",
            "log_record_relabeling",
            "log_blr_requests",
            "log_dyn_requests"
    };
    String[] checkDatabase3Strs = {
            "log_security_incidents",
            "log_initfini",
            "log_connections",
            "log_transactions",
            "log_statement_prepare",
            "log_statement_free",
            "log_statement_start",
            "log_statement_finish",
            "log_procedure_start",
            "log_procedure_finish",
            "log_function_start",
            "log_function_finish",
            "log_trigger_start",
            "log_trigger_finish",
            "log_context",
            "log_errors",
            "log_warnings",
            "print_plan",
            "print_perf",
            "log_blr_requests",
            "print_blr",
            "log_dyn_requests",
            "print_dyn",
            "log_privilege_changes",
            "log_changes_only"};
    String[] checkServicesStrs = {"log_services", "log_service_query"};
    String[] filters = {
            "include_user_filter",
            "exclude_user_filter",
            "include_process_filter",
            "exclude_process_filter",
            "include_filter",
            "exclude_filter",
            "connection_id",
            "log_filename"
    };
    String[] intValues = {
            "max_log_size",
            "time_threshold",
            "max_sql_length",
            "max_blr_length",
            "max_dyn_length",
            "max_arg_length",
            "max_arg_count"
    };
    private JComboBox<String> appropriationBox;
    private String filename;
    private JPanel databasePanel;
    private JPanel servicesPanel;
    private JPanel intValuesPanel;
    private JPanel filtersPanel;
    private JButton saveFileButton;
    private JTextField saveFileField;
    private JButton saveButton;
    private Map<String, JComponent> componentMap;
    private int x = 6;

    public BuildConfigurationPanel() {
        componentMap = new HashMap<>();
        appropriationBox = new JComboBox<>(new String[]{"RedDatabase 2.6", "RedDatabase 3.0"});
        appropriationBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                rebuildDatabasePanel();
            }
        });
        servicesPanel = new JPanel();
        servicesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Services",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        intValuesPanel = new JPanel();
        intValuesPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Int Values",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        filtersPanel = new JPanel();
        filtersPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Filters",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        saveFileButton = new DefaultButton("...");
        saveFileButton.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {

                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().endsWith(".conf") || f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "Configuration Files";
                    }
                });
                int returnVal = fileChooser.showSaveDialog(saveFileButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    filename = file.getName();
                    saveFileField.setText(file.getAbsolutePath());
                }
            }
        });
        saveFileField = new JTextField();
        saveButton = new DefaultButton(TraceManagerPanel.bundleString("Save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        setLayout(new GridBagLayout());

        rebuildDatabasePanel();

        servicesPanel.setLayout(new GridBagLayout());
        int k = 0;
        for (int i = 0; k < checkServicesStrs.length; i++)
            for (int g = 0; g < x && k < checkServicesStrs.length; g++, k++) {
                JCheckBox checkBox = new JCheckBox(checkServicesStrs[k]);
                checkBox.setSelected(true);
                GridBagConstraints gbc = new GridBagConstraints(
                        g, i, 1, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                servicesPanel.add(checkBox, gbc);
                componentMap.put(checkServicesStrs[k], checkBox);
            }

        filtersPanel.setLayout(new GridBagLayout());
        k = 0;
        for (int i = 0; k < filters.length; i++)
            for (int g = 0; g < 2 && k < filters.length; g++, k++) {
                JLabel label = new JLabel(filters[k]);
                GridBagConstraints gbc = new GridBagConstraints(
                        g * 3, i, 1, 1,
                        0, 0, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE, new Insets(1, 1, 1, 1),
                        0, 0);
                filtersPanel.add(label, gbc);
                JTextField field = new JTextField();
                gbc = new GridBagConstraints(
                        g * 3 + 1, i, 2, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                filtersPanel.add(field, gbc);
                componentMap.put(filters[k], field);
            }

        intValuesPanel.setLayout(new GridBagLayout());
        k = 0;
        for (int i = 0; k < intValues.length; i++)
            for (int g = 0; g < 2 && k < intValues.length; g++, k++) {
                JLabel label = new JLabel(intValues[k]);
                GridBagConstraints gbc = new GridBagConstraints(
                        g * 3, i, 1, 1,
                        0, 0, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.NONE, new Insets(1, 1, 1, 1),
                        0, 0);
                intValuesPanel.add(label, gbc);
                NumberTextField field = new NumberTextField();
                field.setValue(0);
                gbc = new GridBagConstraints(
                        g * 3 + 1, i, 2, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                intValuesPanel.add(field, gbc);
                componentMap.put(intValues[k], field);
            }

        JLabel label = new JLabel(TraceManagerPanel.bundleString("ServerVersion"));
        add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(appropriationBox, new GridBagConstraints(1, 0,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(databasePanel, new GridBagConstraints(0, 1,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(servicesPanel, new GridBagConstraints(0, 2,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(filtersPanel, new GridBagConstraints(0, 3,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(intValuesPanel, new GridBagConstraints(0, 4,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel(TraceManagerPanel.bundleString("ConfigFile"));
        add(label, new GridBagConstraints(0, 5,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(saveFileButton, new GridBagConstraints(1, 5,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(saveFileField, new GridBagConstraints(2, 5,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(saveButton, new GridBagConstraints(0, 6,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
    }

    private void rebuildDatabasePanel() {
        if (databasePanel != null)
            remove(databasePanel);
        databasePanel = new JPanel();
        databasePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Database",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        databasePanel.setLayout(new GridBagLayout());
        int k = 0;
        String[] checks;
        if (appropriationBox.getSelectedIndex() == 0)
            checks = checkDatabaseStrs;
        else checks = checkDatabase3Strs;
        for (int i = 0; k < checks.length; i++)
            for (int g = 0; g < x && k < checks.length; g++, k++) {
                JCheckBox checkBox = new JCheckBox(checks[k]);
                checkBox.setSelected(true);
                GridBagConstraints gbc = new GridBagConstraints(
                        g, i, 1, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                databasePanel.add(checkBox, gbc);
                componentMap.put(checks[k], checkBox);
            }

        add(databasePanel, new GridBagConstraints(0, 1,
                3, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        updateUI();

    }

    private void save() {
        try (FileWriter writer = new FileWriter(saveFileField.getText(), false)) {
            writer.write(getConfig());
            writer.flush();
            GUIUtilities.displayInformationMessage("Configuration file was built");
        } catch (IOException e) {
            GUIUtilities.displayExceptionErrorDialog("Error write to file", e);
        }

    }

    public String getConfig() {
        StringBuilder sb = new StringBuilder();
        if (filename != null)
            sb.append("#").append(filename).append("\n");
        if (appropriationBox.getSelectedIndex() == 0)
            sb.append("<database>");
        else sb.append("database\n{");
        sb.append("\n\n");
        sb.append("\tenabled").append(apSymbol()).append("true\n\n");
        sb.append("\tformat").append(apSymbol()).append("0\n\n");
        String[] checks;
        if (appropriationBox.getSelectedIndex() == 0)
            checks = checkDatabaseStrs;
        else checks = checkDatabase3Strs;
        for (int i = 0; i < checks.length; i++) {
            appendProp(sb, checks[i]);
        }
        for (int i = 0; i < filters.length; i++) {
            if (!strFromComponent(componentMap.get(filters[i])).isEmpty())
                appendProp(sb, filters[i]);
        }
        for (int i = 0; i < intValues.length; i++) {
            appendProp(sb, intValues[i]);
        }
        if (appropriationBox.getSelectedIndex() == 0)
            sb.append("</database>");
        else sb.append("}");
        sb.append("\n\n");
        if (appropriationBox.getSelectedIndex() == 0)
            sb.append("<services>");
        else sb.append("services\n{");
        sb.append("\n\n");
        sb.append("\tenabled").append(apSymbol()).append("true\n\n");
        sb.append("\tformat").append(apSymbol()).append("0\n\n");
        for (int i = 0; i < checkServicesStrs.length; i++) {
            appendProp(sb, checkServicesStrs[i]);
        }
        for (int i = 0; i < filters.length; i++) {
            if (!strFromComponent(componentMap.get(filters[i])).isEmpty())
                appendProp(sb, filters[i]);
        }
        appendProp(sb, "max_log_size");
        if (appropriationBox.getSelectedIndex() == 0)
            sb.append("</services>");
        else sb.append("}");
        return sb.toString();
    }

    private String apSymbol() {
        if (appropriationBox.getSelectedIndex() == 0)
            return " ";
        else return " = ";
    }

    private String strFromComponent(JComponent component) {
        if (component instanceof JCheckBox) {
            if (((JCheckBox) component).isSelected())
                return "true";
            else return "false";
        }
        if (component instanceof JTextField) {
            return ((JTextField) component).getText();
        }
        return null;
    }

    private void appendProp(StringBuilder sb, String key) {
        sb.append("\t").append(key).append(apSymbol()).append(strFromComponent(componentMap.get(key))).append("\n\n");
    }

    public JComboBox<String> getAppropriationBox() {
        return appropriationBox;
    }
}
