package project.src.java.core.randomForest;


import project.src.java.core.randomForest.conditionalGenerator.ConditionalGenerator;
import project.src.java.core.randomForest.equationGenerator.EquationGenerator;
import project.src.java.core.randomForest.multiplexerGenerator.MultiplexerGenerator;
import project.src.java.core.randomForest.pipelineGenerator.PipelineGenerator;
import project.src.java.core.randomForest.tableGenerator.parallelTableGenerator.ParallelTableFPGAGenerator;
import project.src.java.core.randomForest.tableGenerator.tableGenerator.TableFPGAGenerator;
import project.src.java.core.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

import java.io.IOException;
import java.util.List;

public class RandomForest {
    public void executeEquationApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) {
        System.out.println("equation approach\n");

        EquationGenerator equationFPGAGenerator = new EquationGenerator();

        equationFPGAGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );

    }

    public void executeConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("conditional approach\n");

        ConditionalGenerator conditionalGenerator = new ConditionalGenerator();

        conditionalGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executeMultiplexerApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("multiplexer approach\n");

        MultiplexerGenerator multiplexerFPGAGenerator = new MultiplexerGenerator();

        multiplexerFPGAGenerator.execute(
            treeList,
            classQnt,
            featureQnt,
            settings
        );
    }

    public void executePipelinedConditionalApproach(List<Tree> treeList, int classQnt, int featureQnt, SettingsCli settings) throws IOException {
        System.out.println("pipelined conditional approach\n");

        PipelineGenerator pipelineGenerator = new PipelineGenerator();

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
