package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.equationGenerator.EquationFPGAGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator.MultiplexerFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings;

import java.io.IOException;
import java.util.List;

public class FPGA {
    public void execute(project.src.java.util.executionSettings.ExecutionSettingsData.Settings settings, List<Tree> treeList, int classQuantity, int featureQuantity) throws IOException {

        System.out.println("\nstarting FPGA random forest generator");

        switch (settings.approach) {
            case "conditional" -> executeConditionalApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (Settings) settings
            );
            case "table" -> executeTableApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings) settings
            );
            case "equation" -> executeEquationApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (Settings) settings
            );
            case "multiplexer" -> executeMultiplexerApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (Settings) settings
            );
        }

        System.out.println("\nfinishing FPGA random forest generator");
    }

    private void executeEquationApproach(List<Tree> treeList, int classQnt, int featureQnt, project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings settings) {
        System.out.println("equation approach\n");

        EquationFPGAGenerator equationFPGAGenerator = new EquationFPGAGenerator();

        equationFPGAGenerator.execute(
                treeList,
                classQnt,
                featureQnt,
                settings
        );

    }

    public void executeConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();

        conditionalGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executeMultiplexerApproach(List<Tree> treeList, int classQnt, int featureQnt, project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings settings) throws IOException {
        System.out.println("Multiplexer approach\n");

        MultiplexerFPGAGenerator multiplexerFPGAGenerator = new MultiplexerFPGAGenerator();

        multiplexerFPGAGenerator.execute(
                treeList,
                classQnt,
                featureQnt,
                settings
        );
    }

    private void executeTableApproach(List<Tree> treeList, int classQnt, int featureQnt, project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings settings) throws  IOException {
        System.out.println("table approach\n");

        var tableGenerator = new TableFPGAGenerator();

        tableGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }
}
