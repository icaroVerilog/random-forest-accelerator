package project.src.java.core.randomForest.tableGenerator.tableGenerator;

import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;


import java.util.List;

public class TableFPGAGenerator {

    public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings){

        FileBuilder.createDir(
            String.format(
                "output/%s_%s_%dtree_%sdeep_run",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            )
        );

        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();

        validationTableGenerator.execute(classQnt, featureQnt, treeList, settings);
        controllerGenerator     .execute(classQnt, featureQnt, settings);
    }
}
