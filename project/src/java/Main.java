package project.src.java;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.PythonDatasetParserCaller;
import project.src.java.util.pythonTreeGeneratorCaller;

import java.io.IOException;
import java.util.List;

public class Main {

    private static String dataset;
    private static String path;
    private static String approach;
    private static String mode;
    private static String precision;

    private static Integer datasetTestPercent = 30;
    private static Integer treeQuantity = 100;

    public static void main(String[] args) throws IOException {
        path = System.getProperty("user.dir");

        dataset = "iris";
        approach = "table";
        mode = "synthesis";
        precision = "integer";

        start();
    }

    public static void start() throws IOException{
        pythonTreeGeneratorCaller caller = new pythonTreeGeneratorCaller();
        caller.execute(path, dataset, datasetTestPercent, treeQuantity, precision);

        PythonDatasetParserCaller datasetParser = new PythonDatasetParserCaller();
        datasetParser.execute(path, dataset, approach);

        List<Tree> trees = Parser.execute(dataset);

        FPGA FPGAGenerator = new FPGA(
            trees,
            dataset,
            Parser.getClassQuantity(),
            Parser.getFeatureQuantity(),
            false
        );

        FPGAGenerator.execute(approach);

        System.out.println("job finished: Success");
    }
}