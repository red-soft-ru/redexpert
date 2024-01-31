package org.executequery.gui.browser.managment.tracemanager.net;

import org.executequery.localization.Bundles;
import org.executequery.log.Log;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnaliseRow {
    LogMessage logMessage;
    StringBuilder logMessages;
    public final static int TIME = 0;
    public final static int READ = TIME + 1;
    public final static int FETCH = READ + 1;
    public final static int WRITE = FETCH + 1;
    public final static int MARK = WRITE + 1;
    public final static int RSORT = MARK + 1;
    public final static int DSORT = RSORT + 1;
    public final static String[] TYPES = {"TIME", "READ", "FETCH", "WRITE", "MARK", "RSORT", "DSORT"};
    public final static Color[] COLORS = new Color[]{new Color(255, 173, 173),
            new Color(255, 214, 165),
            new Color(253, 255, 182),
            new Color(202, 255, 191),
            new Color(155, 246, 255),
            new Color(189, 178, 255),
            new Color(255, 198, 255)
    };

    public final static String[] TOOLTIPS = Bundles.get(AnaliseRow.class, new String[]{"TIME", "READ", "FETCH", "WRITE", "MARK", "RSORT", "DSORT"});


    public final static int TOTAL = 0;
    public final static int AVG = TOTAL + 1;
    public final static int MAX = AVG + 1;
    public final static int MIN = MAX + 1;
    public final static int STD_DEV = MIN + 1;

    public final static String[] PARAMS = new String[]{

            "TOTAL", "AVG", "MAX", "MIN", "STD_DEV"
    };

    List<LogMessage>[] rows;
    List<LogMessage> allRows;
    AnaliseValue[] average = new AnaliseValue[TYPES.length];
    AnaliseValue[] total = new AnaliseValue[TYPES.length];
    AnaliseValue[] max = new AnaliseValue[TYPES.length];
    AnaliseValue[] min = new AnaliseValue[TYPES.length];
    AnaliseValue[] std_dev = new AnaliseValue[TYPES.length];
    long[] count = new long[TYPES.length];

    List<String> plans;
    String planText;

    public LogMessage getLogMessage() {
        return logMessage;
    }

    public String getLogMessages() {
        return logMessages.toString();
    }

    public void setLogMessage(LogMessage logMessage) {
        this.logMessage = logMessage;
    }

    public AnaliseRow() {
        allRows = new ArrayList<>();
        logMessages = new StringBuilder();
        rows = new List[TYPES.length];
        for (int i = 0; i < TYPES.length; i++) {
            rows[i] = new ArrayList<>();
            total[i] = new AnaliseValue(0, i);
            max[i] = new AnaliseValue(0, i);
            min[i] = new AnaliseValue(-1, i);
            average[i] = new AnaliseValue(0, i);
            std_dev[i] = new AnaliseValue(0, i);
        }
        plans = new ArrayList<>();
    }

    public AnaliseValue[] getAverage() {
        return average;
    }

    public AnaliseValue[] getTotal() {
        return total;
    }

    public AnaliseValue[] getMax() {
        return max;
    }

    public AnaliseValue[] getMin() {
        return min;
    }

    public List<LogMessage>[] getRows() {
        return rows;
    }

    public long[] getCount() {
        return count;
    }

    public long getCountAllRows() {
        return allRows.size();
    }

    public AnaliseValue[] getStd_dev() {
        return std_dev;
    }

    void addMessage(LogMessage msg, int type) {
        Long currentValue = getValueFromType(msg, type);
        if (currentValue == null) {
            currentValue = 0L;
            Log.debug("calculate error for type '" + TYPES[type] + "': trace id = " + msg.getId());
        }
        if (logMessage == null)
            logMessage = msg;
        rows[type].add(msg);
        count[type]++;
        total[type].longValue += currentValue;
        if (currentValue > max[type].longValue)
            max[type].longValue = currentValue;
        if (currentValue < min[type].longValue || min[type].longValue == -1)
            min[type].longValue = currentValue;

    }

    public void calculateValues() {
        for (int i = TIME; i < TYPES.length; i++) {
            calculateValues(i);
        }
    }

    void calculateValues(int type) {
        if (count[type] > 0) {
            average[type].longValue = total[type].longValue / count[type];

            long dispersion = 0;
            if (count[type] > 1) {
                for (LogMessage row : rows[type]) {
                    Long value = getValueFromType(row, type);
                    if (value == null)
                        value = 0L;
                    dispersion += (value - average[type].longValue) * (value - average[type].longValue);
                }
                dispersion = dispersion / count[type] - 1;
                std_dev[type].longValue = (long) Math.sqrt(dispersion);
            }
        }
    }

    public void addMessage(LogMessage msg) {
        allRows.add(msg);
        logMessages.append(msg.getBody()).append("\n");
        if (msg.getPlanText() != null) {
            if (plans.isEmpty())
                plans.add(msg.getPlanText());
            else {
                boolean finded = false;
                for (String plan : plans) {
                    if (plan.contentEquals(msg.getPlanText())) {
                        finded = true;
                        break;
                    }
                }
                if (!finded)
                    plans.add(msg.getPlanText());
            }
        }
        for (int i = TIME; i < TYPES.length; i++) {
            addMessage(msg, i);
        }
    }

    public int countPlans() {
        return plans.size();
    }

    public String getPlanText() {
        if (planText == null) {
            StringBuilder sb = new StringBuilder();
            for (String plan : plans) {
                sb.append(plan).append("\n");
            }
            planText = sb.toString();
        }
        return planText;
    }

    Long getValueFromType(LogMessage msg, int type) {
        switch (type) {
            case READ:
                return msg.getCountReads();
            case FETCH:
                return msg.getCountFetches();
            case WRITE:
                return msg.getCountWrites();
            case MARK:
                return msg.getCountMarks();
            case RSORT:
                return msg.getRamCacheMemory();
            case DSORT:
                return msg.getDiskCacheMemory();
            default:
                return msg.getTimeExecution();
        }
    }

    public AnaliseValue getValueFromTypeAndParam(int type, int param) {
        switch (param) {
            case TOTAL:
                return getTotal()[type];
            case AVG:
                return getAverage()[type];
            case MAX:
                return getMax()[type];
            case MIN:
                return getMin()[type];
            case STD_DEV:
                return getStd_dev()[type];
        }
        return null;
    }

    public class AnaliseValue implements Comparable {
        private long longValue;
        private int percent = -1;
        private Color color;
        private int type;
        private boolean isRoundValue = false;

        public AnaliseValue(long longValue, int type) {
            this.longValue = longValue;
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public long getLongValue() {
            return longValue;
        }

        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public String getDisplayValue(boolean isRound) {
            String displayValue = "";
            if (isRound) {
                switch (type) {
                    case TIME:
                        displayValue = roundTime(longValue);
                        break;
                    case RSORT:
                    case DSORT:
                        displayValue = roundBytes(longValue);
                        break;
                    default:
                        displayValue = roundEvents(longValue);
                        break;

                }
            } else displayValue = delimitValue(longValue, "");
            if (percent != -1)
                displayValue = displayValue + " [" + percent + "%" + "]";
            return displayValue;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public boolean isRoundValue() {
            return isRoundValue;
        }

        public void setRoundValue(boolean roundValue) {
            isRoundValue = roundValue;
        }

        private String delimitValue(long value, String result) {
            if (value >= 1000) {
                String div = String.valueOf(value % 1000);
                while (div.length() < 3)
                    div = "0" + div;
                return delimitValue(value / 1000, " " + div + result);
            } else return value + result;
        }

        public String roundBytes(long value) {
            String suff = "b";
            while (value > 10000 && !suff.contentEquals("pb")) {
                value = value / 1024;
                switch (suff) {
                    case "b":
                        suff = "kb";
                        break;
                    case "kb":
                        suff = "mb";
                        break;
                    case "mb":
                        suff = "gb";
                        break;
                    case "gb":
                        suff = "tb";
                        break;
                    case "tb":
                        suff = "pb";
                        break;
                    default:
                        break;
                }
            }
            String result = delimitValue(value, "") +
                    suff;
            return result;
        }

        public String roundTime(long value) {
            String suff = "ms";
            while (value > 10000 && !suff.contentEquals("days")) {
                switch (suff) {
                    case "ms":
                        suff = "s";
                        value = value / 1000;
                        break;
                    case "s":
                        suff = "m";
                        value = value / 60;
                        break;
                    case "m":
                        suff = "h";
                        value = value / 60;
                        break;
                    case "h":
                        suff = "days";
                        value = value / 24;
                        break;
                    default:
                        break;
                }
            }
            String result = delimitValue(value, "") +
                    suff;
            return result;
        }

        public String roundEvents(long value) {
            String suff = "";
            while (value > 10000 && !suff.contentEquals("P")) {
                value = value / 1000;
                switch (suff) {
                    case "":
                        suff = "K";
                        break;
                    case "K":
                        suff = "M";
                        break;
                    case "M":
                        suff = "G";
                        break;
                    case "G":
                        suff = "T";
                        break;
                    case "T":
                        suff = "P";
                        break;
                    default:
                        break;
                }
            }
            String result = delimitValue(value, "") +
                    suff;
            return result;
        }

        @Override
        public int compareTo(Object o) {
            AnaliseValue secondValue = (AnaliseValue) o;
            return Long.compare(getLongValue(), secondValue.getLongValue());
        }
    }
}
