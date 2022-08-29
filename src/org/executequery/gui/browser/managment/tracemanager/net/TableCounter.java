package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.gui.browser.managment.tracemanager.LogConstants;

public class TableCounter {
    private String header;

    private final String[] counters;
    private String body;

    private static final int COL_WIDTH = 10;

    public TableCounter(String header, String body) {
        setHeader(header);
        setBody(body);
        counters = new String[LogConstants.TABLE_COUNTERS.length];
        for (int i = 0; i < LogConstants.TABLE_COUNTERS.length; i++) {
            if (i == 0) {
                int tableLength = header.indexOf(LogConstants.NATURAL);
                tableLength = tableLength - (COL_WIDTH - LogConstants.NATURAL.length());
                counters[0] = (body.substring(0, tableLength).trim());
            } else {
                String col = LogConstants.TABLE_COUNTERS[i];
                int position = header.indexOf(col) + col.length() - COL_WIDTH;
                if (position > 0 && position < body.length()) {
                    String value = body.substring(position, position + COL_WIDTH).trim();
                    counters[i] = value;
                }
            }
        }
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCounter(int col) {
        return counters[col];
    }
}
