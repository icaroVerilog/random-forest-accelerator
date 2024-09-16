package project.src.java.core.randomForest.approaches.fpga;

import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCliCEM;
import project.src.java.util.executionSettings.CLI.Table.SettingsCliT;
import project.src.java.util.executionSettings.CLI.SettingsCLI;

public class ApiGenerator extends BasicGenerator {
	private int IOPinQnt;
	private int precision;
	private String approach;

	public void execute(int classQnt, int featureQnt, SettingsCLI settings){

		if (settings instanceof SettingsCliCEM) {
			switch (((SettingsCliCEM) settings).inferenceParameters.precision){
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
		}
		if (settings instanceof SettingsCliT){
			// TODO: ajustar isso
			this.precision = ((SettingsCliT) settings).inferenceParameters.fieldsBitwidth.comparedValue;
		}

		this.IOPinQnt = 10;
		this.approach = settings.approach;

		String src = "";
		src += "`include \"controller.v\"\n";
		src += generateHeader("fpga_api");
		src += generatePortDeclarations(classQnt, featureQnt);
		src += generateControllerModuleInstantiation(featureQnt);
		src += generateAlwaysBlock(classQnt, featureQnt);
		src += "endmodule\n";

		FileBuilder.execute(
			src, String.format(
				"output/%s_%s_%dtree_%sdeep_run/fpga_api.v",
				settings.dataset,
				settings.approach,
				settings.trainingParameters.estimatorsQuantity,
				settings.trainingParameters.maxDepth
			),
			false
		);
	}

	private String generateHeader(String module_name){
		String src = "";

		String[] basicIOPorts = {"clock","reset","data","voted","compute_vote", "read_new_sample"};

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
		int inputDataBitwidth = featureQnt * this.precision;

		String src = "";

		src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
		src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);

		if (inputDataBitwidth <= this.IOPinQnt - outputDataBitwidth){
			src += tab(1) + generatePort("data", WIRE, INPUT, inputDataBitwidth, true);
		} else {
			src += tab(1) + generatePort("data", WIRE, INPUT, this.IOPinQnt - outputDataBitwidth, true);
		}

		src += "\n";
		src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
		src += tab(1) + generatePort("read_new_sample", REGISTER, OUTPUT,1,true);
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

		if (this.approach.equals("table")){
			src += tab(2) + ".clock(clock),\n";
			src += tab(2) + ".feature(input_buffer)";
			src += tab(2) + ".voted(voted),\n";
		}
		else if (this.approach.equals("conditional")){
			src += tab(2) + ".clock(clock),\n";
			src += tab(2) + ".voted(voted),\n";
			int offset = 0;
			for (int index = 0; index < featureQnt; index++) {
				if (index == featureQnt - 1){
					src += tab(2) + String.format(".feature%d(input_buffer[%d:%d])", index, offset + this.precision - 1, offset);
				} else {
					src += tab(2) + String.format(".feature%d(input_buffer[%d:%d]),\n", index, offset + this.precision - 1, offset);
				}
				offset = offset + this.precision;
			}
		}
		else {
			src += tab(2) + ".voted(voted),\n";
			int offset = 0;
			for (int index = 0; index < featureQnt; index++) {
				if (index == featureQnt - 1){
					src += tab(2) + String.format(".feature%d(input_buffer[%d:%d])", index, offset + this.precision - 1, offset);
				} else {
					src += tab(2) + String.format(".feature%d(input_buffer[%d:%d]),\n", index, offset + this.precision - 1, offset);
				}
				offset = offset + this.precision;
			}
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
		int inputDataBitwidth = featureQnt * this.precision;
		int bufferQnt = (int) Math.ceil((float) inputDataBitwidth / (this.IOPinQnt - outputDataBitwidth));

		String resetBlock = CONDITIONAL_BLOCK;
		String resetBlockExpr = "reset";
		String resetBlockBody = "";

		resetBlockBody += tab(3) + "counter <= 12'b000000000000;\n";
		resetBlockBody += tab(3) + "compute_vote <= 1'b0;\n";
		resetBlockBody += tab(3) + "read_new_sample <= 1'b1;\n";

		resetBlock = resetBlock
				.replace("x", resetBlockExpr)
				.replace("`", resetBlockBody)
				.replace("ind", tab(2));

		String computeVoteBlock     = CONDITIONAL_BLOCK;
		String computeVoteBlockExp  = "";

		if (this.approach.equals("conditional")){
			computeVoteBlockExp  = String.format("counter == 12'b%s", toBin(bufferQnt + 3, 12));
		}
		else {
			computeVoteBlockExp  = String.format("counter == 12'b%s", toBin(bufferQnt + 2, 12));
		}

		String computeVoteBlockBody = "";

		computeVoteBlockBody += tab(4) + "counter <= 12'b00000000000;\n";
		computeVoteBlockBody += tab(4) + "compute_vote <= 1'b1;\n";
		computeVoteBlockBody += tab(4) + "read_new_sample <= 1'b1;\n";

		computeVoteBlock = computeVoteBlock
				.replace("x", computeVoteBlockExp)
				.replace("`", computeVoteBlockBody)
				.replace("ind", tab(3));

		String computeVoteElseBlock     = CONDITIONAL_ELSE_BLOCK;
		String computeVoteElseBlockBody = "";

		computeVoteElseBlockBody += tab(4) + "compute_vote <= 1'b0;\n";
		computeVoteElseBlockBody += tab(4) + "read_new_sample <= 1'b0;\n";

		computeVoteElseBlock = computeVoteElseBlock
				.replace("y", computeVoteElseBlockBody)
				.replace("ind", tab(3));


		String resetBlockElse = CONDITIONAL_ELSE_BLOCK;
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

		String alwaysBlock = ALWAYS_BLOCK;
		alwaysBlock = alwaysBlock
				.replace("border", "posedge")
				.replace("signal", "clock")
				.replace("ind", tab(1))
				.replace("src", src);

		return alwaysBlock;
	}
}