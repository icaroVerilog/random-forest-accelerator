package project.src.java.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;

import java.util.List;

public class ConditionalFPGAGenerator {
    public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings){
        var a = FileBuilder.createDir(String.format("FPGA/%s_conditional_%dtree_%sdeep_run", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth));

        var treeGenerator       = new TreeGenerator();
        var controllerGenerator = new ControllerGenerator();

        treeGenerator      .execute(treeList, featureQnt, classQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, settings);

    }

}
