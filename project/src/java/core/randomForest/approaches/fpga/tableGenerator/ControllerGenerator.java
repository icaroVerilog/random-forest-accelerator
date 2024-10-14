package project.src.java.core.randomForest.approaches.fpga.tableGenerator;

import project.src.java.core.randomForest.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import project.src.java.util.executionSettings.CLI.Table.SettingsCliT;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private Integer precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;

    public void execute(int classBitwidth, int featureQuantity, SettingsCli settings, boolean offlineMode){
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

        this.comparedColumnBitwidth = 8;
        this.tableIndexerBitwidth   = 32;

        String src = "";

        src += generateModuleImports();
        src += generateHeader(MODULE_NAME, offlineMode);

//        if (mode.equals("simulation")){
//            src += generateModuleImports();
//        }


        src += generateIO(featureQuantity, classBitwidth, offlineMode);
        src += generateValidationTableInstantiation(featureQuantity, offlineMode);
        src += generateAlways();

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

    private String generateModuleImports(){
        return "`include \"validation_table.v\"\n";
    }

    private String generateHeader(String moduleName, boolean offlineMode){
        String src = "";

        String[] ioPorts = {
            "clock",
            "reset",
            "feature",
            "voted",
            "read_new_sample",
//            "new_table_entry",
//            "new_table_entry_counter",
            "compute_vote_flag"
        };

        src += String.format("module %s (\n", moduleName);

        for (int index = 0; index <= ioPorts.length; index++){
            if (index == ioPorts.length){
                src += ");\n\n";
            }
            else if (index == ioPorts.length - 1){
                src += tab(1) + ioPorts[index] + "\n";
            }
            else {
                src += tab(1) + ioPorts[index] + ",\n";
            }
        }
        return src;
    }

    private String generateIO(int featureQuantity, int classBitwidth, boolean offlineMode){
        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);

        src += "\n";

        int featuresBusBitwidth = this.precision  * featureQuantity;

        src += tab(1) + generatePort("feature", WIRE, INPUT, featuresBusBitwidth, true);

        if (!offlineMode){
            src += tab(1) + generatePort("new_table_entry", WIRE, INPUT, 64, true);
            src += tab(1) + generatePort("new_table_entry_counter", WIRE, INPUT, 16, true);
        }
        src += "\n";
        src += tab(1) + generatePort("voted", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("w_forest_vote", WIRE, NONE, classBitwidth, true);
        src += "\n";

        return src;
    }

    private String generateValidationTableInstantiation(int featureQuantity, boolean offlineMode){

        int featureBusBitwidth = this.precision * featureQuantity;

        String src = "";

        src += tab(1) + "validation_table validation_table (\n";
        src += tab(2) + String.format(".%s(%s),\n", "clock", "clock");
        src += tab(2) + String.format(".%s(%s),\n", "reset", "reset");
        src += tab(2) + String.format(".%s(%s),\n", "forest_vote", "w_forest_vote");

        if (!offlineMode){
            src += tab(2) + String.format(".%s(%s),\n", "read_new_sample", "read_new_sample");
            src += tab(2) + String.format(".%s(%s),\n", "new_table_entry", "new_table_entry");
        }
        src += tab(2) + String.format(".%s(%s),\n", "compute_vote", "compute_vote");
        src += tab(2) + String.format(".%s(%s),\n", "read_new_sample", "read_new_sample");

        src += tab(2) + String.format(".feature(feature[%d:%d])\n", (featureBusBitwidth - 1), 0);
        src += tab(1) + ");\n";

        return src;
    }

    private String generateAlways(){

        String computeVoteConditional = CONDITIONAL_BLOCK;
        String computeVoteExpr = "";
        String computeVoteBody = "";

        computeVoteExpr += "compute_vote";

        computeVoteBody += tab(3) + "voted <= w_forest_vote;\n";
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
        String src = computeVoteConditional + computeVoteConditionalElse;

        always = always
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("src", src)
            .replace("ind", tab(1));

        return always + "\nendmodule";
    }
}
