package project.src.java;

//import project.src.java.approaches.fpga.FPGA;
//import project.src.java.dotTreeParser.treeStructure.Tree;
//import project.src.java.util.*;
//import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
//import project.src.java.util.executionSettings.ExecutionSettingsData.Table.SettingsT;
//import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;
//import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;
//import project.src.java.util.executionSettings.ExecutionSettingsParser;
//import project.src.java.util.relatory.ReportGenerator;

import project.src.java.core.randomForest.approaches.fpga.FPGA;
import project.src.java.core.randomForest.parsers.dotTreeParser.Parser;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Tree;
import project.src.java.userInterface.Parameter;
import project.src.java.userInterface.UserInterface;
import project.src.java.userInterface.ValidParameters;
import project.src.java.util.*;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.FieldsBitwidth;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.InferenceParameters;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.executionSettings.CLI.SettingsCLI;
import project.src.java.util.executionSettings.CLI.Table.SettingsT;
import project.src.java.util.executionSettings.CLI.TrainingParameters;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ExecutionSettings;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsParser;
import project.src.java.util.messages.Error;
import project.src.java.util.messages.Messages;

import java.io.IOException;
import java.util.List;

/* TODO: verificar valores proibidos para os parametros e tratar esses casos */

public class Main {
    private static String path;
    private static ExecutionSettings executionSettings = null;

    public static void main(String[] args) throws IOException {
        path = System.getProperty("user.dir");
        System.out.println(path);

        ExecutionSettingsParser settingsParser     = new ExecutionSettingsParser();
        InputJsonValidator      inputJsonValidator = new InputJsonValidator();
        UserInterface           userInterface      = new UserInterface();

        SettingsCLI settingsCLI = new SettingsCLI();

        while (true){
            Parameter parameter = userInterface.execute();

            if (parameter == null){
                continue;
            }
            else if (parameter.getParameter().equals(ValidParameters.EXIT)){
                return;
            }
            else if (parameter.getParameter().equals(ValidParameters.HELP)){
                System.out.println(Messages.HELP);
            }
            else if (parameter.getParameter().equals(ValidParameters.READ_SETTINGS)) {
                String filename = parameter.getValue().get("filename");
                try {
                    executionSettings = settingsParser.execute(path, filename);
                    inputJsonValidator.execute(executionSettings);
                } catch (IOException error) {
                    System.out.println(Error.INVALID_FILE);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.RUN_SETTINGS)){
                if (executionSettings == null){
                    System.out.println(Error.NOT_LOADED_SETTINGS);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.READ_DATASET)){
                String filename = parameter.getValue().get("filename");
                settingsCLI.dataset = filename;

            }
            else if (parameter.getParameter().equals(ValidParameters.START_TRAINING)){

                if (settingsCLI.dataset != null){
                    settingsCLI.trainingParameters = new TrainingParameters();

                    settingsCLI.trainingParameters.estimatorsQuantity = Integer.valueOf(parameter.getValue().get("-e"));
                    settingsCLI.trainingParameters.trainingPercent    = Integer.valueOf(parameter.getValue().get("-tp"));
                    settingsCLI.trainingParameters.maxDepth           = Integer.valueOf(parameter.getValue().get("-d"));

                    PythonTreeGeneratorCaller treeGeneratorCaller = new PythonTreeGeneratorCaller();

                    treeGeneratorCaller.execute(
                        path,
                        settingsCLI.dataset,
                        settingsCLI.trainingParameters.trainingPercent,
                        settingsCLI.trainingParameters.estimatorsQuantity,
                        settingsCLI.trainingParameters.maxDepth,
                        "integer"
//                      settingsCLI.trainingParameters.maxDepth
//                      settingsCLI.precision
                    );

                } else {
                    System.out.println(Error.NOT_LOADED_DATASET);
                }
            }
            else if (
                parameter.getParameter().equals(ValidParameters.START_IF_INFERENCE) ||
                parameter.getParameter().equals(ValidParameters.START_MUX_INFERENCE) ||
                parameter.getParameter().equals(ValidParameters.START_EQUATION_INFERENCE)
            ){
                if (settingsCLI.dataset != null & settingsCLI.trainingParameters != null){

                    SettingsCEM settings = new SettingsCEM();
                    settings.dataset            = settingsCLI.dataset;
                    settings.trainingParameters = settingsCLI.trainingParameters;
                    settings.approach           = "conditional";

                    settings.inferenceParameters                = new InferenceParameters();
                    settings.inferenceParameters.fieldsBitwidth = new FieldsBitwidth();
                    settings.inferenceParameters.fieldsBitwidth.comparedValue = Integer.valueOf(parameter.getValue().get("-bw"));

                    FPGA FPGAGenerator = new FPGA();
                    List<Tree> trees = Parser.execute(settingsCLI.dataset);

                    switch (parameter.getParameter()){
                        case ValidParameters.START_IF_INFERENCE -> FPGAGenerator.executeConditionalApproach(
                            trees,
                            Parser.getClassQuantity(),
                            Parser.getFeatureQuantity(),
                            settings
                        );
                        case ValidParameters.START_MUX_INFERENCE -> FPGAGenerator.executeMultiplexerApproach(
                            trees,
                            Parser.getClassQuantity(),
                            Parser.getFeatureQuantity(),
                            settings
                        );
                        case ValidParameters.START_EQUATION_INFERENCE -> FPGAGenerator.executeEquationApproach(
                            trees,
                            Parser.getClassQuantity(),
                            Parser.getFeatureQuantity(),
                            settings
                        );
                        case ValidParameters.START_IF_PIPELINED_INFERENCE -> FPGAGenerator.executePipelineApproach(
                            trees,
                            Parser.getClassQuantity(),
                            Parser.getFeatureQuantity(),
                            settings
                        );
                    }
                } else {
                    System.out.println(Error.NOT_TRAINED_NOT_LOADED_DATASET);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.START_TABLE_INFERENCE)){
                System.out.println(parameter.getValue().keySet());
                if (settingsCLI.dataset != null & settingsCLI.trainingParameters != null){

                    SettingsT settings = new SettingsT();

                    settings.dataset            = settingsCLI.dataset;
                    settings.trainingParameters = settingsCLI.trainingParameters;
                    settings.approach           = "table";

                    settings.inferenceParameters = new project.src.java.util.executionSettings.CLI.Table.InferenceParameters();
                    settings.inferenceParameters.fieldsBitwidth = new project.src.java.util.executionSettings.CLI.Table.FieldsBitwidth();
                    settings.inferenceParameters.fieldsBitwidth.comparedValue  = Integer.valueOf(parameter.getValue().get("-tbw")); /*threshold bitwidth*/
                    settings.inferenceParameters.fieldsBitwidth.index          = Integer.valueOf(parameter.getValue().get("-ibw"));
                    settings.inferenceParameters.fieldsBitwidth.comparedColumn = Integer.valueOf(parameter.getValue().get("-cbw"));

                } else {
                    System.out.println(Error.NOT_TRAINED_NOT_LOADED_DATASET);
                }
            }

//            parameter.print();

//            FileBuilder.setupFolders();
//
//            PythonTreeGeneratorCaller     treeGeneratorCaller     = new PythonTreeGeneratorCaller();
////        PythonDatasetParserCaller     datasetParserCaller     = new PythonDatasetParserCaller();
//
//            FPGA FPGAGenerator = new FPGA();
//
//            HashMap<String, Boolean> estimatorsGenerationController = new HashMap<>();
//
//            for (int index = 0; index < executionsSettings.executionsSettings.size(); index++) {
//                Settings settings = executionsSettings.executionsSettings.get(index);
//
//
//                /* verifica se a flag que indica se devera ou não regerar novas arvores pro mesmo dataset */
//                /*
//                 *  caso no HashMap não ouver uma chave (nome do dataset) retornará null e gerará as arvores para o mesmo
//                 *  caso não retorne null significa que as arvores ja foram geradas, dando continuidade à execução do algoritmo
//                 * */
//
//                if (Objects.equals(executionsSettings.estimatorsGenerationPolicy, "regenerate")){
//                    treeGeneratorCaller.execute(
//                            path,
//                            settings.dataset,
//                            settings.trainingParameters.trainingPercent,
//                            settings.trainingParameters.estimatorsQuantity,
//                            settings.trainingParameters.maxDepth,
//                            settings.precision
//                    );
//                }
//                else if (Objects.equals(executionsSettings.estimatorsGenerationPolicy, "not regenerate")){
//                    if (estimatorsGenerationController.get(executionsSettings.executionsSettings.get(index).dataset) == null) {
//                        treeGeneratorCaller.execute(
//                                path,
//                                settings.dataset,
//                                settings.trainingParameters.trainingPercent,
//                                settings.trainingParameters.estimatorsQuantity,
//                                settings.trainingParameters.maxDepth,
//                                settings.precision
//                        );
//                        estimatorsGenerationController.put(
//                                executionsSettings.executionsSettings.get(index).dataset,
//                                true
//                        );
//                    }
//                }
//                else if (Objects.equals(executionsSettings.estimatorsGenerationPolicy, "keep")){
//                    ;;
//                }
//                else {
//                    break;
//                }
//
//
//                List<Tree> trees = Parser.execute(settings.dataset);
//
//                FPGAGenerator.execute(
//                        settings,
//                        trees,
//                        Parser.getClassQuantity(),
//                        Parser.getFeatureQuantity()
//                );
//            }
//            ReportGenerator reportGenerator = new ReportGenerator();
//            reportGenerator.generateReport();
//            System.out.println("job finished: Success");
        }
    }
}