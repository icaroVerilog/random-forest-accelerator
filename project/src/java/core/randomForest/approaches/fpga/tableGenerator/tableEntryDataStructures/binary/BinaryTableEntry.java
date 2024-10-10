package project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.binary;

public class BinaryTableEntry {
    private final String nodeFlag;
    private final String comparedColumn;
    private final String leftNodeIndex;
    private final String rightNodeIndex;
    private final String threshold;

    public BinaryTableEntry(
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

    public String value(){
        return threshold + nodeFlag + comparedColumn + leftNodeIndex + rightNodeIndex;
    }
}
