package project.src.java.approaches.fpga.pipeline;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.BaseTreeGenerator;
import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TreeGenerator extends BaseTreeGenerator {

	private int comparedValueBitwidth;
	private String precision;

	public void execute(List<Tree> trees, int classQnt, int featureQnt, SettingsCEM settings){
		this.precision = settings.precision;
		this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;

		ReportGenerator reportGenerator = new ReportGenerator();
		ArrayList<Integer> nodeQntByTree = new ArrayList<>();

		for (int index = 0; index < trees.size(); index++){
			System.out.println("generating verilog decision tree" + index);
			Tree currentTree = trees.get(index);
			nodeQntByTree.add(currentTree.getInnerNodes().size() + currentTree.getOuterNodes().size());

			String src = "";

			src += generateHeader(index, featureQnt);
			src += generateParameters(classQnt);
			src += generatePortDeclaration(featureQnt, classQnt, currentTree.getInnerNodes().size(), currentTree.getMaxDepth());
			src += generateAlwaysBlock(featureQnt, currentTree.innerNodes, currentTree.getMaxDepth());
//			src += generateConditionals(trees.get(index).getRoot(), 2);
//			src += generateEndDelimiters();

			FileBuilder.execute(src, String.format("FPGA/%s_pipeline_%dtree_%sdeep_run/tree%d.v", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth, index));
		}
		reportGenerator.createEntry(
				settings.dataset,
				settings.approach,
				settings.trainingParameters.maxDepth,
				nodeQntByTree
		);
	}

	public String generateHeader(int treeIndex, int featureQnt){

		String src = "";

		src += String.format("module tree%d (\n", treeIndex);

		if (this.precision.equals("integer")){
			for (int index = 0; index < featureQnt; index++){
				src += String.format("%sfeature%d,\n", tab(1), index);
			}
		}
		else if (this.precision.equals("decimal")){
			for (int index = 0; index < featureQnt; index++){
				src += String.format("%sft%d_exponent,\n", tab(1), index);
			}
			for (int index = 0; index < featureQnt; index++){
				src += String.format("%sft%d_fraction,\n", tab(1), index);
			}
		}

		src += tab(1) + "clock,\n";
		src += tab(1) + "reset,\n";
		src += tab(1) + "voted_class,\n";
		src += tab(1) + "compute_vote\n";
		src += ");\n";

		return src;
	}

	public String generateParameters(int classQnt){

		int[][] oneHotMatrix = new int[classQnt][classQnt];

		for (int i = 0; i < oneHotMatrix.length; i++) {
			for (int j = 0; j < oneHotMatrix[i].length; j++) {
				if (i == j){
					oneHotMatrix[i][j] = 1;
				}
				else {
					oneHotMatrix[i][j] = 0;
				}
			}
		}

		String src = "";

		for (int index = 0; index < classQnt; index++) {
			String oneHotEncode = Arrays.toString(oneHotMatrix[classQnt - index - 1])
					.replaceAll("[\\[\\]\\s]", "")
					.replace(",", "") + ";";
			src += tab(1) + String.format("parameter class%d = %d'b%s\n", index, classQnt,  oneHotEncode);
		}
		src += "\n";

		return src;
	}

	public String generatePortDeclaration(int featureQnt, int classQnt, int innerNodeQnt, int maxDepth){
		String tab = tab(1);

		String src = "";

		src += tab + "input wire clock;\n";
		src += tab + "input wire reset;\n\n";

		if (this.precision.equals("integer")){
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("feature%d", index), WIRE, INPUT, this.comparedValueBitwidth, true);
			}
		}
		else if (this.precision.equals("decimal")){
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("ft%d_exponent", index), WIRE, INPUT, this.comparedValueBitwidth, true);
			}
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("ft%d_fraction", index), WIRE, INPUT, this.comparedValueBitwidth, true);
			}
		}

		src += "\n";
		src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, classQnt, true);
		src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
		src += "\n";

		if (this.precision.equals("integer")){
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("r_feature%d", index), REGISTER, NONE, this.comparedValueBitwidth, true);
			}
		}
		else if (this.precision.equals("decimal")){
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("r_ft%d_exponent", index), REGISTER, NONE, this.comparedValueBitwidth, true);
			}
			for (int index = 0; index < featureQnt; index++){
				src += tab(1) + generatePort(String.format("r_ft%d_fraction", index), REGISTER, NONE, this.comparedValueBitwidth, true);
			}
		}
		src += "\n";
		src += tab(1) + generatePort("c_register", REGISTER, NONE, innerNodeQnt, true);
		src += "\n";


		for (int index = 0; index < innerNodeQnt; index++) {
			src += tab(1) + generatePort(String.format("node%d", index), REGISTER, NONE, classQnt, true);
		}
		src += "\n";
		src += tab(1) + generatePort("sync_flag", REGISTER, NONE, maxDepth + 2, true);
		src += "\n";

		return src;
	}

	public String generateAlwaysBlock(int featureQnt, HashMap<Integer, InnerNode> innerNodes, int maxDepth){
		String src = "";

		for (int index = 0; index < featureQnt; index++) {
			src += tab(2) + String.format("r_feature%d", index) + " <= " + String.format("feature%d;\n", index);
		}
		src += tab(2) + "sync_flag[0] <= ~reset;\n";
		src += "\n";

		int maxLevel = 0;
		int counter = 0;

		ArrayList<Integer> innerNodeList = new ArrayList<>();

		for (int key: innerNodes.keySet()){
			innerNodeList.add(innerNodes.get(key).getId());

			int threshold = (int) Math.floor(innerNodes.get(key).getComparisson().getThreshold());
			src += tab(2) + String.format("c_register[%d] <= (r_feature%d <= %d'b%s);\n", counter, innerNodes.get(key).getComparisson().getColumn(), this.comparedValueBitwidth ,toBinary(threshold, 8));

			int level = innerNodes.get(key).getLevel();

			if (level > maxLevel){
				maxLevel = level;
			}
			counter = counter + 1;
		}

		int comparisonCounter = innerNodes.keySet().size() - 1;
		int maxDepthCounter = 1;

		ArrayList<ArrayList<Integer>> delayMatrix = new ArrayList<>();
		ArrayList<String> delayRegisters = new ArrayList<>();

		for (int index = maxLevel; index >= 0 ; index--) {
			ArrayList<Integer> delayedComparisons = new ArrayList<>();

			Boolean placeDelay = false;

			String levelSyncConditional = CONDITIONAL3;
			String levelSyncBody = "";

			for (int key: innerNodes.keySet()){
				if (innerNodes.get(key).getLevel() == index){
					String nodeConditionalTrue = CONDITIONAL3;
					String nodeConditionalFalse = CONDITIONAL_ELSE;
					String nodeExpr = "";

					if (maxDepthCounter > 1 ){
						nodeExpr = String.format("c%d_delay_register%d",  comparisonCounter, maxDepthCounter - 1);
						delayedComparisons.add(comparisonCounter);
						placeDelay = true;
					} else {
						nodeExpr = String.format("c_register[%d]", comparisonCounter);
					}

					String nodeBodyTrue = "";
					String nodeBodyFalse = "";

					Node leftNode = innerNodes.get(key).getLeftNode();
					Node rightNode = innerNodes.get(key).getRightNode();

					if (leftNode instanceof InnerNode){
						nodeBodyTrue = tab(4) + String.format(
							"node%d <= node%d;\n",
							comparisonCounter,
							innerNodeList.indexOf(leftNode.getId())
						);
					}
					else if (leftNode instanceof OuterNode) {
						nodeBodyTrue = tab(4) + String.format(
							"node%d <= class%d;\n",
							comparisonCounter,
							((OuterNode) leftNode).getClassNumber()
						);
					}

					if (rightNode instanceof InnerNode){
						nodeBodyFalse = tab(4) + String.format(
							"node%d <= node%d;\n",
							comparisonCounter,
							innerNodeList.indexOf(rightNode.getId())
						);
					}
					else if (rightNode instanceof OuterNode) {
						nodeBodyFalse = tab(4) + String.format(
							"node%d <= class%d;\n",
							comparisonCounter,
							((OuterNode) rightNode).getClassNumber()
						);
					}

					nodeConditionalTrue = nodeConditionalTrue
						.replace("x", nodeExpr)
						.replace("`", nodeBodyTrue)
						.replace("ind", tab(3));

					nodeConditionalFalse = nodeConditionalFalse
						.replace("y", nodeBodyFalse)
						.replace("ind", tab(3));

					levelSyncBody += nodeConditionalTrue + nodeConditionalFalse + "\n";
					comparisonCounter--;
				}
			}

			delayMatrix.add(delayedComparisons);
			if (placeDelay){
				src += "^\n";
			}

			src += tab(2) + String.format("sync_flag[%d] <= sync_flag[%d];\n", maxDepthCounter, maxDepthCounter - 1);
			src += "\n";

			String levelSyncExpr = String.format("sync_flag[%d] == 1'b1", maxDepthCounter);

			levelSyncConditional = levelSyncConditional
				.replace("x", levelSyncExpr)
				.replace("`", levelSyncBody)
				.replace("ind", tab(2));

			maxDepthCounter++;
			src += levelSyncConditional + "\n";
		}

		for (int index = delayMatrix.size(); index > 0; index--) {
			if (index != delayMatrix.size()){
				delayMatrix.get(index - 1).addAll(delayMatrix.get(index));
			}
		}

		for (int index1 = 1; index1 < delayMatrix.size(); index1++) {
			String delay = "";

			for (int index2 = 0; index2 < delayMatrix.get(index1).size(); index2++) {
				if (index1 == 1){
					delay += tab(2) + String.format(
						"c%d_delay_register%d <= c_register[%d];\n",
						delayMatrix.get(index1).get(index2),
						index1,
						delayMatrix.get(index1).get(index2)
					);
				} else {
					delay += tab(2) + String.format(
						"c%d_delay_register%d <= c%d_delay_register%d;\n",
						delayMatrix.get(index1).get(index2),
						index1,
						delayMatrix.get(index1).get(index2),
						index1 - 1
					);
				}

				delayRegisters.add(
					String.format("c%d_delay_register%d", delayMatrix.get(index1).get(index2), index1)
				);
			}
			src = src.replaceFirst("\\^", delay);
		}

		String registers = "";

		for (int index = 0; index < delayRegisters.size(); index++) {
			registers += tab(1) + generatePort(delayRegisters.get(index), REGISTER, NONE, 1, true);
		}

		src += "\n";
		src += tab(2) + String.format("sync_flag[%d] <= sync_flag[%d];\n", maxDepthCounter, maxDepthCounter - 1);
		src += tab(2) + String.format("compute_vote <= sync_flag[%d];\n", maxDepthCounter);
		src += tab(2) + "voted_class <= node0;";
		src += "\n";

		String always = ALWAYS_BLOCK2;
		always = always
			.replace("border", "posedge")
			.replace("signal", "clock")
			.replace("src", src)
			.replace("ind", tab(1));

		return registers + always + "endmodule";
	}
}
