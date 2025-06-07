package project.src.java.core.randomForest.tableGenerator.parallelTableGenerator;

import project.src.java.core.BasicGenerator;
import project.src.java.core.randomForest.tableGenerator.TableEntryGenerator;
import project.src.java.core.randomForest.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.relatory.ReportGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.util.ArrayList;
import java.util.List;

public class TreeGenerator extends BasicGenerator {
	private int precision;
	private int comparedColumnBitwidth;
	private int tableIndexerBitwidth;
	private int voteCounterBitwidth;

	public void execute(
		int classQuantity,
		int featureQuantity,
		List<Tree> treeList,
		SettingsCli settings
	){
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
		this.voteCounterBitwidth    = treeList.size();

		ReportGenerator reportGenerator = new ReportGenerator();
		TableEntryGenerator tableEntryGenerator = new TableEntryGenerator();
		ArrayList<Integer> nodeQntByTree = new ArrayList<>();

		for (int index = 0; index < treeList.size(); index++){
			System.out.println("generating verilog decision tree" + index);

			Tree currentTree = treeList.get(index);
			ArrayList<Tree> singleTreeList = new ArrayList<>();
			singleTreeList.add(currentTree);
			ArrayList<BinaryTableEntry> entries = tableEntryGenerator.execute(singleTreeList, this.precision, true);
			singleTreeList.clear();

			nodeQntByTree.add(currentTree.getInnerNodes().size() + currentTree.getOuterNodes().size());
			int nodeQnt = currentTree.getInnerNodes().size() + currentTree.getOuterNodes().size();

			String src = "";

			src += generateHeader(index);
			src += generateIEE754ComparatorFunction(this.precision);
			src += generatePortInstantiation(featureQuantity);
			src += generateInternalVariables(nodeQnt, classQuantity);
			src += generateWireAssign(featureQuantity);
			src += generateMainAlways(entries);

			FileBuilder.execute(
				src, String.format(
					"output/%s_%s_%dtree_%sdeep_run/tree%d.v",
					settings.dataset,
					settings.approach,
					settings.trainingParameters.estimatorsQuantity,
					settings.trainingParameters.maxDepth,
					index
				),
				false
			);
		}

		reportGenerator.generateReport(
			settings.dataset,
			settings.approach,
			settings.trainingParameters.maxDepth,
			nodeQntByTree
		);
	}

	private String generateHeader(int index){
		String src = "";

		src += String.format("module tree%d (\n", index);
		src += tab(1) + "clock,\n";
		src += tab(1) + "reset,\n";
		src += tab(1) + "start_next,\n";
		src += tab(1) + "voted_class,\n";
		src += tab(1) + "compute_vote,\n";
		src += tab(1) + "features\n";
		src += ");\n";

		return src;
	}

	private String generatePortInstantiation (int featureQnt){
		int comparedValueBusBitwidth = featureQnt * this.precision;

		String src = "";
		src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
		src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
		src += tab(1) + generatePort("start_next", WIRE, INPUT, 1, true);
		src += tab(1) + generatePort("features", WIRE, INPUT, comparedValueBusBitwidth, true);
		src += "\n";
		src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, this.tableIndexerBitwidth, true);
		src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
		src += tab(1) + "\n";

		return src;
	}

	private String generateInternalVariables(int nodeQuantity, int classQuantity){
		String src = "";

		int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

		src += tab(1) + generateMemory("nodes_table", REGISTER, NONE, tableEntryBitwidth, nodeQuantity, true);
		src += tab(1) + generatePort("next", REGISTER, NONE, this.tableIndexerBitwidth, true);
		src += "\n";

		for (int index = 0; index <= classQuantity - 1; index++){
			src += tab(1) + generatePort("class" + (index+1), REGISTER, NONE, this.voteCounterBitwidth, true);
		}
		src += tab(1) + generatePort("ready", REGISTER, NONE, 1, true);
		src += "\n";
		src += tab(1) + generatePort("voted_class_w", WIRE, NONE, this.tableIndexerBitwidth, true);
		src += tab(1) + generatePort("threshold_w", WIRE, NONE, this.precision, true);
		src += tab(1) + generatePort("column_w", WIRE, NONE, this.comparedColumnBitwidth, true);
		src += tab(1) + generatePort("feature_w", WIRE, NONE, this.precision, true);
		src += "\n";

		return src;
	}

	private String generateWireAssign(int featureQuantity){

		int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;

		String src = "";
		src += tab(1) + String.format(
			"assign voted_class_w = nodes_table[next][%d:0];\n",
			this.tableIndexerBitwidth - 1
		);
		src += tab(1) + String.format(
			"assign column_w = nodes_table[next][%d:%d];\n",
			((this.tableIndexerBitwidth * 2) + this.comparedColumnBitwidth) - 1,
			this.tableIndexerBitwidth * 2
		);

		src += tab(1) + String.format(
			"assign threshold_w = nodes_table[next][%d:%d];\n",
			tableEntryBitwidth - 1,
			tableEntryBitwidth - this.precision
		);
		src += "\n";

		src += tab(1) + "assign feature_w = {\n";
		for (int index = (this.precision * featureQuantity) - 1; index >= (this.precision * featureQuantity) - this.precision; index--) {

			if (index != (this.precision * featureQuantity) - this.precision){
				src += tab(2) + String.format("features[%d - (column_w * %d)],\n", index, this.precision);
			} else {
				src += tab(2) + String.format("features[%d - (column_w * %d)]\n",  index, this.precision);
			}
		}
		src += tab(1) + "};\n";

		return src;
	}

	private String generateMainAlways(ArrayList<BinaryTableEntry> tableEntries){

		/*************************** RESET BLOCK ****************************/

		String resetBlock = CONDITIONAL_BLOCK;
		String resetBlockExpr = "reset";
		String resetBlockBody = "";

		for (int index = 0; index < tableEntries.size(); index++){
			int tableEntryBitwidth = this.precision + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2) + 1;
			resetBlockBody += tab(3) + String.format("nodes_table[%d] <= %d'b%s;\n", index, tableEntryBitwidth, tableEntries.get(index).value());
		}
		resetBlockBody += "\n";
		resetBlockBody += tab(3) + String.format(
			"next <= %d'b%s;\n",
			this.tableIndexerBitwidth,
			toBin(0, this.tableIndexerBitwidth)
		);
		resetBlockBody += tab(3) + String.format(
			"voted_class <= %d'b%s;\n",
			this.tableIndexerBitwidth,
			toBin(0, this.tableIndexerBitwidth)
		);
		resetBlockBody += tab(3) + "ready <= 1'b1;\n";
		resetBlockBody += tab(3) + "compute_vote <= 1'b0;\n";

		resetBlock = resetBlock
			.replace("x", resetBlockExpr)
			.replace("`", resetBlockBody)
			.replace("ind", tab(2));

		/******************* INNER NODE PROCESSING BLOCK ********************/

		String thGreaterThanValueBlock = CONDITIONAL_BLOCK;
		String thGreaterThanValueBlockExpr = "";

		thGreaterThanValueBlockExpr += "IEEE754_comparator(threshold_w, feature_w)";

		String thGreaterThanValueBlockBody = tab(6) + String.format(
			"next <= nodes_table[next][%d:%d];\n",
			(this.tableIndexerBitwidth * 2) - 1,
			this.tableIndexerBitwidth
		);

		thGreaterThanValueBlock = thGreaterThanValueBlock
			.replace("x", thGreaterThanValueBlockExpr)
			.replace("`", thGreaterThanValueBlockBody)
			.replace("ind", tab(5));

		String thLessThanValueBlock = CONDITIONAL_ELSE_BLOCK;
		String thLessThanValueBlockBody = tab(6) + String.format(
			"next <= nodes_table[next][%d:%d];\n",
			this.tableIndexerBitwidth - 1,
			0
		);

		thLessThanValueBlock = thLessThanValueBlock
			.replace("y", thLessThanValueBlockBody)
			.replace("ind", tab(5));

		String innerNodeBlock = CONDITIONAL_BLOCK;
		String innerNodeBlockExpr = String.format(
			"!nodes_table[next][%d]",
			((this.precision * 2) + this.comparedColumnBitwidth + (this.tableIndexerBitwidth * 2)) - (this.precision * 2)
		);
		String innerNodeBlockBody = thGreaterThanValueBlock + "\n" + thLessThanValueBlock;

		innerNodeBlock = innerNodeBlock
			.replace("x", innerNodeBlockExpr)
			.replace("`", innerNodeBlockBody)
			.replace("ind", tab(4));

		/******************** OUTER NODE PROCESSING BLOCK ********************/


		String outerNodeBlock = CONDITIONAL_ELSE_BLOCK;
		String outerNodeBlockBody = "";

		outerNodeBlockBody += tab(5) + String.format(
			"next <= nodes_table[next][%d:%d];\n",
			(this.tableIndexerBitwidth * 2) - 1,
			this.tableIndexerBitwidth
		);

		outerNodeBlockBody += tab(5) + "voted_class <= voted_class_w;\n";
		outerNodeBlockBody += tab(5) + "compute_vote <= 1'b1;\n";
		outerNodeBlockBody += tab(5) + "ready <= 1'b0;\n";

		outerNodeBlock = outerNodeBlock
			.replace("y",outerNodeBlockBody)
			.replace("ind", tab(4));

		String sampleProcessingBlock = CONDITIONAL_BLOCK;
		String sampleProcessingBlockExpr = "ready";
		String sampleProcessingBlockBody = innerNodeBlock + "\n" + outerNodeBlock;

		sampleProcessingBlock = sampleProcessingBlock
			.replace("x", sampleProcessingBlockExpr)
			.replace("`", sampleProcessingBlockBody)
			.replace("ind", tab(3));

		/******************** COMPUTE VOTE TRIGGER BLOCK ********************/

		String resetCounterBlock = CONDITIONAL_BLOCK;
		String resetCounterBlockExpr = "compute_vote";
		String resetCounterBlockBody = "";

		resetCounterBlockBody += tab(4) + "compute_vote <= 1'b0;\n";

		resetCounterBlock = resetCounterBlock
			.replace("x", resetCounterBlockExpr)
			.replace("`", resetCounterBlockBody)
			.replace("ind", tab(3));

		/******************** READY TRIGGER BLOCK ********************/

		String readyTriggerBlock = CONDITIONAL_BLOCK;
		String readyTriggerBlockExpr = "start_next";
		String readyTriggerBlockBody = "";

		readyTriggerBlockBody += tab(4) + "ready <= 1'b1;\n";

		readyTriggerBlock = readyTriggerBlock
			.replace("x", readyTriggerBlockExpr)
			.replace("`", readyTriggerBlockBody)
			.replace("ind", tab(3));

		/******************** VALIDATION BLOCK ********************/

		String validationBlock = CONDITIONAL_ELSE_BLOCK;

		validationBlock = validationBlock
			.replace("y", resetCounterBlock + "\n" + readyTriggerBlock + "\n" + sampleProcessingBlock + "\n")
			.replace("ind", tab(2));


		/******************** MAIN ALWAYS BLOCK ********************/

		String mainAlways = ALWAYS_BLOCK;
		mainAlways = mainAlways
			.replace("border", "posedge")
			.replace("signal", "clock")
			.replace("src", resetBlock + "\n" + validationBlock)
			.replace("ind", tab(1));

		return mainAlways + "endmodule";
	}

}
