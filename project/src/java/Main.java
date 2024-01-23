package project.src.java;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.PythonBitwidthValidatorCaller;
import project.src.java.util.executionSettings.ExecutionSettingsParser;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;
import project.src.java.util.PythonTreeGeneratorCaller;
import project.src.java.util.PythonDatasetParserCaller;

import java.io.IOException;
import java.util.List;

public class Main {

    private static String path;

    public static void main(String[] args) throws IOException {
        path = System.getProperty("user.dir");
        start();
    }

    public static void start() throws IOException{

        ExecutionSettingsParser settingsParser = new ExecutionSettingsParser();
        ExecutionSettings settings = settingsParser.execute(path);

        PythonBitwidthValidatorCaller bitwidthValidatorCaller = new PythonBitwidthValidatorCaller();
        int returnCode = bitwidthValidatorCaller.execute(
                path,
                settings.generalParameters.datasetName,
                settings.inferenceParameters.fieldsBitwidth.comparedValue
        );

        if (returnCode != 0) {
            System.out.printf("The number of bits required for the values in the dataset is at least %d bits\n\n", returnCode);
            System.out.println("job finished: Failed");
            System.exit(1);
        }

        PythonTreeGeneratorCaller caller = new PythonTreeGeneratorCaller();
        caller.execute(
            path,
            settings.generalParameters.datasetName,
            settings.trainingParameters.trainingPercent,
            settings.trainingParameters.estimatorsQuantity,
            settings.generalParameters.precision
        );

        List<Tree> trees = Parser.execute(
            settings.generalParameters.datasetName
        );

        FPGA FPGAGenerator = new FPGA(
            trees,
            settings.generalParameters.datasetName,
            Parser.getClassQuantity(),
            Parser.getFeatureQuantity()
        );

        FPGAGenerator.execute(
            settings
        );

        PythonDatasetParserCaller datasetParser = new PythonDatasetParserCaller();
        datasetParser.execute(
                path,
                settings.inferenceParameters.fieldsBitwidth.comparedValue,
                settings.generalParameters.datasetName,
                settings.inferenceParameters.approach,
                settings.generalParameters.precision
        );

        System.out.println("job finished: Success");
    }
}