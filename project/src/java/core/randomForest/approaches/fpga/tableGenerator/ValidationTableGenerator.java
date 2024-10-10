package project.src.java.core.randomForest.approaches.fpga.tableGenerator;

import project.src.java.core.randomForest.approaches.fpga.BasicGenerator;
import project.src.java.core.randomForest.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import project.src.java.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationTableGenerator extends BasicGenerator {

    private final String MODULE_NAME = "validation_table";

    private int precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;
    private int voteCounterBitwidth;

    public void execute(
        int classQuantity,
        int featureQuantity,
        int estimatorsQuantity,
        int classBitwidth,
        ArrayList<BinaryTableEntry> tableEntries,
        SettingsCli settings,
        boolean offlineMode
    ){
        System.out.println("generating validation table");

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

        ReportGenerator reportGenerator = new ReportGenerator();

        /*
         *  the expression calculate the needed bitwidth to hold the votes
         *  the counter can reach the maximum value of votes given by the quantity of trees
         *  because is one vote for each tree
         */

        final double voteCounterBitwidth = Math.ceil(Math.log(estimatorsQuantity) / Math.log(2));
        if ((int) voteCounterBitwidth == 0){
            this.voteCounterBitwidth = 1;
        } else {
            this.voteCounterBitwidth = (int) voteCounterBitwidth;
        }

        String src = "";

        src += generateHeader(MODULE_NAME, offlineMode);
        src += generateIEE754ComparatorFunction(this.precision);
        src += generatePortInstantiation(featureQuantity, classBitwidth, offlineMode);
        src += generateInternalVariables(tableEntries.size(), classQuantity);
        src += generateWireAssign();
        src += generateMainAlways(classQuantity, tableEntries, offlineMode);
        src += generateComputeForestVoteAlways(classQuantity, classBitwidth);

        FileBuilder.execute(
            src, String.format(
                "output/%s_%s_%dtree_%sdeep_run/table.v",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            ),
            false
        );
        ArrayList<Integer> nodeQntByTree = new ArrayList<>();
        nodeQntByTree.add(tableEntries.size());

        reportGenerator.createEntry(
            settings.dataset,
            settings.approach,
            settings.trainingParameters.maxDepth,
            nodeQntByTree
        );

        reportGenerator.generateReport();
    }

    private String generateHeader(String module_name, boolean offlineMode){
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

        String[] integerPrecisionAditionalPorts = {
            "feature",
        };
        ioPorts.addAll(List.of(integerPrecisionAditionalPorts));


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
        int classBitwidth,
        boolean offlineMode
    ){
        int comparedValueBusBitwidth = featureQnt * this.precision;

        String src = "";
        src += tab(1) + "/* ************************ IO ************************ */\n\n";
        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("feature", WIRE, INPUT, comparedValueBusBitwidth, true);

        if (!offlineMode){
            src += tab(1) + "\n";
            src += tab(1) + generatePort("new_table_entry", WIRE, INPUT, 64, true);
            src += tab(1) + generatePort("new_table_entry_counter", WIRE, INPUT, 16, true);
        }
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + "\n";
        src += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT, 1, true);
        src += tab(1) + generatePort("compute_vote_flag", REGISTER, OUTPUT, 1, true);

        return src;
    }

    private String generateInternalVariables(int nodeQuantity, int classQuantity){
        String src = "";

        int tableEntryBitwidth = 0;
        
        src += "\n" + tab(1) + "/* *************************************************** */\n\n";
        tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

        src += tab(1) + generateMemory("nodes_table", REGISTER, NONE, tableEntryBitwidth, nodeQuantity, true);
        src += tab(1) + generatePort("next", REGISTER, NONE, this.tableIndexerBitwidth, true);
        src += "\n";

        for (int index = 0; index <= classQuantity - 1; index++){
            src += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, this.voteCounterBitwidth, true);
        }

        src += "\n";
        src += tab(1) + generatePort("tree_vote_wire", WIRE, NONE, this.tableIndexerBitwidth, true);

        src += tab(1) + generatePort("threshold_wire", WIRE, NONE, this.precision, true);
        src += tab(1) + generatePort("column_wire", WIRE, NONE, this.comparedColumnBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("feature_wire", WIRE, NONE, this.precision, true);
        src += "\n";

        return src;
    }

    private String generateWireAssign(){

        int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

        String src = "";
        src += tab(1) + String.format(
            "assign tree_vote_wire = nodes_table[next][%d:0];\n",
            this.tableIndexerBitwidth - 1
        );
        src += tab(1) + String.format(
            "assign column_wire = nodes_table[next][%d:%d];\n",
            ((this.tableIndexerBitwidth * 2) + this.comparedColumnBitwidth) - 1,
            this.tableIndexerBitwidth * 2
        );

        src += tab(1) + String.format(
            "assign threshold_wire = nodes_table[next][%d:%d];\n",
            tableEntryBitwidth - 1,
            tableEntryBitwidth - this.precision
        );
        src += "\n";

        src += tab(1) + "assign feature_wire = {\n";
        for (int index = this.precision - 1; index >= 0; index--) {
            if (index != 0){
                src += tab(2) + String.format("feature[(column_wire * %d) + %d],\n", this.precision, index);
            } else {
                src += tab(2) + String.format("feature[(column_wire * %d) + %d]\n", this.precision, index);
            }
        }
        src += tab(1) + "};\n";

        return src;
    }

    private String generateMainAlways(int classQuantity, ArrayList<BinaryTableEntry> tableEntries, boolean offlineMode){

        /*************************** RESET BLOCK ****************************/

        String resetBlock = CONDITIONAL_BLOCK;
        String resetBlockExpr = "reset == 1'b1";
        String resetBlockBody = "";

        if (offlineMode){
            for (int index = 0; index < tableEntries.size(); index++){
                int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;
                resetBlockBody += tab(3) + String.format("nodes_table[%d] <= %d'b%s;\n", index, tableEntryBitwidth, tableEntries.get(index).value());
            }
        } else {
            resetBlockBody += tab(3) + "nodes_table[new_table_entry_counter - 1'b1] <= new_table_entry;\n\n";
        }

        resetBlockBody += tab(3) + String.format(
            "next              <= %d'b%s;\n",
            this.tableIndexerBitwidth,
            decimalToBinary(0, this.tableIndexerBitwidth)
        );
        resetBlockBody += tab(3) + "read_new_sample   <= 1'b1;\n";
        resetBlockBody += tab(3) + "compute_vote_flag <= 1'b0;\n";
        resetBlockBody += "\n";

        for (int index = 1; index <= classQuantity; index++){
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, this.voteCounterBitwidth, decimalToBinary(0, this.voteCounterBitwidth));
        }

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("`", resetBlockBody)
                .replace("ind", tab(2));

        /******************* INNER NODE PROCESSING BLOCK ********************/

        String thGreaterThanValueBlock = CONDITIONAL_BLOCK;
        String thGreaterThanValueBlockExpr = "";

        thGreaterThanValueBlockExpr += "IEEE754_comparator(threshold_wire, feature_wire)";

        String thGreaterThanValueBlockBody = tab(6) + String.format(
            "next <= nodes_table[next][%d:%d];\n",
            (this.tableIndexerBitwidth * 2) - 1,
            this.tableIndexerBitwidth
        );

        thGreaterThanValueBlock = thGreaterThanValueBlock
            .replace("x", thGreaterThanValueBlockExpr)
            .replace("`", thGreaterThanValueBlockBody)
            .replace("ind", tab(5));

        String thLessThanValueBlock = CONDITIONAL_ELSE_BLOCK;
        String thLessThanValueBlockBody = tab(6) + String.format(
            "next <= nodes_table[next][%d:%d];\n",
            this.tableIndexerBitwidth - 1,
            0
        );

        thLessThanValueBlock = thLessThanValueBlock
            .replace("y", thLessThanValueBlockBody)
            .replace("ind", tab(5));

        String innerNodeBlock = CONDITIONAL_BLOCK;
        String innerNodeBlockExpr = String.format(
            "~nodes_table[next][%d]",
            ((this.precision * 2) + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2)) - (this.precision * 2)
        );
        String innerNodeBlockBody = thGreaterThanValueBlock + thLessThanValueBlock + "\n";

        innerNodeBlock = innerNodeBlock
            .replace("x", innerNodeBlockExpr)
            .replace("`", innerNodeBlockBody)
            .replace("ind", tab(4));

        /******************** OUTER NODE PROCESSING BLOCK ********************/

        String voteCounterBlocks = "";

        for (int index = 1; index <= classQuantity; index++){
            String voteCounterBlock = CONDITIONAL_BLOCK;
            String voteCounterBlockExpr = String.format("tree_vote_wire == %d'b%s", this.tableIndexerBitwidth, decimalToBinary(index, this.tableIndexerBitwidth));
            String voteCounterBlockBody = String.format("%sclass%d <= class%d + 1'b1;\n",tab(6), index, index);

            voteCounterBlock = voteCounterBlock
                .replace("x", voteCounterBlockExpr)
                .replace("`", voteCounterBlockBody)
                .replace("ind", tab(5));

            if (index == classQuantity){
                voteCounterBlocks += voteCounterBlock;
            } else {
                voteCounterBlocks += voteCounterBlock + "\n";
            }
        }

        String readNewSampleBlock = CONDITIONAL_BLOCK;
        String readNewSampleBlockExpr = String.format(
            "nodes_table[next][%d:%d] == %d'b%s",
            (this.tableIndexerBitwidth * 2) - 1,
            this.tableIndexerBitwidth,
            this.tableIndexerBitwidth,
            decimalToBinary(0, this.tableIndexerBitwidth)
        );
        String readNewSampleBlockBody = "";

        readNewSampleBlockBody += tab(6) + "read_new_sample <= 1'b1;\n";
        readNewSampleBlockBody += tab(6) + "compute_vote_flag <= read_new_sample;\n";

        readNewSampleBlock = readNewSampleBlock
            .replace("x", readNewSampleBlockExpr)
            .replace("`", readNewSampleBlockBody)
            .replace("ind", tab(5));

        String outerNodeBlock = CONDITIONAL_ELSE_BLOCK;
        String outerNodeBlockBody = "";

        outerNodeBlockBody += voteCounterBlocks + "\n\n";
        outerNodeBlockBody += tab(5) + String.format(
            "next <= nodes_table[next][%d:%d];\n\n",
            (this.tableIndexerBitwidth * 2) - 1,
            this.tableIndexerBitwidth
        );
        outerNodeBlockBody += readNewSampleBlock + "\n";

        outerNodeBlock = outerNodeBlock
            .replace("y",outerNodeBlockBody)
            .replace("ind", tab(4));

        String sampleProcessingBlock = CONDITIONAL_BLOCK;
        String sampleProcessingBlockExpr = "read_new_sample == 1'b0";
        String sampleProcessingBlockBody = innerNodeBlock + outerNodeBlock + "\n";

        sampleProcessingBlock = sampleProcessingBlock
            .replace("x", sampleProcessingBlockExpr)
            .replace("`", sampleProcessingBlockBody)
            .replace("ind", tab(3));

        /******************** COUNTER RESET BLOCK ********************/

        String resetCounterBlock = CONDITIONAL_BLOCK;
        String resetCounterBlockExpr = "compute_vote_flag";
        String resetCounterBlockBody = "";

        for (int index = 1; index <= classQuantity; index++){
            String bits = "";
            for (int index2 = 0; index2 <= this.voteCounterBitwidth - 1; index2++){
                bits += "0";
            }
            resetCounterBlockBody += tab(4) + String.format("class%d <= %d'b%s;\n", index, this.voteCounterBitwidth, bits);
        }

        resetCounterBlock = resetCounterBlock
            .replace("x", resetCounterBlockExpr)
            .replace("`", resetCounterBlockBody)
            .replace("ind", tab(3));

        String validationBlock = CONDITIONAL_ELSE_BLOCK;
        String validationBlockBody = "";

        validationBlockBody += tab(3) + "read_new_sample <= 1'b0;\n";
        validationBlockBody += tab(3) + "compute_vote_flag <= read_new_sample;\n\n";

        validationBlock = validationBlock
            .replace("y", resetCounterBlock + "\n" + validationBlockBody + sampleProcessingBlock + "\n")
            .replace("ind", tab(2));


        /******************** MAIN ALWAYS BLOCK ********************/

        String mainAlways = ALWAYS_BLOCK;
        mainAlways = mainAlways
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", resetBlock + validationBlock)
            .replace("ind", tab(1));


        return mainAlways + "\n";
    }

    private String generateComputeForestVoteAlways(int classQuantity, int classBitwidth){

        String src = "";

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQuantity; index++){
            classes.add(String.format("class%d", index + 1));
        }

        for (int index1 = 0; index1 < classQuantity; index1++) {

            String computeMajorClassBlock = CONDITIONAL_BLOCK;
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
                .replace("`", computeMajorClassBlockBody)
                .replace("ind", tab(2));

            if (index1 == classQuantity-1){
                src += computeMajorClassBlock;
            } else {
                src += computeMajorClassBlock + "\n";
            }
        }

        String alwaysBlock = ALWAYS_BLOCK;
        alwaysBlock = alwaysBlock
            .replace("border", "negedge")
            .replace("signal", "read_new_sample")
            .replace("src", src)
            .replace("ind", tab(1));

        return alwaysBlock + "\n" + "endmodule";
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
