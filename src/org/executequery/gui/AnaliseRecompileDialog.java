package org.executequery.gui;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseExecutable;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.LoadingObjectsHelper;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlMessages;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AnaliseRecompileDialog extends BaseDialog {
    public StringBuilder sb;
    public StringBuilder invalidSb;
    JProgressBar progressBar;
    JPanel panel;

    DatabaseObjectNode databaseObjectNode;

    LoggingOutputPanel logPane;
    boolean onlyInvalid;

    public AnaliseRecompileDialog(String name, boolean modal, DatabaseObjectNode databaseObjectNode, boolean onlyInvalid) {
        super(name, modal);
        this.databaseObjectNode = databaseObjectNode;
        this.onlyInvalid = onlyInvalid;
        init();
    }

    private void init() {
        sb = new StringBuilder();
        invalidSb = new StringBuilder();
        panel = new JPanel();
        progressBar = new JProgressBar();
        logPane = new LoggingOutputPanel();
        panel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(GridBagHelper.DEFAULT_CONSTRAINTS);
        gbh.defaults();
        panel.add(progressBar, gbh.fillHorizontally().spanX().get());
        panel.add(logPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        setPreferredSize(new Dimension(500, 500));
        setContentPane(panel);
        SwingWorker sw = new SwingWorker("loadingDataForRecompile") {
            @Override
            public Object construct() {
                executeAnalise();
                return null;
            }

            @Override
            public void finished() {
                success = true;
                dispose();
            }
        };
        sw.start();
    }

    void addOutputMessage(int type, String text) {
        logPane.append(type, text);
    }

    public boolean success = false;

    @Override
    public void display() {
        super.display();
    }

    public void executeAnalise() {
        if (databaseObjectNode != null) {

            List<DatabaseObjectNode> childs = databaseObjectNode.getChildObjects();
            DefaultDatabaseMetaTag metaTag = (DefaultDatabaseMetaTag) databaseObjectNode.getDatabaseObject();
            long start = System.currentTimeMillis();
            if (childs != null) {
                progressBar.setMaximum(childs.size());
                LoadingObjectsHelper loadingObjectsHelper = new LoadingObjectsHelper(childs.size());
                for (int i = 0; i < childs.size(); i++) {
                    progressBar.setValue(i);
                    AbstractDatabaseObject databaseObject = (AbstractDatabaseObject) childs.get(i).getDatabaseObject();
                    addOutputMessage(SqlMessages.PLAIN_MESSAGE, bundleString("generateScript", databaseObject.getName()));
                    loadingObjectsHelper.preparingLoadForObject(databaseObject);
                    String s = databaseObject.getCreateSQLText();
                    loadingObjectsHelper.postProcessingLoadForObject(databaseObject);
                    StringBuilder stringBuilder = sb;
                    if (databaseObject instanceof DefaultDatabaseExecutable && !((DefaultDatabaseExecutable) databaseObject).isValid())
                        stringBuilder = invalidSb;
                    else if (onlyInvalid)
                        continue;
                    stringBuilder.append(s);
                    if (!stringBuilder.toString().trim().endsWith("^"))
                        stringBuilder.append("^");
                }
                sb.insert(0, invalidSb);
                if (metaTag.getSubType() == NamedObject.PACKAGE)
                    sb.insert(0, "set term ; ^");
                loadingObjectsHelper.releaseResources();

            }
            Log.info("Analise time = "+(System.currentTimeMillis()-start));
        }

    }

    protected String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }
}
