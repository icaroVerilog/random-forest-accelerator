package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;

import java.util.List;

public class ConditionalFPGAGenerator {
    public void execute(
        List<Tree> treeList,
        int classQnt,
        int featureQnt,
        int samplesQnt,
        boolean debugMode,
        ExecutionSettings settings,
        String dataset
    ){

        var controllerGenerator = new ControllerGenerator();
        var treeGenerator       = new TreeGenerator();
        var apiGenerator        = new ApiGenerator();

        treeGenerator      .execute(treeList, classQnt, featureQnt, dataset, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, samplesQnt, debugMode, dataset);
        apiGenerator       .execute(featureQnt, classQnt, debugMode, dataset);

    }

}
