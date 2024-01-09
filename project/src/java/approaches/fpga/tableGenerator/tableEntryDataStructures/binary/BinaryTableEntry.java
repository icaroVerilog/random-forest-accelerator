package project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary;

public record BinaryTableEntry(
        String integerThreshold,
        String decimalThreshold,
        String nodeFlag,
        String comparedColumn,
        String leftNodeIndex,
        String rightNodeIndex
) {
    public String value(){
        System.out.println(integerThreshold + decimalThreshold + nodeFlag + comparedColumn + leftNodeIndex + rightNodeIndex);
        return integerThreshold + decimalThreshold + nodeFlag + comparedColumn + leftNodeIndex + rightNodeIndex;
    }
}
