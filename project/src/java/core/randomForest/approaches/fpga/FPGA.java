package project.src.java.core.randomForest.approaches.fpga;


import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.equationGenerator.EquationFPGAGenerator;
import project.src.java.core.randomForest.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator.MultiplexerFPGAGenerator;
import project.src.java.core.randomForest.approaches.fpga.pipeline.PipelineFPGAGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCEM;

import java.io.IOException;
import java.util.List;

public class FPGA {
    public void executeEquationApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings) {
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
        System.out.println("multiplexer approach\n");

        MultiplexerFPGAGenerator multiplexerFPGAGenerator = new MultiplexerFPGAGenerator();

        multiplexerFPGAGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executePipelinedConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCEM settings) throws IOException {
        System.out.println("pipelined conditional approach\n");

        PipelineFPGAGenerator pipelineGenerator = new PipelineFPGAGenerator();

        pipelineGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );

    }
//
//    private void executeTableApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsT settings) throws  IOException {
//        System.out.println("table approach\n");
//
//        var tableGenerator = new TableFPGAGenerator();
//
//        tableGenerator.execute(
//            treeList,
//            classQnt,
//            featureQnt,
//            settings
//        );
//    }
}
