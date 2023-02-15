package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConditionalFPGAGenerator {
    public void execute(List<Tree> treeList, String dataset, Integer classQnt, Integer featureQnt){

        var controllerGenerator = new ControllerGenerator();
//        var treeGenerator = new TreeGenerator();
//
//        treeGenerator.execute(treeList, dataset, classQnt, featureQnt);
        controllerGenerator.execute(treeList.size(), dataset, classQnt, featureQnt);
//        System.out.println(dataset);
//        System.out.println(classQnt);
//        System.out.println(featureQnt);

    }

}
