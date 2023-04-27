package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator extends BasicGenerator {

    public void execute(
            Integer treeQnt,
            Integer classQnt,
            Integer featureQnt,
            Integer samplesQnt,
            Boolean debugMode,
            String dataset
    ){

        System.out.println("generating controller");

        String sourceCode = "";

        sourceCode += generateImports(treeQnt);
        sourceCode += generateIO(featureQnt, classQnt, treeQnt, debugMode);
        sourceCode += generateMemoryRead(featureQnt, samplesQnt, debugMode);
        
        for (int index = 0; index < treeQnt; index++){
            sourceCode += generateModuleInstantiation(featureQnt, index);
        }

        sourceCode += generateInitialBlock(featureQnt, classQnt, debugMode);
        sourceCode += generateAlwaysBlock(featureQnt, samplesQnt, classQnt, treeQnt, debugMode);

        FileBuilder.execute(sourceCode, "FPGA/" + dataset + "/controller.v");
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
        );
        return imports;
    }

    private String generateIO(Integer featureQnt, Integer classQnt, Integer treeQnt, Boolean debugMode){

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String ind1 = generateTab(1);

        ArrayList<String> moduleIO = new ArrayList<>();
        String sourceCode;

        moduleIO.add("clock");
        moduleIO.add("most_voted");

        for (int index = 0; index < featureQnt; index++){
            moduleIO.add("ft" + index + "_exponent");
        }
        for (int index = 0; index < featureQnt; index++){
            moduleIO.add("ft" + index + "_fraction");
        }

        sourceCode = "\n\n";
        sourceCode += generateModule(
                "controller",
                moduleIO
        );

        sourceCode += "\n\n";

        for (int index = 0; index < classQnt; index++){
            sourceCode += generateIndentation(1);
            sourceCode += generatePort(
                "class" + generateBinaryNumber(index, bitwidth),
                INTEGER,
                NONE,
                1,
                true
            );
        }


        sourceCode += ind1 + generatePort("clock", WIRE, INPUT, 1, true);
        sourceCode += ind1 + generatePort("most_voted", REGISTER, OUTPUT, 2, true);
        sourceCode += "\n";

        for (int index = 0; index < featureQnt; index++){
            sourceCode += generateIndentation(1);
            sourceCode += generatePort(
                "ft" + index + "_exponent",
                WIRE,
                INPUT,
                FEATURE_BITWIDTH,
                false
            );
            sourceCode += "\n";
        }

        for (int index = 0; index < featureQnt; index++){
            sourceCode += generateIndentation(1);
            sourceCode += generatePort(
                    "ft" + index + "_fraction",
                    WIRE,
                    INPUT,
                    FEATURE_BITWIDTH,
                    false
            );
            sourceCode += "\n";
        }

        sourceCode += "\n";

        for (int index = 0; index < treeQnt; index++){
            sourceCode += generateIndentation(1);
            sourceCode += generatePort("voted_class" + index,
                    WIRE,
                    NONE,
                    3,
                    false
            );
            sourceCode += "\n";
        }

        sourceCode += "\n";

        return sourceCode;
    }
    
    private String generateMemoryRead(Integer featureQnt, Integer samplesQnt, Boolean debugMode) {

        if (debugMode) {
            String tab = generateTab(1);
            String memRegistersExponent = IntStream.range(0, featureQnt)
                    .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_e" + " [0:" + (samplesQnt - 1) + "];")
                    .collect(Collectors.joining("\n"));

            String memRegistersFractional = IntStream.range(0, featureQnt)
                    .mapToObj(index -> tab + "reg [31:0] mem_feature_" + index + "_f" + " [0:" + (samplesQnt - 1) + "];")
                    .collect(Collectors.joining("\n"));

            return memRegistersExponent + "\n" + memRegistersFractional + "\n\n";
        } else {
            return "";
        }
    }

    private String generateModuleInstantiation(Integer featureQnt, Integer treeIndex){

        String ind = generateIndentation(1);
        String ind2 = generateIndentation(2);

        String moduleFeatureExponent = ".ftZ_exponent(ftZ_exponent),";
        String moduleFeatureFraction = ".ftZ_fraction(ftZ_fraction),";

        String sourceCode = "";
        String processed = "";
        String module = MODULE_INSTANCE;

        sourceCode += ind2 + ".clock(clock),\n";
        sourceCode += ind2 + ".voted_class(voted_class),";

        for (int index = 0; index < featureQnt; index++){
            processed = moduleFeatureExponent.replace("Z", Integer.toString(index));
            sourceCode += "\n";

            sourceCode += ind2 + processed;
        }

        for (int index = 0; index < featureQnt; index++){

            if (index == featureQnt - 1){
                int commaPosition = processed.lastIndexOf(",");
                processed = processed.substring(0, commaPosition);
            }
            else {
                processed = moduleFeatureFraction.replace("Z", Integer.toString(index));
            }
            sourceCode += "\n";

            sourceCode += ind2 + processed;
        }

        module = module
                .replace("moduleName", "tree" + treeIndex.toString())
                .replace("ports", sourceCode)
                .replace("ind", ind);

        return module;




//        String tab1 = generateTab(1);
//        String tab2 = generateTab(2);
//
//        String moduleAlias = tab1 + "tree" + treeIndex + " tree" + treeIndex + "(\n";
//        String exponent = IntStream.range(0, featureQnt)
//                .mapToObj(index -> tab2 + ".ft" + index + "_exponent(ft" + index + "_exponent),")
//                .collect(Collectors.joining("\n")
//        );
//        String fraction = IntStream.range(0, featureQnt)
//                .mapToObj(index -> tab2 + ".ft" + index + "_fraction(ft" + index + "_fraction),")
//                .collect(Collectors.joining("\n")
//        );
//
//        String output = "\n "+ tab2 + ".voted_class(voted_class" + treeIndex + "),\n";
//        String clock = tab2 + ".clock(clock)\n";
//
//        return moduleAlias + exponent + "\n" + fraction + output + clock + tab1 + ");\n\n";
    }

    private String generateInitialBlock(Integer featureQnt, Integer classQnt, Boolean debugMode){
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);
        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String counterSetup = tab2 + "counter = 0;\n\n";

        String classSetup = IntStream.range(0, classQnt)
                .mapToObj(
                        index -> tab2 + "class" + String.format("%" + bitwidth +
                                "s", Integer.toBinaryString(index)).replaceAll(" ", "0") + " = 0;"
                )
                .collect(Collectors.joining("\n")
                );


        String initialBlockOpen = tab1 + "initial begin\n\n";
        String initialBlockClose = tab1 + "end\n\n";

        if (debugMode) {
            String readmemExponent = IntStream.range(0, featureQnt)
                    .mapToObj(index -> tab2 + "$readmemb(" + '"' + "dataset/feature" + index + "_exponent.bin" + '"' + ", mem_feature_" + index + "_e);")
                    .collect(Collectors.joining("\n")
                    );

            String readmemFraction = IntStream.range(0, featureQnt)
                    .mapToObj(index -> tab2 + "$readmemb(" + '"' + "dataset/feature" + index + "_fraction.bin" + '"' + ", mem_feature_" + index + "_f);")
                    .collect(Collectors.joining("\n")
                    );

            return initialBlockOpen + classSetup + "\n" + counterSetup + readmemExponent + "\n" + readmemFraction + "\n\n" + initialBlockClose;

        }
        else {
            return initialBlockOpen + classSetup + "\n" + initialBlockClose;
        }
    }

    private String generateAlwaysBlock(Integer featuresQnt ,Integer samplesQnt, Integer classQnt, Integer treeQnt, Boolean debugMode) {
        String tab1 = generateTab(1);
        String tab2 = generateTab(2);
        String tab3 = generateTab(3);

        String alwaysBlockOpen = tab1 + "always @(posedge clock) begin\n";
        String alwaysBlockClose = tab1 + "end\n";
        String moduleClose = "endmodule";

        if (debugMode){
            String conditionalOpen = tab2 + "if (counter < " + samplesQnt + ") begin\n";
            String conditionalClose = tab2 + "end\n";

            String conditionalElse = tab2 + "else begin\n" + tab3 + "$finish;\n" + tab2 + "end";

            String featureExponent = IntStream.range(0, featuresQnt)
                    .mapToObj(index -> tab3 + "ft" + index + "_exponent <= mem_feature_" + index + "_e[counter];")
                    .collect(Collectors.joining("\n")
                    );

            String featureFraction = IntStream.range(0, featuresQnt)
                    .mapToObj(index -> tab3 + "ft" + index + "_fraction <= mem_feature_" + index + "_f[counter];")
                    .collect(Collectors.joining("\n"));

            int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

            String voteAccumulator = "";

            for (int index1 = 0; index1 < classQnt; index1++){
                voteAccumulator += tab3 + "class" + String.format("%" + bitwidth + "s", Integer.toBinaryString(index1)).replaceAll(" ", "0");
                voteAccumulator += " = ";

                for (int index2 = 0; index2 < treeQnt; index2++){
                    voteAccumulator += "voted_class" + index2 + "[" + index1 + "]" + " + ";

                    if (index2 + 1 == treeQnt){
                        voteAccumulator += "class" + String.format("%" + bitwidth + "s", Integer.toBinaryString(index1)).replaceAll(" ", "0");
                        voteAccumulator += ";\n";
                    }
                }
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

            return alwaysBlockOpen +
                    conditionalOpen +
                    featureExponent + "\n" +
                    featureFraction + "\n\n" +
                    voteAccumulator +
                    teste +
                    counterIncrement +
                    conditionalClose +
                    conditionalElse  + "\n" +
                    alwaysBlockClose +
                    moduleClose;
        }
        else {

            int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

            String voteAccumulator = "";

            for (int index1 = 0; index1 < classQnt; index1++){
                voteAccumulator += tab3 + "class" + String.format("%" + bitwidth + "s", Integer.toBinaryString(index1)).replaceAll(" ", "0");
                voteAccumulator += " = ";

                for (int index2 = 0; index2 < treeQnt; index2++){
                    voteAccumulator += "voted_class" + index2 + "[" + index1 + "]" + " + ";

                    if (index2 + 1 == treeQnt){
                        voteAccumulator += "class" + String.format("%" + bitwidth + "s", Integer.toBinaryString(index1)).replaceAll(" ", "0");
                        voteAccumulator += ";\n";
                    }
                }
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

            return alwaysBlockOpen +
                    voteAccumulator +
                    teste +
                    alwaysBlockClose +
                    moduleClose;
        }
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
