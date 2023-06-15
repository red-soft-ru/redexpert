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
    private long totalTime;
    private long avgTime;
    private long callCount;

    public ProfilerData() {
        this(-1, -1, "ROOT NODE", 0);
        this.callCount = 0;
    }

    public ProfilerData(int id, int callerId, String packageName, String routineName, long totalTime) {
        this(id, callerId, (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim(), totalTime);
    }

    public ProfilerData(int id, int callerId, String processName, long totalTime) {
        this.id = id;
        this.callerId = callerId;
        this.processName = processName;
        this.totalTime = totalTime;
        this.avgTime = totalTime;
        this.callCount = 1;
    }

    public boolean compareAndMergeData(ProfilerData comparingData) {

        if (this.callerId == comparingData.callerId && Objects.equals(this.processName, comparingData.processName)) {
            this.callCount++;
            this.avgTime = (this.avgTime + comparingData.avgTime) / 2;
            this.totalTime += comparingData.totalTime;
            return true;
        }

        return false;
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

    public ProfilerData getCopy() {
        return new ProfilerData(id, callerId, processName, totalTime);
    }

}
