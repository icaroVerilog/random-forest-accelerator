package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntryDecimalPrecision;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntryIntegerPrecision;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntry;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryInnerNodeDecimalPrecision;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryInnerNodeIntegerPrecision;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.raw.RawTableEntryOuterNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


public class TableEntryGenerator extends BasicGenerator {
    private final ArrayList<RawTableEntry>    rawTableEntries    = new ArrayList<>();
    private final ArrayList<BinaryTableEntry> binaryTableEntries = new ArrayList<>();

    public ArrayList<BinaryTableEntry> execute(
            List<Tree> treeList,
            Table settings,
            boolean offlineMode
    ){
        Node root;
        int offset = 0;

        if (settings.precision.equals("decimal")) {
            for (int index = 0; index < treeList.size(); index++) {
                root = treeList.get(index).getRoot();
                generateNodeRawTableEntryDecimalPrecision(root);

                if (index == treeList.size() - 1) {
                    offset = generateBinaryTableEntryDecimalPrecision(
                        offset,
                        settings.inferenceParameters.fieldsBitwidth.comparedValue,
                        settings.inferenceParameters.fieldsBitwidth.comparedColumn,
                        settings.inferenceParameters.fieldsBitwidth.index,
                        true
                    );
                } else {
                    offset = generateBinaryTableEntryDecimalPrecision(
                        offset,
                        settings.inferenceParameters.fieldsBitwidth.comparedValue,
                        settings.inferenceParameters.fieldsBitwidth.comparedColumn,
                        settings.inferenceParameters.fieldsBitwidth.index,
                        false
                    );
                }
                rawTableEntries.clear();
            }
        }

        else if (settings.precision.equals("integer")){
            for (int index = 0; index < treeList.size(); index++) {
                root = treeList.get(index).getRoot();
                generateNodeRawTableEntryIntegerPrecision(root);

                if (index == treeList.size() - 1) {
                    offset = generateBinaryTableEntryIntegerPrecision(
                        offset,
                        settings.inferenceParameters.fieldsBitwidth.comparedValue,
                        settings.inferenceParameters.fieldsBitwidth.comparedColumn,
                        settings.inferenceParameters.fieldsBitwidth.index,
                        true
                    );
                } else {
                    offset = generateBinaryTableEntryIntegerPrecision(offset,
                    settings.inferenceParameters.fieldsBitwidth.comparedValue,
                    settings.inferenceParameters.fieldsBitwidth.comparedColumn,
                    settings.inferenceParameters.fieldsBitwidth.index,
                    false);
                }
                rawTableEntries.clear();
            }
        }


        if (!offlineMode){
            String table = "";

            for (int index = 0; index < binaryTableEntries.size(); index++){
                if (index == binaryTableEntries.size() - 1){
                    table += binaryTableEntries.get(index).value();
                } else {
                    table += binaryTableEntries.get(index).value() + "\n";
                }
            }
            FileBuilder.execute(table, String.format("FPGA/%s_table_run/table_entries.bin", settings.dataset));
        }
        return binaryTableEntries;
    }

    private void generateNodeRawTableEntryDecimalPrecision(Node node){
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
//            System.out.println(newNode.getComparisson().getThreshold().toString());
            rawTableEntries.add(
                new RawTableEntryInnerNodeDecimalPrecision(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold().toString(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntryDecimalPrecision(newNode.getLeftNode());
//            System.out.println(newNode.getComparisson().getThreshold().toString());
            rawTableEntries.add(
                new RawTableEntryInnerNodeDecimalPrecision(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold().toString(),
                    newNode.getComparisson().getColumn()
                )
            );
            generateNodeRawTableEntryDecimalPrecision(newNode.getRightNode());
        }
    }

    private void generateNodeRawTableEntryIntegerPrecision(Node node){
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
                    new RawTableEntryInnerNodeIntegerPrecision(
                            newNode.getId(),
                            (int) Math.floor(newNode.getComparisson().getThreshold()),
                            newNode.getComparisson().getColumn()
                    )
            );
            generateNodeRawTableEntryIntegerPrecision(newNode.getLeftNode());

            rawTableEntries.add(
                    new RawTableEntryInnerNodeIntegerPrecision(
                            newNode.getId(),
                            (int) Math.floor(newNode.getComparisson().getThreshold()),
                            newNode.getComparisson().getColumn()
                    )
            );
            generateNodeRawTableEntryIntegerPrecision(newNode.getRightNode());
        }
    }

    private Integer generateBinaryTableEntryDecimalPrecision(
        int offset,
        int comparedValueBitwidth,
        int comparedColumnBitwidth,
        int tableIndexerBitwidth,
        boolean lastTreeFlag
    ){

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
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryInnerNodeDecimalPrecision){
                    if (this.rawTableEntries.get(index2).getId() == index){
                        if (!leftValueReadFLag){

                            leftNodeIndex = this.rawTableEntries.get(index2+1).getId() + offset;
                            thresholdInteger = ((RawTableEntryInnerNodeDecimalPrecision) this.rawTableEntries.get(index2)).getIntegerThreshold();
                            thresholdDecimal = ((RawTableEntryInnerNodeDecimalPrecision) this.rawTableEntries.get(index2)).getDecimalThreshold();
                            comparedColumn   = ((RawTableEntryInnerNodeDecimalPrecision) this.rawTableEntries.get(index2)).getColumn();

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
                        /* calc the maximum value what the bitfield can represent */
                        comparedColumn = ((int) Math.pow(2, comparedColumnBitwidth)) - 1;
                        this.rawTableEntries.remove(index2);
                    }
                }
            }
            BinaryTableEntryDecimalPrecision entry = new BinaryTableEntryDecimalPrecision(
                generateBinaryNumber(outerNodeFlag ? 1 : 0, 1),
                generateBinaryNumber(comparedColumn,   comparedColumnBitwidth),
                generateBinaryNumber(leftNodeIndex,    tableIndexerBitwidth),
                generateBinaryNumber(rightNodeIndex,   tableIndexerBitwidth),
                generateBinaryNumber(thresholdInteger, comparedValueBitwidth),
                generateBinaryNumber(thresholdDecimal, comparedValueBitwidth)
            );
            binaryTableEntries.add(entry);
        }
        return uniqueIdentifiers.size() + offset;
    }

    private Integer generateBinaryTableEntryIntegerPrecision(
            int offset,
            int comparedValueBitwidth,
            int comparedColumnBitwidth,
            int tableIndexerBitwidth,
            boolean lastTreeFlag
    ){
        var identifiers = new ArrayList<Integer>();

        for (int index = 0; index < this.rawTableEntries.size(); index++){
            identifiers.add(this.rawTableEntries.get(index).getId());
        }
        var uniqueIdentifiers = new HashSet<>(Arrays.stream((identifiers.toArray())).toList());

        for (int index = 0; index < this.rawTableEntries.size(); index++){
            boolean leftValueReadFLag = false;

            int     threshold      = 0;
            boolean outerNodeFlag  = false;
            int     comparedColumn = 0;
            int     leftNodeIndex  = 0;
            int     rightNodeIndex = 0;

            for (int index2 = 0; index2 < this.rawTableEntries.size(); index2++){
                if (this.rawTableEntries.get(index2) instanceof RawTableEntryInnerNodeIntegerPrecision){
                    if (this.rawTableEntries.get(index2).getId() == index){
                        if (!leftValueReadFLag){

                            leftNodeIndex = this.rawTableEntries.get(index2+1).getId() + offset;
                            threshold = ((RawTableEntryInnerNodeIntegerPrecision) this.rawTableEntries.get(index2)).getThreshold();
                            comparedColumn = ((RawTableEntryInnerNodeIntegerPrecision) this.rawTableEntries.get(index2)).getColumn();

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
                        threshold = 0;
                        outerNodeFlag = true;
                        /* calc the maximum value what the bitfield can represent */
                        comparedColumn = ((int) Math.pow(2, comparedColumnBitwidth)) - 1;

                        this.rawTableEntries.remove(index2);
                    }
                }
            }
            BinaryTableEntryIntegerPrecision entry = new BinaryTableEntryIntegerPrecision(
                    generateBinaryNumber(outerNodeFlag ? 1 : 0, 1),
                    generateBinaryNumber(comparedColumn, comparedColumnBitwidth),
                    generateBinaryNumber(leftNodeIndex, tableIndexerBitwidth),
                    generateBinaryNumber(rightNodeIndex, tableIndexerBitwidth),
                    generateBinaryNumber(threshold, comparedValueBitwidth)
            );
            binaryTableEntries.add(entry);
        }
        return uniqueIdentifiers.size() + offset;
    }
}
