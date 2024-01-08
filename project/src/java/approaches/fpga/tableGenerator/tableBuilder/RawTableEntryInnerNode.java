package project.src.java.approaches.fpga.tableGenerator.tableBuilder;

import java.util.Arrays;

public class RawTableEntryInnerNode extends RawTableEntry {
    private Integer integerThreshold;
    private Integer decimalThreshold;
    private Integer column;

    public RawTableEntryInnerNode (
        Integer id,
        String threshold,
        Integer column
    ) {
        this.id = id;
        this.column = column;
        var splitted = threshold.split("\\.");
        this.integerThreshold = Integer.valueOf(splitted[0]);
        this.decimalThreshold = Integer.valueOf(splitted[1]);
    }
}
