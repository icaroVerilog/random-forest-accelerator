package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.dotTreeParser.treeStructure.Comparisson;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGenerator {

    int BITWIDTH = 32;

    String dataset;
    Integer classQuantity;
    Integer featureQuantity;

    public void execute(List<Tree> trees, String dataset, Integer classQuantity, Integer featureQuantity){

        this.dataset         = dataset;
        this.classQuantity   = classQuantity;
        this.featureQuantity = featureQuantity;

        System.out.println(classQuantity);
        System.out.println(featureQuantity);

        for (int index = 0; index < trees.size(); index++){
            String sourceCode = new String();

            sourceCode += generateHeader(index, this.featureQuantity);
            sourceCode += generatePortDeclaration(this.featureQuantity, this.classQuantity);
            sourceCode += generateAlwaysBlock();
            sourceCode += generateConditionals(trees.get(index).getRoot(), 2);
            sourceCode += generateEndDelimiters();

            FileBuilder.execute(sourceCode, "FPGA/tree" + index + ".v");
        }
    }

    public String generateHeader(int treeIndex, int featureQuantity){

        String tab = generateTab(1);
        String header = "module tree" + treeIndex + "(\n";
        String FI = IntStream.range(0, featureQuantity)
                .mapToObj(index -> "ft" + index + "_integral")
                .collect(Collectors.joining(", ")
        );
        String FF = IntStream.range(0, featureQuantity)
                .mapToObj(index -> "ft" + index + "_fractional")
                .collect(Collectors.joining(", ")
        );

        String clkAndOut = "clock, voted_class";

        return header + tab + FI + ",\n" + tab + FF + ",\n" +
               tab + clkAndOut + "\n);\n";
    }

    public String generatePortDeclaration(int featureQuantity, int classQuantity){

        String tab = generateTab(1);

        String CLK = tab + "input wire clock;\n\n";

        String FI = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "input wire [31:0] ft" + index + "_integral;\n")
                .collect(Collectors.joining("")
        );
        String FF = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "input wire [31:0] ft" + index + "_fractional;\n")
                .collect(Collectors.joining("")
        );

        int bitwidth = (int) Math.ceil(Math.sqrt(classQuantity));
        String votedClass = "\n" + tab + "output reg [" + (bitwidth - 1) + ":0] voted_class;\n\n\n";


        String CL = IntStream.range(0, classQuantity)
                .mapToObj(
                        index -> tab + "parameter class" + index + " = " +
                                bitwidth + "'b" + String.format("%" + bitwidth +
                                "s;", Integer.toBinaryString(index)).replaceAll(" ", "0")
                )
                .collect(Collectors.joining("\n")
        );

        return CLK + FI + "\n" + FF  + votedClass + CL;
    }

    public String generateAlwaysBlock(){

        String tab = generateTab(1);

        return "\n\n" + tab + "always @(clock) begin\n";
    }

    public String generateConditionals(Node node, int tab){

        var tabs = IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );

        if (node instanceof OuterNode){
            OuterNode newNode = (OuterNode) node;
            return tabs + "voted_class = class" + newNode.getClassNumber() + ";\n";
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

        var threshold = c.getThreshold().toString().split("\\.");
        int intIntegralThreshold = Integer.parseInt(threshold[0]);
        int intFractionalThreshold = Integer.parseInt(threshold[1]);

        String binaryIntegralTh = String.format(BITWIDTH + "'b%" + BITWIDTH + "s", Integer.toBinaryString(intIntegralThreshold)).replaceAll(" ", "0");
        String binaryFractionalTh = String.format(BITWIDTH + "'b%" + BITWIDTH + "s", Integer.toBinaryString(intFractionalThreshold)).replaceAll(" ", "0");

        String first = "";
        String second = "";

        first += "(" + "ft" + c.getColumn() + "_integral " + c.getComparissonType() + " " + binaryIntegralTh + ")";
        second += "((" + "ft" + c.getColumn() + "_integral == " + binaryIntegralTh + ") & ft" + c.getColumn() + "_fractional " + c.getComparissonType() + " " + binaryFractionalTh + ")";

//        System.out.println(c.getFeatureName());
//        System.out.println(c.getColumn());

        return first + " | " + second;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += generateTab(1) + "end\n";
        code += "endmodule";

        return code;
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
