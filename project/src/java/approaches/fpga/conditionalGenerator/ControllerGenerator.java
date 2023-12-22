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

        for (int index = 0; index < treeQnt; index++){
            sourceCode += generateModuleInstantiation(featureQnt, index);
        }

//        sourceCode += generateInitialBlock(featureQnt, classQnt, debugMode);
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

        String ind1 = tab(1);

        ArrayList<String> moduleIO = new ArrayList<>();
        String sourceCode;

        moduleIO.add("clock");
        moduleIO.add("voted");

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
            sourceCode += tab(1);
            sourceCode += generatePort(
                "class" + generateBinaryNumber(index, bitwidth),
                INTEGER,
                NONE,
                1,
                true
            );
        }

        sourceCode += ind1 + generatePort("clock", WIRE, INPUT, 1, true);
        sourceCode += ind1 + generatePort("voted", REGISTER, OUTPUT, bitwidth, true);
        sourceCode += "\n";

        for (int index = 0; index < featureQnt; index++){
            sourceCode += tab(1);
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
            sourceCode += tab(1);
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
            sourceCode += tab(1);
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

    private String generateModuleInstantiation(Integer featureQnt, Integer treeIndex){

        String indentation1 = tab(1);
        String indentation2 = tab(2);

        String moduleFeatureExponent = ".ftZ_exponent(ftZ_exponent),";
        String moduleFeatureFraction = ".ftZ_fraction(ftZ_fraction),";

        String sourceCode = "";
        String processed = "";
        String module = MODULE_INSTANCE;

        sourceCode += indentation2 + ".clock(clock),\n";
        sourceCode += indentation2 + ".voted_class(voted_class" + treeIndex + "),";

        for (int index = 0; index < featureQnt; index++){
            processed = moduleFeatureExponent.replace("Z", Integer.toString(index));
            sourceCode += "\n";

            sourceCode += indentation2 + processed;
        }

        for (int index = 0; index < featureQnt; index++){

            if (index == featureQnt - 1){
                processed = moduleFeatureFraction.replace("Z", Integer.toString(index));
                int commaPosition = processed.lastIndexOf(",");
                processed = processed.substring(0, commaPosition);
            }
            else {
                processed = moduleFeatureFraction.replace("Z", Integer.toString(index));
            }
            sourceCode += "\n";

            sourceCode += indentation2 + processed;
        }

        module = module
                .replace("moduleName", "tree" + treeIndex.toString())
                .replace("ports", sourceCode)
                .replace("ind", indentation1);

        return module;
    }

    private String generateInitialBlock(Integer featureQnt, Integer classQnt, Boolean debugMode){
        String tab1 = tab(1);
        String tab2 = tab(2);
        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String counterSetup = tab2 + "counter = 0;\n\n";

        String classSetup = IntStream.range(0, classQnt)
                .mapToObj(index -> tab2 + "class" + String.format("%" + bitwidth +
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

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String always = ALWAYS_BLOCK;
        String ind1 = tab(1);
        String ind2 = tab(2);
        String ind3 = tab(3);

        String sourceCode = "";



        for (int index1 = 0; index1 < classQnt; index1++){

            String voteCounter = ind2 + "class" + generateBinaryNumber(index1, bitwidth) + " <= ";

            for (int index2 = 0; index2 < treeQnt; index2++){
                if (index2 == treeQnt - 1){
                    voteCounter += "voted_class" + index2 + "[" + index1 + "];\n";
                }
                else {
                    voteCounter += "voted_class" + index2 + "[" + index1 + "] + ";
                }
            }
            sourceCode += voteCounter;
        }

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQnt; index++){
            classes.add(String.format("%" + bitwidth + "s", Integer.toBinaryString(index)).replaceAll(" ", "0"));
        }

        for (int index1 = 0; index1 < classQnt; index1++) {

            String conditional = CONDITIONAL;
            String expression = "";
            String body = "";
            String comparrison = "";

            expression = "x";

            for (int index2 = 0; index2 < classQnt; index2++) {
                if (Objects.equals(classes.get(index1), classes.get(index2))) {
                    continue;
                }
                else {
                    comparrison += "(class" + classes.get(index1) + " > class" + classes.get(index2) + ")";
                }
            }

            expression = expression.replace("x", comparrison);
            expression = expression.replace(")(", ") & (");

            body = "voted <= " + bitwidth + "'b" + classes.get(index1) + ";";

            conditional = conditional.replace("x", expression);
            conditional = conditional.replace("y", body);
            conditional = conditional.replace("ind2", ind3);
            conditional = conditional.replace("ind", ind2);
            sourceCode += conditional;
        }

        always = always.replace("clk", "clock");
        always = always.replace("src", sourceCode);
        always = always.replace("ind", ind1);
        always += "\nendmodule"; /* provisorio */

        return always;
    }
}
