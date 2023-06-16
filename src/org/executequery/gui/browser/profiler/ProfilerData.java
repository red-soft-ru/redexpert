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
    private final String processType;
    private long totalTime;
    private double totalTimePercentage;
    private long avgTime;
    private long callCount;

    public ProfilerData() {
        this(-1, -1, "ROOT NODE", "ROOT", 0, 0);
        this.callCount = 0;
    }

    public ProfilerData(int id, int callerId, String packageName, String routineName, String processType, long totalTime) {
        this(id, callerId, (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim(), processType, totalTime);
    }

    public ProfilerData(int id, int callerId, String processName, String processType, long totalTime) {
        this(id, callerId, processName, processType, totalTime, 100);
    }

    public ProfilerData(int id, int callerId, String processName, String processType, long totalTime, double totalTimePercentage) {
        this.id = id;
        this.callerId = callerId;
        this.processName = processName;
        this.processType = processType;
        this.totalTime = totalTime;
        this.avgTime = totalTime;
        this.totalTimePercentage = totalTimePercentage;
        this.callCount = 1;
    }

    public boolean compareAndMergeData(ProfilerData comparingData) {

        if (this.callerId == comparingData.callerId && Objects.equals(this.processName, comparingData.processName)) {
            this.callCount++;
            this.avgTime = (this.avgTime + comparingData.avgTime) / 2;
            this.totalTime += comparingData.totalTime;
            this.totalTimePercentage += comparingData.totalTimePercentage;
            return true;
        }

        return false;
    }

    public void setTotalTimePercentage(double totalTimePercentage) {
        this.totalTimePercentage = totalTimePercentage;
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

    public String getProcessType() {
        return processType;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public double getTotalTimePercentage() {
        return totalTimePercentage;
    }

    public long getAvgTime() {
        return avgTime;
    }

    public long getCallCount() {
        return callCount;
    }

    public ProfilerData getCopy() {
        return new ProfilerData(id, callerId, processName, processType, totalTime, totalTimePercentage);
    }

}
