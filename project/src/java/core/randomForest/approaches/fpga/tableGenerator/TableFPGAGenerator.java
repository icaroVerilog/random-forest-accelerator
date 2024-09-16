package project.src.java.core.randomForest.approaches.fpga.tableGenerator;

import project.src.java.core.randomForest.approaches.fpga.ApiGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Table.SettingsJsonT;


import java.util.List;

public class TableFPGAGenerator {

    public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsJsonT settings){

        var a = FileBuilder.createDir(
            String.format(
                "FPGA/%s_%s_%dtree_%sdeep_run",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            )
        );

        /* calculate the needed bitwidth to represent each class */
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));

        var tableEntryGenerator      = new TableEntryGenerator();
        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();
        var apiGenerator             = new ApiGenerator();

        var tableEntries = tableEntryGenerator.execute(treeList, settings, true);

        validationTableGenerator.execute(classQnt, featureQnt, treeList.size(), classBitwidth, tableEntries, settings, false);
        controllerGenerator     .execute(classBitwidth, featureQnt, settings, true);
//        apiGenerator            .execute(classQnt, featureQnt, settings);
    }
}
