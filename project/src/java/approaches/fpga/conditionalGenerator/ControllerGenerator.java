package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.util.FileBuilder;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator {

    public void execute(Integer treeQuantity, String dataset, Integer classQuantity, Integer featureQuantity, Integer samplesQnt){

        String sourceCode = "";

        sourceCode += generateImports(treeQuantity);
        sourceCode += generateIO(featureQuantity, classQuantity);
        sourceCode += generateMemoryRead(featureQuantity, samplesQnt);
        
        for (int index = 0; index < treeQuantity; index++){
            sourceCode += generateModuleInstantiation(featureQuantity, index);
        }

        sourceCode += generateInitialBlock(featureQuantity);
        sourceCode += generateAlwaysBlock(featureQuantity, samplesQnt);


        FileBuilder.execute(sourceCode, "FPGA/controller.v");
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
        String counter = tab + "integer counter;\n\n";
        String FI = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_exponent;\n")
                .collect(Collectors.joining("")
        );
        String FF = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_fraction;\n")
                .collect(Collectors.joining("")
        );

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));
        String votedClass = "\n" + tab + "wire [" + (bitwidth - 1) + ":0] voted_class;\n\n\n";

        return "\n\n" + module + "\n" + clock + counter + FI + FF + votedClass;
    }
    
    private String generateMemoryRead(Integer featureQnt, Integer samplesQnt){
        String tab = generateTab(1);
        String memRegistersExponent = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_e" + " [0:" + (samplesQnt -1) + "];")
                .collect(Collectors.joining("\n"));

        String memRegistersFractional = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_f" + " [0:" + (samplesQnt -1) + "];")
                .collect(Collectors.joining("\n"));

        return memRegistersExponent + "\n" + memRegistersFractional + "\n\n";
    }

    private String generateModuleInstantiation(Integer featureQnt, Integer treeIndex){
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);

        String moduleAlias = tab1 + "tree" + treeIndex + " tree" + treeIndex + "(\n";
        String exponent = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + ".ft" + index + "_exponent(ft" + index + "_exponent),")
                .collect(Collectors.joining("\n")
        );
        String fraction = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + ".ft" + index + "_fraction(ft" + index + "_fraction),")
                .collect(Collectors.joining("\n")
        );

        String output = "\n "+ tab2 + ".voted_class(voted_class)\n";

        return moduleAlias + exponent + "\n" + fraction + output + tab1 + ");\n\n";
    }

    private String generateInitialBlock(Integer featureQnt){
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);

        String clockSetup = tab2 + "clock = 0;\n\n";

        String initialBlockOpen = tab1 + "initial begin\n\n";
        String initialBlockClose = tab1 + "end\n\n";

        String readmemExponent = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + "$readmemb(" + '"' + "dataset/feature" + index + "_exponent.bin" + '"' + ", mem_feature_" + index + "_e);")
                .collect(Collectors.joining("\n")
        );

        String readmemFraction = IntStream.range(0, featureQnt)
                .mapToObj(index -> tab2 + "$readmemb(" + '"' + "dataset/feature" + index + "_fraction.bin" + '"' + ", mem_feature_" + index + "_f);")
                .collect(Collectors.joining("\n")
        );

//        String memoryReadLoopBegin = tab2 + "for (integer index = 0; index < " + (samplesQnt - 1) + " ;index++) begin\n";
//        String memoryReadLoopEnd = "end\n";
//
//        String loopBody1 = IntStream.range(0, samplesQnt)
//                .mapToObj(index -> tab3 + "$readmemb(feature" + index + "fraction.bin")
//                .collect(Collectors.joining("\n")
//        );

        return initialBlockOpen + clockSetup + readmemExponent + "\n" + readmemFraction + "\n\n" + initialBlockClose;

    }

    private String generateAlwaysBlock(Integer featuresQnt ,Integer samplesQnt) {
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);
        String tab3 = generateTab(3);

        String alwaysBlockOpen = tab1 + "always @(clock) begin\n";
        String alwaysBlockClose = tab1 + "end";

        String conditionalOpen = tab2 + "if (counter < " + samplesQnt + ") begin\n";
        String conditionalClose = tab2 + "end\n";

        String conditionalElse = tab2 + "else begin\n" + tab3 + "$finish;\n" + tab2 + "end";

        String conditionalBody1 = IntStream.range(0, featuresQnt)
                .mapToObj(index -> tab3 + "ft" + index + "_exponent <= mem_feature_" + index + "_e[counter];")
                .collect(Collectors.joining("\n")
        );

        String conditionalBody2 = IntStream.range(0, featuresQnt)
                .mapToObj(index -> tab3 + "ft" + index + "_fraction <= mem_feature_" + index + "_f[counter];")
                .collect(Collectors.joining("\n")
        );

        return alwaysBlockOpen +
               conditionalOpen +
               conditionalBody1 + "\n" +
               conditionalBody2 + "\n" +
               conditionalClose +
               conditionalElse  + "\n" +
               alwaysBlockClose + "\n" +
               "endmodule";
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
