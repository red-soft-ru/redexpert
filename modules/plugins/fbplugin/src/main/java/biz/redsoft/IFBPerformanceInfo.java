package biz.redsoft;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public interface IFBPerformanceInfo {
    long getPerfFetches();

    void setPerfFetches(long perfFetches);

    long getPerfMarks();

    void setPerfMarks(long perfMarks);

    long getPerfReads();

    void setPerfReads(long perfReads);

    long getPerfWrites();

    void setPerfWrites(long perfWrites);

    long getPerfCurrentMemory();

    void setPerfCurrentMemory(long perfCurrentMemory);

    long getPerfDeltaMemory();

    void setPerfDeltaMemory(long perfDeltaMemory);

    long getPerfMaxMemory();

    void setPerfMaxMemory(long perfMaxMemory);

    long getPerfBuffers();

    void setPerfBuffers(long perfBuffers);

    long getPerfPageSize();

    void setPerfPageSize(long perfPageSize);

    IFBPerformanceInfo processInfo(IFBPerformanceInfo before, IFBPerformanceInfo after);

    String getPerformanceInfo();
}
