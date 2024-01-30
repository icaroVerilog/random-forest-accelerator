package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.*;


import java.util.List;

public class TableFPGAGenerator {

    public void execute(
        List<Tree> treeList,
        Integer classQuantity,
        Integer featureQuantity,
        SettingsTable settings
    ){

        FileBuilder.createDir(String.format("FPGA/%s_table_run", settings.dataset));

        /* calculate the needed bitwidth to represent each class */
        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));

        var tableEntryGenerator      = new TableEntryGenerator();
        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();

        var tableEntries = tableEntryGenerator.execute(
            treeList,
            settings,
            true
        );

        validationTableGenerator.execute(
            classQuantity,
            featureQuantity,
            treeList.size(),
            classBitwidth,
            tableEntries,
            settings,
            true
        );

        controllerGenerator.execute(
            classBitwidth,
            featureQuantity,
            settings,
            true
        );
    }
}
