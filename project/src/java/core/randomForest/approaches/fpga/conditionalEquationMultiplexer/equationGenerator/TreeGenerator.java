package project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.equationGenerator;

import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCliCEM;
import project.src.java.core.randomForest.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeGenerator extends BaseTreeGenerator {

    private int comparedValueBitwidth;
    private int precision;

    public void execute(List<Tree> trees, Integer classQnt, Integer featureQnt, SettingsCliCEM settings){
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

            src += generateHeader(String.format("tree%d", index), featureQnt);
            src += generatePortDeclaration(featureQnt, classQnt, this.comparedValueBitwidth);
            src += generateComparisonWires(trees.get(index));
            src += generateComparisonAssigns(trees.get(index), classQnt);
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

        // TODO: "ajustar para receber o max depth de forma correta"
        reportGenerator.createEntry(
            settings.dataset,
            settings.approach,
            settings.trainingParameters.maxDepth,
            nodeQntByTree
        );
    }

    private String generateComparisonWires(Tree tree) {
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
            src += "\tassign c"+key+" = "+generateComparison(node.getComparisson(), this.comparedValueBitwidth)+";\n";
        }
        return src;
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
        var finalSrc = "\n";
        for (int index = 0; index < classQnt; index++) {
            finalSrc += tab(1) + String.format("assign voted_class[%d] = %s;\n", index, src[index]);
        }

        finalSrc = finalSrc.replace("null", "1'b0");

        return finalSrc;
    }
}
