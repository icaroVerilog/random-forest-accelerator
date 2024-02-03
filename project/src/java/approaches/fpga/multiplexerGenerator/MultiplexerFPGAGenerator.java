package project.src.java.approaches.fpga.multiplexerGenerator;

import project.src.java.approaches.fpga.ControllerGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.List;

public class MultiplexerFPGAGenerator {
    public void execute(
            List<Tree> treeList,
            int classQnt,
            int featureQnt,
            Settings settings
    ){
        var a = FileBuilder.createDir(String.format("FPGA/%s_multiplexer_run", settings.dataset));

        var controllerGenerator = new ControllerGenerator();
//        var treeGenerator       = new TreeGenerator();
//
//        treeGenerator      .execute(treeList, classQnt, featureQnt, dataset);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, settings);
    }
}
