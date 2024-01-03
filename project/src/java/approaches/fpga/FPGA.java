package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;

import java.io.IOException;
import java.util.List;

public class FPGA {

    String datasetName;
    Integer classQuantity;
    Integer featureQuantity;
    List<Tree> treeList;
    Boolean debugMode;

    public FPGA(
        List<Tree> treeList,
        String datasetName,
        Integer classQuantity,
        Integer featureQuantity,
        Boolean debugMode
    ){
        this.treeList = treeList;
        this.datasetName = datasetName;
        this.classQuantity = classQuantity;
        this.featureQuantity = featureQuantity;
        this.debugMode = debugMode;
    }

    public void execute(String approach) throws IOException {

        System.out.println("\n========================================\n");
        System.out.println("starting FPGA random forest generator");

        if (approach.equals("conditional")){
            executeConditionalApproach();
        }
        if (approach.equals("table")){
            executeTableApproach();
        }

        System.out.println("\nfinishing FPGA random forest generator");
        System.out.println("\n========================================\n");
    }
    public void executeConditionalApproach() throws IOException {
        System.out.println("conditional approach\n");

        var conditionalGenerator = new ConditionalFPGAGenerator();
        var datasetParser = new DatasetParser();

        int samplesQnt = datasetParser.readDataset(datasetName);
        conditionalGenerator.execute(
            this.treeList,
            this.classQuantity,
            this.featureQuantity,
            samplesQnt,
            this.debugMode,
            this.datasetName
        );
    }

    private void executeTableApproach() throws  IOException {
        System.out.println("table approach\n");

        var tableGenerator = new TableFPGAGenerator();

        tableGenerator.execute(
            this.treeList,
            this.classQuantity,
            this.featureQuantity,
            this.debugMode,
            this.datasetName
        );
    }
}
