package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;

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
        int classBitwidth = (int) Math.ceil(Math.log(classQuantity) / Math.log(2));
        /*
         *  the expression calcule the needed bitwidth to hold the votes
         *  the counter can reach the maximum value of votes gived by the quantity of trees
         *  because is one vote for each tree
         */
        int voteCounterBitwidth = (int) Math.ceil(Math.log(treeList.size()) / Math.log(2));


        var validationTableGenerator = new ValidationTableGenerator();
        var controllerGenerator      = new ControllerGenerator();

        validationTableGenerator.execute(classQuantity, featureQuantity, classBitwidth, voteCounterBitwidth, datasetName);
        controllerGenerator.execute(classBitwidth, featureQuantity, datasetName);
    }
}
