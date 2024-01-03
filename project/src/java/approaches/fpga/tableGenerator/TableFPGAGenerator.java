package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.List;

public class TableFPGAGenerator {
    public void execute(
        List<Tree> treeList,
        Integer classQnt,
        Integer featureQnt,
        Boolean debugMode,
        String dataset
    ){
        var validationTableGenerator = new ValidationTableGenerator();

        validationTableGenerator.execute(treeList, classQnt, featureQnt, dataset);
    }
}
