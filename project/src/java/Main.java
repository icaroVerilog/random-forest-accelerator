package project.src.java;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.PythonBitwidthValidatorCaller;
import project.src.java.util.executionSettings.ExecutionSettingsParser;
import project.src.java.util.PythonTreeGeneratorCaller;
import project.src.java.util.PythonDatasetParserCaller;
import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings;

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
        ExecutionSettings executionsSettings = settingsParser.execute(path);

        FileBuilder.setupFolders();

        PythonBitwidthValidatorCaller bitwidthValidatorCaller = new PythonBitwidthValidatorCaller();
        PythonTreeGeneratorCaller     treeGeneratorCaller     = new PythonTreeGeneratorCaller();
        PythonDatasetParserCaller     datasetParserCaller     = new PythonDatasetParserCaller();

//        int returnCode = bitwidthValidatorCaller.execute(
//                path,
//                settings.generalParameters.datasetName,
//                settings.inferenceParameters.table.fieldsBitwidth.comparedValue
//        );
//
//        if (returnCode != 0) {
//            System.out.printf("the number of bits required for the values in the dataset is at least %d bits\n\n", returnCode);
//            System.out.println("job finished: Failed");
//            System.exit(1);
//        }

        FPGA FPGAGenerator = new FPGA();

        for (int index = 0; index < executionsSettings.executionsSettings.size(); index++) {
            project.src.java.util.executionSettings.ExecutionSettingsData.Settings settings = executionsSettings.executionsSettings.get(index);

            treeGeneratorCaller.execute(
                    path,
                    settings.dataset,
                    settings.trainingParameters.trainingPercent,
                    settings.trainingParameters.estimatorsQuantity,
                    settings.precision
            );

            List<Tree> trees = Parser.execute(settings.dataset);

            FPGAGenerator.execute(
                    settings,
                    trees,
                    Parser.getClassQuantity(),
                    Parser.getFeatureQuantity()
            );

            if (settings instanceof Settings settingsT){
                datasetParserCaller.execute(
                        path,
                        settingsT.inferenceParameters.fieldsBitwidth.comparedValue,
                        settings.dataset,
                        settings.approach,
                        settings.precision
                );
            }
            else if (settings instanceof project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings settingsC){
                datasetParserCaller.execute(
                        path,
                        settingsC.inferenceParameters.fieldsBitwidth.comparedValue,
                        settings.dataset,
                        settings.approach,
                        settings.precision
                );
            }
        }

        System.out.println("job finished: Success");
    }
}