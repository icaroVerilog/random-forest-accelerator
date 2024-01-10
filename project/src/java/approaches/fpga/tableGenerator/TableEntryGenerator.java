package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntry;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryInnerNode;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryOuterNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class TableEntryGenerator extends BasicGenerator {

    private final ArrayList<RawTableEntry>    rawTableEntries    = new ArrayList<>();
    private final ArrayList<BinaryTableEntry> binaryTableEntries = new ArrayList<>();

    public ArrayList<BinaryTableEntry> execute(List<Tree> treeList, String datasetName, boolean offlineMode){
        Node teste;
        int offset = 0;

        for (int index = 0; index < treeList.size(); index++) {
            teste = treeList.get(index).getRoot();
            generateNodeRawTableEntry(teste);

            if (index == treeList.size() - 1){
                offset = generateBinaryTableEntry(offset, true);
            } else {
                offset = generateBinaryTableEntry(offset, false);
            }
            this.rawTableEntries.clear();
        }
        if (offlineMode){
            String table = "";
            for (int index = 0; index < this.binaryTableEntries.size(); index++){
                if (index == binaryTableEntries.size() - 1){
                    table += this.binaryTableEntries.get(index).value();
                } else {
                    table += this.binaryTableEntries.get(index).value() + "\n";
                }
            }
            FileBuilder.execute(table, String.format("FPGA/table/%s/table_entries.bin", datasetName));
        }

        return this.binaryTableEntries;
    }

    private void generateNodeRawTableEntry(Node node){
        if (node instanceof OuterNode){
            OuterNode newNode = (OuterNode) node;
            this.rawTableEntries.add(
                new RawTableEntryOuterNode(
                    newNode.getId(),
                    newNode.getClassNumber()
                )
            );
        }
        else {
            InnerNode newNode = (InnerNode) node;

            this.rawTableEntries.add(
                new RawTableEntryInnerNode(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold().toString(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntry(newNode.getLeftNode());

            this.rawTableEntries.add(
                new RawTableEntryInnerNode(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold().toString(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntry(newNode.getRightNode());
        }
    }

    private Integer generateBinaryTableEntry(int offset, boolean lastTreeFlag){
        var identifiers = new ArrayList<Integer>();

        for (int index = 0; index < this.rawTableEntries.size(); index++){
            identifiers.add(this.rawTableEntries.get(index).getId());
        }
        var uniqueIdentifiers = new HashSet<>(Arrays.stream((identifiers.toArray())).toList());

        for (int index = 0; index < this.rawTableEntries.size(); index++){
            boolean leftValueReadFLag = false;

            int     thresholdInteger = 0;
            int     thresholdDecimal = 0;
            boolean outerNodeFlag    = false;
            int     comparedColumn   = 0;
            int     leftNodeIndex    = 0;
            int     rightNodeIndex   = 0;

            for (int index2 = 0; index2 < this.rawTableEntries.size(); index2++){
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryInnerNode){
                    if (this.rawTableEntries.get(index2).getId() == index){
                        if (!leftValueReadFLag){

                            leftNodeIndex = this.rawTableEntries.get(index2+1).getId() + offset;
                            thresholdInteger = ((RawTableEntryInnerNode) this.rawTableEntries.get(index2)).getIntegerThreshold();
                            thresholdDecimal = ((RawTableEntryInnerNode) this.rawTableEntries.get(index2)).getDecimalThreshold();
                            comparedColumn = ((RawTableEntryInnerNode) this.rawTableEntries.get(index2)).getColumn();

                            leftValueReadFLag = true;
                        } else {
                            rightNodeIndex = this.rawTableEntries.get(index2+1).getId() + offset;
                        }
                    }
                }
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryOuterNode) {
                    if (this.rawTableEntries.get(index2).getId() == index){
                        if (lastTreeFlag){
                            leftNodeIndex = 0;
                        } else {
                            leftNodeIndex = uniqueIdentifiers.size() + offset;
                        }
                        leftValueReadFLag = true;
                        rightNodeIndex = ((RawTableEntryOuterNode) this.rawTableEntries.get(index2)).getNodeClass() + 1;
                        thresholdInteger = 0;
                        thresholdDecimal = 0;
                        outerNodeFlag = true;
                        comparedColumn = 8191; /* valor maximo capaz de armazenar em 13 bits */

                        this.rawTableEntries.remove(index2);
                    }
                }
            }
//            System.out.printf("node %d | th inteiro: %d | th decimal: %d | flag: %b | coluna: %d | esquerdo: %d | direito: %d\n", index + offset, thresholdInteger, thresholdDecimal, outerNodeFlag, comparedColumn, leftNodeIndex, rightNodeIndex);
            BinaryTableEntry entry = new BinaryTableEntry(
                generateBinaryNumber(thresholdInteger, 12),
                generateBinaryNumber(thresholdDecimal, 12),
                generateBinaryNumber(outerNodeFlag ? 1 : 0, 1),
                generateBinaryNumber(comparedColumn, 13),
                generateBinaryNumber(leftNodeIndex, 13),
                generateBinaryNumber(rightNodeIndex, 13)
            );
            this.binaryTableEntries.add(entry);
        }
        return uniqueIdentifiers.size() + offset;
    }
}
