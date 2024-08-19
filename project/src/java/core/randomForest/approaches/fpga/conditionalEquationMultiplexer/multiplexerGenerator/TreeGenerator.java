package project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator;

import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeGenerator extends BaseTreeGenerator {

    private int comparedValueBitwidth;
    private String precision;

    public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCEM settings){
        // TODO: ajustar o settings para receber a precisão
//        this.precision = settings.precision;
        this.precision = "integer";
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        ReportGenerator reportGenerator = new ReportGenerator();
        ArrayList<Integer> nodeQntByTree = new ArrayList<>();

        for (int index = 0; index < trees.size(); index++){
            System.out.println("generating verilog decision tree" + index);

            nodeQntByTree.add(trees.get(index).getInnerNodes().size() + trees.get(index).getOuterNodes().size());

            String src = "";

            src += generateHeader(String.format("tree%d", index), featureQnt);
            src += generatePortDeclaration(featureQnt, classQnt, this.comparedValueBitwidth);
            src += generateParameters(classQnt);
            src += "\n\tassign voted_class = \n";
            src += generateMux(trees.get(index).getRoot(), 2);
            src += ";\n";
            src += generateEndDelimiters();

            FileBuilder.execute(src, String.format("output/%s_multiplexer_%dtree_%sdeep_run/tree%d.v", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth, index));
        }
        reportGenerator.createEntry(
                settings.dataset,
                settings.approach,
				Integer.valueOf(settings.trainingParameters.maxDepth),
                nodeQntByTree
        );
    }

    private String generateParameters(int classQnt){
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
