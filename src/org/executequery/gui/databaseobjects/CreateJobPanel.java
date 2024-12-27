package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseJob;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.JobsLogPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.celleditor.picker.TimestampPicker;
import org.underworldlabs.swing.cron.CronPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;

public class CreateJobPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.JOB);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.JOB);

    private DefaultDatabaseJob job;

    // --- GUI components ---

    private TimestampPicker startDatePicker;
    private TimestampPicker endDatePicker;

    private SimpleSqlTextPanel sqlTextPanel;
    private SimpleTextArea bashTextPanel;
    private CronPanel cronPanel;
    private JPanel taskPanel;

    private JComboBox<?> jobTypeCombo;
    private JTextField databaseField;
    private JCheckBox activeCheck;
    private JTextField idField;

    // ---

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
    protected void init() {
        centralPanel.setVisible(false);
        connectionsCombo.setEnabled(false);

        startDatePicker = new TimestampPicker();
        endDatePicker = new TimestampPicker();

        taskPanel = new JPanel(new GridBagLayout());
        cronPanel = new CronPanel(editing);

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.getTextPane().setDatabaseConnection(connection);

        bashTextPanel = new SimpleTextArea("BASH");
        bashTextPanel.setFont(sqlTextPanel.getFont());
        bashTextPanel.setVisible(false);

        activeCheck = WidgetFactory.createCheckBox("activeCheck", bundleStaticString("active"));

        jobTypeCombo = WidgetFactory.createComboBox("jobTypeCombo", new String[]{"PSQL", "BASH"});
        jobTypeCombo.setPreferredSize(new Dimension(100, jobTypeCombo.getPreferredSize().height));
        jobTypeCombo.addItemListener(e -> checkJobType());

        tabbedPane.add(bundleString("Task"), taskPanel);
        tabbedPane.add(bundleString("Schedule"), cronPanel);
        addCommentTab(null);

        if (!editing)
            arrange();
    }

    @Override
    protected void initEdited() {

        idField = WidgetFactory.createTextField("idField");
        idField.setEditable(false);

        databaseField = WidgetFactory.createTextField("databaseField");
        databaseField.setEditable(false);

        reset();
        arrange();

        simpleCommentPanel.setDatabaseObject(job);
        nameField.setEditable(false);
        tabbedPane.addTab(bundleString("Log"), new JobsLogPanel(job));
        addCreateSqlTab(job);
    }

    @Override
    protected void reset() {

        if (job == null)
            return;

        jobTypeCombo.setSelectedIndex(job.getJobType());
        startDatePicker.setDateTime(job.getStartDate());
        endDatePicker.setDateTime(job.getEndDate());
        cronPanel.setCronString(job.getCronSchedule());
        databaseField.setText(job.getDatabase());
        activeCheck.setSelected(job.isActive());
        nameField.setText(job.getName());
        idField.setText(job.getId());

        if (job.getJobType() == DefaultDatabaseJob.PSQL_TYPE)
            sqlTextPanel.setSQLText(job.getSource());
        else
            bashTextPanel.getTextAreaComponent().setText(job.getSource());
    }

    private void arrange() {

        // --- task panel ---

        GridBagHelper gbh = new GridBagHelper().fillBoth();

        taskPanel.removeAll();
        taskPanel.add(sqlTextPanel, gbh.setMaxWeightY().spanX().get());
        taskPanel.add(bashTextPanel, gbh.get());
        gbh.setWidth(1).setMinWeightY().setMinWeightX().leftGap(5).fillNone();
        taskPanel.add(new JLabel(bundleString("jobType")), gbh.nextRowFirstCol().get());
        taskPanel.add(jobTypeCombo, gbh.nextCol().get());
        taskPanel.add(activeCheck, gbh.nextCol().anchorNorthEast().spanX().get());

        // --- top panel ---

        topGbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillHorizontally().anchorNorthWest();

        topPanel.removeAll();
        topPanel.add(new JLabel(Bundles.getCommon("connection")), topGbh.setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(connectionsCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(Bundles.getCommon("name")), topGbh.nextCol().setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(nameField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).fillBoth().get());
        if (editing) {
            topPanel.add(new JLabel(bundleString("ID")), topGbh.nextCol().setWidth(1).setMinWeightX().topGap(4).rightGap(0).fillHorizontally().get());
            topPanel.add(idField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
            topPanel.add(new JLabel(bundleString("Database")), topGbh.nextCol().setMinWeightX().topGap(4).rightGap(0).get());
            topPanel.add(databaseField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());
        }

        topPanel.add(new JLabel(bundleString("startDate")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(startDatePicker, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());
        topPanel.add(new JLabel(bundleString("endDate")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(endDatePicker, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());

        if (parent != null) {
            ((BaseDialog) parent).setPreferredSize(new Dimension(700, 450));
            parent.setResizable(false);
        }
    }

    private void checkJobType() {
        boolean sqlTaskSelected = jobTypeCombo.getSelectedIndex() == DefaultDatabaseJob.PSQL_TYPE;

        sqlTextPanel.setVisible(sqlTaskSelected);
        bashTextPanel.setVisible(!sqlTaskSelected);
    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(generateQuery(), "^");
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
    public int getType() {
        return NamedObject.JOB;
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        job = (DefaultDatabaseJob) databaseObject;
    }

    @Override
    protected String generateQuery() {
        return editing ? getGenerateAlterQuery() : getGenerateCreateQuery();
    }

    private String getGenerateCreateQuery() {
        return SQLUtils.generateCreateJob(
                nameField.getText(),
                cronPanel.getCronString(),
                activeCheck.isSelected(),
                startDatePicker.isNull() ?
                        null :
                        startDatePicker.getDateTime(),
                endDatePicker.isNull() ?
                        null :
                        endDatePicker.getDateTime(),
                jobTypeCombo.getSelectedIndex(),
                jobTypeCombo.getSelectedIndex() == DefaultDatabaseJob.PSQL_TYPE ?
                        sqlTextPanel.getSQLText() :
                        bashTextPanel.getTextAreaComponent().getText(),
                simpleCommentPanel.getComment(),
                false,
                getDatabaseConnection()
        );
    }

    private String getGenerateAlterQuery() {
        return SQLUtils.generateAlterJob(
                job,
                nameField.getText(),
                cronPanel.getCronString(),
                activeCheck.isSelected(),
                startDatePicker.isNull() ?
                        null :
                        startDatePicker.getDateTime(),
                endDatePicker.isNull() ?
                        null :
                        endDatePicker.getDateTime(),
                jobTypeCombo.getSelectedIndex(),
                jobTypeCombo.getSelectedIndex() == DefaultDatabaseJob.PSQL_TYPE ?
                        sqlTextPanel.getSQLText() :
                        bashTextPanel.getTextAreaComponent().getText(),
                simpleCommentPanel.getComment(),
                false
        );
    }

    @Override
    public void setParameters(Object[] params) {
        // do nothing
    }

}
