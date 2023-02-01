package project.src.java.gpuGenerator.conditional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import project.src.java.model.Comparisson;
import project.src.java.model.Tree;
import project.src.java.model.Nodes.InnerNode;
import project.src.java.model.Nodes.Node;
import project.src.java.model.Nodes.OuterNode;
import project.src.java.parser.Parser;
import project.src.java.util.FileBuilder;

public class ConditionalGPUGenerator {
    public static void execute(List<Tree> trees) {
        String sourceCode = new String();

        sourceCode += generateFunctionSignature(Parser.featuresNames.size());
        sourceCode += generateClassInitialization(Parser.classesNames.size());
        sourceCode += generateIfTrees(trees);
        
        FileBuilder.execute(sourceCode, "gpu/conditional/rf_with_if.cu");
    }

    private static String generateIfTrees(List<Tree> trees) {
        var code = "\n\tif (i < N) {\n";
        code += trees.stream()
            .map(ts -> generateIfTree(ts.getRoot(), 2))
            .collect(Collectors.joining("\n"));
        code += generateComparissons(trees.get(0).getClassQuantity());
        code += "\t}\n}";
        return code;
    }

    private static String generateIfTree(Node node, int tab) {
        
        //Generating tabs for the code indentation
        //tab = 2 --> tabs = "\t\t"
        var tabs = IntStream.range(0, tab)
            .mapToObj(t -> "\t")
            .collect(Collectors.joining(""));
        if(node instanceof OuterNode){
            var outerNode = (OuterNode) node;
            return tabs + "Class[" + outerNode.getClassNumber() + "]++;\n";
        }else{
            var innerNode = (InnerNode) node;
            String code = new String();
            code += tabs + "if ("+decodeToIf(innerNode.getComparisson())+") {\n";
            code += generateIfTree(innerNode.getLeftNode(), tab + 1);
            code += tabs + "} else {\n";
            code += generateIfTree(innerNode.getRightNode(), tab + 1);
            code += tabs + "}\n";
            return code;
        }
    }

    private static String decodeToIf(Comparisson comparisson) {
        return "F[" + comparisson.getColumn() + "] " 
            + comparisson.getComparissonType() + " " 
            + comparisson.getThreshold();
    }

   

    private static String generateClassInitialization(int featureQuantity) {
        var classInitialization = IntStream.range(0, featureQuantity)
            .mapToObj(i -> "\tClass["+i+"] = 0;")
            .collect(Collectors.joining("\n"));

        String code = "\tint i = blockIdx.x * blockDim.x + threadIdx.x;" + "\n" +
            "\tint Class["+featureQuantity+"]; " + "\n"
            + classInitialization;
        return code;
    }

    private static String generateFunctionSignature(int featureQuantity) {
        String features = IntStream.range(0, featureQuantity)
            .mapToObj( i -> "float *F"+i)
            .collect(Collectors.joining(", "));
        String code = "__global__ void RF_with_IF("+features+", int *P, const int N)\n{";
        return code;
    }

    public static String generateComparissons(int classQuantity){
        LinkedList queue = new LinkedList<String>();
        HashMap map = new HashMap<String, String>();
        for (int i = 0; i < classQuantity; i++) {
            var key = "Class["+i+"]";
            queue.addLast(key);
            map.put(key, i+"");
        }
        String comparisson = "";
        int cont = 0;
        while(queue.size() > 1){
            String comp1 = (String) queue.removeFirst();
            String comp2 = (String) queue.removeFirst();
            String value1 = (String) map.get(comp1);
            String value2 = (String) map.get(comp2);
            comparisson += String.format("\t\tint p%d = (%s > %s)?%s:%s;\n", cont, comp1, comp2, value1, value2);
            comparisson += String.format("\t\tint Q%d = (%s > %s)?%s:%s;\n", cont, comp1, comp2, comp1, comp2);
            queue.addLast("Q"+cont);
            map.put("Q"+cont, "p"+cont);
            cont++;
        }
        comparisson += "\t\tP[i] = "+map.get(queue.remove())+";";

        return comparisson;
    }

}
