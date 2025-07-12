package project.src.java.core.randomForest.pipelineGenerator;

import project.src.java.core.randomForest.BaseTreeGenerator;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import project.src.java.relatory.ReportGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TreeGenerator extends BaseTreeGenerator {

	private int precision;
	private int maxDepth;

	public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings){

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
			case "e4m3":
				this.precision = E4M3;
				break;
			default:
				this.precision = 0;
				break;
		}

		this.maxDepth = 0;

		ReportGenerator reportGenerator = new ReportGenerator();
		ArrayList<Integer> nodeQntByTree = new ArrayList<>();

		for (int index = 0; index < treeList.size(); index++) {
			if (treeList.get(index).getMaxDepth() > this.maxDepth) {
				this.maxDepth = treeList.get(index).getMaxDepth();
			}
		}

		for (int index = 0; index < treeList.size(); index++){
			System.out.println("generating verilog decision tree" + index);
			Tree currentTree = treeList.get(index);
			nodeQntByTree.add(currentTree.getInnerNodes().size() + currentTree.getOuterNodes().size());

			String src = "";

			src += generateHeader(index);
			src += generateFloatingPointComparatorFunction(this.precision);
			src += generateParameters(currentTree.innerNodes, classQnt);
			src += generatePortDeclaration(featureQnt, classQnt, currentTree.getInnerNodes().size(), currentTree.getMaxDepth());
			src += generateAlwaysBlock(featureQnt, currentTree.innerNodes, currentTree.getMaxDepth());

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

	public String generateHeader(int treeIndex){
		String src = "";

		src += String.format("module tree%d (\n", treeIndex);

		src += tab(1) + "clk,\n";
		src += tab(1) + "rst,\n";
		src += tab(1) + "valid_data,\n";
		src += tab(1) + "voted_class,\n";
		src += tab(1) + "compute_vote,\n";
		src += tab(1) + "data\n";
		src += ");\n";

		return src;
	}

	public String generateParameters(HashMap<Integer, InnerNode> innerNodes, int classQnt){
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

		int counter = 0;

		for (int key: innerNodes.keySet()){

			double threshold = innerNodes.get(key).getComparisson().getThreshold();

			if (this.precision == E4M3){
//				System.out.printf("%s %s %s\n",threshold, toE4M3DS(threshold), toE4M3GPT(threshold));
				src += tab(1) + String.format(
					"parameter threshold%d_%d = %d'b%s;\n",
					counter,
					innerNodes.get(key).getComparisson().getColumn(),
					this.precision,
					toE4M3DS(threshold)
				);
			} else {
				src += tab(1) + String.format(
					"parameter threshold%d_%d = %d'b%s;\n",
					counter,
					innerNodes.get(key).getComparisson().getColumn(),
					this.precision,
					toIEEE754(threshold, this.precision)
				);
			}
			counter++;
		}
		src += "\n";
		return src;
	}

	public String generatePortDeclaration(int featureQnt, int classQnt, int innerNodeQnt, int maxDepth){
		String tab = tab(1);
		String src = "";

		src += tab + "input wire clk;\n";
		src += tab + "input wire rst;\n\n";
		src += tab + "input wire valid_data;\n\n";
		src += tab(1) + generatePort("data", WIRE, INPUT, this.precision * featureQnt, true);
		src += "\n";
		src += tab(1) + generatePort("voted_class", REGISTER, OUTPUT, classQnt, true);
		src += tab(1) + generatePort("compute_vote", REGISTER, OUTPUT, 1, true);
		src += "\n";

		for (int index = 0; index < featureQnt; index++){
			src += tab(1) + generatePort(String.format("feature%d", index), REGISTER, NONE, this.precision, true);
		}

		src += "\n";
		src += tab(1) + generatePort("comparison", REGISTER, NONE, innerNodeQnt, true);
		src += "\n";

		for (int index = 0; index < innerNodeQnt; index++) {
			src += tab(1) + generatePort(String.format("node%d", index), REGISTER, NONE, classQnt, true);
		}

		if (this.maxDepth > maxDepth){
			for (int index = 0; index < this.maxDepth - maxDepth; index++) {
				src += tab(1) + generatePort(String.format("delay_node%d", index), REGISTER, NONE, classQnt, true);
			}
		}

		src += "\n";
		if (this.maxDepth > maxDepth){
			int additionalBits = this.maxDepth - maxDepth;
			src += tab(1) + generatePort("sync_flag", REGISTER, NONE, maxDepth + 2 + additionalBits, true);
		} else {
			src += tab(1) + generatePort("sync_flag", REGISTER, NONE, maxDepth + 2, true);
		}

		src += "\n";

		return src;
	}

	public String generateAlwaysBlock(int featureQnt, HashMap<Integer, InnerNode> innerNodes, int maxDepth){
		String src = "";

		String validDataCheck = CONDITIONAL_BLOCK;
		String validDataCheckBody = "";

		for (int index = 0; index < featureQnt; index++) {
			int upperBit = ((featureQnt * this.precision) - 1) - (index * this.precision);
			int lowerBit = ((featureQnt * this.precision)) - ((index + 1) * this.precision);
			validDataCheckBody += tab(3) + String.format("feature%d <= data[%d:%d];\n", index, upperBit, lowerBit);
		}

		validDataCheckBody += tab(3) + "sync_flag[0] <= 1'b1;\n";

		validDataCheck = validDataCheck
			.replace("x", "valid_data")
			.replace("`", validDataCheckBody)
			.replace("ind", tab(2));

		String validDataCheckElse = CONDITIONAL_ELSE_BLOCK;
		String validDataCheckBodyElse = "";

		validDataCheckBodyElse += tab(3) + "sync_flag[0] <= 1'b0;\n";

		validDataCheckElse = validDataCheckElse
			.replace("y", validDataCheckBodyElse)
			.replace("ind", tab(2));

		src += validDataCheck;
		src += "\n";
		src += validDataCheckElse;
		src += "\n";

		int counter = 0;

		ArrayList<Integer> innerNodeList = new ArrayList<>();

		for (int key: innerNodes.keySet()){
			innerNodeList.add(innerNodes.get(key).getId());

			if (this.precision == E4M3){
				src += tab(2) + String.format(
					"comparison[%d] <= E4M3_comparator(threshold%d_%d, feature%d);\n",
					counter,
					counter,
					innerNodes.get(key).getComparisson().getColumn(),
					innerNodes.get(key).getComparisson().getColumn()
				);
			} else {
				src += tab(2) + String.format(
					"comparison[%d] <= IEEE754_comparator(threshold%d_%d, feature%d);\n",
					counter,
					counter,
					innerNodes.get(key).getComparisson().getColumn(),
					innerNodes.get(key).getComparisson().getColumn()
				);
			}
			counter = counter + 1;
		}

		int comparisonCounter = innerNodes.keySet().size() - 1;
		int maxDepthCounter = 1;

		ArrayList<ArrayList<Integer>> delayMatrix = new ArrayList<>();
		ArrayList<String> delayRegisters = new ArrayList<>();

		for (int index = maxDepth - 1; index >= 0 ; index--) {
			ArrayList<Integer> delayedComparisons = new ArrayList<>();

			Boolean placeDelay = false;

			String levelSyncConditional = CONDITIONAL_BLOCK;
			String levelSyncBody = "";

			for (int key: innerNodes.keySet()){
				if (innerNodes.get(key).getLevel() == index){
					String nodeConditionalTrue = CONDITIONAL_BLOCK;
					String nodeConditionalFalse = CONDITIONAL_ELSE_BLOCK;
					String nodeExpr = "";

					if (maxDepthCounter > 1 ){
						nodeExpr = String.format("c%d_bypass_r%d",  comparisonCounter, maxDepthCounter - 1);
						delayedComparisons.add(comparisonCounter);
						placeDelay = true;
					} else {
						nodeExpr = String.format("comparison[%d]", comparisonCounter);
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

					levelSyncBody += nodeConditionalTrue + "\n" + nodeConditionalFalse;
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
			src += "\n";
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
						"c%d_bypass_r%d <= comparison[%d];\n",
						delayMatrix.get(index1).get(index2),
						index1,
						delayMatrix.get(index1).get(index2)
					);
				} else {
					delay += tab(2) + String.format(
						"c%d_bypass_r%d <= c%d_bypass_r%d;\n",
						delayMatrix.get(index1).get(index2),
						index1,
						delayMatrix.get(index1).get(index2),
						index1 - 1
					);
				}

				delayRegisters.add(
					String.format("c%d_bypass_r%d", delayMatrix.get(index1).get(index2), index1)
				);
			}
			src = src.replaceFirst("\\^", delay);
		}

		if (this.maxDepth > maxDepth){
			for (int index = 0; index < this.maxDepth - maxDepth; index++) {
				src += tab(2) + String.format("sync_flag[%d] <= sync_flag[%d];\n", maxDepthCounter, maxDepthCounter - 1);
				maxDepthCounter++;
			}
		}

		src += tab(2) + String.format("sync_flag[%d] <= sync_flag[%d];\n", maxDepthCounter, maxDepthCounter - 1);
		src += tab(2) + String.format("compute_vote <= sync_flag[%d];\n", maxDepthCounter);

		if (this.maxDepth > maxDepth){
			for (int index = 0; index < this.maxDepth - maxDepth; index++) {
				if (index == 0){
					src += tab(2) + String.format("delay_node%d <= node0;\n", index);
				} else {
					src += tab(2) + String.format("delay_node%d <= delay_node%d;\n", index, index - 1);
				}
			}
			src += tab(2) + String.format("voted_class <= delay_node%d;\n", (this.maxDepth - maxDepth) - 1);
		} else {
			src += tab(2) + "voted_class <= node0;\n";
		}

		String registers = "";

		for (int index = 0; index < delayRegisters.size(); index++) {
			registers += tab(1) + generatePort(delayRegisters.get(index), REGISTER, NONE, 1, true);
		}

		String always = ALWAYS_BLOCK;
		always = always
			.replace("border", "posedge")
			.replace("signal", "clk")
			.replace("src", src)
			.replace("ind", tab(1));

		return registers + always + "endmodule";
	}
}
