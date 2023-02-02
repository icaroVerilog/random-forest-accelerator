package project.src.java;

import java.io.IOException;
import java.util.List;

import com.sun.source.util.Trees;
import project.src.java.approaches.fpga.generator.ConditionalFPGAGenerator;
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

        ConditionalFPGAGenerator a = new ConditionalFPGAGenerator();
        a.execute(trees);
    }
}