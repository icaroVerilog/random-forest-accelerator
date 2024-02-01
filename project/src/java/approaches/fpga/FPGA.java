package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.equationGenerator.EquationFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.io.IOException;
import java.util.List;

public class FPGA {
    public void execute(project.src.java.util.executionSettings.ExecutionSettingsData.Settings settings, List<Tree> treeList, int classQuantity, int featureQuantity) throws IOException {

        System.out.println("\nstarting FPGA random forest generator");

        if (settings.approach.equals("conditional")){
            executeConditionalApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings) settings
            );
        }
        else if (settings.approach.equals("table")){
            executeTableApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings) settings
            );
        }
        else if (settings.approach.equals("equation")){
            executeEquationApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings) settings
            );
        }

        System.out.println("\nfinishing FPGA random forest generator");
    }

    private void executeEquationApproach(List<Tree> treeList, int classQuantity, int featureQuantity, project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings settings) {
        System.out.println("equation approach\n");

        EquationFPGAGenerator equationFPGAGenerator = new EquationFPGAGenerator();

        equationFPGAGenerator.execute(
                treeList,
                classQuantity,
                featureQuantity,
                settings
        );

    }




    public void executeConditionalApproach(List<Tree> treeList, int classQuantity, int featureQuantity, project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();

        conditionalGenerator.execute(
            treeList,
            classQuantity,
            featureQuantity,
            settings
        );
    }




    private void executeTableApproach(List<Tree> treeList, int classQuantity, int featureQuantity, project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings settings) throws  IOException {
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
