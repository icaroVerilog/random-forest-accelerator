package project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.binary;

public class BinaryTableEntryDecimalPrecision extends BinaryTableEntry {
    protected String integerThreshold;
    protected String decimalThreshold;

    public BinaryTableEntryDecimalPrecision(
            String nodeFlag,
            String comparedColumn,
            String leftNodeIndex,
            String rightNodeIndex,
            String integerThreshold,
            String decimalThreshold
    ) {
        this.nodeFlag = nodeFlag;
        this.comparedColumn = comparedColumn;
        this.leftNodeIndex = leftNodeIndex;
        this.rightNodeIndex = rightNodeIndex;
        this.integerThreshold = integerThreshold;
        this.decimalThreshold = decimalThreshold;
    }

    @Override
    public String value(){
        return integerThreshold + decimalThreshold + nodeFlag + comparedColumn + leftNodeIndex + rightNodeIndex;
    }
}
