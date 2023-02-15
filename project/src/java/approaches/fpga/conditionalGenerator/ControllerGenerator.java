package project.src.java.approaches.fpga.conditionalGenerator;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator {

    public void execute(Integer treeQuantity, String dataset, Integer classQuantity, Integer featureQuantity){
        String sourceCode = new String();

        sourceCode += generateImports(treeQuantity);
        sourceCode += generateRegisters(featureQuantity, classQuantity);

        System.out.println(sourceCode);
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
        );
        return imports;
    }

    private String generateRegisters(Integer featureQuantity, Integer classQnt){
        String tab = generateTab(1);
        String module = "module controller();\n";
        String clock = tab + "reg clock;\n";
        String FI = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_integral;\n")
                .collect(Collectors.joining("")
        );
        String FF = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_fractional;\n")
                .collect(Collectors.joining("")
        );

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));
        String votedClass = "\n" + tab + "wire [" + (bitwidth - 1) + ":0] voted_class;\n\n\n";

        return clock + FI + FF + votedClass;
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
