package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.log.Log;

import java.util.ArrayList;
import java.util.List;

public class AnaliseRow {
    LogMessage logMessage;
    public final static int TIME = 0;
    public final static int READ = 1;
    public final static int FETCH = 2;
    public final static int WRITE = 3;
    public final static String[] TYPES = {"TIME", "READ", "FETCH", "WRITE"};
    List<LogMessage>[] rows;
    long[] average = new long[4];
    long[] total = new long[4];
    long[] max = new long[4];
    long[] dispersion = new long[4];
    long[] count = new long[4];

    public AnaliseRow() {
        rows = new List[4];
        for (int i = 0; i < 4; i++)
            rows[i] = new ArrayList<>();
    }

    public LogMessage getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(LogMessage logMessage) {
        this.logMessage = logMessage;
    }

    public long[] getAverage() {
        return average;
    }

    public long[] getTotal() {
        return total;
    }

    public long[] getMax() {
        return max;
    }

    public long[] getDispersion() {
        return dispersion;
    }

    public List<LogMessage>[] getRows() {
        return rows;
    }

    public long[] getCount() {
        return count;
    }

    public void addMessage(LogMessage msg) {
        for (int i = TIME; i < 4; i++) {
            addMessage(msg, i);
        }
    }

    void addMessage(LogMessage msg, int type) {
        Long currentValue = getValueFromType(msg, type);
        if (currentValue != null) {
            if (logMessage == null)
                logMessage = msg;
            rows[type].add(msg);
            count[type]++;
            total[type] += currentValue;
            if (currentValue > max[type])
                max[type] = currentValue;
        } else Log.debug("calculate error for type '" + TYPES[type] + "': trace id = " + msg.getId());
    }

    public void calculateValues() {
        for (int i = TIME; i < 4; i++) {
            calculateValues(i);
        }
    }

    void calculateValues(int type) {
        if (count[type] > 0) {
            average[type] = total[type] / count[type];

            long sko = 0;
            if (count[type] > 1) {
                for (LogMessage row : rows[type]) {
                    long value = getValueFromType(row, type);
                    sko += (value - average[type]) * (value - average[type]);
                }
                sko = sko / count[type] - 1;
                dispersion[type] = (long) Math.sqrt(sko);
            }
        }
    }

    Long getValueFromType(LogMessage msg, int type) {
        switch (type) {
            case READ:
                return msg.getCountReads();
            case FETCH:
                return msg.getCountFetches();
            case WRITE:
                return msg.getCountWrites();
            default:
                return msg.getTimeExecution();
        }
    }
}
