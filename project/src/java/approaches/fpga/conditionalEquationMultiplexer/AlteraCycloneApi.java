package project.src.java.approaches.fpga.conditionalEquationMultiplexer;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings;

public class AlteraCycloneApi extends BasicGenerator {
    private int IOPinQnt;
    private int comparedValueBitwidth;
    private String precision;

    public void execute(int classQnt, int featureQnt, Settings settings){

        this.IOPinQnt = settings.platform.inputBitwidth - 3;
        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;
        this.precision = settings.precision;

        String src = "";
        src += generateHeader("cycloneIV_api");
        src += generatePortDeclarations(classQnt, featureQnt);
        src += generateControllerModuleInstantiation(featureQnt);
        src += generateAlwaysBlock(classQnt, featureQnt);
        src += "endmodule\n";

        FileBuilder.execute(src, String.format("FPGA/%s_%s_run/cycloneIV_api.v", settings.dataset, settings.approach));
    }

    private String generateHeader(String module_name){
        String src = "";

        String[] basicIOPorts = {"clock","reset","data","voted","compute_vote"};

        src += String.format("module %s (\n", module_name);

        for (int index = 0; index <= basicIOPorts.length; index++){
            if (index == basicIOPorts.length){
                src += ");\n";
            }
            else if (index == basicIOPorts.length - 1){
                src += tab(1) + basicIOPorts[index] + "\n";
            }
            else {
                src += tab(1) + basicIOPorts[index] + ",\n";
            }
        }
        return src;
    }

    private String generatePortDeclarations(int classQnt, int featureQnt){
        int outputDataBitwidth = (int) Math.ceil(Math.sqrt(classQnt));
        int inputDataBitwidth = featureQnt * this.comparedValueBitwidth;

        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("data", WIRE, INPUT, this.IOPinQnt - outputDataBitwidth, true);
        src += "\n";
        src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
        src += tab(1) + generatePort("voted", WIRE, OUTPUT, outputDataBitwidth,true);
        src += "\n";
        src += tab(1) + generatePort("counter", REGISTER, NONE, 12,true);
        src += tab(1) + generatePort("input_buffer", REGISTER, NONE, inputDataBitwidth, true);

        if (inputDataBitwidth > this.IOPinQnt - outputDataBitwidth){

            double bufferQnt = Math.ceil((float) inputDataBitwidth / (this.IOPinQnt - outputDataBitwidth));

            for (int index = 0; index < bufferQnt; index++) {
                src += tab(1) + generatePort(String.format("input_buffer_part%d", index), REGISTER, NONE, this.IOPinQnt - outputDataBitwidth, true);
            }
        }

        return src;
    }

    private String generateControllerModuleInstantiation(int featureQnt){
        String src = "";
        src += tab(2) + ".voted(voted),\n";

        int offset = 0;
        for (int index = 0; index < featureQnt; index++) {
            if (index == featureQnt - 1){
                src += tab(2) + String.format(".feature%d(input_buffer[%d:%d])", index, offset + this.comparedValueBitwidth - 1, offset);
            } else {
                src += tab(2) + String.format(".feature%d(input_buffer[%d:%d]),\n", index, offset + this.comparedValueBitwidth - 1, offset);
            }
            offset = offset + this.comparedValueBitwidth;
        }

        String module = MODULE_INSTANCE;

        module = module
                .replace("moduleName", "controller")
                .replace("ports", src)
                .replace("ind", tab(1));

        return module;
    }

    private String generateAlwaysBlock(int classQnt, int featureQnt){
        int outputDataBitwidth = (int) Math.ceil(Math.sqrt(classQnt));
        int inputDataBitwidth = featureQnt * this.comparedValueBitwidth;
        int bufferQnt = (int) Math.ceil((float) inputDataBitwidth / (this.IOPinQnt - outputDataBitwidth));

        String resetBlock = CONDITIONAL2;
        String resetBlockExpr = "reset";
        String resetBlockBody = "";

        resetBlockBody += tab(3) + "counter <= 12'b000000000000;\n";
        resetBlockBody += tab(3) + "compute_vote <= 1'b0;\n";

        resetBlock = resetBlock
            .replace("x", resetBlockExpr)
            .replace("y", resetBlockBody)
            .replace("ind", tab(2));

        String computeVoteBlock     = CONDITIONAL2;
        String computeVoteBlockExp  = String.format("counter == 12'b%s", toBinary(bufferQnt + 1, 12));
        String computeVoteBlockBody = "";

        computeVoteBlockBody += tab(4) + "counter <= 12'b00000000001;\n";
        computeVoteBlockBody += tab(4) + "compute_vote <= 1'b1;\n";

        computeVoteBlock = computeVoteBlock
            .replace("x", computeVoteBlockExp)
            .replace("y", computeVoteBlockBody)
            .replace("ind", tab(3));

        String computeVoteElseBlock     = CONDITIONAL_ELSE;
        String computeVoteElseBlockBody = "";

        computeVoteElseBlockBody += tab(4) + "compute_vote <= 1'b0;\n";

        computeVoteElseBlock = computeVoteElseBlock
            .replace("y", computeVoteElseBlockBody)
            .replace("ind", tab(3));

        computeVoteBlockBody += tab(4) + "counter <= 12'b00000000001;\n";
        computeVoteBlockBody += tab(4) + "compute_vote <= 1'b1;\n";

        computeVoteBlock = computeVoteBlock
            .replace("x", computeVoteBlockExp)
            .replace("y", computeVoteBlockBody)
            .replace("ind", tab(3));

        String resetBlockElse = CONDITIONAL_ELSE;
        String resetBlockElseBody = "";

        if (inputDataBitwidth > this.IOPinQnt - outputDataBitwidth) {
            for (int index = bufferQnt - 1; index >= 0; index--) {
                if (index == bufferQnt - 1) {
                    resetBlockElseBody += tab(3) + String.format("input_buffer_part%d <= data;\n", index);
                } else {
                    resetBlockElseBody += tab(3) + String.format("input_buffer_part%d <= input_buffer_part%d;\n", index, index + 1);
                }
            }

            String bufferConcatenation = "";
            for (int index = bufferQnt - 1; index >= 0; index--) {
                if (index == 0) {
                    bufferConcatenation += String.format("input_buffer_part%d", index);
                } else {
                    bufferConcatenation += String.format("input_buffer_part%d,", index);
                }
            }
            resetBlockElseBody += "\n";
            resetBlockElseBody += tab(3) + String.format("input_buffer <= {%s};\n", bufferConcatenation);
            resetBlockElseBody += "\n";
        } else {
            resetBlockElseBody += tab(3) + "input_buffer <= data;\n";
        }
        resetBlockElseBody += tab(3) + "counter <= counter + 1'b1;\n";
        resetBlockElseBody += "\n";
        resetBlockElseBody += computeVoteBlock + computeVoteElseBlock + "\n";

        resetBlockElse = resetBlockElse.replace("y", resetBlockElseBody).replace("ind", tab(2));

        String src = "";
        src += resetBlock;
        src += resetBlockElse;

        String alwaysBlock = ALWAYS_BLOCK2;
        alwaysBlock = alwaysBlock
            .replace("border", "posedge")
            .replace("signal", "clock")
            .replace("ind", tab(1))
            .replace("src", src);

        return alwaysBlock;
    }
}
