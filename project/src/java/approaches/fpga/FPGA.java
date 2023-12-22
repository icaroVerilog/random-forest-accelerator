package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;

import java.io.IOException;
import java.util.List;

public class FPGA {

    String dataset;
    Integer classQnt;
    Integer featureQnt;
    List<Tree> treeList;
    Boolean debugMode;

    public FPGA(List<Tree> treeList, String dataset, Integer classQnt, Integer featureQnt, Boolean debugMode){
        this.treeList = treeList;
        this.dataset = dataset;
        this.classQnt = classQnt;
        this.featureQnt = featureQnt;
        this.debugMode = debugMode;
    }

    public void execute() throws IOException {

        System.out.println("\n========================================\n");
        System.out.println("starting FPGA random forest generator");

//        executeConditionalApproach();
        executeTableApproach();

        System.out.println("\nfinishing FPGA random forest generator");
        System.out.println("\n========================================\n");
    }
    public void executeConditionalApproach() throws IOException {

        System.out.println("conditional approach\n");

        var conditionalGen = new ConditionalFPGAGenerator();
        var datasetParser = new DatasetParser();

        int samplesQnt = datasetParser.readDataset(dataset);
        conditionalGen.execute(treeList, classQnt, featureQnt, samplesQnt, debugMode, dataset);
    }

    private void executeTableApproach() throws  IOException {
        System.out.println("table approach\n");

        var tableGen = new TableFPGAGenerator();

        tableGen.execute(treeList, classQnt, featureQnt, debugMode, dataset);
    }
}
