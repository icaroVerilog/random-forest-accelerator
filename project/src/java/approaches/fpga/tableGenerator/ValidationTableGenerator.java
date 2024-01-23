package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationTableGenerator extends BasicGenerator {

    /*TODO: refatorar de forma que os dados muito utilizados sejam atributos da classe, e nao parametros das funções */

    private final String MODULE_NAME = "validation_table";

    private int comparedValueBitwidth;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;
    private int voteCounterBitwidth;
    private String precision;

    public void execute(
        int classQuantity,
        int featureQuantity,
        int estimatorsQuantity,
        int classBitwidth,
        ArrayList<BinaryTableEntry> tableEntries,
        ExecutionSettings settings,
        boolean offlineMode
    ){
        System.out.println("generating validation table");

        comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;
        comparedColumnBitwidth = settings.inferenceParameters.fieldsBitwidth.comparedColumn;
        tableIndexerBitwidth   = settings.inferenceParameters.fieldsBitwidth.index;
        precision              = settings.generalParameters.precision;

        /*
         *  the expression calculate the needed bitwidth to hold the votes
         *  the counter can reach the maximum value of votes given by the quantity of trees
         *  because is one vote for each tree
         */
        voteCounterBitwidth = (int) Math.ceil(Math.log(estimatorsQuantity) / Math.log(2));

        String src = "";

        src += generateHeader(MODULE_NAME, offlineMode);
        src += generatePortInstantiation(featureQuantity, classBitwidth);

        src += generateInternalVariables(tableEntries.size(), classQuantity);
        src += "\n";
        src += generateWireAssign();
        src += generateMainAlways(classQuantity, tableEntries, offlineMode);
        src += "\n";
        src += generateComputeForestVoteAlways(classQuantity, classBitwidth);
        src += "\n" + "endmodule";

        FileBuilder.createDir(String.format("FPGA/table/%s", settings.generalParameters.datasetName));
        FileBuilder.execute(src, String.format("FPGA/table/%s/validation_table.v", settings.generalParameters.datasetName));
    }

    private String generateHeader(
        String module_name,
        boolean offlineMode
    ){
        String[] basicIOPorts = {
            "clock",
            "reset",
            "forest_vote",
            "read_new_sample",
            "compute_vote_flag"
        };


        ArrayList<String> ioPorts = new ArrayList<>();

        ioPorts.addAll(List.of(basicIOPorts));

        if (!offlineMode){
            String[] onlineModeAditionalPorts = {
                "new_table_entry",
                "new_table_entry_counter",
            };
            ioPorts.addAll(List.of(onlineModeAditionalPorts));
        }
        if (precision.equals("integer")){
            String[] integerPrecisionAditionalPorts = {
                "feature",
            };
            ioPorts.addAll(List.of(integerPrecisionAditionalPorts));
        }
        if (precision.equals("decimal")){
            String[] decimalPrecisionAditionalPorts = {
                "feature_integer",
                "feature_decimal",
            };
            ioPorts.addAll(List.of(decimalPrecisionAditionalPorts));
        }

        String header = String.format("module %s (\n", module_name);
        String ports = "";

        for (int index = 0; index <= ioPorts.size(); index++){
            if (index == ioPorts.size()){
                ports += ");\n\n";
            }
            else if (index == ioPorts.size() - 1){
                ports += tab(1) + ioPorts.get(index) + "\n";
            }
            else {
                ports += tab(1) + ioPorts.get(index) + ",\n";
            }
        }
        return header + ports;
    }

    private String generatePortInstantiation (
        int featureQnt,
        int classBitwidth
    ){
        int comparedValueBitwidth = featureQnt * comparedColumnBitwidth;

        String src = "";
        src += tab(1) + "/* ************************ IO ************************ */\n\n";
        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";

        if (precision.equals("integer")){
            src += tab(1) + generatePort("feature", WIRE, INPUT, comparedValueBitwidth * 2, true);
        }
        if (precision.equals("decimal")){
            src += tab(1) + generatePort("feature_integer", WIRE, INPUT, comparedValueBitwidth, true);
            src += tab(1) + generatePort("feature_decimal", WIRE, INPUT, comparedValueBitwidth, true);
        }

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

    private String generateInternalVariables(int nodeQuantity, int classQuantity){
        String src = "";

        int tableEntryBitwidth = (comparedValueBitwidth * 2) + comparedColumnBitwidth + (tableIndexerBitwidth * 2) + 1;
        
        src += "\n" + tab(1) + "/* *************************************************** */\n\n";
        src += tab(1) + generateMemory("nodes_table", REGISTER, NONE, tableEntryBitwidth, nodeQuantity, true);
        src += tab(1) + generatePort("next", REGISTER, NONE, tableIndexerBitwidth, true);
        src += "\n";

        for (int index = 0; index <= classQuantity - 1; index++){
            src += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, voteCounterBitwidth, true);
        }

        src += "\n";
        src += tab(1) + generatePort("tree_vote_wire", WIRE, NONE, tableIndexerBitwidth, true);

        if (precision.equals("integer")){
            src += tab(1) + generatePort("threshold_wire", WIRE, NONE, (comparedValueBitwidth * 2), true);
            src += tab(1) + generatePort("column_wire", WIRE, NONE, comparedColumnBitwidth, true);
            src += "\n";

            src += tab(1) + generatePort("feature_wire", WIRE, NONE, (comparedValueBitwidth * 2), true);
        }
        if (precision.equals("decimal")){
            src += tab(1) + generatePort("threshold_integer_wire", WIRE, NONE, comparedValueBitwidth, true);
            src += tab(1) + generatePort("threshold_decimal_wire", WIRE, NONE, comparedValueBitwidth, true);
            src += tab(1) + generatePort("column_wire", WIRE, NONE, comparedColumnBitwidth, true);
            src += "\n";

            src += tab(1) + generatePort("feature_integer_wire", WIRE, NONE, comparedValueBitwidth, true);
            src += tab(1) + generatePort("feature_decimal_wire", WIRE, NONE, comparedValueBitwidth, true);
        }
        return src;
    }

    private String generateWireAssign(){

        int tableEntryBitwidth = (comparedValueBitwidth * 2) + comparedColumnBitwidth + (tableIndexerBitwidth * 2) + 1;

        String src = "";
        src += tab(1) + "assign tree_vote_wire = nodes_table[next][12:0];\n";
        src += tab(1) + "assign column_wire = nodes_table[next][38:26];\n";

        if (precision.equals("integer")){
            src += tab(1) + "assign threshold_wire = nodes_table[next][63:40];\n";
            src += "\n";

            src += tab(1) + "assign feature_wire = {\n";
            for (int index = (comparedValueBitwidth * 2) - 1; index >= 0; index--) {
                src += tab(2) + String.format("feature[(column_wire * %d) + %d],\n", (comparedValueBitwidth * 2), index);
            }
            src += tab(1) + "};\n";
        }
        if (precision.equals("decimal")){
            src += tab(1) + String.format(
                "assign threshold_integer_wire = nodes_table[next][%d:%d];\n",
                tableEntryBitwidth - 1,
                tableEntryBitwidth - comparedValueBitwidth
            );
            src += tab(1) + String.format(
                    "assign threshold_decimal_wire = nodes_table[next][%d:%d];\n",
                    (tableEntryBitwidth - comparedValueBitwidth) - 1,
                    (tableEntryBitwidth - comparedValueBitwidth) - comparedValueBitwidth
            );
            src += "\n";

            src += tab(1) + "assign feature_integer_wire = {\n";
            for (int index = comparedValueBitwidth - 1; index >= 0; index--) {
                src += tab(2) + String.format("feature_integer[(column_wire * %d) + %d],\n", comparedValueBitwidth, index);
            }
            src += tab(1) + "};\n";
            src += tab(1) + "assign feature_decimal_wire = {\n";
            for (int index = comparedValueBitwidth - 1; index >= 0; index--) {
                src += tab(2) + String.format("feature_decimal[(column_wire * %d) + %d],\n", comparedValueBitwidth, index);
            }
            src += tab(1) + "};\n";
        }
        return src;
    }

    private String generateMainAlways(int classQuantity, ArrayList<BinaryTableEntry> tableEntries, boolean offlineMode){

        /*************************** RESET BLOCK ****************************/

        String resetBlock = CONDITIONAL2;
        String resetBlockExpr = "reset == 1'b1";
        String resetBlockBody = "";

        if (offlineMode){
            for (int index = 0; index < tableEntries.size(); index++){
                int tableEntryBitwidth = (comparedValueBitwidth * 2) + comparedColumnBitwidth + (tableIndexerBitwidth * 2) + 1;
                resetBlockBody += String.format("%snodes_table[%d] <= %d'b%s;\n", tab(3), index, tableEntryBitwidth, tableEntries.get(index).value());
            }
        } else {
            resetBlockBody += tab(3) + "nodes_table[new_table_entry_counter - 1'b1] <= new_table_entry;\n\n";
        }

        resetBlockBody += tab(3) + String.format(
            "next              <= %d'b%s;\n",
            tableIndexerBitwidth,
            decimalToBinary(0, tableIndexerBitwidth)
        );
        resetBlockBody += tab(3) + "read_new_sample   <= 1'b1;\n";
        resetBlockBody += tab(3) + "compute_vote_flag <= 1'b0;\n";
        resetBlockBody += "\n";

        for (int index = 1; index <= classQuantity; index++){
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, voteCounterBitwidth, decimalToBinary(0, voteCounterBitwidth));
        }

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("y", resetBlockBody)
                .replace("ind", tab(2));

        /******************* INNER NODE PROCESSING BLOCK ********************/

        String thGreaterThanValueBlock = CONDITIONAL2;
        String thGreaterThanValueBlockExpr = "";

        if (precision.equals("integer")){
            thGreaterThanValueBlockExpr += "threshold_wire >= feature_wire";
        }
        if (precision.equals("decimal")){
            thGreaterThanValueBlockExpr += "\n";
            thGreaterThanValueBlockExpr += tab(6) + "(threshold_integer_wire > feature_integer_wire) ||\n";
            thGreaterThanValueBlockExpr += tab(6) + "(\n";
            thGreaterThanValueBlockExpr += tab(7) + "(threshold_integer_wire == feature_integer_wire) && \n";
            thGreaterThanValueBlockExpr += tab(7) + "(threshold_decimal_wire >= feature_decimal_wire) \n";
            thGreaterThanValueBlockExpr += tab(6) + ")\n" + tab(5);
        }



        String thGreaterThanValueBlockBody = tab(6) + String.format(
            "next <= nodes_table[next][%d:%d];\n",
            (tableIndexerBitwidth * 2) - 1,
            tableIndexerBitwidth
        );

        thGreaterThanValueBlock = thGreaterThanValueBlock
            .replace("x", thGreaterThanValueBlockExpr)
            .replace("y", thGreaterThanValueBlockBody)
            .replace("ind", tab(5));

        String thLessThanValueBlock = CONDITIONAL_ELSE;
        String thLessThanValueBlockBody = tab(6) + String.format(
            "next <= nodes_table[next][%d:%d];\n",
            tableIndexerBitwidth - 1,
            0
        );

        thLessThanValueBlock = thLessThanValueBlock
                .replace("y", thLessThanValueBlockBody)
                .replace("ind", tab(5));

        String innerNodeBlock = CONDITIONAL2;
        String innerNodeBlockExpr = String.format(
            "~nodes_table[next][%d]",
            ((comparedValueBitwidth * 2) + comparedColumnBitwidth + (tableIndexerBitwidth * 2)) - (comparedValueBitwidth * 2)
        );
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
        String readNewSampleBlockExpr = String.format(
            "nodes_table[next][%d:%d] == %d'b%s",
            (tableIndexerBitwidth * 2) - 1,
            tableIndexerBitwidth,
            tableIndexerBitwidth,
            decimalToBinary(0, tableIndexerBitwidth)
        );
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
        outerNodeBlockBody += tab(5) + String.format(
            "next <= nodes_table[next][%d:%d];\n\n",
            (tableIndexerBitwidth * 2) - 1,
            tableIndexerBitwidth
        );
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

    private String decimalToBinary(int decimalValue, int numberOfBits){
        StringBuilder binaryValue = new StringBuilder();

        for (int i = numberOfBits - 1; i >= 0; i--) {
            int bit = (decimalValue >> i) & 1;
            binaryValue.append(bit);
        }
        return binaryValue.toString();
    }
}
