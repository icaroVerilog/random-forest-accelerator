package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.List;

public class TableFPGAGenerator {

    String MODE = "offline";

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
        var buildNodesTable = new TableEntryGenerator();

        buildNodesTable.execute(treeList);

//        validationTableGenerator.execute(classQuantity, featureQuantity, classBitwidth, datasetName, MODE);
//        controllerGenerator.execute(classBitwidth, featureQuantity, datasetName, MODE);
    }
}
