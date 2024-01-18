package project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw;

public class RawTableEntryInnerNodeDecimalPrecision extends RawTableEntry {
    private Integer integerThreshold;
    private Integer decimalThreshold;
    private Integer column;

    public RawTableEntryInnerNodeDecimalPrecision(
        Integer id,
        String threshold,
        Integer column
    ) {
        this.id = id;
        this.column = column;
        var split = threshold.split("\\.");
        this.integerThreshold = Integer.valueOf(split[0]);
        this.decimalThreshold = Integer.valueOf(split[1]);
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
