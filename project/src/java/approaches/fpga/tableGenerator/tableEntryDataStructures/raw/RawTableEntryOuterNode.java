package project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw;

public class RawTableEntryOuterNode extends RawTableEntry {
    private Integer nodeClass;

    public RawTableEntryOuterNode(Integer id, Integer nodeClass) {
        this.id = id;
        this.nodeClass = nodeClass;
    }
    public Integer getNodeClass(){ return this.nodeClass; }
}
