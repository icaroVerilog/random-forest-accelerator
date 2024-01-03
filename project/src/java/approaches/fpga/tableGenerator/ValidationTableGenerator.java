package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValidationTableGenerator extends BasicGenerator {
    public void execute(List<Tree> trees, Integer classQnt, Integer featureQnt, String dataset){

        /*
        *  a variavel classbitwidth deverá ser ajustada de forma a se encaixar com o numero
        *  de classes que o dataset tem
        *
        *   nodeQnt tambem deverá ser ajustado deacordo com o numero de árvores
        *
        * */

        String SRC = "";
        SRC += generateHeader("validation_table");
        SRC += generatePortInstantiation(featureQnt, 12, 8);
        SRC += generateInternalVariables(14, classQnt, 16);
        SRC += "\n";
        SRC += generateWireAssign();
        SRC += generateMainAlways(classQnt, 16);
        SRC += "\n";
        SRC += generateComputeForestVoteAlways(classQnt, 8);
        SRC += "\n" + "endmodule";

        FileBuilder.createDir(String.format("FPGA/table/%s", dataset));
        FileBuilder.execute(SRC, String.format("FPGA/table/%s/validation_table.v", dataset));
    }

    private String generateHeader(String module_name){
        String[] IoNames = {
                "clock",
                "reset",
                "forest_vote",
                "read_new_sample",
                "ft_exponent",
                "ft_fraction",
                "new_table_entry",
                "new_table_entry_counter",
                "compute_vote_flag"
        };

        String header = String.format("module %s (\n", module_name);
        String IO = IntStream.range(0, IoNames.length)
            .mapToObj(index -> tab(1) + IoNames[index])
            .collect(Collectors.joining(",\n")
        );

        IO += "\n);\n";

        return header + IO;
    }

    private String generatePortInstantiation(int featureQnt, int featureBitwidth, int forestVoteBitwidth){

        int valueBitwidth = featureQnt * featureBitwidth;

        String SRC = "";
        SRC += tab(1) + "/* ************************ IO ************************ */\n\n";
        SRC += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        SRC += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        SRC += "\n";
        SRC += tab(1) + generatePort("ft_exponent", WIRE, INPUT, valueBitwidth, true);
        SRC += tab(1) + generatePort("ft_fraction", WIRE, INPUT, valueBitwidth, true);
        SRC += tab(1) + generatePort("new_table_entry", WIRE, INPUT, 64, true);
        SRC += tab(1) + generatePort("new_table_entry_counter", WIRE, INPUT, 13, true);
        SRC += "\n";
        SRC += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, forestVoteBitwidth, true);
        SRC += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT, 1, true);
        SRC += tab(1) + generatePort("compute_vote_flag", REGISTER, OUTPUT, 1, true);

        return SRC;
    }

    private String generateInternalVariables(int nodeQnt, int classQnt, int classBitwidth){
        String SRC = "";
        SRC += "\n" + tab(1) + "/* *************************************************** */\n\n";
        SRC += tab(1) + generatePortBus("nodes_table", REGISTER, NONE, 64, nodeQnt, true);
        SRC += tab(1) + generatePort("next", REGISTER, NONE, 13, true);
        SRC += "\n";

        for (int index = 0; index <= classQnt - 1; index++){
            SRC += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, classBitwidth, true);
        }

        SRC += "\n";
        SRC += tab(1) + generatePort("tree_vote_wire", WIRE, NONE, 13, true);
        SRC += tab(1) + generatePort("th_exponent_wire", WIRE, NONE, 12, true);
        SRC += tab(1) + generatePort("ft_exponent_wire", WIRE, NONE, 12, true);
        SRC += tab(1) + generatePort("column_wire", WIRE, NONE, 13, true);
        SRC += tab(1) + generatePort("column_value_wire", WIRE, NONE, 12, true);

        return SRC;
    }

    private String generateWireAssign(){
        String SRC = "";
        SRC += tab(1) + "assign tree_vote_wire = nodes_table[next][12:0];\n";
        SRC += tab(1) + "assign th_exponent_wire = nodes_table[next][63:52];\n";
        SRC += tab(1) + "assign column_wire = nodes_table[next][38:26];\n";
        SRC += tab(1) + """
                        assign column_value_wire = {
                                ft_exponent[(column_wire * 12) + 11],
                                ft_exponent[(column_wire * 12) + 10],
                                ft_exponent[(column_wire * 12) + 9],
                                ft_exponent[(column_wire * 12) + 8],
                                ft_exponent[(column_wire * 12) + 7],
                                ft_exponent[(column_wire * 12) + 6],
                                ft_exponent[(column_wire * 12) + 5],
                                ft_exponent[(column_wire * 12) + 4],
                                ft_exponent[(column_wire * 12) + 3],
                                ft_exponent[(column_wire * 12) + 2],
                                ft_exponent[(column_wire * 12) + 1],
                                ft_exponent[(column_wire * 12) + 0]
                            };
                        """;
        return SRC;
    }

    private String generateMainAlways(int classQnt, int classBitwidth){

        /*************************** RESET BLOCK ****************************/

        String resetBlock = CONDITIONAL2;
        String resetBlockExpr = "reset == 1'b1";
        String resetBlockBody = "";

        resetBlockBody += tab(3) + "nodes_table[new_table_entry_counter - 1'b1] <= new_table_entry;\n\n";

        for (int index = 1; index <= classQnt; index++){
            String bits = "";

            for (int index2 = 0; index2 <= classBitwidth - 1; index2++){
                bits += "0";
            }
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, classBitwidth, bits);
        }

        resetBlockBody += "\n";
        resetBlockBody += tab(3) + "next              <= 13'b0000000000000;\n";
        resetBlockBody += tab(3) + "read_new_sample   <= 1'b1;\n";
        resetBlockBody += tab(3) + "compute_vote_flag <= 1'b0;\n";

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("y", resetBlockBody)
                .replace("ind", tab(2));

        /******************* INNER NODE PROCESSING BLOCK ********************/

        String thGreaterThanValueBlock = CONDITIONAL2;
        String thGreaterThanValueBlockExpr = "th_exponent_wire >= column_value_wire";
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

        for (int index = 1; index <= classQnt; index++){
            String voteCounterBlock = CONDITIONAL2;
            String voteCounterBlockExpr = String.format("tree_vote_wire == %d'b%s", classBitwidth, decimalToBinary(index, classBitwidth));
            String voteCounterBlockBody = String.format("%sclass%d <= class%d + 1'b1;\n",tab(6), index, index);

            voteCounterBlock = voteCounterBlock
                    .replace("x", voteCounterBlockExpr)
                    .replace("y", voteCounterBlockBody)
                    .replace("ind", tab(5));

            if (index == classQnt){
                voteCounterBlocks += voteCounterBlock;
            }
            else {
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

        for (int index = 1; index <= classQnt; index++){
            String bits = "";
            for (int index2 = 0; index2 <= classBitwidth - 1; index2++){
                bits += "0";
            }
            resetCounterBlockBody += tab(4) + String.format("class%d <= %d'b%s;\n", index, classBitwidth, bits);
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
            .replace("y", validationBlockBody + resetCounterBlock + "\n" + sampleProcessingBlock + "\n")
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

    private String generateComputeForestVoteAlways(int classQnt, int forestVoteBitwidth){

        String SRC = "";

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQnt; index++){
            classes.add(String.format("class%d", index + 1));
        }

        for (int index1 = 0; index1 < classQnt; index1++) {

            String computeMajorClassBlock = CONDITIONAL2;
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
            System.out.println(computeMajorClassBlockExpr);
            int position = computeMajorClassBlockExpr.lastIndexOf("&&");
            computeMajorClassBlockExpr = computeMajorClassBlockExpr.substring(0, position-1);

            computeMajorClassBlockBody = String.format(
                "%sforest_vote == %d'b%s;\n",
                tab(3),
                forestVoteBitwidth,
                decimalToBinary(index1+1, forestVoteBitwidth)
            );

            computeMajorClassBlock = computeMajorClassBlock
                    .replace("x", computeMajorClassBlockExpr)
                    .replace("y", computeMajorClassBlockBody)
                    .replace("ind", tab(2));;

            if (index1 == classQnt-1){
                SRC += computeMajorClassBlock;
            } else {
                SRC += computeMajorClassBlock + "\n";
            }
        }

        String alwaysBlock = ALWAYS_BLOCK2;
        alwaysBlock = alwaysBlock
            .replace("border", "negedge")
            .replace("signal", "read_new_sample")
            .replace("src", SRC)
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
