package project.src.java.core.randomForest.approaches.fpga.tableGenerator;

import project.src.java.core.randomForest.approaches.fpga.BasicGenerator;
import project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntry;
import project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryInnerNode;
import project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryOuterNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class TableEntryGenerator extends BasicGenerator {
    private final ArrayList<RawTableEntry>    rawTableEntries    = new ArrayList<>();
    private final ArrayList<BinaryTableEntry> binaryTableEntries = new ArrayList<>();

    private int precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;

    public ArrayList<BinaryTableEntry> execute(List<Tree> treeList, SettingsCli settings, boolean offlineMode){

        switch (settings.inferenceParameters.precision){
            case "double":
                this.precision = DOUBLE_PRECISION;
                break;
            case "normal":
                this.precision = NORMAL_PRECISION;
                break;
            case "half":
                this.precision = HALF_PRECISION;
                break;
            default:
                this.precision = 0;
                break;
        }

        this.comparedColumnBitwidth = 8;
        this.tableIndexerBitwidth   = 32;

        Node root;
        int offset = 0;

        for (int index = 0; index < treeList.size(); index++) {
            root = treeList.get(index).getRoot();
            generateNodeRawTableEntry(root);

            if (index == treeList.size() - 1) {
                offset = generateBinaryTableEntry(
                    offset,
                    true
                );
            } else {
                offset = generateBinaryTableEntry(offset,
                    false
                );
            }
            rawTableEntries.clear();
        }

        if (!offlineMode){
            String table = "";

            for (int index = 0; index < this.binaryTableEntries.size(); index++){
                if (index == this.binaryTableEntries.size() - 1){
                    table += this.binaryTableEntries.get(index).value();
                } else {
                    table += this.binaryTableEntries.get(index).value() + "\n";
                }
            }
            FileBuilder.execute(table, String.format("FPGA/%s_table_run/table_entries.bin", settings.dataset), false);
            return null;
        } else {
            return this.binaryTableEntries;
        }
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

    private Integer generateBinaryTableEntry(int offset, boolean lastTreeFlag){
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
                        comparedColumn = ((int) Math.pow(2, this.comparedColumnBitwidth)) - 1;

                        this.rawTableEntries.remove(index2);
                    }
                }
            }
            BinaryTableEntry entry = new BinaryTableEntry(
                toBin(outerNodeFlag ? 1 : 0, 1),
                toBin(comparedColumn, this.comparedColumnBitwidth),
                toBin(leftNodeIndex,  this.tableIndexerBitwidth),
                toBin(rightNodeIndex, this.tableIndexerBitwidth),
                toIEEE754(threshold,  this.precision)
            );
            this.binaryTableEntries.add(entry);
        }
        return uniqueIdentifiers.size() + offset;
    }
}
