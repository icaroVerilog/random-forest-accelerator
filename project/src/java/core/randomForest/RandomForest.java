package project.src.java.core.randomForest;


import project.src.java.core.randomForest.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.core.randomForest.equationGenerator.EquationFPGAGenerator;
import project.src.java.core.randomForest.multiplexerGenerator.MultiplexerFPGAGenerator;
import project.src.java.core.randomForest.pipeline.PipelineFPGAGenerator;
import project.src.java.core.randomForest.table.parallelTableGenerator.ParallelTableFPGAGenerator;
import project.src.java.core.randomForest.table.tableGenerator.TableFPGAGenerator;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.io.IOException;
import java.util.List;

public class RandomForest {
    public void executeEquationApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) {
        System.out.println("equation approach\n");

        EquationFPGAGenerator equationFPGAGenerator = new EquationFPGAGenerator();

        equationFPGAGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );

    }

    public void executeConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalFPGAGenerator conditionalGenerator = new ConditionalFPGAGenerator();

        conditionalGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executeMultiplexerApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("multiplexer approach\n");

        MultiplexerFPGAGenerator multiplexerFPGAGenerator = new MultiplexerFPGAGenerator();

        multiplexerFPGAGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executePipelinedConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("pipelined conditional approach\n");

        PipelineFPGAGenerator pipelineGenerator = new PipelineFPGAGenerator();

        pipelineGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );

    }

    public void executeTableApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws  IOException {
        System.out.println("table approach\n");

        var tableGenerator = new TableFPGAGenerator();

        tableGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executeParallelTableApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws  IOException {
        System.out.println("parallel table approach\n");

        var parallelTableGenerator = new ParallelTableFPGAGenerator();

        parallelTableGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }
}
