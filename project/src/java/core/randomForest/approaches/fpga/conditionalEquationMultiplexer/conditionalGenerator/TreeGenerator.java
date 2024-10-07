package project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator;

import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Comparison;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCliCEM;
import project.src.java.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGenerator extends BaseTreeGenerator {
    //TODO: agora que estou utilizando o padrao IEEE754 ajustar a comparação
    private Integer precision;

    public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCliCEM settings){
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

        ReportGenerator reportGenerator = new ReportGenerator();
        ArrayList<Integer> nodeQntByTree = new ArrayList<>();

        for (int index = 0; index < trees.size(); index++){
            System.out.println("generating verilog decision tree" + index);

            nodeQntByTree.add(trees.get(index).getInnerNodes().size() + trees.get(index).getOuterNodes().size());

            String src = "";

            src += generateHeader(index, featureQnt);
            src += generatePortDeclaration(featureQnt, classQnt);
            src += generateParameters(classQnt);
            src += generateAlwaysBlock();
            src += generateConditionals(trees.get(index).getRoot(), 2);
            src += generateEndDelimiters();

            FileBuilder.execute(
                src, String.format(
                    "output/%s_%s_%dtree_%sdeep_run/tree%d.v",
                    settings.dataset,
                    settings.approach,
                    settings.trainingParameters.estimatorsQuantity,
                    settings.trainingParameters.maxDepth,
                    index
                ),
                false
            );
        }
        reportGenerator.createEntry(
            settings.dataset,
            settings.approach,
            settings.trainingParameters.maxDepth,
            nodeQntByTree
        );
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

        for (int index = 0; index < featureQnt; index++){
            src += tab(1) + generatePort(String.format("feature%d", index), WIRE, INPUT, this.precision, true);
        }

        src += "\n";
        src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, classQnt, true);
        src += "\n";

        return src;
    }

    public String generateParameters(int classQnt){

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
            src += tab(1) + String.format("parameter class%d = %d'b%s\n", index, classQnt,  oneHotEncode);
        }
        return src;
    }

    public String generateAlwaysBlock(){

        String tab = tab(1);

        return "\n" + tab + "always @(posedge clock) begin\n";
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

    public String generateComparison(Comparison comparison){

        String src = "";

        int threshold = (int) Math.floor(comparison.getThreshold());

        src = String.format(
            "feature%d <= %d'b%s",
            comparison.getColumn(),
            this.precision,
            toBin(threshold, this.precision)
        );
        return src;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += tab(1) + "end\n";
        code += "endmodule";

        return code;
    }
}
