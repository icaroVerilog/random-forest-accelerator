package project.src.java.approaches.fpga;


import project.src.java.approaches.fpga.conditionalGenerator.ConditionalFPGAGenerator;
import project.src.java.dotTreeParser.treeStructure.Tree;

import java.io.IOException;
import java.util.List;

public class FPGA {

    String dataset;
    Integer classQnt;
    Integer featureQnt;

    List<Tree> treeList;

    public FPGA(List<Tree> treeList, String dataset, Integer classQnt, Integer featureQnt){
        this.treeList = treeList;
        this.dataset = dataset;
        this.classQnt = classQnt;
        this.featureQnt = featureQnt;
    }

    public void executeConditionalApproach(){
        var conditionalGen = new ConditionalFPGAGenerator();
        conditionalGen.execute(treeList, dataset, classQnt, featureQnt);
    }

}
