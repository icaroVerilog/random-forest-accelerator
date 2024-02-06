package project.src.java;

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.util.FileBuilder;
import project.src.java.util.PythonBitwidthValidatorCaller;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;
import project.src.java.util.executionSettings.ExecutionSettingsParser;
import project.src.java.util.PythonTreeGeneratorCaller;
import project.src.java.util.PythonDatasetParserCaller;
import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;

import java.io.IOException;
import java.util.HashMap;
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

        HashMap<String, Boolean> estimatorsGenerationController = new HashMap<>();

        for (int index = 0; index < executionsSettings.executionsSettings.size(); index++) {
            Settings settings = executionsSettings.executionsSettings.get(index);


            /* verifica se a flag que indica se devera ou não regerar novas arvores pro mesmo dataset */
            /*
            *  caso no HashMap não ouver uma chave (nome do dataset) retornará null e gerará as arvores para o mesmo
            *  caso não retorne null significa que as arvores ja foram geradas, dando continuidade à execução do algoritmo
            * */

            if (!executionsSettings.regenerateEstimators){
                if (estimatorsGenerationController.get(executionsSettings.executionsSettings.get(index).dataset) == null){
                    treeGeneratorCaller.execute(
                        path,
                        settings.dataset,
                        settings.trainingParameters.trainingPercent,
                        settings.trainingParameters.estimatorsQuantity,
                        settings.trainingParameters.maxDepth,
                        settings.precision
                    );
                    estimatorsGenerationController.put(
                        executionsSettings.executionsSettings.get(index).dataset,
                        true
                    );
                }
            } else {
                treeGeneratorCaller.execute(
                    path,
                    settings.dataset,
                    settings.trainingParameters.trainingPercent,
                    settings.trainingParameters.estimatorsQuantity,
                    settings.trainingParameters.maxDepth,
                    settings.precision
                );
            }

            List<Tree> trees = Parser.execute(settings.dataset);

            FPGAGenerator.execute(
                settings,
                trees,
                Parser.getClassQuantity(),
                Parser.getFeatureQuantity()
            );

//            if (settings instanceof project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings settingsT){
//                int a = datasetParserCaller.execute(
//                    path,
//                    settingsT.inferenceParameters.fieldsBitwidth.comparedValue,
//                    settings.dataset,
//                    settings.approach,
//                    settings.precision
//                );
//            }
//            else if (settings instanceof project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings settingsCEM){
//                datasetParserCaller.execute(
//                    path,
//                    settingsCEM.inferenceParameters.fieldsBitwidth.comparedValue,
//                    settings.dataset,
//                    settings.approach,
//                    settings.precision
//                );
//            }
        }

        System.out.println("job finished: Success");
    }
}