package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseJob;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.JobsLogPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.celleditor.picker.DefaultDateTimePicker;
import org.underworldlabs.swing.cron.CronPanel;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CreateJobPanel extends AbstractCreateObjectPanel{
    public static final String CREATE_TITLE=getCreateTitle(NamedObject.JOB);
    public static final String EDIT_TITLE=getEditTitle(NamedObject.JOB);
    private SimpleSqlTextPanel sqlTextPanel;
    private SimpleTextArea bashTextPanel;
    private JComboBox jobTypeCombo;
    private JCheckBox activeBox;

    private CronPanel cronPanel;

    private DefaultDateTimePicker startDatePicker;
    private DefaultDateTimePicker endDatePicker;

    private JTextField idField;
    private JTextField databaseField;

    private DefaultDatabaseJob job;
    public CreateJobPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateJobPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public CreateJobPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    @Override
    protected void reset() {
        nameField.setText(job.getName());
        cronPanel.setCron(job.getCronSchedule());
        jobTypeCombo.setSelectedIndex(job.getJobType());
        if(job.getJobType()==DefaultDatabaseJob.PSQL_TYPE)
            sqlTextPanel.setSQLText(job.getSource());
        else bashTextPanel.getTextAreaComponent().setText(job.getSource());
        startDatePicker.setDateTime(job.getStartDate());
        endDatePicker.setDateTime(job.getEndDate());
        activeBox.setSelected(job.isActive());
        idField.setText(job.getId());
        databaseField.setText(job.getDatabase());
    }

    @Override
    protected void init() {
        centralPanel.setVisible(false);
        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.getTextPane().setDatabaseConnection(connection);
        bashTextPanel = new SimpleTextArea();
        jobTypeCombo = new JComboBox(new String[]{"PSQL","BASH"});
        jobTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    checkJobType();
                }
            }
        });
        activeBox = new JCheckBox(bundleStaticString("active"));
        startDatePicker = new DefaultDateTimePicker();
        startDatePicker.setVisibleNullBox(true);
        endDatePicker = new DefaultDateTimePicker();
        endDatePicker.setVisibleNullBox(true);
        cronPanel = new CronPanel(editing);

        topGbh.addLabelFieldPair(topPanel, bundleString("startDate"), startDatePicker, null, true, false);
        topGbh.addLabelFieldPair(topPanel, bundleString("endDate"), endDatePicker, null, false, true);
        topGbh.addLabelFieldPair(topPanel, bundleString("jobType"), jobTypeCombo, null, true, false);
        topPanel.add(activeBox, topGbh.nextCol().setLabelDefault().get());
        tabbedPane.add("SQL", sqlTextPanel);
        tabbedPane.add(bundleString("Schedule"), cronPanel);
        addCommentTab(null);
    }

    private void checkJobType()
    {
        if (jobTypeCombo.getSelectedIndex()== DefaultDatabaseJob.PSQL_TYPE) {
                tabbedPane.remove(0);
                tabbedPane.insertTab("SQL", null, sqlTextPanel, null, 0);
            } else {
                tabbedPane.remove(0);
                tabbedPane.insertTab("Bash", null, bashTextPanel, null, 0);
            }
            tabbedPane.setSelectedIndex(0);
    }

    @Override
    protected void initEdited() {
        simpleCommentPanel.setDatabaseObject(job);
        idField = new JTextField();
        idField.setEditable(false);
        databaseField = new JTextField();
        databaseField.setEditable(false);
        topGbh.addLabelFieldPair(topPanel, bundleString("ID"), idField, null, false, false);
        topGbh.addLabelFieldPair(topPanel, bundleString("Database"), databaseField, null, false, true);
        reset();
        tabbedPane.addTab(bundleString("Log"), new JobsLogPanel(job));
        addCreateSqlTab(job);
    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(generateQuery(),"^");
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.JOB];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        job=(DefaultDatabaseJob) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    @Override
    protected String generateQuery() {

        if (!editing) {

            return SQLUtils.generateCreateJob(
                    nameField.getText(),
                    cronPanel.getCron(),
                    activeBox.isSelected(),
                    startDatePicker.isNull() ? null : startDatePicker.getDateTime(),
                    endDatePicker.isNull() ? null : endDatePicker.getDateTime(),
                    jobTypeCombo.getSelectedIndex(),
                    jobTypeCombo.getSelectedIndex() == DefaultDatabaseJob.PSQL_TYPE ?
                            sqlTextPanel.getSQLText() :
                            bashTextPanel.getTextAreaComponent().getText(),
                    simpleCommentPanel.getComment(), false, getDatabaseConnection());

        } else {

            return SQLUtils.generateAlterJob(
                    job, nameField.getText(), cronPanel.getCron(), activeBox.isSelected(),
                    startDatePicker.isNull() ? null : startDatePicker.getDateTime(),
                    endDatePicker.isNull() ? null : endDatePicker.getDateTime(),
                    jobTypeCombo.getSelectedIndex(),
                    jobTypeCombo.getSelectedIndex() == DefaultDatabaseJob.PSQL_TYPE ?
                            sqlTextPanel.getSQLText() :
                            bashTextPanel.getTextAreaComponent().getText(),
                    simpleCommentPanel.getComment(), false);
        }
    }

}
