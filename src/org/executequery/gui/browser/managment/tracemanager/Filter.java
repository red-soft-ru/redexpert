package org.executequery.gui.browser.managment.tracemanager;

/**
 * Created by vasiliy on 23.12.16.
 */
public class Filter {
    public enum FilterType {
        HIGHLIGHT("Highlight"), FILTER("Filter");
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
