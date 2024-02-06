package project.src.java.approaches.fpga.conditionalEquationMultiplexer;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator extends BasicGenerator {
    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private String precision;

    public void execute(int treeQnt, int classQnt, int featureQnt, Settings settings){
        System.out.println("generating controller");

        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        String src = "";

//        src += generateImports(treeQnt);
        src += generateHeader(this.MODULE_NAME, featureQnt);
        src += generateIO(featureQnt, classQnt, treeQnt);

        for (int index = 0; index < treeQnt; index++){
            src += generateTreeModuleInstantiation(featureQnt, index);
        }
        for (int index = 0; index < classQnt; index++) {
            src += generateModuleAdder(treeQnt, index);
        }

        src += generateModuleMajority(classQnt);
        src += generateEndDelimiters();

        FileBuilder.execute(src, String.format("FPGA/%s_%s_run/controller.v", settings.dataset, settings.approach));
    }

    private String generateImports(int treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
                );
        imports += "\n`include \"adder.v\"";
        imports += "\n`include \"winner.v\"\n";
        return imports;
    }

    public String generateHeader(String module_name, int featureQnt){
        String src = "";

        String[] basicIOPorts = {"voted"};

        ArrayList<String> ioPorts = new ArrayList<>(List.of(basicIOPorts));

        for (int index = 0; index < featureQnt; index++) {
            ioPorts.add(String.format("feature%d", index));
        }

        src += String.format("module %s (\n", module_name);

        for (int index = 0; index <= ioPorts.size(); index++){
            if (index == ioPorts.size()){
                src += ");\n";
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

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String src = "";

        src += tab(1) + generatePort("voted", WIRE, OUTPUT, bitwidth, true);
        src += "\n";

        for (int index = 0; index < featureQnt; index++){
            src += tab(1) + generatePort("feature" + index, WIRE, INPUT, this.comparedValueBitwidth, true);
        }

        src += "\n";
        for (int index = 0; index < treeQnt; index++) {
            src += tab(1) + generatePort("voted_class" + index, WIRE, NONE, classQnt, true);
        }

        src += "\n";

        int sumBits = (int)(Math.log(Math.abs(treeQnt)) / Math.log(2)) + 1; // Logaritmo na base 2
        for (int index = 0; index < classQnt; index++) {
            src += tab(1) + generatePort("sum_class" + index, WIRE, NONE, sumBits, true);
        }

        return src;
    }

    private String generateTreeModuleInstantiation(int featureQnt, int treeIndex){
        String src = "";

        src += tab(2) + ".voted_class(voted_class" + treeIndex + "),\n";

        for (int index = 0; index < featureQnt; index++) {
            if (index + 1 == featureQnt) {
                src += tab(2) + String.format(".feature%d(feature%d)", index, index);
            } else {
                src += tab(2) + String.format(".feature%d(feature%d),\n", index, index);
            }
        }

        String module = MODULE_INSTANCE;
        module = module
                .replace("moduleName", "tree" + treeIndex)
                .replace("ports", src)
                .replace("ind", tab(1));
        return module;
    }

    private String generateModuleAdder(int treeQnt, int classNumber) {
        String src = "";

        src += tab(2) + ".sum(sum_class" + classNumber + "),\n";

        for (int index = 0; index < treeQnt; index++) {
            if (index + 1 == treeQnt) {
                src += tab(2) + String.format(".vote%d(voted_class%d[%d])", index, index, classNumber);
            } else {
                src += tab(2) + String.format(".vote%d(voted_class%d[%d]),\n", index, index, classNumber);
            }
        }

        String module = MODULE_VARIABLE_INSTANCE;
        module = module
                .replace("moduleName", "adder")
                .replace("moduleVariableName", "adder" + classNumber)
                .replace("ports", src)
                .replace("ind", tab(1));
        return module;
    }

    private String generateModuleMajority(int classQnt) {
        String src = "";

        src += tab(2) + ".voted(voted),\n";

        for (int index = 0; index < classQnt; index++) {
            if (index + 1 == classQnt) {
                src += tab(2) + String.format(".class%d_votes(sum_class%d)", index, index);
            } else {
                src += tab(2) + String.format(".class%d_votes(sum_class%d),\n", index, index);
            }
        }

        String module = MODULE_INSTANCE;

        module = module
                .replace("moduleName", "majority")
                .replace("ports", src)
                .replace("ind", tab(1));

        return module;
    }
}
