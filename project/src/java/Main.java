package project.src.java;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.PythonDatasetParserCaller;
import project.src.java.util.executionSettings.ExecutionSettingsParser;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;
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
        precision = "decimal";

        start();
    }

    public static void start() throws IOException{

        ExecutionSettingsParser settingsParser = new ExecutionSettingsParser();
        ExecutionSettings settings = settingsParser.execute(path);

        pythonTreeGeneratorCaller caller = new pythonTreeGeneratorCaller();
        caller.execute(
            path,
            settings.getGeneralParameters().getDataset(),
            settings.getTrainingParameters().getTrainingPercent(),
            settings.getTrainingParameters().getEstimatorsQuantity(),
            settings.getGeneralParameters().getPrecision()
        );

        List<Tree> trees = Parser.execute(
            settings.getGeneralParameters().getDataset()
        );

        FPGA FPGAGenerator = new FPGA(
            trees,
            settings.getGeneralParameters().getDataset(),
            Parser.getClassQuantity(),
            Parser.getFeatureQuantity(),
            false
        );

        FPGAGenerator.execute(approach, precision);

        PythonDatasetParserCaller datasetParser = new PythonDatasetParserCaller();
        datasetParser.execute(
                path,
                settings.getGeneralParameters().getDataset(),
                settings.getInferenceParameters().getApproach(),
                settings.getGeneralParameters().getPrecision()
        );

        System.out.println("job finished: Success");
    }
}