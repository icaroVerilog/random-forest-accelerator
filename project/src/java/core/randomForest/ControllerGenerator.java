package project.src.java.core.randomForest;

import project.src.java.core.BasicGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.util.List;

public class ControllerGenerator extends BasicGenerator {
    private final String MODULE_NAME = "controller";

    private Integer precision;
    private Integer maxDepth;
    private String approach;

    public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCli settings){
        System.out.println("generating controller");

        switch (settings.inferenceParameters.precision){
            case "double":
                this.precision = DOUBLE_PRECISION;
                break;
            case "normal":
                this.precision = NORMAL_PRECISION;
                break;
            case "half":
                this.precision = HALF_PRECISION;
                break;
            default:
                this.precision = 0;
                break;
        }

        this.maxDepth = 0;
        this.approach = settings.approach;

        for (int index = 0; index < trees.size(); index++) {
            if (trees.get(index).getMaxDepth() > this.maxDepth) {
                this.maxDepth = trees.get(index).getMaxDepth();
            }
        }

        String src = "";

        src += generateHeader(this.MODULE_NAME, featureQnt);
        src += generateIO(featureQnt, classQnt, trees.size());

        for (int index = 0; index < trees.size(); index++){
            src += generateTreeModuleInstantiation(featureQnt, index);
        }
        for (int index = 0; index < classQnt; index++) {
            src += generateAdderModuleInstantiation(trees.size(), index);
        }

        src += generatorMajorityModuleInstantiation(classQnt);
        src += generateAlwaysBlock(trees.size());

        FileBuilder.execute(
            src, String.format(
                "output/%s_%s_%dtree_%sdeep_run/controller.v",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            ),
            false
        );
    }

    private String generateHeader(String module_name, int featureQnt){
        String src = "";

        src += "module controller (\n";

        src += tab(1) + "clock,\n";
        src += tab(1) + "reset,\n";
        src += tab(1) + "compute_vote,\n";
        src += tab(1) + "forest_vote,\n";
        src += tab(1) + "features\n";
        src += ");\n";

        return src;
    }

    private String generateIO(int featureQnt, int classQnt, int treeQnt){
        int outputBitwidth = (int)(Math.log(Math.abs(classQnt)) / Math.log(2)) + 1; // Logaritmo na base 2
        String src = "";

        int sumBitwidth = (int) Math.ceil(Math.sqrt(treeQnt));

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";

        src += tab(1) + generatePort("features", WIRE, INPUT, this.precision * featureQnt, true);

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

        for (int index = 0; index < classQnt; index++) {
            src += tab(1) + generatePort("sum_class" + index, WIRE, NONE, sumBitwidth, true);
        }
        src += "\n";

        src += tab(1) + generatePort("w_forest_vote", WIRE, NONE, outputBitwidth, true);

        return src;
    }

    private String generateTreeModuleInstantiation(int featureQnt, int treeIndex){
        String src = "";

        src += tab(2) + ".clock(clock),\n";
        src += tab(2) + ".reset(reset),\n";
        src += tab(2) + String.format(".voted_class(voted_class%d),\n", treeIndex);
        src += tab(2) + String.format(".compute_vote(compute_vote%d),\n", treeIndex);
        src += tab(2) + ".features(features)";

        String module = MODULE_INSTANCE;
        module = module
            .replace("moduleName", "tree" + treeIndex)
            .replace("ports", src)
            .replace("ind", tab(1));

        return module;
    }

    private String generateAdderModuleInstantiation(int treeQnt, int classNumber){
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

    private String generatorMajorityModuleInstantiation(int classQnt){
        String src = "";

        src += tab(2) + ".voted(w_forest_vote),\n";

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

    private String generateAlwaysBlock(int treeQnt){

        String computeVoteConditional = CONDITIONAL_BLOCK;
        String computeVoteExpr = "";
        String computeVoteBody = "";

        for (int index = 0; index < treeQnt; index++) {
            if (index == treeQnt - 1){
                computeVoteExpr += String.format("compute_vote%d", index);
            } else {
                computeVoteExpr += String.format("compute_vote%d && ", index);
            }
        }

        computeVoteBody += tab(3) + "forest_vote <= w_forest_vote;\n";
        computeVoteBody += tab(3) + "compute_vote <= 1'b1;\n";

        computeVoteConditional = computeVoteConditional
            .replace("x", computeVoteExpr)
            .replace("`", computeVoteBody)
            .replace("ind", tab(2));

        String computeVoteConditionalElse = CONDITIONAL_ELSE_BLOCK;
        String computeVoteConditionalElseBody = tab(3) + "compute_vote <= 1'b0;\n";

        computeVoteConditionalElse = computeVoteConditionalElse
                .replace("y", computeVoteConditionalElseBody)
                .replace("ind", tab(2));

        String always = ALWAYS_BLOCK;
        String src = computeVoteConditional + "\n" + computeVoteConditionalElse;

        always = always
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", src)
            .replace("ind", tab(1));

        return always + "endmodule";
    }
}
