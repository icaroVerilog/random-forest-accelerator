package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator {

    public void execute(Integer treeQuantity, String dataset, Integer classQuantity, Integer featureQuantity, Integer samplesQnt){

        String sourceCode = "";

        sourceCode += generateImports(treeQuantity);
        sourceCode += generateIO(featureQuantity, classQuantity, treeQuantity);
        sourceCode += generateMemoryRead(featureQuantity, samplesQnt);
        
        for (int index = 0; index < treeQuantity; index++){
            sourceCode += generateModuleInstantiation(featureQuantity, index);
        }

        sourceCode += generateInitialBlock(featureQuantity);
        sourceCode += generateAlwaysBlock(featureQuantity, samplesQnt, classQuantity, treeQuantity);

        FileBuilder.execute(sourceCode, "FPGA/controller.v");
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
        );
        return imports;
    }

    private String generateIO(Integer featureQuantity, Integer classQnt, Integer treeQnt){

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String tab = generateTab(1);
        String module = "module controller();\n";
        String clock = "\n" + tab + "reg clock;\n";
        String counter = tab + "integer counter;\n\n";

        String classes = "";
        for (int index = 0; index < classQnt; index++){
            classes += tab + "integer class" + String.format("%" + bitwidth + "s", Integer.toBinaryString(index)).replaceAll(" ", "0") + ";\n";
        }

        String FI = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_exponent;\n")
                .collect(Collectors.joining("")
        );
        String FF = IntStream.range(0, featureQuantity)
                .mapToObj(index -> tab + "reg [31:0] ft" + index + "_fraction;\n")
                .collect(Collectors.joining("")
        );

        String mostVoted = "\n" + tab + "reg [" + (bitwidth - 1) + ":0] most_voted;\n";
//        String votedClass = "\n" + tab + "wire [" + (bitwidth - 1) + ":0] voted_class;\n\n\n";
        String votedClass = IntStream.range(0, treeQnt)
                .mapToObj(index -> tab + "wire [" + (bitwidth - 1) + ":0] voted_class" + index + ";")
                .collect(Collectors.joining("\n"));

        return "\n\n" + module + "\n" + classes + clock + counter + FI + FF + mostVoted + votedClass + "\n\n";
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

        String output = "\n "+ tab2 + ".voted_class(voted_class" + treeIndex + "),\n";
        String clock = tab2 + ".clock(clock)\n";

        return moduleAlias + exponent + "\n" + fraction + output + clock + tab1 + ");\n\n";
    }

    private String generateInitialBlock(Integer featureQnt){
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);

        String clockSetup = tab2 + "clock = 0;\n";
        String counterSetup = tab2 + "counter = 0;\n\n";

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

        return initialBlockOpen + clockSetup + counterSetup + readmemExponent + "\n" + readmemFraction + "\n\n" + initialBlockClose;

    }

    private String generateAlwaysBlock(Integer featuresQnt ,Integer samplesQnt, Integer classQnt, Integer treeQnt) {
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);
        String tab3 = generateTab(3);
        String tab4 = generateTab(4);

        String alwaysBlockOpen = tab1 + "always @(posedge clock) begin\n";
        String alwaysBlockClose = tab1 + "end";

        String conditionalOpen = tab2 + "if (counter < " + samplesQnt + ") begin\n";
        String conditionalClose = tab2 + "end\n";

        String conditionalElse = tab2 + "else begin\n" + tab3 + "$finish;\n" + tab2 + "end";

        String featureExponent = IntStream.range(0, featuresQnt)
                .mapToObj(index -> tab3 + "ft" + index + "_exponent <= mem_feature_" + index + "_e[counter];")
                .collect(Collectors.joining("\n")
        );

        String featureFraction = IntStream.range(0, featuresQnt)
                .mapToObj(index -> tab3 + "ft" + index + "_fraction <= mem_feature_" + index + "_f[counter];")
                .collect(Collectors.joining("\n")
        );


        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String switchCases = "";

        for (int index = 0; index < treeQnt; index++){

            String switchCaseOpen = "\n" + tab3 + "case (voted_class" + index + ")      \n";

            String switchCaseBody = IntStream.range(0, classQnt)
                    .mapToObj(
                            index2 -> tab4 + bitwidth + "'b" +
                                    String.format("%" + bitwidth + "s", Integer.toBinaryString(index2)).replaceAll(" ", "0") + ": " + "class" +
                                    String.format("%" + bitwidth + "s", Integer.toBinaryString(index2)).replaceAll(" ", "0") + " <= class" +
                                    String.format("%" + bitwidth + "s", Integer.toBinaryString(index2)).replaceAll(" ", "0") + " + 1;"
                    )
                    .collect(Collectors.joining("\n")
                    );

            String switchCaseClose = "\n" + tab4 + "default: class00 <= class00;\n" + tab3 + "endcase";

            switchCases += switchCaseOpen + switchCaseBody + switchCaseClose;

        }

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQnt; index++){
            classes.add(String.format("%" + bitwidth + "s", Integer.toBinaryString(index)).replaceAll(" ", "0"));
        }

        String teste = "";

        for (int index1 = 0; index1 < classQnt; index1++) {

            String comparrison = "";


            if (index1 == 0){
                teste += "\n" + tab3 + "most_voted <= " + "(x) * " + classes.get(index1).length() + "'b" + classes.get(index1) + " + \n";
            }
            if (index1 == classQnt - 1){
                teste += generateTab(7) + "(x) * " + classes.get(index1).length() + "'b" + classes.get(index1) + ";\n\n";
            }
            else {
                teste += generateTab(7) + "(x) * " + classes.get(index1).length() + "'b" + classes.get(index1) + " + \n";
            }

            for (int index2 = 0; index2 < classQnt; index2++) {
                if (Objects.equals(classes.get(index1), classes.get(index2))) {
                    continue;
                }
                else {
                    comparrison += "(class" + classes.get(index1) + " > class" + classes.get(index2) + ")";
                }
            }

            teste = teste.replace("x", comparrison);
            teste = teste.replace(")(", ") & (");
        }

        String counterIncrement = generateTab(3) + "counter <= counter + 1;\n\n";

        System.out.println(teste);



        return alwaysBlockOpen +
               conditionalOpen +
               featureExponent + "\n" +
               featureFraction + "\n" +
                switchCases + "\n" +
                teste +
                counterIncrement +
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
