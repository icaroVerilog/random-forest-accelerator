package project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.raw;

public class RawTableEntryInnerNodeIntegerPrecision extends RawTableEntry {
    private Integer threshold;
    private Integer column;

    public RawTableEntryInnerNodeIntegerPrecision(
            Integer id,
            Integer threshold,
            Integer column
    ) {
        this.id = id;
        this.column = column;
        this.threshold = threshold;
    }

    public Integer getThreshold() {
        return this.threshold;
    }
    public Integer getColumn() {
        return this.column;
    }
}
