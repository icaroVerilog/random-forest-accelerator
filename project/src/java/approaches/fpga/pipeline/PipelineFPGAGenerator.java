package project.src.java.approaches.fpga.pipeline;

import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;

import java.util.List;

public class PipelineFPGAGenerator {
	public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings){
		var a = FileBuilder.createDir(String.format("FPGA/%s_pipeline_%dtree_%sdeep_run", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth));

		var treeGenerator = new TreeGenerator();

		treeGenerator.execute(treeList, classQnt, featureQnt, settings);
	}
}
