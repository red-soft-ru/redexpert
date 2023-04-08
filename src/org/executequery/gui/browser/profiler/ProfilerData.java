package org.executequery.gui.browser.profiler;

import java.util.Objects;

/**
 * Class describing the data that is displayed in the node of the <code>JTree</code>.
 *
 * @author Alexey Kozlov
 */
class ProfilerData {

    private final int id;
    private final int callerId;
    private final String processName;
    private final long totalTime;
    private final long avgTime;
    private final long callCount;

    public ProfilerData(int id, int callerId, String processName, long totalTime, long avgTime, long callCount) {
        this.id = id;
        this.callerId = callerId;
        this.processName = processName;
        this.totalTime = totalTime;
        this.avgTime = avgTime;
        this.callCount = callCount;
    }

    public ProfilerData(int id, int callerId, String packageName, String routineName, long totalTime, long avgTime, long callCount) {
        this.id = id;
        this.callerId = callerId;
        this.processName = (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim();
        this.totalTime = totalTime;
        this.avgTime = avgTime;
        this.callCount = callCount;
    }

    public boolean isSame(ProfilerData comparingData) {
        return this.callerId == comparingData.callerId &&
                Objects.equals(this.processName, comparingData.processName) &&
                this.totalTime == comparingData.totalTime &&
                this.avgTime == comparingData.avgTime &&
                this.callCount == comparingData.callCount;
    }

    public int getId() {
        return id;
    }

    public int getCallerId() {
        return callerId;
    }

    public String getProcessName() {
        return processName;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public long getCallCount() {
        return callCount;
    }

}
