package project.src.java.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.dotTreeParser.treeStructure.Comparison;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.relatory.ReportGenerator;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGenerator extends BaseTreeGenerator {

    private int comparedValueBitwidth;
    private String precision;

    public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCEM settings){
        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

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

            FileBuilder.execute(src, String.format("FPGA/%s_conditional_%dtree_%sdeep_run/tree%d.v", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth, index));
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

        if (this.precision.equals("integer")){
            /*
            *  Como a comparação é menor ou igual, devemos arrendondar para baixo para funcionar corretamente
            *   utilizando apenas numeros inteiros
            */

            int threshold = (int) Math.floor(comparison.getThreshold());

            src = String.format(
                "feature%d <= %d'b%s",
                comparison.getColumn(),
                this.comparedValueBitwidth,
                toBinary(threshold, this.comparedValueBitwidth)
            );
        }
        if (this.precision.equals("decimal")){
            var threshold = comparison.getThreshold().toString().split("\\.");
            String integerThreshold = toBinary(Integer.parseInt(threshold[0]), this.comparedValueBitwidth);
            String decimalThreshold = toBinary(Integer.parseInt(threshold[0]), this.comparedValueBitwidth);

            src += String.format(
                    "(ft%d_exponent > %d'b%s) || ((ft%d_exponent == %d'b%s) && (ft%d_fraction >= %d'b%s))",
                    comparison.getColumn(),
                    this.comparedValueBitwidth,
                    integerThreshold,
                    comparison.getColumn(),
                    this.comparedValueBitwidth,
                    integerThreshold,
                    comparison.getColumn(),
                    this.comparedValueBitwidth,
                    decimalThreshold
            );
        }
        return src;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += tab(1) + "end\n";
        code += "endmodule";

        return code;
    }
}
