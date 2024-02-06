package project.src.java.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* TODO: refatorar tudo, o codigo ta feio */

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private String precision;

    public void execute(int treeQnt, int classQnt, int featureQnt, Settings settings){
        System.out.println("generating controller");

        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        String src = "";

        src += generateHeader(this.MODULE_NAME);

        if (settings.mode.equals("simulation")){
            src += generateImports(treeQnt);
        }
        src += generateIO(featureQnt, classQnt, treeQnt);
        for (int index = 0; index < treeQnt; index++){
            src += generateModuleInstantiation(featureQnt, index);
        }
        src += generateAlwaysBlock(classQnt, treeQnt, false);

        FileBuilder.execute(src, String.format("FPGA/%s_conditional_run/%s.v", settings.dataset, this.MODULE_NAME));
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
        );
        return imports;
    }

    public String generateHeader(String module_name){
        String src = "";

        String[] basicIOPorts = {"clock", "voted", "features"};

        ArrayList<String> ioPorts = new ArrayList<>(List.of(basicIOPorts));

        src += String.format("module %s (\n", module_name);

        for (int index = 0; index <= ioPorts.size(); index++){
            if (index == ioPorts.size()){
                src += ");\n\n";
            }
            else if (index == ioPorts.size() - 1){
                src += tab(1) + ioPorts.get(index) + "\n";
            }
            else {
                src += tab(1) + ioPorts.get(index) + ",\n";
            }
        }
        return src;
    }

    private String generateIO(int featureQnt, int classQnt, int treeQnt){
        String src = "";

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);

        for (int index = 0; index < classQnt; index++){
            src += tab(1);
            src += generatePort(
                "class" + toBinary(index, bitwidth), INTEGER, NONE, 1, true
            );
        }
        src += tab(1) + generatePort("voted", REGISTER, OUTPUT, bitwidth, true);
        src += "\n";

        if (this.precision.equals("integer")){
            src += tab(1) + generatePort("features", WIRE, INPUT, (this.comparedValueBitwidth * featureQnt), false);
        }
        else if (this.precision.equals("decimal")){
            src += tab(1) + generatePort("features", WIRE, INPUT, (this.comparedValueBitwidth * featureQnt * 2), false);
        }
        src += "\n";

        for (int index = 0; index < treeQnt; index++){
            src += tab(1) + generatePort("voted_class" + index, WIRE, NONE, classQnt, false);
            src += "\n";
        }

        src += "\n";

        return src;
    }

    private String generateModuleInstantiation(int featureQnt, int treeIndex){

        String src = "";

        src += tab(2) + ".clock(clock),\n";
        src += tab(2) + ".voted_class(voted_class" + treeIndex + "),\n";

        int offset = 0;

        if (this.precision.equals("integer")){
            for (int index = 0; index < featureQnt; index++) {
                if (index + 1 == featureQnt) {
                    src += tab(2) + String.format(".feature%d(features[%d:%d])", index, (offset + this.comparedValueBitwidth -1), offset);
                } else {
                    src += tab(2) + String.format(".feature%d(features[%d:%d]),\n", index, (offset + this.comparedValueBitwidth -1), offset);
                }
                offset += this.comparedValueBitwidth;
            }
        }
        else if (this.precision.equals("decimal")) {
            for (int index = 0; index < featureQnt; index++) {
                src += tab(2) + String.format(".feature%d_integer(features[%d:%d]),\n", index, (offset + this.comparedValueBitwidth -1), offset);
                offset += this.comparedValueBitwidth;
            }
            for (int index = 0; index < featureQnt; index++) {
                if (index + 1 == featureQnt){
                    src += tab(2) + String.format(".feature%d_decimal(feature[%d:%d])", index, (offset + this.comparedValueBitwidth -1), offset);
                } else {
                    src += tab(2) + String.format(".feature%d_decimal(feature[%d:%d]),\n", index, (offset + this.comparedValueBitwidth -1), offset);
                }

                offset += this.comparedValueBitwidth;
            }
        }
        String module = MODULE_INSTANCE;
        module = module
                .replace("moduleName", "tree" + treeIndex)
                .replace("ports", src)
                .replace("ind", tab(1));

        return module;
    }

    private String generateAlwaysBlock(int classQnt, int treeQnt, boolean debugMode) {

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String always = ALWAYS_BLOCK;

        String src = "";

        int counter = classQnt - 1;
        for (int index1 = 0; index1 < classQnt; index1++){

            String voteCounter = tab(2) + "class" + toBinary(index1, bitwidth) + " <= ";

            for (int index2 = 0; index2 < treeQnt; index2++){
                if (index2 == treeQnt - 1){
                    voteCounter += String.format("voted_class%d[%d];\n", index2, counter);
                }
                else {
                    voteCounter += String.format("voted_class%d[%d] + ", index2, counter);
                }
            }
            counter--;
            src += voteCounter;
        }

        ArrayList<String> classes = new ArrayList<>();
        for (int index = 0; index < classQnt; index++){
            classes.add(String.format("%" + bitwidth + "s", Integer.toBinaryString(index)).replaceAll(" ", "0"));
//            System.out.println();
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
            expression = expression.replace(")(", ") && (");

            body = "voted <= " + bitwidth + "'b" + classes.get(index1) + ";";

            conditional = conditional.replace("x", expression);
            conditional = conditional.replace("y", body);
            conditional = conditional.replace("ind2", tab(3));
            conditional = conditional.replace("ind", tab(2));
            src += conditional;
        }

        always = always.replace("clk", "clock");
        always = always.replace("src", src);
        always = always.replace("ind", tab(1));
        always += "\nendmodule"; /* provisorio */

        return always;
    }
}
