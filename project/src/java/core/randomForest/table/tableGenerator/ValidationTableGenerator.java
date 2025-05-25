package project.src.java.core.randomForest.table.tableGenerator;

import project.src.java.core.BasicGenerator;
import project.src.java.core.randomForest.table.TableEntryGenerator;
import project.src.java.core.randomForest.table.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import project.src.java.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationTableGenerator extends BasicGenerator {

    private int precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;
    private int voteCounterBitwidth;

    public void execute(
        int classQnt,
        int featureQnt,
        List<Tree> treeList,
        SettingsCli settings
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

        // TODO: Ajustar para serem parâmetros variáveis
        this.comparedColumnBitwidth = 8;
        this.tableIndexerBitwidth   = 32;

        ReportGenerator reportGenerator = new ReportGenerator();
        TableEntryGenerator tableEntryGenerator = new TableEntryGenerator();
        ArrayList<BinaryTableEntry> tableEntries = tableEntryGenerator.execute(treeList, this.precision, false);

        /*
         *  the expression calculate the needed bitwidth to hold the votes
         *  the counter can reach the maximum value of votes given by the quantity of trees
         *  because is one vote for each tree
         */

        final double voteCounterBitwidth = Math.ceil(Math.log(treeList.size()) / Math.log(2));
        if ((int) voteCounterBitwidth == 0){
            this.voteCounterBitwidth = 1;
        } else {
            this.voteCounterBitwidth = (int) voteCounterBitwidth;
        }

        String src = "";

        src += generateHeader();
        src += generateIEE754ComparatorFunction(this.precision);
        src += generatePortInstantiation(featureQnt, classQnt);
        src += generateInternalVariables(tableEntries.size(), classQnt);
        src += generateWireAssign(featureQnt);
        src += generateMainAlways(classQnt, tableEntries);
        src += generateComputeForestVoteAlways(classQnt);

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

        reportGenerator.generateReport(
            settings.dataset,
            settings.approach,
            settings.trainingParameters.maxDepth,
            nodeQntByTree
        );
    }

    private String generateHeader(){
        String src = "";

        src += "module validation_table (\n";
        src += tab(1) + "clock,\n";
        src += tab(1) + "reset,\n";
        src += tab(1) + "forest_vote,\n";
        src += tab(1) + "compute_vote,\n";
        src += tab(1) + "features\n";
        src += ");\n";

        return src;
    }

    private String generatePortInstantiation (int featureQnt, int classQnt){
        int comparedValueBusBitwidth = featureQnt * this.precision;
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));

        String src = "";
        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("features", WIRE, INPUT, comparedValueBusBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += tab(1) + "\n";

        return src;
    }

    private String generateInternalVariables(int nodeQuantity, int classQnt){
        String src = "";

        int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

        src += tab(1) + generateMemory("nodes_table", REGISTER, NONE, tableEntryBitwidth, nodeQuantity, true);
        src += tab(1) + generatePort("next", REGISTER, NONE, this.tableIndexerBitwidth, true);
        src += tab(1) + generatePort("counter", REGISTER, NONE, 2, true);
        src += "\n";

        for (int index = 0; index <= classQnt - 1; index++){
            src += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, this.voteCounterBitwidth, true);
        }
        src += tab(1) + generatePort("ready", REGISTER, NONE, 1, true);
        src += "\n";
        src += tab(1) + generatePort("tree_vote_w", WIRE, NONE, this.tableIndexerBitwidth, true);
        src += tab(1) + generatePort("threshold_w", WIRE, NONE, this.precision, true);
        src += tab(1) + generatePort("column_w", WIRE, NONE, this.comparedColumnBitwidth, true);
        src += tab(1) + generatePort("feature_w", WIRE, NONE, this.precision, true);
        src += "\n";

        return src;
    }

    private String generateWireAssign(int featureQnt){

        int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

        String src = "";
        src += tab(1) + String.format(
            "assign tree_vote_w = nodes_table[next][%d:0];\n",
            this.tableIndexerBitwidth - 1
        );
        src += tab(1) + String.format(
            "assign column_w = nodes_table[next][%d:%d];\n",
            ((this.tableIndexerBitwidth * 2) + this.comparedColumnBitwidth) - 1,
            this.tableIndexerBitwidth * 2
        );

        src += tab(1) + String.format(
            "assign threshold_w = nodes_table[next][%d:%d];\n",
            tableEntryBitwidth - 1,
            tableEntryBitwidth - this.precision
        );
        src += "\n";

        src += tab(1) + "assign feature_w = {\n";
        for (int index = (this.precision * featureQnt) - 1; index >= (this.precision * featureQnt) - this.precision; index--) {
            if (index != (this.precision * featureQnt) - this.precision){
                src += tab(2) + String.format("features[%d - (column_w * %d)],\n", index, this.precision);
            } else {
                src += tab(2) + String.format("features[%d - (column_w * %d)]\n",  index, this.precision);
            }
        }
        src += tab(1) + "};\n";

        return src;
    }

    private String generateMainAlways(
        int classQnt,
        ArrayList<BinaryTableEntry> tableEntries
    ){
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));

        /*************************** RESET BLOCK ****************************/

        String resetBlock = CONDITIONAL_BLOCK;
        String resetBlockExpr = "reset";
        String resetBlockBody = "";

        for (int index = 0; index < tableEntries.size(); index++){
            int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;
            resetBlockBody += tab(3) + String.format("nodes_table[%d] <= %d'b%s;\n", index, tableEntryBitwidth, tableEntries.get(index).value());
        }
        resetBlockBody += "\n";
        resetBlockBody += tab(3) + String.format(
            "next <= %d'b%s;\n",
            this.tableIndexerBitwidth,
            toBin(0, this.tableIndexerBitwidth)
        );
        resetBlockBody += tab(3) + String.format(
            "forest_vote <= %d'b%s;\n",
            classBitwidth,
            toBin(0, classBitwidth)
        );
        resetBlockBody += tab(3) + "ready <= 1'b1;\n";
        resetBlockBody += tab(3) + "compute_vote <= 1'b0;\n";

        for (int index = 1; index <= classQnt; index++){
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, this.voteCounterBitwidth, decimalToBinary(0, this.voteCounterBitwidth));
        }

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("`", resetBlockBody)
                .replace("ind", tab(2));

        /******************* INNER NODE PROCESSING BLOCK ********************/

        String thGreaterThanValueBlock = CONDITIONAL_BLOCK;
        String thGreaterThanValueBlockExpr = "";

        thGreaterThanValueBlockExpr += "IEEE754_comparator(threshold_w, feature_w)";

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
            "!nodes_table[next][%d]",
            ((this.precision * 2) + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2)) - (this.precision * 2)
        );
        String innerNodeBlockBody = thGreaterThanValueBlock + "\n" + thLessThanValueBlock;

        innerNodeBlock = innerNodeBlock
            .replace("x", innerNodeBlockExpr)
            .replace("`", innerNodeBlockBody)
            .replace("ind", tab(4));

        /******************** OUTER NODE PROCESSING BLOCK ********************/

        String voteCounterBlocks = "";

        for (int index = 1; index <= classQnt; index++){
            String voteCounterBlock = CONDITIONAL_BLOCK;
            String voteCounterBlockExpr = String.format("tree_vote_w == %d'b%s", this.tableIndexerBitwidth, decimalToBinary(index, this.tableIndexerBitwidth));
            String voteCounterBlockBody = String.format("%sclass%d <= class%d + 1'b1;\n",tab(6), index, index);

            voteCounterBlock = voteCounterBlock
                .replace("x", voteCounterBlockExpr)
                .replace("`", voteCounterBlockBody)
                .replace("ind", tab(5));

            if (index == classQnt){
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

        readNewSampleBlockBody += tab(6) + "compute_vote <= 1'b1;\n";
        readNewSampleBlockBody += tab(6) + "ready <= 1'b0;\n";

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
        String sampleProcessingBlockExpr = "ready";
        String sampleProcessingBlockBody = innerNodeBlock + "\n" + outerNodeBlock;

        sampleProcessingBlock = sampleProcessingBlock
            .replace("x", sampleProcessingBlockExpr)
            .replace("`", sampleProcessingBlockBody)
            .replace("ind", tab(3));

        /******************** COUNTER RESET BLOCK ********************/

        String resetCounterBlock = CONDITIONAL_BLOCK;
        String resetCounterBlockExpr = "compute_vote";
        String resetCounterBlockBody = "";

        for (int index = 1; index <= classQnt; index++){
            String bits = "";
            for (int index2 = 0; index2 <= this.voteCounterBitwidth - 1; index2++){
                bits += "0";
            }
            resetCounterBlockBody += tab(4) + String.format("class%d <= %d'b%s;\n", index, this.voteCounterBitwidth, bits);
        }
        resetCounterBlockBody += tab(4) + "compute_vote <= 1'b0;\n";

        resetCounterBlock = resetCounterBlock
            .replace("x", resetCounterBlockExpr)
            .replace("`", resetCounterBlockBody)
            .replace("ind", tab(3));

        /******************** SYNC BLOCK ********************/

        String syncBlock = CONDITIONAL_BLOCK;
        String syncBlockExpr = "counter != 2'b10 && ready == 1'b0";
        String syncBlockBody = tab(4) + "counter <= counter + 1'b1;\n";

        syncBlock = syncBlock
            .replace("x", syncBlockExpr)
            .replace("`", syncBlockBody)
            .replace("ind", tab(3));

        String syncBlockElse = CONDITIONAL_ELSE_BLOCK;
        String syncBlockElseBody = "";

        syncBlockElseBody += tab(4) + "ready <= 1'b1;\n";
        syncBlockElseBody += tab(4) + "counter <= 2'b00;\n";

        syncBlockElse = syncBlockElse
            .replace("y", syncBlockElseBody)
            .replace("ind", tab(3));

        /******************** VALIDATION BLOCK ********************/

        String validationBlock = CONDITIONAL_ELSE_BLOCK;

        validationBlock = validationBlock
            .replace("y", resetCounterBlock + "\n\n" + syncBlock + "\n" + syncBlockElse + "\n" + sampleProcessingBlock + "\n")
            .replace("ind", tab(2));


        /******************** MAIN ALWAYS BLOCK ********************/

        String mainAlways = ALWAYS_BLOCK;
        mainAlways = mainAlways
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", resetBlock + "\n" + validationBlock)
            .replace("ind", tab(1));

        return mainAlways;
    }

    private String generateComputeForestVoteAlways(int classQnt){
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));

        String src = "";

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQnt; index++){
            classes.add(String.format("class%d", index + 1));
        }

        for (int index1 = 0; index1 < classQnt; index1++) {

            String computeMajorClassBlock = CONDITIONAL_BLOCK;
            String computeMajorClassBlockExpr = "";
            String computeMajorClassBlockBody = "";

            for (int index2 = 0; index2 < classQnt; index2++) {
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

                src += computeMajorClassBlock + "\n";
        }

        String alwaysBlock = ALWAYS_BLOCK;
        alwaysBlock = alwaysBlock
            .replace("border", "posedge")
            .replace("signal", "compute_vote")
            .replace("src", src)
            .replace("ind", tab(1));

        return alwaysBlock + "endmodule";
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
