package project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.equationGenerator;

import project.src.java.core.randomForest.approaches.fpga.ApiGenerator;
import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.AdderGenerator;
import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.ControllerGenerator;
import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.MajorityGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.util.List;

public class EquationFPGAGenerator {
    public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) {
        var a = FileBuilder.createDir(
            String.format(
                "output/%s_%s_%dtree_%sdeep_run",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            )
        );

        var treeGenerator       = new TreeGenerator();
        var controllerGenerator = new ControllerGenerator();
        var adderGenerator      = new AdderGenerator();
        var majorityGenerator   = new MajorityGenerator();
        var apiGenerator        = new ApiGenerator();

        treeGenerator      .execute(treeList, classQnt, featureQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, settings);
        adderGenerator     .execute(treeList.size(), settings);
        majorityGenerator  .execute(treeList.size(), classQnt, settings);
//        apiGenerator       .execute(classQnt, featureQnt, settings);
    }
}
