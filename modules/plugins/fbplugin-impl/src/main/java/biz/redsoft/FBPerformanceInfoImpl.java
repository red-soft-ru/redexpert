package biz.redsoft;

/**
 * Created by vasiliy on 12.01.17.
 */
public class FBPerformanceInfoImpl implements IFBPerformanceInfo{
    private long perfFetches;
    private long perfMarks;
    private long perfReads;
    private long perfWrites;
    private long perfCurrentMemory;
    private long perfDeltaMemory;
    private long perfMaxMemory;
    private long perfBuffers;
    private long perfPageSize;

    FBPerformanceInfoImpl() {

    }

    public long getPerfFetches() {
        return perfFetches;
    }

    public void setPerfFetches(long perfFetches) {
        this.perfFetches = perfFetches;
    }

    public long getPerfMarks() {
        return perfMarks;
    }

    public void setPerfMarks(long perfMarks) {
        this.perfMarks = perfMarks;
    }

    public long getPerfReads() {
        return perfReads;
    }

    public void setPerfReads(long perfReads) {
        this.perfReads = perfReads;
    }

    public long getPerfWrites() {
        return perfWrites;
    }

    public void setPerfWrites(long perfWrites) {
        this.perfWrites = perfWrites;
    }

    public long getPerfCurrentMemory() {
        return perfCurrentMemory;
    }

    public void setPerfCurrentMemory(long perfCurrentMemory) {
        this.perfCurrentMemory = perfCurrentMemory;
    }

    public long getPerfDeltaMemory() {
        return perfDeltaMemory;
    }

    public void setPerfDeltaMemory(long perfDeltaMemory) {
        this.perfDeltaMemory = perfDeltaMemory;
    }

    public long getPerfMaxMemory() {
        return perfMaxMemory;
    }

    public void setPerfMaxMemory(long perfMaxMemory) {
        this.perfMaxMemory = perfMaxMemory;
    }

    public long getPerfBuffers() {
        return perfBuffers;
    }

    public void setPerfBuffers(long perfBuffers) {
        this.perfBuffers = perfBuffers;
    }

    public long getPerfPageSize() {
        return perfPageSize;
    }

    public void setPerfPageSize(long perfPageSize) {
        this.perfPageSize = perfPageSize;
    }

    public IFBPerformanceInfo processInfo(IFBPerformanceInfo before, IFBPerformanceInfo after) {
        FBPerformanceInfoImpl resultInfo = new FBPerformanceInfoImpl();

        resultInfo.setPerfBuffers(after.getPerfBuffers());
        resultInfo.setPerfCurrentMemory(after.getPerfCurrentMemory());
        resultInfo.setPerfDeltaMemory(after.getPerfCurrentMemory() - before.getPerfCurrentMemory());
        resultInfo.setPerfFetches(after.getPerfFetches() - before.getPerfFetches());
        resultInfo.setPerfMarks(after.getPerfMarks() - before.getPerfMarks());
        resultInfo.setPerfMaxMemory(after.getPerfMaxMemory());
        resultInfo.setPerfPageSize(after.getPerfPageSize());
        resultInfo.setPerfReads(after.getPerfReads() - before.getPerfReads());
        resultInfo.setPerfWrites(after.getPerfWrites() - before.getPerfWrites());

        return resultInfo;
    }

    public String getPerformanceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance info:\n\n");
        sb.append("Reads: " + this.perfReads + "\n");
        sb.append("Writes: " + this.perfWrites + "\n");
        sb.append("Fetches: " + this.perfFetches + "\n");
        sb.append("Marks: " + this.perfMarks + "\n");
        sb.append("Page size: " + this.perfPageSize + "\n");
        sb.append("Buffers: " + this.perfBuffers + "\n");
        sb.append("Current memory: " + this.perfCurrentMemory + "\n");
        sb.append("Delta memory: " + this.perfDeltaMemory + "\n");
        sb.append("Max memory: " + this.perfMaxMemory + "\n");
        sb.append("\nEnd of performance info");

        return sb.toString();
    }
}
