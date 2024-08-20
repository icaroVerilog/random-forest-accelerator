package project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer;

import project.src.java.core.randomForest.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCEM;

import java.util.ArrayList;
import java.util.List;

public class ControllerGenerator extends BasicGenerator {
    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private String approach;

    public void execute(int treeQnt, int classQnt, int featureQnt, SettingsCEM settings){
        System.out.println("generating controller");

        this.approach = settings.approach;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        String src = "";

        src += generateImports(treeQnt);
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

        FileBuilder.execute(
            src, String.format(
                "output/%s_%s_%dtree_%sdeep_run/controller.v",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            )
        );
    }

    private String generateImports(int treeQuantity){
        String src = "";

        for (int index = 0; index < treeQuantity; index++) {
            src += String.format("`include \"tree%d.v\"\n", index);
        }
        src += "`include \"adder.v\"\n";
        src += "`include \"majority.v\"\n\n";

        return src;
    }

    public String generateHeader(String module_name, int featureQnt){
        String src = "";

        String[] basicIOPorts;
        if (this.approach.equals("conditional")){
            basicIOPorts = new String[]{"clock","voted"};
        } else {
            basicIOPorts = new String[]{"voted"};
        }

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
        String src = "";

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        src += tab(1) + generatePort("voted", WIRE, OUTPUT, bitwidth, true);
        if (this.approach.equals("conditional")){
            src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        }
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

        if (this.approach.equals("conditional")){
            src += tab(2) + ".clock(clock),\n";
        }

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
