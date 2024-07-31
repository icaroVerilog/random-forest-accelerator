package project.src.java.approaches.fpga.pipeline;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;

import java.util.ArrayList;
import java.util.List;

public class ControllerGenerator extends BasicGenerator {
    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private Integer maxDepth;
    private String approach;

    public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCEM settings){
        System.out.println("generating controller");

        this.maxDepth = 0;
        this.approach = settings.approach;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

        for (int index = 0; index < trees.size(); index++) {
            if (trees.get(index).getMaxDepth() > this.maxDepth) {
                this.maxDepth = trees.get(index).getMaxDepth();
            }
        }

        String src = "";

        src += generateImports(trees.size());
        src += generateHeader(this.MODULE_NAME, featureQnt);
        src += generateIO(featureQnt, classQnt, trees.size());

        for (int index = 0; index < trees.size(); index++){
            src += generateTreeModuleInstantiation(featureQnt, index);
        }
        for (int index = 0; index < classQnt; index++) {
            src += generateModuleAdder(trees.size(), index);
        }

        src += generateModuleMajority(classQnt);
        src += generateAlways(trees.size());
        src += generateEndDelimiters();

        FileBuilder.execute(src, String.format("FPGA/%s_%s_%dtree_%sdeep_run/controller.v", settings.dataset, settings.approach, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth));
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

    private String generateHeader(String module_name, int featureQnt){
        String src = "";

        String[] basicIOPorts = {
            "reset",
            "clock",
            "compute_vote",
            "forest_vote"
        };

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
        int outputBitwidth = (int)(Math.log(Math.abs(treeQnt)) / Math.log(2)) + 1; // Logaritmo na base 2
        String src = "";

        int sumBitwidth = (int) Math.ceil(Math.sqrt(treeQnt));
        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, bitwidth, true);
        src += "\n";

        for (int index = 0; index < featureQnt; index++){
            src += tab(1) + generatePort("feature" + index, WIRE, INPUT, this.comparedValueBitwidth, true);
        }
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, outputBitwidth, true);
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += "\n";

        for (int index = 0; index < treeQnt; index++) {
            src += tab(1) + generatePort("compute_vote" + index, WIRE, NONE, 1, true);
        }
        src += "\n";

        for (int index = 0; index < treeQnt; index++) {
            src += tab(1) + generatePort("voted_class" + index, WIRE, NONE, classQnt, true);
        }
        src += "\n";

        for (int index = 0; index < treeQnt; index++) {
            src += tab(1) + generatePort("sum_class" + index, WIRE, NONE, sumBitwidth, true);
        }
        src += "\n";

        src += tab(1) + generatePort("r_forest_vote", WIRE, NONE, outputBitwidth, true);

        return src;
    }

    private String generateTreeModuleInstantiation(int featureQnt, int treeIndex){
        String src = "";

        src += tab(2) + ".clock(clock),\n";
        src += tab(2) + ".reset(reset),\n";

        src += tab(2) + String.format(".voted_class(voted_class%d),\n", treeIndex);
        src += tab(2) + String.format(".compute_vote(compute_vote%d),\n", treeIndex);

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

    private String generateModuleAdder(int treeQnt, int classNumber){
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

    private String generateModuleMajority(int classQnt){
        String src = "";

        src += tab(2) + ".voted(r_forest_vote),\n";

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

    private String generateAlways(int treeQnt){

        String computeVoteConditional = CONDITIONAL3;
        String computeVoteExpr = "";
        String computeVoteBody = "";

        for (int index = 0; index < treeQnt; index++) {
            if (index == treeQnt - 1){
                computeVoteExpr += String.format("compute_vote%d", index);
            } else {
                computeVoteExpr += String.format("compute_vote%d && ", index);
            }
        }

        computeVoteBody += tab(3) + "forest_vote <= r_forest_vote;\n";
        computeVoteBody += tab(3) + "compute_vote <= 1'b1;\n";

        computeVoteConditional = computeVoteConditional
            .replace("x", computeVoteExpr)
            .replace("`", computeVoteBody)
            .replace("ind", tab(2));

        String computeVoteConditionalElse = CONDITIONAL_ELSE;
        String computeVoteConditionalElseBody = tab(3) + "compute_vote <= 1'b0;\n";

        computeVoteConditionalElse = computeVoteConditionalElse
                .replace("y", computeVoteConditionalElseBody)
                .replace("ind", tab(2));

        String always = ALWAYS_BLOCK2;
        String src = computeVoteConditional + computeVoteConditionalElse;

        always = always
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", src)
            .replace("ind", tab(1));

        return always;
    }
}
