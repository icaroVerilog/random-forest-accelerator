package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValidationTableGenerator extends BasicGenerator {

    private final Integer VOTE_COUNTER_BITWIDTH = 16;

    public void execute(
        int classQuantity,
        int featureQuantity,
        int classBitwidth,
        ArrayList<BinaryTableEntry> tableEntries,
        String datasetName,
        boolean offlineMode
    ){
        System.out.println("generating validation table");

        /*
         *  the expression calculate the needed bitwidth to hold the votes
         *  the counter can reach the maximum value of votes given by the quantity of trees
         *  because is one vote for each tree
         */
//        int voteCounterBitwidth = (int) Math.ceil(Math.log(treeList.size()) / Math.log(2));

        var configs = new Configurations();

        String src = "";

        src += generateHeader("validation_table", offlineMode);
        src += generatePortInstantiation(
            featureQuantity,
            configs.INTEGER_PART_BITWIDTH,
            configs.DECIMAL_PART_BITWIDTH,
            classBitwidth
        );

        src += generateInternalVariables(
            201,
            classQuantity,
            configs.INDEX_BITWIDTH,
            configs.INTEGER_PART_BITWIDTH,
            configs.DECIMAL_PART_BITWIDTH,
            configs.COMPARATED_COLUMN_BITWIDTH
        );

        src += "\n";
        src += generateWireAssign();
        src += generateMainAlways(classQuantity, configs.INDEX_BITWIDTH, VOTE_COUNTER_BITWIDTH, tableEntries, offlineMode);
        src += "\n";
        src += generateComputeForestVoteAlways(classQuantity, classBitwidth);
        src += "\n" + "endmodule";

        FileBuilder.createDir(String.format("FPGA/table/%s", datasetName));
        FileBuilder.execute(src, String.format("FPGA/table/%s/validation_table.v", datasetName));
    }

    private String generateHeader(String module_name, boolean offlineMode){

        String[] ioPorts = {
            "clock",
            "reset",
            "forest_vote",
            "read_new_sample",
            "feature_integer",
            "feature_decimal",
            "new_table_entry",
            "new_table_entry_counter",
            "compute_vote_flag"
        };

        if (offlineMode) {
            ioPorts = new String[]{
                "clock",
                "reset",
                "forest_vote",
                "read_new_sample",
                "feature_integer",
                "feature_decimal",
                "compute_vote_flag"
            };
        }

        String header = String.format("module %s (\n", module_name);
        String ports = "";

        for (int index = 0; index <= ioPorts.length; index++){
            if (index == ioPorts.length){
                ports += ");\n\n";
            }
            else if (index == ioPorts.length - 1){
                ports += tab(1) + ioPorts[index] + "\n";
            }
            else {
                ports += tab(1) + ioPorts[index] + ",\n";
            }
        }
        return header + ports;
    }

    private String generatePortInstantiation (
        int featureQnt,
        int exponentFeatureBitwidth,
        int fractionFeatureBitwidth,
        int classBitwidth
    ){
        int exponentValueBitwidth = featureQnt * exponentFeatureBitwidth;
        int fractionValueBitwidth = featureQnt * fractionFeatureBitwidth;

        String src = "";
        src += tab(1) + "/* ************************ IO ************************ */\n\n";
        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("feature_integer", WIRE, INPUT, exponentValueBitwidth, true);
        src += tab(1) + generatePort("feature_decimal", WIRE, INPUT, fractionValueBitwidth, true);
        src += tab(1) + "\n";
        src += tab(1) + generatePort("new_table_entry", WIRE, INPUT, 64, true);
        src += tab(1) + generatePort("new_table_entry_counter", WIRE, INPUT, 16, true);
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + "\n";
        src += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT, 1, true);
        src += tab(1) + generatePort("compute_vote_flag", REGISTER, OUTPUT, 1, true);

        return src;
    }

    private String generateInternalVariables(
        int nodeQuantity,
        int classQuantity,
        int tableIndexerBitwidth,
        int integerPartFeatureBitwidth,
        int decimalPartFeatureBitwidth,
        int comparatedColumnBitwidth
    ){
        String src = "";

        src += "\n" + tab(1) + "/* *************************************************** */\n\n";
        src += tab(1) + generatePortBus("nodes_table", REGISTER, NONE, 64, nodeQuantity, true);
        src += tab(1) + generatePort("next", REGISTER, NONE, tableIndexerBitwidth, true);
        src += "\n";

        for (int index = 0; index <= classQuantity - 1; index++){
            src += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, VOTE_COUNTER_BITWIDTH, true);
        }

        src += "\n";
        src += tab(1) + generatePort("tree_vote_wire", WIRE, NONE, tableIndexerBitwidth, true);
        src += tab(1) + generatePort("threshold_integer_wire", WIRE, NONE, integerPartFeatureBitwidth, true);
        src += tab(1) + generatePort("threshold_decimal_wire", WIRE, NONE, decimalPartFeatureBitwidth, true);
        src += tab(1) + generatePort("column_wire", WIRE, NONE, comparatedColumnBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("feature_integer_wire", WIRE, NONE, integerPartFeatureBitwidth, true);
        src += tab(1) + generatePort("feature_decimal_wire", WIRE, NONE, decimalPartFeatureBitwidth, true);

        return src;
    }

    private String generateWireAssign(){
        String src = "";
        src += tab(1) + "assign tree_vote_wire = nodes_table[next][12:0];\n";
        src += tab(1) + "assign threshold_integer_wire = nodes_table[next][63:52];\n";
        src += tab(1) + "assign threshold_decimal_wire = nodes_table[next][51:40];\n";
        src += tab(1) + "assign column_wire = nodes_table[next][38:26];\n";
        src += "\n";
        src += tab(1) + """
                        assign feature_integer_wire = {
                                feature_integer[(column_wire * 12) + 11],
                                feature_integer[(column_wire * 12) + 10],
                                feature_integer[(column_wire * 12) + 9],
                                feature_integer[(column_wire * 12) + 8],
                                feature_integer[(column_wire * 12) + 7],
                                feature_integer[(column_wire * 12) + 6],
                                feature_integer[(column_wire * 12) + 5],
                                feature_integer[(column_wire * 12) + 4],
                                feature_integer[(column_wire * 12) + 3],
                                feature_integer[(column_wire * 12) + 2],
                                feature_integer[(column_wire * 12) + 1],
                                feature_integer[(column_wire * 12) + 0]
                            };
                        """;
        src += "\n";
        src += tab(1) + """
                        assign feature_decimal_wire = {
                                feature_decimal[(column_wire * 12) + 11],
                                feature_decimal[(column_wire * 12) + 10],
                                feature_decimal[(column_wire * 12) + 9],
                                feature_decimal[(column_wire * 12) + 8],
                                feature_decimal[(column_wire * 12) + 7],
                                feature_decimal[(column_wire * 12) + 6],
                                feature_decimal[(column_wire * 12) + 5],
                                feature_decimal[(column_wire * 12) + 4],
                                feature_decimal[(column_wire * 12) + 3],
                                feature_decimal[(column_wire * 12) + 2],
                                feature_decimal[(column_wire * 12) + 1],
                                feature_decimal[(column_wire * 12) + 0]
                            };
                        """;
        return src;
    }

    private String generateMainAlways(
        int classQuantity,
        int tableIndexerBitwidth,
        int voteCounterBitwidth,
        ArrayList<BinaryTableEntry> tableEntries,
        boolean offlineMode
    ){

        /*************************** RESET BLOCK ****************************/

        String resetBlock = CONDITIONAL2;
        String resetBlockExpr = "reset == 1'b1";
        String resetBlockBody = "";

        if (offlineMode){
            for (int index = 0; index < tableEntries.size(); index++){
                resetBlockBody += String.format("%snodes_table[%d] <= 64'b%s;\n", tab(3), index, tableEntries.get(index).value());
            }
        } else {
            resetBlockBody += tab(3) + "nodes_table[new_table_entry_counter - 1'b1] <= new_table_entry;\n\n";
        }

        resetBlockBody += tab(3) + "next              <= 13'b0000000000000;\n";
        resetBlockBody += tab(3) + "read_new_sample   <= 1'b1;\n";
        resetBlockBody += tab(3) + "compute_vote_flag <= 1'b0;\n";
        resetBlockBody += "\n";

        for (int index = 1; index <= classQuantity; index++){
            String bits = "";

            for (int index2 = 0; index2 <= voteCounterBitwidth - 1; index2++){
                bits += "0";
            }
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, voteCounterBitwidth, bits);
        }

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("y", resetBlockBody)
                .replace("ind", tab(2));

        /******************* INNER NODE PROCESSING BLOCK ********************/

        String thGreaterThanValueBlock = CONDITIONAL2;
        String thGreaterThanValueBlockExpr = "\n";
        thGreaterThanValueBlockExpr += tab(6) + "(threshold_integer_wire > feature_integer_wire) ||\n";
        thGreaterThanValueBlockExpr += tab(6) + "(\n";
        thGreaterThanValueBlockExpr += tab(7) + "(threshold_integer_wire == feature_integer_wire) && \n";
        thGreaterThanValueBlockExpr += tab(7) + "(threshold_decimal_wire >= feature_decimal_wire) \n";
        thGreaterThanValueBlockExpr += tab(6) + ")\n" + tab(5);

        String thGreaterThanValueBlockBody = tab(6) + "next <= nodes_table[next][25:13];\n";

        thGreaterThanValueBlock = thGreaterThanValueBlock
                .replace("x", thGreaterThanValueBlockExpr)
                .replace("y", thGreaterThanValueBlockBody)
                .replace("ind", tab(5));

        String thLessThanValueBlock = CONDITIONAL_ELSE;
        String thLessThanValueBlockBody = tab(6) + "next <= nodes_table[next][12:0];\n";

        thLessThanValueBlock = thLessThanValueBlock
                .replace("y", thLessThanValueBlockBody)
                .replace("ind", tab(5));

        String innerNodeBlock = CONDITIONAL2;
        String innerNodeBlockExpr = "~nodes_table[next][39]";
        String innerNodeBlockBody = thGreaterThanValueBlock + thLessThanValueBlock + "\n";

        innerNodeBlock = innerNodeBlock
                .replace("x", innerNodeBlockExpr)
                .replace("y", innerNodeBlockBody)
                .replace("ind", tab(4));

        /******************** OUTER NODE PROCESSING BLOCK ********************/

        String voteCounterBlocks = "";

        for (int index = 1; index <= classQuantity; index++){
            String voteCounterBlock = CONDITIONAL2;
            String voteCounterBlockExpr = String.format("tree_vote_wire == %d'b%s", tableIndexerBitwidth, decimalToBinary(index, tableIndexerBitwidth));
            String voteCounterBlockBody = String.format("%sclass%d <= class%d + 1'b1;\n",tab(6), index, index);

            voteCounterBlock = voteCounterBlock
                    .replace("x", voteCounterBlockExpr)
                    .replace("y", voteCounterBlockBody)
                    .replace("ind", tab(5));

            if (index == classQuantity){
                voteCounterBlocks += voteCounterBlock;
            } else {
                voteCounterBlocks += voteCounterBlock + "\n";
            }
        }

        String readNewSampleBlock = CONDITIONAL2;
        String readNewSampleBlockExpr = "nodes_table[next][25:13] == 13'b0000000000000";
        String readNewSampleBlockBody = "";

        readNewSampleBlockBody += tab(6) + "read_new_sample <= 1'b1;\n";
        readNewSampleBlockBody += tab(6) + "compute_vote_flag <= read_new_sample;\n";

        readNewSampleBlock = readNewSampleBlock
            .replace("x", readNewSampleBlockExpr)
            .replace("y", readNewSampleBlockBody)
            .replace("ind", tab(5)
        );

        String outerNodeBlock = CONDITIONAL_ELSE;
        String outerNodeBlockBody = "";

        outerNodeBlockBody += voteCounterBlocks + "\n\n";
        outerNodeBlockBody += tab(5) + "next <= nodes_table[next][25:13];\n\n";
        outerNodeBlockBody += readNewSampleBlock + "\n";

        outerNodeBlock = outerNodeBlock
                .replace("y",outerNodeBlockBody)
                .replace("ind", tab(4));

        String sampleProcessingBlock = CONDITIONAL2;
        String sampleProcessingBlockExpr = "read_new_sample == 1'b0";
        String sampleProcessingBlockBody = innerNodeBlock + outerNodeBlock + "\n";

        sampleProcessingBlock = sampleProcessingBlock
                .replace("x", sampleProcessingBlockExpr)
                .replace("y", sampleProcessingBlockBody)
                .replace("ind", tab(3));

        /******************** COUNTER RESET BLOCK ********************/

        String resetCounterBlock = CONDITIONAL2;
        String resetCounterBlockExpr = "compute_vote_flag";
        String resetCounterBlockBody = "";

        for (int index = 1; index <= classQuantity; index++){
            String bits = "";
            for (int index2 = 0; index2 <= voteCounterBitwidth - 1; index2++){
                bits += "0";
            }
            resetCounterBlockBody += tab(4) + String.format("class%d <= %d'b%s;\n", index, voteCounterBitwidth, bits);
        }

        resetCounterBlock = resetCounterBlock
            .replace("x", resetCounterBlockExpr)
            .replace("y", resetCounterBlockBody)
            .replace("ind", tab(3)
        );

        String validationBlock = CONDITIONAL_ELSE;
        String validationBlockBody = "";

        validationBlockBody += tab(3) + "read_new_sample <= 1'b0;\n";
        validationBlockBody += tab(3) + "compute_vote_flag <= read_new_sample;\n\n";

        validationBlock = validationBlock
            .replace("y", resetCounterBlock + "\n" + validationBlockBody + sampleProcessingBlock + "\n")
            .replace("ind", tab(2)
        );

        /******************** MAIN ALWAYS BLOCK ********************/

        String mainAlways = ALWAYS_BLOCK2;
        mainAlways = mainAlways
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", resetBlock + validationBlock)
            .replace("ind", tab(1)
        );
        return mainAlways;
    }

    private String generateComputeForestVoteAlways(int classQuantity, int classBitwidth){

        String src = "";

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQuantity; index++){
            classes.add(String.format("class%d", index + 1));
        }

        for (int index1 = 0; index1 < classQuantity; index1++) {

            String computeMajorClassBlock = CONDITIONAL2;
            String computeMajorClassBlockExpr = "";
            String computeMajorClassBlockBody = "";

            for (int index2 = 0; index2 < classQuantity; index2++) {
                if (Objects.equals(classes.get(index1), classes.get(index2))) {
                    continue;
                }
                else {
                    computeMajorClassBlockExpr += String.format("(%s > %s) && ", classes.get(index1), classes.get(index2));
                }
            }

            int position = computeMajorClassBlockExpr.lastIndexOf("&&");
            computeMajorClassBlockExpr = computeMajorClassBlockExpr.substring(0, position-1);

            computeMajorClassBlockBody = String.format(
                "%sforest_vote <= %d'b%s;\n",
                tab(3),
                classBitwidth,
                decimalToBinary(index1+1, classBitwidth)
            );

            computeMajorClassBlock = computeMajorClassBlock
                    .replace("x", computeMajorClassBlockExpr)
                    .replace("y", computeMajorClassBlockBody)
                    .replace("ind", tab(2));;

            if (index1 == classQuantity-1){
                src += computeMajorClassBlock;
            } else {
                src += computeMajorClassBlock + "\n";
            }
        }

        String alwaysBlock = ALWAYS_BLOCK2;
        alwaysBlock = alwaysBlock
            .replace("border", "negedge")
            .replace("signal", "read_new_sample")
            .replace("src", src)
            .replace("ind", tab(1)
        );
        return alwaysBlock;
    }

    private String decimalToBinary(int decimalValue, int numberOfBits) {
        StringBuilder binaryValue = new StringBuilder();

        for (int i = numberOfBits - 1; i >= 0; i--) {
            int bit = (decimalValue >> i) & 1;
            binaryValue.append(bit);
        }
        return binaryValue.toString();
    }
}
