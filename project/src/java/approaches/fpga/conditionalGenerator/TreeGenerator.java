package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Comparisson;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.SettingsConditional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGenerator extends BasicGenerator {

    private int comparedValueBitwidth;
    private String precision;

    public void execute(List<Tree> trees, int featureQnt, int classQnt, SettingsConditional settings){

        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        for (int index = 0; index < trees.size(); index++){

            System.out.println("generating verilog decision tree" + index);

            String src = "";

            src += generateHeader(index, featureQnt);
            src += generatePortDeclaration(featureQnt, classQnt);
            src += generateAlwaysBlock();
            src += generateConditionals(trees.get(index).getRoot(), 2);
            src += generateEndDelimiters();

            FileBuilder.execute(src, String.format("FPGA/%s_conditional_run/tree%d.v", settings.dataset, index));
        }
    }

    public String generateHeader(int treeIndex, int featureQnt){

        String src = "";

        src += String.format("module tree%d (\n", treeIndex);

        if (this.precision.equals("integer")){
            for (int index = 0; index < featureQnt; index++){
                src += String.format("%sfeature%d,\n", tab(1), index);
            }
        }
        else if (this.precision.equals("decimal")){
            for (int index = 0; index < featureQnt; index++){
                src += String.format("%sft%d_exponent,\n", tab(1), index);
            }
            for (int index = 0; index < featureQnt; index++){
                src += String.format("%sft%d_fraction,\n", tab(1), index);
            }
        }


        src += tab(1) + "clock,\n";
        src += tab(1) + "voted_class";
        src += "\n);\n";

        return src;
    }

    public String generatePortDeclaration(int featureQnt, int classQnt){
        String tab = tab(1);

        String src = "";

        src += tab + "input wire clock;\n\n";

        if (this.precision.equals("integer")){
            for (int index = 0; index < featureQnt; index++){
                src += tab(1) + generatePort(String.format("feature%d", index), WIRE, INPUT, this.comparedValueBitwidth, true);
            }
        }
        else if (this.precision.equals("decimal")){
            for (int index = 0; index < featureQnt; index++){
                src += tab(1) + generatePort(String.format("ft%d_exponent", index), WIRE, INPUT, this.comparedValueBitwidth, true);
            }
            for (int index = 0; index < featureQnt; index++){
                src += tab(1) + generatePort(String.format("ft%d_fraction", index), WIRE, INPUT, this.comparedValueBitwidth, true);
            }
        }

        src += "\n";
        src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, (int) Math.ceil(Math.sqrt(classQnt)), true);
        src += "\n";

        int[][] oneHotMatrix = new int[classQnt][classQnt];

        for (int i = 0; i < oneHotMatrix.length; i++) {
            for (int j = 0; j < oneHotMatrix[i].length; j++) {
                if (i + j == classQnt - 1){
                    oneHotMatrix[i][j] = 1;
                }
                else {
                    oneHotMatrix[i][j] = 0;
                }
            }
        }

        for (int index = 0; index < classQnt; index++) {
            String oneHot = Arrays.toString(oneHotMatrix[index]);
            oneHot = oneHot
                    .replace(" ", "")
                    .replace("[", "")
                    .replace("]","")
                    .replace(",","");

            src += tab(1) + String.format("parameter class%d = %d'b%s;\n", index, oneHotMatrix.length, oneHot);
        }

        return src;
    }

    public String generateAlwaysBlock(){

        String tab = tab(1);

        return "\n\n" + tab + "always @(posedge clock) begin\n";
    }

    public String generateConditionals(Node node, int tab){

        var tabs = IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );

        if (node instanceof OuterNode){
            OuterNode newNode = (OuterNode) node;
            return tabs + "voted_class <= class" + newNode.getClassNumber() + ";\n";
        }
        else {
            InnerNode newNode = (InnerNode) node;
            String code = "";
            code += tabs + "if (" + generateComparison(newNode.getComparisson()) +") begin\n";
            code += generateConditionals(newNode.getLeftNode(), tab + 1);
            code += tabs + "end \n" + tabs + "else begin\n";
            code += generateConditionals(newNode.getRightNode(), tab + 1);
            code += tabs + "end\n";

            return code;
        }
    }

    public String generateComparison(Comparisson c){

        String expression = "";

        if (this.precision.equals("integer")){
            /*
            *  Como a comparação é menor ou igual, devemos arrendondar para baixo para funcionar corretamente
            *   utilizando apenas numeros inteiros
            */

            int threshold = (int) Math.floor(c.getThreshold());

            expression = String.format(
                "feature%d >= %d'b%s",
                c.getColumn(),
                this.comparedValueBitwidth,
                decimalToBinary(threshold, this.comparedValueBitwidth)
            );
        }
        if (this.precision.equals("decimal")){
            var threshold = c.getThreshold().toString().split("\\.");
            String integerThreshold = decimalToBinary(Integer.parseInt(threshold[0]), this.comparedValueBitwidth);
            String decimalThreshold = decimalToBinary(Integer.parseInt(threshold[0]), this.comparedValueBitwidth);

            expression += String.format(
                    "(ft%d_exponent > %d'b%s) || ((ft%d_exponent == %d'b%s) && (ft%d_fraction >= %d'b%s))",
                    c.getColumn(),
                    this.comparedValueBitwidth,
                    integerThreshold,
                    c.getColumn(),
                    this.comparedValueBitwidth,
                    integerThreshold,
                    c.getColumn(),
                    this.comparedValueBitwidth,
                    decimalThreshold
            );
        }

        return expression;

//        String binaryIntegralTh = String.format(FEATURE_BITWIDTH + "'b%" + FEATURE_BITWIDTH + "s", Integer.toBinaryString(intIntegralThreshold)).replaceAll(" ", "0");
//        String binaryFractionalTh = String.format(FEATURE_BITWIDTH + "'b%" + FEATURE_BITWIDTH + "s", Integer.toBinaryString(intFractionalThreshold)).replaceAll(" ", "0");
//
//        String first = "";
//        String second = "";
//
//        first += "(" + "ft" + c.getColumn() + "_exponent  > " + binaryIntegralTh + ")";
//        second += "((" + "ft" + c.getColumn() + "_exponent == " + binaryIntegralTh + ") & ft" + c.getColumn() + "_fraction " + c.getComparissonType() + " " + binaryFractionalTh + ")";
//
//        return first + " | " + second;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += tab(1) + "end\n";
        code += "endmodule";

        return code;
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
