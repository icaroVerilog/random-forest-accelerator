package project.src.java.core.randomForest.tableGenerator.tableEntry.raw;

public class RawTableEntryInnerNode extends RawTableEntry {
    private Double threshold;
    private Integer column;

    public RawTableEntryInnerNode(
            Integer id,
            Double threshold,
            Integer column
    ) {
        this.id = id;
        this.column = column;
        this.threshold = threshold;
    }

    public Double getThreshold() {
        return this.threshold;
    }
    public Integer getColumn() {
        return this.column;
    }
}
