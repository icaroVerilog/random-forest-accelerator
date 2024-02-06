package project.src.java.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.Arrays;
import java.util.List;

public class TreeGenerator extends BaseTreeGenerator {

    private int comparedValueBitwidth;
    private String precision;

    public void execute(List<Tree> trees, Integer classQnt, Integer featureQnt, Settings settings){
        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        for (int index = 0; index < trees.size(); index++){

            System.out.println("generating verilog decision tree" + index);

            String sourceCode = "";

            sourceCode += generateHeader(String.format("tree%d", index), featureQnt);
            sourceCode += generatePortDeclaration(featureQnt, classQnt, this.comparedValueBitwidth);
            sourceCode += generateParameters(classQnt);
            sourceCode += "\n\tassign voted_class = \n";
            sourceCode += generateMux(trees.get(index).getRoot(), 2);
            sourceCode += ";\n";
            sourceCode += generateEndDelimiters();

            FileBuilder.execute(sourceCode, String.format("FPGA/%s_multiplexer_run/tree%d.v", settings.dataset, index));
        }
    }

    private String generateParameters(int classQnt){
        int bitWidth = (int) Math.ceil(Math.sqrt(classQnt));

        int[][] oneHotMatrix = new int[classQnt][classQnt];

        for (int i = 0; i < oneHotMatrix.length; i++) {
            for (int j = 0; j < oneHotMatrix[i].length; j++) {
                if (i == j){
                    oneHotMatrix[i][j] = 1;
                }
                else {
                    oneHotMatrix[i][j] = 0;
                }
            }
        }

        String src = "";

        for (int index = 0; index < classQnt; index++) {
            String oneHotEncode = Arrays.toString(oneHotMatrix[classQnt - index - 1])
                    .replaceAll("[\\[\\]\\s]", "")
                    .replace(",", "") + ";";
            src += tab(1) + String.format("parameter class%d = %d'b%s\n", index, (bitWidth + 1),  oneHotEncode);
        }
        return src;
    }

    private String generateMux(Node node, int tab){

        if (node instanceof OuterNode) {
            OuterNode newNode = (OuterNode) node;
            return tab(tab) + "class" + newNode.getClassNumber() + "\n";
        } else {
            InnerNode newNode = (InnerNode) node;
            String code = "";
            code += tab(tab) +"((" + generateComparison(newNode.getComparisson(), this.comparedValueBitwidth) +") ? \n";
            code += generateMux(newNode.getLeftNode(), tab + 1);
            code += tab(tab) + ":\n";
            code += generateMux(newNode.getRightNode(), tab + 1);
            code += tab(tab) + ")\n";

            return code;
        }
    }
}
