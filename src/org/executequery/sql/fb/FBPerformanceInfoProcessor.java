package org.executequery.sql.fb;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
//import org.firebirdsql.gds.ng.InfoProcessor;

import java.sql.SQLException;

import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;

/**
 * Created by vasiliy on 12.01.17.
 */
public class FBPerformanceInfoProcessor/* implements InfoProcessor<FBPerformanceInfo>*/ {

    FBPerformanceInfo out = null;

//    @Override
    public FBPerformanceInfo process(byte[] info) throws SQLException {
        if (info.length == 0) {
            throw new SQLException("Response buffer for database information request is empty");
        }
        FBPerformanceInfo out = new FBPerformanceInfo();
        int value;
        int len;
        int i = 0;
        while (info[i] != ISCConstants.isc_info_end) {
            switch (info[i++]) {
                case ISCConstants.isc_info_reads:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfReads(value);
                    break;
                case ISCConstants.isc_info_writes:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfWrites(value);
                    break;
                case ISCConstants.isc_info_fetches:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfFetches(value);
                    break;
                case ISCConstants.isc_info_marks:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfMarks(value);
                    break;
                case ISCConstants.isc_info_page_size:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfPageSize(value);
                    break;
                case ISCConstants.isc_info_num_buffers:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfBuffers(value);
                    break;
                case ISCConstants.isc_info_current_memory:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfCurrentMemory(value);
                    break;
                case ISCConstants.isc_info_max_memory:
                    len = iscVaxInteger2(info, i);
                    i += 2;
                    value = iscVaxInteger(info, i, len);
                    i += len;
                    out.setPerfMaxMemory(value);
                    break;
                case ISCConstants.isc_info_truncated:
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_info_truncated).toSQLException();
                default:
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_infunk).toSQLException();
            }
        }
        return out;
    }
}
