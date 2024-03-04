package project.src.java.approaches.fpga.conditionalEquationMultiplexer.multiplexerGenerator;

import project.src.java.approaches.fpga.conditionalEquationMultiplexer.AdderGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.AlteraCycloneApi;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.ControllerGenerator;
import project.src.java.approaches.fpga.conditionalEquationMultiplexer.MajorityGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;

import java.util.List;

public class MultiplexerFPGAGenerator {
    public void execute(
            List<Tree> treeList,
            int classQnt,
            int featureQnt,
            SettingsCEM settings
    ){
        var a = FileBuilder.createDir(String.format("FPGA/%s_multiplexer_%dtree_%sdeep_run", settings.dataset, settings.trainingParameters.estimatorsQuantity, settings.trainingParameters.maxDepth));

        var treeGenerator       = new TreeGenerator();
        var controllerGenerator = new ControllerGenerator();
        var adderGenerator      = new AdderGenerator();
        var majorityGenerator   = new MajorityGenerator();
        var alteraCycloneAPI    = new AlteraCycloneApi();

        treeGenerator      .execute(treeList, classQnt, featureQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, settings);
        adderGenerator     .execute(treeList.size(), settings);
        majorityGenerator  .execute(treeList.size(), classQnt, settings);
        alteraCycloneAPI   .execute(classQnt, featureQnt, settings);
    }
}
