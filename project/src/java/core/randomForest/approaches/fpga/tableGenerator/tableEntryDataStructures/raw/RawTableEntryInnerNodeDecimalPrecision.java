package project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.raw;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RawTableEntryInnerNodeDecimalPrecision extends RawTableEntry {
    private final Integer integerThreshold;
    private final Integer decimalThreshold;
    private final Integer column;

    public RawTableEntryInnerNodeDecimalPrecision(
        Integer id,
        String threshold,
        Integer column
    ) {
        this.id = id;
        this.column = column;

        Pattern pattern = Pattern.compile("([0-9]+.0[0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(threshold);
        if (matcher.find()){
            float convertedToFloat = Float.parseFloat(threshold);
            convertedToFloat = (float) Math.floor(convertedToFloat);
            String aux = Float.toString(convertedToFloat);

            var split = aux.split("\\.");
            this.integerThreshold = Integer.valueOf(split[0]);
            this.decimalThreshold = Integer.valueOf(split[1]);
//            System.out.printf("old %s\n", threshold);
//            System.out.printf("new %d.%d\n", integerThreshold, decimalThreshold);
        }
        else {
            var split = threshold.split("\\.");
            this.integerThreshold = Integer.valueOf(split[0]);
            this.decimalThreshold = Integer.valueOf(split[1]);
        }
    }

    public Integer getIntegerThreshold() {
        return this.integerThreshold;
    }
    public Integer getDecimalThreshold() {
        return this.decimalThreshold;
    }
    public Integer getColumn() {
        return this.column;
    }
}
