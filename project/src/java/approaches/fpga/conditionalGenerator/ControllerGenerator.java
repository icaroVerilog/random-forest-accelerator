package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.util.FileBuilder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator {

    public void execute(Integer treeQuantity, String dataset, Integer classQuantity, Integer featureQuantity){

        String sourceCode = "";

        sourceCode += generateImports(treeQuantity);
        sourceCode += generateIO(featureQuantity, classQuantity);
        sourceCode += generateMemoryRead(featureQuantity, 10);
        
        for (int index = 0; index < treeQuantity; index++){
            sourceCode += generateModuleInstantiation(featureQuantity, index);
        }

        FileBuilder.execute(sourceCode, "FPGA/controller.v");

        System.out.println(sourceCode);
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
        );
        return imports;
    }

    private String generateIO(Integer featureQuantity, Integer classQnt){
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

        return "\n\n" + module + "\n" + clock + FI + FF + votedClass;
    }
    
    private String generateMemoryRead(Integer featureQnt, Integer samplesQnt){
        String tab = generateTab(1);
        String memRegistersIntegral = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_i" + "[0:" + (samplesQnt -1) + "];")
                .collect(Collectors.joining("\n"));

        String memRegistersFractional = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_f" + "[0:" + (samplesQnt -1) + "];")
                .collect(Collectors.joining("\n"));

        return memRegistersIntegral + "\n" + memRegistersFractional + "\n\n";
    }

    private String generateModuleInstantiation(Integer featureQnt, Integer treeIndex){
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);

        String moduleAlias = tab1 + "tree" + treeIndex + " tree" + treeIndex + "(\n";
        String integral = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + ".ft" + index + "_integral(ft" + index + "_integral),")
                .collect(Collectors.joining("\n")
        );
        String fractional = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + ".ft" + index + "_fractional(ft" + index + "_fractional),")
                .collect(Collectors.joining("\n")
        );

        String output = "\n "+ tab2 + ".voted_class(voted_class)\n";

        return moduleAlias + integral + "\n" + fractional + output + tab1 + ");\n\n";
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
