package project.src.java.approaches.fpga.conditionalEquationMultiplexer.equationGenerator;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.List;
import java.util.Map;

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
            sourceCode += generatePortDeclaration(featureQnt, classQnt);
            sourceCode += generateComparisonWires(trees.get(index), this.comparedValueBitwidth);
            sourceCode += generateComparisonAssigns(trees.get(index), classQnt);
            sourceCode += generateEndDelimiters();

            FileBuilder.execute(sourceCode, String.format("FPGA/%s_equation_run/tree%d.v", settings.dataset, index));
        }
    }

    private String generateComparisonAssigns(Tree tree, int classQnt) {
        String[] src = new String[classQnt];
        var outerNodes = tree.getOuterNodes();
        for (Map.Entry<Integer, OuterNode> entry : outerNodes.entrySet()) {
            Integer key = entry.getKey();
            Node node = entry.getValue();
            boolean first = true;
            int classNumber = entry.getValue().getClassNumber();
            src[classNumber] = src[classNumber] == null ? "" : src[classNumber];
            var o = src[classNumber].isEmpty() ? "" : " | ";
            src[classNumber] += o;
            while(node.getFather() != null){
                var innerNode = node.getFather();
                var n = innerNode.getLeftNode() == node ? "": "~";
                var e = first ? "" : " & ";
                src[classNumber] += e + n + "c"+ innerNode.getId();
                node = innerNode;
                first = false;
            }
        }
        var finalSrc = "";
        for (int i = 0; i < classQnt; i++) {
            finalSrc += "\tassign voted_class["+i+"] = "+ src[i] + ";\n";
        }
        return finalSrc;
    }

    public String generatePortDeclaration(int featureQnt, int classQnt){

        String src = "";

        for (int index = 0; index < featureQnt; index++) {
            src += tab(1) + generatePort(String.format("feature%d", index), WIRE, INPUT, this.comparedValueBitwidth, true);
        }
        src += "\n";
        src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, (int) Math.ceil(Math.sqrt(classQnt)), true);

        return src;
    }

    protected String generateComparisonWires(Tree tree, int comparedValueBitwidth) {
        String src = "\n";
        var comparisons = tree.getInnerNodes();
        for (Map.Entry<Integer, InnerNode> entry : comparisons.entrySet()) {
            Integer key = entry.getKey();
            src += "\twire c"+key+";\n";
        }

        src += "\n";

        for (Map.Entry<Integer, InnerNode> entry : comparisons.entrySet()) {
            Integer key = entry.getKey();
            InnerNode node = entry.getValue();
            var featureIndex = node.getComparisson().getColumn();
            src += "\tassign c"+key+" = "+generateComparison(node.getComparisson(), comparedValueBitwidth)+";\n";
        }
        return src;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += "\nendmodule";

        return code;
    }
}
