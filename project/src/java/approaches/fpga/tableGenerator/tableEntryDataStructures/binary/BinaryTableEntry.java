package project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary;

public abstract class BinaryTableEntry {
    protected String nodeFlag;
    protected String comparedColumn;
    protected String leftNodeIndex;
    protected String rightNodeIndex;

    public abstract String value();
}
