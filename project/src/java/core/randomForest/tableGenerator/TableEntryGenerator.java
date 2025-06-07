package project.src.java.core.randomForest.tableGenerator;

import project.src.java.core.BasicGenerator;
import project.src.java.core.randomForest.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.core.randomForest.tableGenerator.tableEntryDataStructures.raw.RawTableEntry;
import project.src.java.core.randomForest.tableGenerator.tableEntryDataStructures.raw.RawTableEntryInnerNode;
import project.src.java.core.randomForest.tableGenerator.tableEntryDataStructures.raw.RawTableEntryOuterNode;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class TableEntryGenerator extends BasicGenerator {
    private final ArrayList<RawTableEntry>    rawTableEntries    = new ArrayList<>();
    private final ArrayList<BinaryTableEntry> binaryTableEntries = new ArrayList<>();

    public ArrayList<BinaryTableEntry> execute(List<Tree> treeList, int precision, boolean oneHot){
        this.rawTableEntries.clear();
        this.binaryTableEntries.clear();
        Node root;
        int offset = 0;

        for (int index = 0; index < treeList.size(); index++) {
            root = treeList.get(index).getRoot();
            generateNodeRawTableEntry(root);

            if (index == treeList.size() - 1) {
                offset = generateBinaryTableEntry(
                    8,
                    32,
                    precision,
                    offset,
                    true,
                    oneHot
                );
            } else {
                offset = generateBinaryTableEntry(
                    8,
                    32,
                    precision,
                    offset,
                    false,
                    oneHot
                );
            }
            rawTableEntries.clear();
        }
        return this.binaryTableEntries;
    }

    private void generateNodeRawTableEntry(Node node){
        if (node instanceof OuterNode){
            OuterNode newNode = (OuterNode) node;

            rawTableEntries.add(
                new RawTableEntryOuterNode(
                    newNode.getId(),
                    newNode.getClassNumber()
                )
            );
        } else {
            InnerNode newNode = (InnerNode) node;
            rawTableEntries.add(
                new RawTableEntryInnerNode(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntry(newNode.getLeftNode());

            rawTableEntries.add(
                new RawTableEntryInnerNode(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntry(newNode.getRightNode());
        }
    }

    private Integer generateBinaryTableEntry(
        int comparedColumnBitwidth,
        int tableIndexerBitwidth,
        int precision,
        int offset,
        boolean lastTreeFlag,
        boolean onehot
    ){
        var identifiers = new ArrayList<Integer>();

        for (int index = 0; index < this.rawTableEntries.size(); index++){
            identifiers.add(this.rawTableEntries.get(index).getId());
        }
        var uniqueIdentifiers = new HashSet<>(Arrays.stream((identifiers.toArray())).toList());

        for (int index1 = 0; index1 < this.rawTableEntries.size(); index1++){
            boolean leftValueReadFLag = false;

            double  threshold      = 0;
            boolean outerNodeFlag  = false;
            int     comparedColumn = 0;
            int     leftNodeIndex  = 0;
            int     rightNodeIndex = 0;

            for (int index2 = 0; index2 < this.rawTableEntries.size(); index2++){
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryInnerNode){
                    if (this.rawTableEntries.get(index2).getId() == index1){
                        if (!leftValueReadFLag){

                            leftNodeIndex = this.rawTableEntries.get(index2 + 1).getId() + offset;
                            threshold = ((RawTableEntryInnerNode) this.rawTableEntries.get(index2)).getThreshold();
                            comparedColumn = ((RawTableEntryInnerNode) this.rawTableEntries.get(index2)).getColumn();

                            leftValueReadFLag = true;
                        } else {
                            rightNodeIndex = this.rawTableEntries.get(index2 + 1).getId() + offset;
                        }
                    }
                }
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryOuterNode) {
                    if (this.rawTableEntries.get(index2).getId() == index1){
                        if (lastTreeFlag){
                            leftNodeIndex = 0;
                        } else {
                            leftNodeIndex = uniqueIdentifiers.size() + offset;
                        }
                        leftValueReadFLag = true;
                        rightNodeIndex = ((RawTableEntryOuterNode) this.rawTableEntries.get(index2)).getNodeClass() + 1;
                        threshold = 0;
                        outerNodeFlag = true;
                        /* calc the maximum value what the bitfield can represent */
                        comparedColumn = ((int) Math.pow(2, comparedColumnBitwidth)) - 1;

                        this.rawTableEntries.remove(index2);
                    }
                }
            }
            BinaryTableEntry entry = null;
            if (onehot && outerNodeFlag) {
                entry = new BinaryTableEntry(
                    toBin(1, 1),
                    toBin(comparedColumn, comparedColumnBitwidth),
                    toBin(leftNodeIndex,  tableIndexerBitwidth),
                    toOneHot(rightNodeIndex, tableIndexerBitwidth),
                    toIEEE754(threshold,  precision)
                );
            } else {
                entry = new BinaryTableEntry(
                    toBin(outerNodeFlag ? 1 : 0, 1),
                    toBin(comparedColumn, comparedColumnBitwidth),
                    toBin(leftNodeIndex,  tableIndexerBitwidth),
                    toBin(rightNodeIndex, tableIndexerBitwidth),
                    toIEEE754(threshold,  precision)
                );
            }
            this.binaryTableEntries.add(entry);
        }
        return uniqueIdentifiers.size() + offset;
    }
}
