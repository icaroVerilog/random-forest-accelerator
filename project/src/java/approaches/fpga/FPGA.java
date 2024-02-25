package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.equationGenerator.EquationFPGAGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator.MultiplexerFPGAGenerator;
import project.src.java.approaches.fpga.tableGenerator.TableFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table.SettingsT;

import java.io.IOException;
import java.util.List;

public class FPGA {
    public void execute(Settings settings, List<Tree> treeList, int classQuantity, int featureQuantity) throws IOException {

        System.out.println("\nstarting FPGA random forest generator");

        switch (settings.approach) {
            case "conditional" -> executeConditionalApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsCEM) settings
            );
            case "table" -> executeTableApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsT) settings
            );
            case "equation" -> executeEquationApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsCEM) settings
            );
            case "multiplexer" -> executeMultiplexerApproach(
                    treeList,
                    classQuantity,
                    featureQuantity,
                    (SettingsCEM) settings
            );
        }

        System.out.println("\nfinishing FPGA random forest generator");
    }

    private void executeEquationApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings) {
        System.out.println("equation approach\n");

        EquationFPGAGenerator equationFPGAGenerator = new EquationFPGAGenerator();

        equationFPGAGenerator.execute(
                treeList,
                classQnt,
                featureQnt,
                settings
        );

    }

    public void executeConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();

        conditionalGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executeMultiplexerApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings) throws IOException {
        System.out.println("Multiplexer approach\n");

        MultiplexerFPGAGenerator multiplexerFPGAGenerator = new MultiplexerFPGAGenerator();

        multiplexerFPGAGenerator.execute(
                treeList,
                classQnt,
                featureQnt,
                settings
        );
    }

    private void executeTableApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsT settings) throws  IOException {
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
