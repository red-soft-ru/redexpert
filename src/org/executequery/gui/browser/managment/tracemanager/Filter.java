package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.browser.TraceManagerPanel;

/**
 * Created by vasiliy on 23.12.16.
 */
public class Filter {
    public enum FilterType {
        HIGHLIGHT(TraceManagerPanel.bundleString("Highlight")), FILTER(TraceManagerPanel.bundleString("Filter"));
        final private String title;

        FilterType(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
