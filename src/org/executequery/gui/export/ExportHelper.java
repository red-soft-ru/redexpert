package org.executequery.gui.export;

interface ExportHelper {

    /**
     * Exports the provided data.
     *
     * @param data The object containing the data to be exported.
     * @return <code>true</code> if the export operation was successful,
     * <code>false</code> otherwise.
     */
    boolean export(Object data);

}
