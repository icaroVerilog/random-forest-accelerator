package project.src.java.approaches.fpga.equationGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private String precision;

    public void execute(
            Integer treeQnt,
            Integer classQnt,
            Integer featureQnt,
            Boolean debugMode,
            Settings settings
    ){
        System.out.println("generating controller");

        this.precision = settings.precision;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        String src = "";

        src += generateImports(treeQnt);
        src += generateIO(featureQnt, classQnt, treeQnt, debugMode);

        for (int index = 0; index < treeQnt; index++){
            src += generateModuleInstantiation(featureQnt, index);
        }
        for (int index = 0; index < classQnt; index++) {
            src += generateModuleAdder(treeQnt, index);
        }

        src += generateModuleWinner(classQnt);
        src += "\nendmodule";

        FileBuilder.execute(src, String.format("FPGA/%s_equation_run/controller.v", settings.dataset));
    }

    private String generateModuleAdder(Integer treeQnt, int classNumber) {
        String indentation1 = tab(1);
        String indentation2 = tab(2);

        String votes = ".voteZ(voted_classZ["+classNumber+"]),";
        String src = "";
        String processed = "";
        String module = MODULE_VARIABLE_INSTANCE;

        for (int index = 0; index < treeQnt; index++){
            processed = votes.replace("Z", Integer.toString(index));
            src += "\n";

            src += indentation2 + processed;
        }
        src += "\n"+ indentation2 + ".sum(sum_class"+classNumber+")";
        module = module
                .replace("moduleName", "adder")
                .replace("moduleVariableName", "adder"+classNumber)
                .replace("ports", src)
                .replace("ind", indentation1);

        return module;
    }

    private String generateModuleWinner(Integer classQnt) {
        String indentation1 = tab(1);
        String indentation2 = tab(2);

        String votes = ".vZ(sum_classZ),";
        String src = "";
        String processed = "";
        String module = MODULE_INSTANCE;

        for (int index = 0; index < classQnt; index++){
            processed = votes.replace("Z", Integer.toString(index));
            src += "\n";
            src += indentation2 + processed;
        }
        src += "\n" + indentation2 + ".Winner(voted)";
        module = module
                .replace("moduleName", "winner")
                .replace("ports", src)
                .replace("ind", indentation1);

        return module;
    }

    private String generateImports(Integer treeQuantity){
        String imports = IntStream.range(0, treeQuantity)
                .mapToObj(index -> "`include " + "\"" + "tree" + index + ".v" + "\"")
                .collect(Collectors.joining("\n")
                );
        imports += "\n`include \"adder.v\"";
        imports += "\n`include \"winner.v\"\n";
        return imports;
    }

    private String generateIO(Integer featureQnt, Integer classQnt, Integer treeQnt, Boolean debugMode){

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        String ind1 = tab(1);

        ArrayList<String> moduleIO = new ArrayList<>();
        String src;

        moduleIO.add("voted");

        for (int index = 0; index < featureQnt; index++){
            moduleIO.add("ft" + index );
        }

        src = "\n\n";
        src += generateModule(
                "controller",
                moduleIO
        );

        src += "\n\n";

        for (int index = 0; index < classQnt; index++){
            src += tab(1);
            src += generatePort(
                    "class" + generateBinaryNumber(index, bitwidth),
                    INTEGER,
                    NONE,
                    1,
                    true
            );
        }

        src += ind1 + generatePort("voted", NONE, OUTPUT, bitwidth, true);
        src += "\n";

        for (int index = 0; index < featureQnt; index++){
            src += tab(1);
            src += generatePort(
                    "ft" + index,
                    WIRE,
                    INPUT,
                    this.comparedValueBitwidth,
                    false
            );
            src += "\n";
        }

        src += "\n";
        for (int index = 0; index < treeQnt; index++) {
            src += tab(1);
            src += generatePort("voted_class" + index,
                    WIRE,
                    NONE,
                    (int) Math.ceil(Math.sqrt(classQnt)),
                    false
            );
            src += "\n";

        }

        int sumBits = (int)(Math.log(Math.abs(treeQnt)) / Math.log(2)) + 1; // Logaritmo na base 2
        for (int index = 0; index < classQnt; index++) {
            src += tab(1);
            src += generatePort("sum_class"+index,
                    WIRE,
                    NONE,
                    sumBits,
                    false
            );
            src += "\n";
        }
        src += "\n";

        return src;
    }

    private String generateModuleInstantiation(Integer featureQnt, Integer treeIndex){

        String moduleFeatureExponent = ".ftZ(ftZ),";
        String src = "";
        String processed = "";
        String module = MODULE_INSTANCE;

        for (int index = 0; index < featureQnt; index++){
            processed = moduleFeatureExponent.replace("Z", Integer.toString(index));
            src += "\n";

            src += tab(2) + processed;
        }
        src += "\n" + tab(2) + ".voted_class(voted_class" + treeIndex + ")";

        module = module
                .replace("moduleName", "tree" + treeIndex.toString())
                .replace("ports", src)
                .replace("ind", tab(1));

        return module;
    }
}
