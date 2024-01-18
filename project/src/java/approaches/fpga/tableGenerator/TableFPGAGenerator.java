package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.List;

public class TableFPGAGenerator {

    public void execute(
        List<Tree> treeList,
        Integer classQuantity,
        Integer featureQuantity,
        Boolean debugMode,
        String datasetName,
        String precision
    ){

        /* calculate the needed bitwidth to represent each class */
//        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));
        int classBitwidth = 13;

        var tableEntryGenerator      = new TableEntryGenerator();
        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();

        var tableEntries = tableEntryGenerator.execute(treeList, datasetName, precision,true);
        validationTableGenerator.execute(classQuantity, featureQuantity, classBitwidth, tableEntries, datasetName, true);
        controllerGenerator.execute(classBitwidth, featureQuantity, datasetName, true);
    }
}
