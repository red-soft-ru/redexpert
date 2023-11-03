package org.executequery.gui.browser.profiler;

import org.executequery.gui.text.SQLTextArea;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Class describing the data that is displayed in the node of the <code>JTree</code>.
 *
 * @author Alexey Kozlov
 */
class ProfilerData {

    public static final String ROOT = "ROOT";
    public static final String SELF_TIME = "SELF_TIME";
    public static final String PSQL = "PSQL";
    public static final String BLOCK = "BLOCK";
    public static final String FUNCTION = "FUNCTION";
    public static final String PROCEDURE = "PROCEDURE";

    private final int id;
    private final int callerId;
    private final String processName;
    private final String processType;
    private final String sourceCode;
    private List<PsqlLine> psqlStats;
    private long totalTime;
    private double totalTimePercentage;
    private long avgTime;
    private long callCount;

    public ProfilerData() {
        this(-1, -1, "ROOT NODE", ROOT, null, null, 0, 0, 1, 0);
    }

    public ProfilerData(int id, int callerId, String packageName, String routineName, String processType, String sourceCode, long totalTime) {
        this(id, callerId, (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim(), processType, sourceCode, totalTime);
    }

    public ProfilerData(int id, int callerId, String processName, String processType, String sourceCode, long totalTime) {
        this(id, callerId, processName, processType, sourceCode, null, totalTime, totalTime, 1, 100);
    }

    public ProfilerData(int id, int callerId, String processName, String processType, long totalTime, long avgTime, int callCount) {
        this(id, callerId, processName, processType, null, null, totalTime, avgTime, callCount, 100);
    }

    protected ProfilerData(int id, int callerId, String processName, String processType, String sourceCode,
                           List<PsqlLine> psqlStats, long totalTime, double totalTimePercentage) {
        this(id, callerId, processName, processType, sourceCode, psqlStats, totalTime, totalTime, 1, totalTimePercentage);
    }

    protected ProfilerData(int id, int callerId, String processName, String processType, String sourceCode,
                           List<PsqlLine> psqlStats, long totalTime, long avgTime, int callCount, double totalTimePercentage) {
        this.id = id;
        this.callerId = callerId;
        this.processName = processName;
        this.processType = processType;
        this.sourceCode = sourceCode;
        this.psqlStats = psqlStats;
        this.totalTime = totalTime;
        this.avgTime = avgTime;
        this.callCount = callCount;
        this.totalTimePercentage = totalTimePercentage;
    }

    public boolean compareAndMergeData(ProfilerData comparingData) {

        if (this.callerId == comparingData.callerId && Objects.equals(this.processName, comparingData.processName)) {

            this.callCount++;
            this.avgTime = (this.avgTime + comparingData.avgTime) / 2;
            this.totalTime += comparingData.totalTime;

            this.totalTimePercentage += comparingData.totalTimePercentage;
            if (this.totalTimePercentage > 100.00) this.totalTimePercentage = 100.00;

            return true;
        }

        return false;
    }

    private void setupPsqlStats() {

        int shift = Integer.MAX_VALUE;
        for (PsqlLine line : psqlStats)
            shift = Integer.min(shift, line.number);
        shift *= -1;

        SQLTextArea sql = new SQLTextArea();
        List<String> sourceCodeLines = Arrays.asList(sourceCode.split("\n"));
        sourceCodeLines.forEach(line -> sql.append(line.trim() + "\n"));

        for (int i = 0; i < sql.getLineCount(); i++) {

            int type = sql.getTokenListForLine(i).getType();
            String line = sourceCodeLines.get(i).trim();

            if (type < TokenTypes.RESERVED_WORD
                    || line.isEmpty()
                    || line.equalsIgnoreCase("begin")) {

                shift++;
            } else break;
        }

        for (PsqlLine line : psqlStats)
            line.setString(Integer.sum(line.number, shift + 1) + ": " + sourceCodeLines.get(line.number + shift).trim());
    }

    public void setPsqlStats(List<PsqlLine> psqlStats) {
        this.psqlStats = psqlStats;
        setupPsqlStats();
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

    public String getSourceCode() {
        return sourceCode;
    }

    public List<PsqlLine> getPsqlStats() {
        return psqlStats;
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

    public double getTotalTimePercentage() {
        return totalTimePercentage;
    }

    public ProfilerData getCopy() {
        return new ProfilerData(id, callerId, processName, processType, sourceCode, psqlStats, totalTime, totalTimePercentage);
    }

    public static class PsqlLine {

        private final int number;
        private String string;
        private final int callCount;
        private final long avgTime;
        private final long totalTime;

        public PsqlLine(int number, long totalTime, int callCount) {
            this.number = number;
            this.string = "line " + number;
            this.totalTime = totalTime;
            this.callCount = callCount;
            this.avgTime = totalTime / callCount;
        }

        public void setString(String string) {
            this.string = string;
        }

        public int getNumber() {
            return number;
        }

        public String getString() {
            return string;
        }

        public int getCallCount() {
            return callCount;
        }

        public long getAvgTime() {
            return avgTime;
        }

        public long getTotalTime() {
            return totalTime;
        }

    } // PSQLLineStatistic class

}
