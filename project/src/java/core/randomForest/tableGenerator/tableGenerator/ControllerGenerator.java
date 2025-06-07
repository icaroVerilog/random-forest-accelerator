package project.src.java.core.randomForest.tableGenerator.tableGenerator;

import project.src.java.core.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private Integer precision;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;

    public void execute(int classQnt, int featureQnt, SettingsCli settings){
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

        src += generateHeader();
        src += generateIO(classQnt, featureQnt);
        src += generateValidationTableInstantiation();
        src += generateAlwaysBlock(classQnt);

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

    private String generateIO(int classQnt, int featureQnt){
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));
        int featuresBusBitwidth = this.precision * featureQnt;

        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("features", WIRE, INPUT, featuresBusBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("forest_vote", REGISTER, OUTPUT, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += "\n";
        src += tab(1) + generatePort("forest_vote_w", WIRE, NONE, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote_w", WIRE, NONE, 1, true);
        src += "\n";

        return src;
    }

    private String generateValidationTableInstantiation(){
        String src = "";

        src += tab(1) + "validation_table validation_table (\n";
        src += tab(2) + ".clock(clock),\n";
        src += tab(2) + ".reset(reset),\n";
        src += tab(2) + ".forest_vote(forest_vote_w),\n";
        src += tab(2) + ".compute_vote(compute_vote_w),\n";
        src += tab(2) + ".features(features)\n";
        src += tab(1) + ");\n";
        return src;
    }

    private String generateAlwaysBlock(int classQnt){
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));
        String computeVoteConditional = CONDITIONAL_BLOCK;
        String computeVoteExpr = "";
        String computeVoteBody = "";

        computeVoteExpr += "reset";

        computeVoteBody += tab(3) + "compute_vote <= 1'b0;\n";
        computeVoteBody += tab(3) + String.format(
            "forest_vote <= %d'b%s;\n",
            classBitwidth,
            toBin(0, classBitwidth)
        );

        computeVoteConditional = computeVoteConditional
            .replace("x", computeVoteExpr)
            .replace("`", computeVoteBody)
            .replace("ind", tab(2));


        String computeVoteConditionalElse = CONDITIONAL_ELSE_BLOCK;
        String computeVoteConditionalElseBody = "";
        computeVoteConditionalElseBody += tab(3) + "forest_vote <= forest_vote_w;\n";
        computeVoteConditionalElseBody += tab(3) + "compute_vote <= compute_vote_w;\n";

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
