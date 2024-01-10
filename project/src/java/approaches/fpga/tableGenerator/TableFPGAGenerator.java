package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.tableGenerator.tableEntryDataStructures.binary.BinaryTableEntry;
import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.ArrayList;
import java.util.List;

public class TableFPGAGenerator {

    public void execute(
        List<Tree> treeList,
        Integer classQuantity,
        Integer featureQuantity,
        Boolean debugMode,
        String datasetName
    ){

        /* calculate the needed bitwidth to represent each class */
//        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));
        int classBitwidth = 13;

        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();
        var tableEntryGenerator      = new TableEntryGenerator();

        var tableEntries = tableEntryGenerator.execute(treeList, datasetName, false);
        validationTableGenerator.execute(classQuantity, featureQuantity, classBitwidth, tableEntries, datasetName, false);
        controllerGenerator.execute(classBitwidth, featureQuantity, datasetName, false);
    }
}
