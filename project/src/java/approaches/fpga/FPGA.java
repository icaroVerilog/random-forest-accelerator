package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;

import java.io.IOException;
import java.util.List;

public class FPGA {

    String datasetName;
    Integer classQuantity;
    Integer featureQuantity;
    List<Tree> treeList;

    public FPGA(
        List<Tree> treeList,
        String datasetName,
        Integer classQuantity,
        Integer featureQuantity
    ){
        this.treeList = treeList;
        this.datasetName = datasetName;
        this.classQuantity = classQuantity;
        this.featureQuantity = featureQuantity;
    }

    public void execute(ExecutionSettings settings) throws IOException {

//        System.out.println("\n========================================\n");
        System.out.println("\nstarting FPGA random forest generator");

        if (settings.inferenceParameters.approach.equals("conditional")){
            executeConditionalApproach();
        }
        if (settings.inferenceParameters.approach.equals("table")){
            executeTableApproach(settings);
        }

        System.out.println("\nfinishing FPGA random forest generator");
//        System.out.println("\n========================================\n");
    }
    public void executeConditionalApproach() throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();
        DatasetParser datasetParser = new DatasetParser();

        int samplesQnt = datasetParser.readDataset(datasetName);
        conditionalGenerator.execute(
            this.treeList,
            this.classQuantity,
            this.featureQuantity,
            samplesQnt,
            false,
            this.datasetName
        );
    }

    private void executeTableApproach(ExecutionSettings settings) throws  IOException {
        System.out.println("table approach\n");

        var tableGenerator = new TableFPGAGenerator();

        tableGenerator.execute(
            this.treeList,
            this.classQuantity,
            this.featureQuantity,
            settings
        );
    }
}
