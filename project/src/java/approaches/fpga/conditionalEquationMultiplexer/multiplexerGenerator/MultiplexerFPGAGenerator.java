package project.src.java.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.AdderGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.ControllerGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.MajorityGenerator;
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

        var treeGenerator       = new TreeGenerator();
        var controllerGenerator = new ControllerGenerator();
        var adderGenerator      = new AdderGenerator();
        var majorityGenerator   = new MajorityGenerator();

        treeGenerator      .execute(treeList, classQnt, featureQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, settings);
        adderGenerator     .execute(treeList.size(), settings);
        majorityGenerator  .execute(treeList.size(), classQnt, settings);
    }
}
