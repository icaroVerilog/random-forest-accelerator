package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.ExecutionSettingsData.SettingsConditional;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;
import project.src.java.util.executionSettings.ExecutionSettingsData.SettingsTable;

import java.io.IOException;
import java.util.List;

public class FPGA {
    public void execute(Settings settings, List<Tree> treeList, int classQuantity, int featureQuantity) throws IOException {

        System.out.println("\nstarting FPGA random forest generator");

        if (settings.approach.equals("conditional")){
            executeConditionalApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsConditional) settings
            );
        }
        else if (settings.approach.equals("table")){
            executeTableApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsTable) settings
            );
        }

        System.out.println("\nfinishing FPGA random forest generator");
    }
    public void executeConditionalApproach(List<Tree> treeList, int classQuantity, int featureQuantity, SettingsConditional settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();

        conditionalGenerator.execute(
            treeList,
            classQuantity,
            featureQuantity,
            settings
        );
    }

    private void executeTableApproach(List<Tree> treeList, int classQuantity, int featureQuantity, SettingsTable settings) throws  IOException {
        System.out.println("table approach\n");

        var tableGenerator = new TableFPGAGenerator();

        tableGenerator.execute(
            treeList,
            classQuantity,
            featureQuantity,
            settings
        );
    }
}
