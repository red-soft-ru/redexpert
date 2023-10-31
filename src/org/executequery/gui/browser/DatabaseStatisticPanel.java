package org.executequery.gui.browser;

import biz.redsoft.IFBStatisticManager;
import org.executequery.base.TabView;
import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.localization.Bundles;

public class DatabaseStatisticPanel extends AbstractServiceManagerPanel implements TabView {
    public static final String TITLE = Bundles.get(DatabaseStatisticPanel.class, "title");
    private IFBStatisticManager statisticManager;


    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    @Override
    protected void initOtherComponents() {

    }

    @Override
    protected void setEnableElements() {

    }

    @Override
    protected void changeDatabaseConnection() {

    }

    @Override
    protected void arrangeComponents() {

    }

    @Override
    protected void postInitActions() {

    }
}
