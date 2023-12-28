package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValidationTableGenerator extends BasicGenerator {
    public void execute(){

        String SRC = "";
        SRC += generateHeader("validation_table");
        SRC += generatePortInstantiation(48, 48, 10);
        SRC += generateInternalVariables(14, 3, 8);
//        SRC += generateMainAlways(3, 8);
//        SRC += generateComputeTreeVoteAlways(3, 8);

        FileBuilder.createDir("FPGA/table");
        FileBuilder.execute(SRC, "FPGA/table/validation.v");
    }

    private String generateHeader(String module_name){
        String[] IoNames = {"clock", "reset", "forest_vote", "read_new_sample", "ft_exponent", "ft_fraction", "new_table_entry", "new_table_entry_counter", "compute_vote_flag"};

        String header = String.format("module %s (\n", module_name);
        String IO = IntStream.range(0, IoNames.length)
            .mapToObj(index -> tab(1) + IoNames[index])
            .collect(Collectors.joining(",\n")
        );

        IO += "\n);\n";

        return header + IO;
    }

    private String generatePortInstantiation(int ftExponentBitwidth, int ftFractionBitwidth, int forestVoteBitwidth){
        String SRC = "";
        SRC += tab(1) + "/* ************************ IO ************************ */\n\n";
        SRC += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        SRC += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        SRC += tab(1) + generatePort("ft_exponent", WIRE, INPUT, ftExponentBitwidth, true);
        SRC += tab(1) + generatePort("ft_fraction", WIRE, INPUT, ftFractionBitwidth, true);
        SRC += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT, 1, true);
        SRC += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, forestVoteBitwidth, true);

        return SRC;
    }

    private String generateInternalVariables(int nodeQuantity, int classQuantity, int classBitwidth){
        String SRC = "";
        SRC += "\n" + tab(1) + "/* *************************************************** */\n\n";
        SRC += tab(1) + generatePortBus("nodes_table", REGISTER, NONE, 64, nodeQuantity, true);
        SRC += tab(1) + generatePort("next", REGISTER, NONE, 13, true);
        SRC += tab(1) + generatePort("tree_vote", REGISTER, NONE, 13, true);

        for (int index = 0; index <= classQuantity - 1; index++){
            SRC += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, classBitwidth, true);
        }

        SRC += tab(1) + generatePort("compute_vote_flag", REGISTER, NONE, 1, true);
        SRC += tab(1) + generatePort("th_exponent_wire", WIRE, NONE, 12, true);
        SRC += tab(1) + generatePort("ft_exponent_wire", WIRE, NONE, 12, true);
        SRC += tab(1) + generatePort("column_wire", WIRE, NONE, 13, true);
        SRC += tab(1) + generatePort("column_value_wire", WIRE, NONE, 12, true);

        return SRC;
    }

//    private String generateWireAssign(){
//        String SRC = "";
//
//    }

    private String generateMainAlways(int classQuantity, int classBitwidth){

        /* *************** Reset Block *************** */

        String resetBlock = CONDITIONAL2;
        String resetBlockExpr = "reset == 1'b1";
        String resetBlockBody = "";

        resetBlockBody += tab(3) + "nodes_table[new_table_entry_counter - 1'b1] <= new_table_entry;\n\n";

        for (int index = 1; index <= classQuantity; index++){
            String bits = "";

            for (int index2 = 0; index2 <= classBitwidth - 1; index2++){
                bits += "0";
            }
            resetBlockBody += tab(3) + String.format("class%d <= %d'b%s;\n", index, classBitwidth, bits);
        }

        resetBlockBody += "\n";
        resetBlockBody += tab(3) + "next              <= 13'b0000000000000;\n";
        resetBlockBody += tab(4) + "tree_vote         <= 13'b0000000000000;\n";
        resetBlockBody += tab(4) + "read_new_sample   <= 1'b1;\n";
        resetBlockBody += tab(4) + "compute_vote_flag <= 1'b0;\n";

        resetBlock = resetBlock
                .replace("x", resetBlockExpr)
                .replace("y", resetBlockBody)
                .replace("ind", tab(2));


        /* *************** Validation Block *************** */

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


        String outerNodeBlock = CONDITIONAL_ELSE;
        String outerNodeBlockBody = "";

        outerNodeBlockBody += tab(5) + "tree_vote <= nodes_table[next][12:0];\n";
        outerNodeBlockBody += tab(5) + "next  <= nodes_table[next][25:13];\n";
        outerNodeBlockBody += tab(5) + "compute_vote_flag <= ~compute_vote_flag;\n";

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

        String validationBlock = CONDITIONAL_ELSE;
        String validationBlockBody = "";

        validationBlockBody += tab(3) + "read_new_sample <= 1'b0;\n\n";

        validationBlock = validationBlock
            .replace("y", validationBlockBody + sampleProcessingBlock + "\n")
            .replace("ind", tab(2)
        );

        String mainAlways = ALWAYS_BLOCK;
        mainAlways = mainAlways
            .replace("src", resetBlock + validationBlock)
            .replace("ind", tab(1)
        );


        return mainAlways;
    }

    private String generateComputeTreeVoteAlways(int classQuantity, int classBitwidth){
        String nextPositionVerifierBlock = CONDITIONAL2;
        String nextPositionVerifierBlockExpr = "next == 13'b0000000000000";
        String nextPositionVerifierBlockBody = "read_new_sample <= 1'b1;";

        nextPositionVerifierBlock = nextPositionVerifierBlock
                .replace("x", nextPositionVerifierBlockExpr)
                .replace("y", tab(3) + nextPositionVerifierBlockBody + "\n")
                .replace("ind", tab(2));

        String treeVoteBlocks = "";

        for (int index = 1; index <= classQuantity; index++){
            String treeVoteBlock = CONDITIONAL2;
            String treeVoteBlockExpr = String.format("tree_vote == %d'b%s", classBitwidth, decimalToBinary(index, classBitwidth));
            String treeVoteBlockBody = String.format("%sclass%d <= class%d + 1'b1;\n",tab(3), index, index);

            treeVoteBlock = treeVoteBlock
                    .replace("x", treeVoteBlockExpr)
                    .replace("y", treeVoteBlockBody)
                    .replace("ind", tab(2));

            if (index == classQuantity){
                treeVoteBlocks += treeVoteBlock;
            }
            else {
                treeVoteBlocks += treeVoteBlock + "\n";
            }
        }

        String posedgeComputeVoteAlwaysBlock = ALWAYS_BLOCK2;
        String negedgeComputeVoteAlwaysBlock = ALWAYS_BLOCK2;

        posedgeComputeVoteAlwaysBlock = posedgeComputeVoteAlwaysBlock
                .replace("border", "posedge")
                .replace("signal", "compute_vote_flag")
                .replace("src", nextPositionVerifierBlock + "\n" + treeVoteBlocks)
                .replace("ind", tab(1));

        negedgeComputeVoteAlwaysBlock = negedgeComputeVoteAlwaysBlock
                .replace("border", "negedge")
                .replace("signal", "compute_vote_flag")
                .replace("src", nextPositionVerifierBlock + "\n" + treeVoteBlocks)
                .replace("ind", tab(1));

        return posedgeComputeVoteAlwaysBlock + negedgeComputeVoteAlwaysBlock;
    }

//    private String generateComputeForestVoteAlways(){
//
//    }

    public static String decimalToBinary(int decimalValue, int numberOfBits) {
        StringBuilder binaryValue = new StringBuilder();

        for (int i = numberOfBits - 1; i >= 0; i--) {
            int bit = (decimalValue >> i) & 1;
            binaryValue.append(bit);
        }
        return binaryValue.toString();
    }
}
