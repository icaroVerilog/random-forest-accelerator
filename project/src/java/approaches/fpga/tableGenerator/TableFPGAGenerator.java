package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.AlteraCycloneApi;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table.SettingsT;


import java.util.List;

public class TableFPGAGenerator {

    public void execute(List<Tree> treeList, int classQnt, int featureQnt, SettingsT settings){

        var a = FileBuilder.createDir(String.format("FPGA/%s_table_%dtree_%sdeep_run", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth));

        /* calculate the needed bitwidth to represent each class */
        int classBitwidth = (int) Math.ceil(Math.log(classQnt) / Math.log(2));

        var tableEntryGenerator      = new TableEntryGenerator();
        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();
        var alteraCycloneAPI         = new AlteraCycloneApi();

        var tableEntries = tableEntryGenerator.execute(treeList, settings, true);

        validationTableGenerator.execute(
            classQnt,
            featureQnt,
            treeList.size(),
            classBitwidth,
            tableEntries,
            settings,
            true
        );

        controllerGenerator.execute(
            classBitwidth,
            featureQnt,
            settings,
            true
        );
    }
}
