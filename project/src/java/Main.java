package project.src.java;

import java.io.IOException;
import java.util.List;
import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.PythonScriptCaller;

public class Main {

    private static String dataset;
    private static String path;

    public static void main(String[] args) throws IOException {
        dataset = "iris";
        path = System.getProperty("user.dir");

        start();
    }

    public static void start() throws IOException{

        PythonScriptCaller caller = new PythonScriptCaller();
        caller.execute(path, dataset);


        List<Tree> trees = Parser.execute(dataset);

        FPGA FPGAGenerator = new FPGA(
            trees,
            dataset,
            Parser.getClassQuantity(),
            Parser.getFeatureQuantity(),
            false
        );

        FPGAGenerator.execute("table");

        System.out.println("job finished: Success");
    }
}