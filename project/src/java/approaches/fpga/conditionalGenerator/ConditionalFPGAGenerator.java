package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.Conditional;

import java.io.File;
import java.util.List;

public class ConditionalFPGAGenerator {
    public void execute(
        List<Tree> treeList,
        int classQnt,
        int featureQnt,
        int samplesQnt,
        Conditional settings
    ){

        var a = FileBuilder.createDir(String.format("FPGA/%s_conditional_run", settings.dataset));

        var controllerGenerator = new ControllerGenerator();
        var treeGenerator       = new TreeGenerator();
        var apiGenerator        = new ApiGenerator();

        treeGenerator      .execute(treeList, featureQnt, classQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, samplesQnt, settings);
//        apiGenerator       .execute(featureQnt, classQnt, debugMode, dataset);

    }

}
