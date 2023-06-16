package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.log.Log;

import java.util.ArrayList;
import java.util.List;

public class AnaliseRow {
    LogMessage logMessage;
    List<LogMessage> rows;
    long averageTime;
    long totalTime;
    long count;

    public AnaliseRow() {
        rows = new ArrayList<>();
        averageTime = 0;
        totalTime = 0;
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
    public void addMessage(LogMessage msg)
    {
        if(logMessage==null)
            logMessage=msg;
        rows.add(msg);
        count++;
        if(msg.getTimeExecution()!=null) {
            totalTime += msg.getTimeExecution();
            averageTime = totalTime / count;
        } else Log.error("Time execution error: trace id = "+msg.getId());
    }
}
