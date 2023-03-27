package project.src.java;

import java.io.IOException;
import java.util.List;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.approaches.fpga.conditionalGenerator.TreeGenerator;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;

public class Main {

    private static String dataset;
    public static void main(String[] args) throws IOException {
        dataset = "Iris";
        start();
    }

    public static void start() throws IOException{
        List<Tree> trees = Parser.execute(dataset);
        FPGA a = new FPGA(trees, dataset, Parser.getClassQuantity(), Parser.getFeatureQuantity(), true);
        a.execute();
    }
}