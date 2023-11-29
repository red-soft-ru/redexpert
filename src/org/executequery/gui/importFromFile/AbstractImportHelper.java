package org.executequery.gui.importFromFile;

import org.executequery.localization.Bundles;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

abstract class AbstractImportHelper implements ImportHelper {

    private final List<String> headers;

    protected ImportDataFromFilePanel parent;
    protected boolean isFirstRowHeaders;
    protected int previewRowCount;
    protected String pathToFile;

    protected AbstractImportHelper(ImportDataFromFilePanel parent, String pathToFile, int previewRowCount, boolean isFirstRowHeaders) {
        this.headers = new LinkedList<>();
        this.parent = parent;
        this.pathToFile = pathToFile;
        this.previewRowCount = previewRowCount;
        this.isFirstRowHeaders = isFirstRowHeaders;
    }

    protected void createHeaders(int count) {
        headers.clear();
        for (int i = 0; i < count; i++)
            headers.add("COLUMN" + (i + 1));
    }

    protected void createHeaders(List<String> newHeaders) {
        headers.clear();
        headers.addAll(newHeaders);
    }

    protected String bundleString(String key) {
        return Bundles.get(ImportDataFromFilePanel.class, key);
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public int getColumnsCount() {
        return headers.size();
    }

}
