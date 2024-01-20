package project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary;

public class BinaryTableEntryIntegerPrecision extends BinaryTableEntry {
    protected String threshold;

    public BinaryTableEntryIntegerPrecision(
            String nodeFlag,
            String comparedColumn,
            String leftNodeIndex,
            String rightNodeIndex,
            String threshold
    ) {
        this.nodeFlag = nodeFlag;
        this.comparedColumn = comparedColumn;
        this.leftNodeIndex = leftNodeIndex;
        this.rightNodeIndex = rightNodeIndex;
        this.threshold = threshold;
    }

    @Override
    public String value(){
        return threshold + nodeFlag + comparedColumn + leftNodeIndex + rightNodeIndex;
    }
}
