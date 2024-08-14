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

import project.src.java.userInterface.Parameter;
import project.src.java.userInterface.UserInterface;
import project.src.java.userInterface.ValidParameters;
import project.src.java.util.*;
import project.src.java.util.customExceptions.InvalidCommandException;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ExecutionSettings;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsParser;
import project.src.java.util.messages.Error;
import project.src.java.util.messages.Messages;

import java.io.IOException;

public class Main {
    private static String path;
    private static ExecutionSettings executionSettings = null;

    public static void main(String[] args) throws IOException {
        path = System.getProperty("user.dir");
        System.out.println(path);

        ExecutionSettingsParser settingsParser     = new ExecutionSettingsParser();
        InputJsonValidator      inputJsonValidator = new InputJsonValidator();
        UserInterface userInterface = new UserInterface();

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
                    System.out.println(Error.UNLOADED_SETTINGS);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.START_TRAINING)){
                System.out.println(parameter.getValue().keySet());
                System.out.println(parameter.getValue().entrySet());
            }
            else if (
               parameter.getParameter().equals(ValidParameters.START_MUX_INFERENCE) ||
               parameter.getParameter().equals(ValidParameters.START_EQUATION_INFERENCE)
            ) {
                System.out.println(parameter.getValue().keySet());
            }
            else if (parameter.getParameter().equals(ValidParameters.START_IF_INFERENCE)){
                System.out.println(parameter.getValue().keySet());
            }
            else if (parameter.getParameter().equals(ValidParameters.START_TABLE_INFERENCE)){
                System.out.println(parameter.getValue().keySet());
            }
            else {
                String commandError = Error.INVALID_COMMAND.replace("x", parameter.getParameter());
                System.out.println(commandError);
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