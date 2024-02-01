package project.src.java.approaches.fpga.equationGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.List;

public class EquationFPGAGenerator {
    public void execute(
            List<Tree> treeList,
            int classQnt,
            int featureQnt,
            Settings settings
    ) {
        var a = FileBuilder.createDir(String.format("FPGA/%s_equation_run", settings.dataset));

        var treeGenerator       = new TreeGenerator();
        var controllerGenerator = new ControllerGenerator();

        treeGenerator.execute(treeList, classQnt, featureQnt, settings);
        controllerGenerator.execute(treeList.size(), classQnt, featureQnt, false, settings);
    }
}
