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
			src += generateAlwaysBlock(featureQnt, currentTree.innerNodes);
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

	public String generateAlwaysBlock(int featureQnt, HashMap<Integer, InnerNode> innerNodes){
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
		src += tab(2) + "sync_flag[1] <= sync_flag[0];\n";

		int comparisonCounter = innerNodes.keySet().size() - 1;

		System.out.println(innerNodeList);

		for (int index = maxLevel; index >= 0 ; index--) {
			for (int key: innerNodes.keySet()){
				if (innerNodes.get(key).getLevel() == index){
					String nodeConditional = CONDITIONAL2;

					String nodeExpr = String.format("c_register[%d]", comparisonCounter);
					String nodeBody = "";

					Node leftNode = innerNodes.get(key).getLeftNode();

					if (leftNode instanceof InnerNode){
						nodeBody = tab(3) + String.format(
							"node%d <= node%d;\n",
							comparisonCounter,
							innerNodeList.indexOf(leftNode.getId())
						);
					}
					else if (leftNode instanceof OuterNode) {
						nodeBody = tab(3) + String.format(
							"node%d <= class%d;\n",
							comparisonCounter,
							((OuterNode) leftNode).getClassNumber()
						);
					}

					nodeConditional = nodeConditional
							.replace("x", nodeExpr)
							.replace("y", nodeBody)
							.replace("ind", tab(2));

					comparisonCounter--;
					src += nodeConditional + "\n";
				}

			}
			src += "\n===========\n";
//			String levelSyncConditional = CONDITIONAL2;

			System.out.println("--------------------");
		}

		String conditional = CONDITIONAL2;

//		for (int key: innerNodes.keySet()){
//			System.out.println(key);
//			for (int index = 0; index < maxLevel; index++) {
//				if (innerNodes.get(key).getLevel() == maxLevel){
//					System.out.printf("a: %d\n", maxLevel);
//				}
//			}
//			maxLevel--;
			//			if (innerNodes.get(key).getLevel() == )
//			int threshold = (int) Math.floor(innerNodes.get(key).getComparisson().getThreshold());
//			src += tab(2) + String.format("c_register[%d] <= (r_feature%d <= %d'b%s);\n", counter, innerNodes.get(key).getComparisson().getColumn(), this.comparedValueBitwidth ,toBinary(threshold, 8));
//
//			int level = innerNodes.get(key).getLevel();
//
//			if (level > maxLevel){
//				maxLevel = level;
//			}
//			counter = counter + 1;
//		}

		String always = ALWAYS_BLOCK2;
		always = always
				.replace("border", "posedge")
				.replace("signal", "clock")
				.replace("src", src)
				.replace("ind", tab(1));

		return always;
	}
}
