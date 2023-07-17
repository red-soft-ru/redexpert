package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.log.Log;

import java.util.ArrayList;
import java.util.List;

public class AnaliseRow {
    LogMessage logMessage;
    List<LogMessage> rows;
    long averageTime;
    long totalTime;
    long maxTime;
    long minTime;
    long dispersionTime;
    long count;

    public AnaliseRow() {
        rows = new ArrayList<>();
        averageTime = 0;
        totalTime = 0;
        maxTime = 0;
        minTime = Long.MAX_VALUE;
        dispersionTime = 0;
        count = 0;
    }

    public LogMessage getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(LogMessage logMessage) {
        this.logMessage = logMessage;
    }

    public List<LogMessage> getRows() {
        return rows;
    }

    public void setRows(List<LogMessage> rows) {
        this.rows = rows;
    }

    public long getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(long averageTime) {
        this.averageTime = averageTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public long getDispersionTime() {
        return dispersionTime;
    }

    public void setDispersionTime(long dispersionTime) {
        this.dispersionTime = dispersionTime;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void addMessage(LogMessage msg) {
        if (msg.getTimeExecution() != null) {
            if (logMessage == null)
                logMessage = msg;
            rows.add(msg);
            count++;
            totalTime += msg.getTimeExecution();
            averageTime = totalTime / count;
            if (msg.getTimeExecution() > maxTime)
                maxTime = msg.getTimeExecution();
            if (msg.getTimeExecution() < minTime)
                minTime = msg.getTimeExecution();
            long sko = 0;
            if (count > 1) {
                for (LogMessage row : rows) {
                    sko += (row.getTimeExecution() - averageTime) * (row.getTimeExecution() - averageTime);
                }
                sko = sko / count - 1;
                dispersionTime = (long) Math.sqrt(sko);
            }
        } else Log.error("Time execution error: trace id = "+msg.getId());
    }
}
