package project.src.java.core.randomForest.tableGenerator.parallelTableGenerator;

import project.src.java.core.randomForest.AdderGenerator;
import project.src.java.core.randomForest.MajorityGenerator;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import java.util.List;

public class ParallelTableFPGAGenerator {
	public void execute(List<Tree> treeList, int classQuantity, int featureQuantity, SettingsCli settings){

		FileBuilder.createDir(
			String.format(
				"output/%s_%s_%dtree_%sdeep_run",
				settings.dataset,
				settings.approach,
				settings.trainingParameters.estimatorsQuantity,
				settings.trainingParameters.maxDepth
			)
		);

		var treeGenerator 	    = new TreeGenerator();
		var controllerGenerator = new ControllerGenerator();
		var adderGenerator      = new AdderGenerator();
		var majorityGenerator   = new MajorityGenerator();

		treeGenerator	   .execute(classQuantity, featureQuantity, treeList, settings);
		controllerGenerator.execute(featureQuantity, treeList.size(), classQuantity, settings);
		adderGenerator     .execute(treeList.size(), settings);
		majorityGenerator  .execute(treeList.size(), classQuantity, settings);
	}
}
