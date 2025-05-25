package project.src.java.core.randomForest.table.parallelTableGenerator;

import project.src.java.core.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private Integer precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;

    public void execute(int featureQuantity, int treeQuantity, int classQnt, SettingsCli settings){
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

        // TODO: Ajustar para serem parâmetros variáveis
        this.comparedColumnBitwidth = 8;
        this.tableIndexerBitwidth   = 32;

        String src = "";

        src += generateHeader();
        src += generateIO(featureQuantity, classQnt);
        src += generateInternalVariables(treeQuantity, classQnt);
        src += generateTreeModuleInstantiation(treeQuantity);

        for (int index = 0; index < classQnt; index++) {
            src += generateAdderModuleInstantiation(treeQuantity, index);
        }

        src += generatorMajorityModuleInstantiation(classQnt);
        src += generateAlwaysBlock(classQnt, treeQuantity);

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


    private String generateHeader(){
        String src = "";

        src += "module controller (\n";
        src += tab(1) + "clock,\n";
        src += tab(1) + "reset,\n";
        src += tab(1) + "forest_vote,\n";
        src += tab(1) + "compute_vote,\n";
        src += tab(1) + "features\n";
        src += ");\n";

        return src;
    }

    private String generateIO(int featureQuantity, int classQuantity){
        int featuresBusBitwidth = this.precision * featureQuantity;
        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));

        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("features", WIRE, INPUT, featuresBusBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += "\n";

        return src;
    }

    private String generateInternalVariables(int treeQuantity, int classQuantity){
        int sumBitwidth = (int) Math.ceil(Math.sqrt(treeQuantity));
        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));

        String src = "";

        for (int index = 0; index < treeQuantity; index++) {
            src += tab(1) + generatePort(String.format("tree%d_compute_vote_w", index), WIRE, NONE, 1, true);
        }
        src += "\n";

        for (int index = 0; index < classQuantity; index++) {
            src += tab(1) + generatePort("sum_class" + index, WIRE, NONE, sumBitwidth, true);
        }
        src += "\n";

        for (int index = 0; index < treeQuantity; index++) {
            src += tab(1) + generatePort(String.format("tree%d_vote_w", index), WIRE, NONE, this.tableIndexerBitwidth, true);
        }
        src += tab(1) + generatePort("forest_vote_w", WIRE, NONE, classBitwidth, true);
        src += "\n";

        for (int index = 0; index < treeQuantity; index++) {
            src += tab(1) + generatePort(String.format("tree%d_vote", index), REGISTER, NONE, this.tableIndexerBitwidth, true);
        }
        src += "\n";

        for (int index = 0; index < treeQuantity; index++) {
            src += tab(1) + generatePort(String.format("tree%d_compute_vote", index), REGISTER, NONE, 1, true);
        }
        src += "\n";
        src += tab(1) + generatePort("forest_vote_available", REGISTER, NONE, 1, true);
        src += tab(1) + generatePort("start_next", REGISTER, NONE, 1, true);
        src += "\n";

        return src;
    }

    private String generateTreeModuleInstantiation(int treeQuantity){
        String src = "";

        for (int index = 0; index < treeQuantity; index++) {
            src += tab(1) + String.format("tree%d tree%d(\n", index, index);
            src += tab(2) + ".clock(clock),\n";
            src += tab(2) + ".reset(reset),\n";
            src += tab(2) + ".start_next(start_next),\n";
            src += tab(2) + String.format(".voted_class(tree%d_vote_w),\n", index);
            src += tab(2) + String.format(".compute_vote(tree%d_compute_vote_w),\n", index);
            src += tab(2) + ".features(features)\n";
            src += tab(1) + ");\n\n";
        }
        return src;
    }

    private String generateAdderModuleInstantiation(int treeQnt, int classNumber){
        String src = "";

        src += tab(2) + ".sum(sum_class" + classNumber + "),\n";

        for (int index = 0; index < treeQnt; index++) {
            if (index + 1 == treeQnt) {
                src += tab(2) + String.format(".vote%d(tree%d_vote[%d])", index, index, classNumber);
            } else {
                src += tab(2) + String.format(".vote%d(tree%d_vote[%d]),\n", index, index, classNumber);
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

        src += tab(2) + ".voted(forest_vote_w),\n";

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

    private String generateAlwaysBlock(int classQuantity, int treeQuantity){
        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));

        String reset = CONDITIONAL_BLOCK;
        String resetExpr = "reset";
        String resetBody = "";

        resetBody += tab(3) + "compute_vote <= 1'b0;\n";
        resetBody += tab(3) + "start_next <= 1'b0;\n";
        resetBody += tab(3) + "forest_vote_available <= 1'b0;\n";

        for (int index = 0; index < treeQuantity; index++) {
            resetBody += tab(3) + String.format(
                "tree%d_compute_vote <= 1'b0;\n",
                index
            );
        }

        for (int index = 0; index < treeQuantity; index++) {
            resetBody += tab(3) + String.format(
                "tree%d_vote <= %d'b%s;\n",
                index,
                this.tableIndexerBitwidth,
                toBin(0, this.tableIndexerBitwidth)
            );
        }

        resetBody += tab(3) + String.format(
            "forest_vote <= %d'b%s;\n",
            classBitwidth,
            toBin(0, classBitwidth)
        );

        reset = reset
            .replace("x", resetExpr)
            .replace("`", resetBody)
            .replace("ind", tab(2));


        String triggerTreeComputeVoteConditionals = "";

        for (int index = 0; index < treeQuantity; index++) {
            String triggerTreeComputeVoteConditional = CONDITIONAL_BLOCK;
            String triggerTreeComputeVoteExpr = String.format(
                "tree%d_compute_vote_w",
                index
            );
            String triggerTreeComputeVoteBody = String.format(
                tab(4) + "tree%d_compute_vote <= 1'b1;\n",
                index
            );

            triggerTreeComputeVoteConditional = triggerTreeComputeVoteConditional
                .replace("x", triggerTreeComputeVoteExpr)
                .replace("`", triggerTreeComputeVoteBody)
                .replace("ind", tab(3));

            triggerTreeComputeVoteConditionals += triggerTreeComputeVoteConditional + "\n";
        }

        String computeVoteVerifyConditional = CONDITIONAL_BLOCK;
        String computeVoteVerifyConditionalExpr = "";
        String computeVoteVerifyConditionalBody = "";

        for (int index = 0; index < treeQuantity; index++) {
            if (index + 1 == treeQuantity){
                computeVoteVerifyConditionalExpr += String.format(
                    "tree%d_compute_vote",
                    index
                );
            } else {
                computeVoteVerifyConditionalExpr += String.format(
                    "tree%d_compute_vote && ",
                    index
                );
            }
        }

        for (int index = 0; index < treeQuantity; index++) {
            computeVoteVerifyConditionalBody += String.format(
                tab(4) + "tree%d_vote <= tree%d_vote_w;\n",
                index,
                index
            );
        }

        for (int index = 0; index < treeQuantity; index++) {
            computeVoteVerifyConditionalBody += String.format(
                tab(4) + "tree%d_compute_vote <= 1'b0;\n",
                index
            );
        }
        computeVoteVerifyConditionalBody += tab(4) + "forest_vote_available <= 1'b1;\n";

        computeVoteVerifyConditional = computeVoteVerifyConditional
            .replace("x", computeVoteVerifyConditionalExpr)
            .replace("`", computeVoteVerifyConditionalBody)
            .replace("ind", tab(3));


        String forestVoteAvailableConditional = CONDITIONAL_BLOCK;
        String forestVoteAvailableConditionalExpr = "forest_vote_available";
        String forestVoteAvailableConditionalBody = "";

        forestVoteAvailableConditionalBody += tab(4) + "forest_vote <= forest_vote_w;\n";
        forestVoteAvailableConditionalBody += tab(4) + "compute_vote <= 1'b1;\n";
        forestVoteAvailableConditionalBody += tab(4) + "forest_vote_available <= 1'b0;\n";

        forestVoteAvailableConditional = forestVoteAvailableConditional
            .replace("x", forestVoteAvailableConditionalExpr)
            .replace("`", forestVoteAvailableConditionalBody)
            .replace("ind", tab(3));

        String forestVoteRestartProcessConditional = CONDITIONAL_BLOCK;
        String forestVoteRestartProcessConditionalBody = "";
        forestVoteRestartProcessConditionalBody += tab(5) + "start_next <= 1'b1;\n";
        forestVoteRestartProcessConditionalBody += tab(5) + "compute_vote <= 1'b0;\n";

        forestVoteRestartProcessConditional = forestVoteRestartProcessConditional
            .replace("x", "compute_vote")
            .replace("`", forestVoteRestartProcessConditionalBody)
            .replace("ind", tab(4));


        String forestVoteRestartProcessConditionalElse = CONDITIONAL_ELSE_BLOCK;
        String forestVoteRestartProcessConditionalElseBody = "";
        forestVoteRestartProcessConditionalElseBody += tab(5) + "start_next <= 1'b0;\n";

        forestVoteRestartProcessConditionalElse = forestVoteRestartProcessConditionalElse
            .replace("y", forestVoteRestartProcessConditionalElseBody)
            .replace("ind", tab(4));


        String forestVoteAvailableConditionalElse = CONDITIONAL_ELSE_BLOCK;
        forestVoteAvailableConditionalElse = forestVoteAvailableConditionalElse
            .replace("y", forestVoteRestartProcessConditional + "\n" + forestVoteRestartProcessConditionalElse)
            .replace("ind", tab(3));


        String computeVoteConditionalElse = CONDITIONAL_ELSE_BLOCK;
        String computeVoteConditionalElseBody = "";

        computeVoteConditionalElseBody += triggerTreeComputeVoteConditionals + "\n";
        computeVoteConditionalElseBody += computeVoteVerifyConditional + "\n\n";
        computeVoteConditionalElseBody += forestVoteAvailableConditional + "\n";
        computeVoteConditionalElseBody += forestVoteAvailableConditionalElse;

        computeVoteConditionalElse = computeVoteConditionalElse
            .replace("y", computeVoteConditionalElseBody)
            .replace("ind", tab(2));

        String always = ALWAYS_BLOCK;
        String src = reset + "\n" + computeVoteConditionalElse;

        always = always
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", src)
            .replace("ind", tab(1));

        return always + "endmodule";
    }
}
