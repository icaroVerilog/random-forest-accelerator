package project.src.java.approaches.fpga.equationGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Comparisson;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeGenerator extends BasicGenerator {

    private int comparedValueBitwidth;
    private String precision;

    public void execute(List<Tree> trees, Integer classQnt, Integer featureQnt, Settings settings){

        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        for (int index = 0; index < trees.size(); index++){

            System.out.println("generating verilog decision tree" + index);

            String src = "";

            src += generateHeader(index, featureQnt);
            src += generatePortDeclaration(featureQnt, classQnt);
            src += generateComparisonWires(trees.get(index));
            src += generateComparisonAssigns(trees.get(index), classQnt);
            src += generateEndDelimiters();

            FileBuilder.execute(src, String.format("FPGA/%s_equation_run/tree%d.v", settings.dataset, index));
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

    private String generateComparisonWires(Tree tree) {
        var src = "\n";
        var comparisons = tree.getInnerNodes();
        for (Map.Entry<Integer, InnerNode> entry : comparisons.entrySet()) {
            Integer key = entry.getKey();
            src += "\twire c"+key+";\n";
        }

        for (Map.Entry<Integer, InnerNode> entry : comparisons.entrySet()) {
            Integer key = entry.getKey();
            InnerNode node = entry.getValue();
            var featureIndex = node.getComparisson().getColumn();
            src += "\tassign c"+key+" = "+generateComparison(node.getComparisson())+";\n";
        }
        return src;
    }

    public String generateHeader(int treeIndex, int featureQnt){

        String tab = tab(1);
        String header = "module tree" + treeIndex + "(\n";
        String FI = IntStream.range(0, featureQnt)
                .mapToObj(index -> "ft" + index)
                .collect(Collectors.joining(", ")
                );
        String clkAndOut = "clock, voted_class";

        return header + tab + FI + ",\n" +
                tab + clkAndOut + "\n);\n";
    }

    public String generatePortDeclaration(int featureQnt, int classQnt){
        String src = "";

        src += tab(1) + "input wire clock;\n\n";

        for (int index = 0; index < featureQnt; index++){
            src += tab(1) + generatePort(String.format("ft%d", index), WIRE, INPUT, this.comparedValueBitwidth, true);
        }
        src += "\n";

        src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, ((int) Math.ceil(Math.sqrt(classQnt))), true);

        return src;
    }

    public String generateComparison(Comparisson c){

        var threshold = c.getThreshold().toString().split("\\.");

//        String binaryIntegralTh = String.format(FEATURE_BITWIDTH + "'b%" + FEATURE_BITWIDTH + "s", integralThreshold).replaceAll(" ", "0");
        String binaryIntegralTh = String.format("%d'b%s", this.comparedValueBitwidth, generateBinaryNumber(Integer.parseInt(threshold[0]), this.comparedValueBitwidth));
        String first = "";

        //System.out.println(c.getComparissonType());

        first += "ft" + c.getColumn() + " " + c.getComparissonType() + " " + binaryIntegralTh;
        return first;
    }

    public String generateEndDelimiters(){
        String code = "";

        code += "\nendmodule";

        return code;
    }
}
