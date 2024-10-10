package project.src.java;

//import project.src.java.approaches.fpga.FPGA;
//import project.src.java.dotTreeParser.treeStructure.Tree;
//import project.src.java.util.*;
//import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsJsonCEM;
//import project.src.java.util.executionSettings.ExecutionSettingsData.Table.SettingsJsonT;
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
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.InferenceParameters;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;
import project.src.java.util.executionSettings.CLI.SettingsCLI;
import project.src.java.util.executionSettings.CLI.Table.SettingsCliT;
import project.src.java.util.executionSettings.CLI.TrainingParameters;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ConditionalEquationMux.SettingsJsonCEM;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ExecutionSettings;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Settings;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Table.SettingsJsonT;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsParser;
import project.src.java.messages.Error;
import project.src.java.messages.Messages;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static project.src.java.messages.Error.INVALID_FLAG_VALUE;

/* TODO: verificar valores proibidos para os parametros e tratar esses casos */

public class Main {
    private static String path;
    private static ExecutionSettings executionSettings = null;

    public static void main(String[] args) throws IOException {
        path = System.getProperty("user.dir");
        System.out.println(path);

        ExecutionSettingsParser settingsParser = new ExecutionSettingsParser();
        InputJsonValidator inputJsonValidator = new InputJsonValidator();
        UserInterface userInterface = new UserInterface();

        SettingsCLI settingsCLI = new SettingsCLI();

        while (true) {
            Parameter parameter = userInterface.execute();

            if (parameter == null) {
                continue;
            }
            else if (parameter.getParameter().equals(ValidParameters.EXIT)) {
                return;
            }
            else if (parameter.getParameter().equals(ValidParameters.HELP)) {
                System.out.println(Messages.HELP);
            }
            else if (parameter.getParameter().equals(ValidParameters.READ_DATASET)) {
                String filename = parameter.getValue().get("filename");
                boolean exists = (new File(path + "/datasets/" + filename)).exists();

                if (exists) {
                    settingsCLI.dataset = filename;
                } else {
                    System.out.println(Error.INVALID_FILE);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.BINARIZE_DATASET)) {
                String filename = parameter.getValue().get("filename");
                int bitwidth = Integer.parseInt(parameter.getValue().get("-bw"));

                if (!(new File(path + "/datasets/" + filename)).exists()) {
                    System.out.println(Error.INVALID_FILE);
                } else {
                    // TODO: verificar se a quantidade de bits consegue representar os valores corretamente
                    PythonDatasetParserCaller a = new PythonDatasetParserCaller();
                    a.execute(path, bitwidth, filename);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.START_TRAINING)) {
                if (settingsCLI.dataset != null) {
                    settingsCLI.trainingParameters = new TrainingParameters();

                    settingsCLI.trainingParameters.estimatorsQuantity = Integer.valueOf(parameter.getValue().get("-e"));
                    settingsCLI.trainingParameters.trainingPercent = Integer.valueOf(parameter.getValue().get("-tp"));
                    settingsCLI.trainingParameters.maxDepth = Integer.valueOf(parameter.getValue().get("-d"));

                    PythonTreeGeneratorCaller treeGeneratorCaller = new PythonTreeGeneratorCaller();

                    treeGeneratorCaller.execute(
                        path,
                        settingsCLI.dataset,
                        settingsCLI.trainingParameters.trainingPercent,
                        settingsCLI.trainingParameters.estimatorsQuantity,
                        settingsCLI.trainingParameters.maxDepth
                    );

                } else {
                    System.out.println(Error.NOT_LOADED_DATASET);
                }
            }
            else if (
                parameter.getParameter().equals(ValidParameters.START_IF_INFERENCE) ||
                parameter.getParameter().equals(ValidParameters.START_MUX_INFERENCE) ||
                parameter.getParameter().equals(ValidParameters.START_EQUATION_INFERENCE) ||
                parameter.getParameter().equals(ValidParameters.START_IF_PIPELINED_INFERENCE)
            ) {
                if (settingsCLI.dataset != null & settingsCLI.trainingParameters != null) {
                    SettingsCli settings = new SettingsCli();
                    settings.dataset = settingsCLI.dataset;
                    settings.trainingParameters = settingsCLI.trainingParameters;

                    settings.inferenceParameters = new InferenceParameters();

                    if (
                        !Objects.equals(parameter.getValue().get("-p"), "half") &&
                        !Objects.equals(parameter.getValue().get("-p"), "normal") &&
                        !Objects.equals(parameter.getValue().get("-p"), "double")
                    ){
                        System.out.println(INVALID_FLAG_VALUE.replace("x", "-p"));
                        continue;
                    }

                    settings.inferenceParameters.precision = parameter.getValue().get("-p");

                    FPGA FPGAGenerator = new FPGA();
                    List<Tree> trees = Parser.execute(settingsCLI.dataset);

                    switch (parameter.getParameter()) {
                        case ValidParameters.START_IF_INFERENCE:
                            settings.approach = "conditional";

                            FPGAGenerator.executeConditionalApproach(
                                trees,
                                Parser.getClassQuantity(),
                                Parser.getFeatureQuantity(),
                                settings
                            );
                            break;
                        case ValidParameters.START_MUX_INFERENCE:
                            settings.approach = "multiplexer";

                            FPGAGenerator.executeMultiplexerApproach(
                                trees,
                                Parser.getClassQuantity(),
                                Parser.getFeatureQuantity(),
                                settings
                            );
                            break;
                        case ValidParameters.START_EQUATION_INFERENCE:
                            settings.approach = "equation";

                            FPGAGenerator.executeEquationApproach(
                                trees,
                                Parser.getClassQuantity(),
                                Parser.getFeatureQuantity(),
                                settings
                            );
                            break;
                        case ValidParameters.START_IF_PIPELINED_INFERENCE:
                            settings.approach = "conditional_pipelined";

                            FPGAGenerator.executePipelinedConditionalApproach(
                                trees,
                                Parser.getClassQuantity(),
                                Parser.getFeatureQuantity(),
                                settings
                            );
                            break;
                    }
                } else {
                    System.out.println(Error.NOT_TRAINED_NOT_LOADED_DATASET);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.START_TABLE_INFERENCE)) {
                System.out.println(parameter.getValue().keySet());
                if (settingsCLI.dataset != null & settingsCLI.trainingParameters != null) {

                    SettingsCliT settings = new SettingsCliT();

                    settings.dataset = settingsCLI.dataset;
                    settings.trainingParameters = settingsCLI.trainingParameters;
                    settings.approach = "table";

                    settings.inferenceParameters = new project.src.java.util.executionSettings.CLI.Table.InferenceParameters();
                    settings.inferenceParameters.fieldsBitwidth = new project.src.java.util.executionSettings.CLI.Table.FieldsBitwidth();
                    settings.inferenceParameters.precision = parameter.getValue().get("-p");

                    FPGA FPGAGenerator = new FPGA();
                    List<Tree> trees = Parser.execute(settingsCLI.dataset);

                    FPGAGenerator.executeTableApproach(
                        trees,
                        Parser.getClassQuantity(),
                        Parser.getFeatureQuantity(),
                        settings
                    );

                } else {
                    System.out.println(Error.NOT_TRAINED_NOT_LOADED_DATASET);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.READ_SETTINGS)) {
                String filename = parameter.getValue().get("filename");
                try {
                    executionSettings = settingsParser.execute(path, filename);
                    inputJsonValidator.execute(executionSettings);
                } catch (IOException error) {
                    System.out.println(error);
                    System.out.println(Error.INVALID_FILE);
                }
            }
            else if (parameter.getParameter().equals(ValidParameters.RUN_SETTINGS)) {
                if (executionSettings == null) {
                    System.out.println(Error.NOT_LOADED_SETTINGS);
                } else {
                    PythonTreeGeneratorCaller treeGeneratorCaller = new PythonTreeGeneratorCaller();
                    FPGA FPGAGenerator = new FPGA();

                    HashMap<String, Boolean> estimatorsGenerationController = new HashMap<>();

                    for (int index = 0; index < executionSettings.executionsSettings.size(); index++) {
                        Settings jsonSettings = executionSettings.executionsSettings.get(index);

                        /* verifica se a flag que indica se devera ou não regerar novas arvores pro mesmo dataset */
                        /*
                         *  caso no HashMap não ouver uma chave (nome do dataset) retornará null e gerará as arvores para o mesmo
                         *  caso não retorne null significa que as arvores ja foram geradas, dando continuidade à execução do algoritmo
                         * */

                        if (Objects.equals(executionSettings.estimatorsGenerationPolicy, "regenerate")) {
                            treeGeneratorCaller.execute(
                                path,
                                jsonSettings.dataset,
                                jsonSettings.trainingParameters.trainingPercent,
                                jsonSettings.trainingParameters.estimatorsQuantity,
                                jsonSettings.trainingParameters.maxDepth
                            );
                        } else if (Objects.equals(executionSettings.estimatorsGenerationPolicy, "not regenerate")) {
                            if (estimatorsGenerationController.get(executionSettings.executionsSettings.get(index).dataset) == null) {
                                treeGeneratorCaller.execute(
                                    path,
                                    jsonSettings.dataset,
                                    jsonSettings.trainingParameters.trainingPercent,
                                    jsonSettings.trainingParameters.estimatorsQuantity,
                                    jsonSettings.trainingParameters.maxDepth
                                );
                                estimatorsGenerationController.put(
                                    executionSettings.executionsSettings.get(index).dataset,
                                    true
                                );
                            }
                        } else if (Objects.equals(executionSettings.estimatorsGenerationPolicy, "keep")) {
                            ;
                            ;
                        }

                        List<Tree> trees = Parser.execute(jsonSettings.dataset);

                        if (jsonSettings instanceof SettingsJsonCEM) {
                            SettingsCli settings = new SettingsCli();
                            settings.inferenceParameters.precision = ((SettingsJsonCEM) jsonSettings).inferenceParameters.precision;
                            settings.trainingParameters.trainingPercent = jsonSettings.trainingParameters.trainingPercent;
                            settings.trainingParameters.estimatorsQuantity = jsonSettings.trainingParameters.estimatorsQuantity;
                            settings.trainingParameters.maxDepth = jsonSettings.trainingParameters.maxDepth;

                            switch (jsonSettings.approach) {
                                case "conditional":
                                    settings.approach = "conditional";
                                    FPGAGenerator.executeConditionalApproach(
                                        trees,
                                        Parser.getClassQuantity(),
                                        Parser.getFeatureQuantity(),
                                        settings
                                    );
                                    break;
                                case "multiplexer":
                                    settings.approach = "multiplexer";

                                    FPGAGenerator.executeMultiplexerApproach(
                                        trees,
                                        Parser.getClassQuantity(),
                                        Parser.getFeatureQuantity(),
                                        settings
                                    );
                                    break;
                                case "equation":
                                    settings.approach = "equation";

                                    FPGAGenerator.executeEquationApproach(
                                        trees,
                                        Parser.getClassQuantity(),
                                        Parser.getFeatureQuantity(),
                                        settings
                                    );
                                    break;
                                case "conditional_pipeline":
                                    settings.approach = "conditional_pipeline";

                                    FPGAGenerator.executePipelinedConditionalApproach(
                                        trees,
                                        Parser.getClassQuantity(),
                                        Parser.getFeatureQuantity(),
                                        settings
                                    );
                                    break;
                            }
                        } else if (jsonSettings instanceof SettingsJsonT) {
                            SettingsCliT settings = new SettingsCliT();
                            settings.inferenceParameters.fieldsBitwidth.comparedValue  = ((SettingsJsonT) jsonSettings).inferenceParameters.fieldsBitwidth.comparedValue;
                            settings.inferenceParameters.fieldsBitwidth.index          = ((SettingsJsonT) jsonSettings).inferenceParameters.fieldsBitwidth.index;
                            settings.inferenceParameters.fieldsBitwidth.comparedColumn = ((SettingsJsonT) jsonSettings).inferenceParameters.fieldsBitwidth.comparedColumn;
                            settings.trainingParameters.trainingPercent                = jsonSettings.trainingParameters.trainingPercent;
                            settings.trainingParameters.estimatorsQuantity             = jsonSettings.trainingParameters.estimatorsQuantity;
                            settings.trainingParameters.maxDepth                       = jsonSettings.trainingParameters.maxDepth;
                            settings.approach = "table";

                            System.out.println(settings.inferenceParameters.fieldsBitwidth.toString());

//                            FPGAGenerator.executeTableApproach(
//                                trees,
//                                Parser.getClassQuantity(),
//                                Parser.getFeatureQuantity(),
//                                settings
//                            );
                        }
                    }
//                    ReportGenerator reportGenerator = new ReportGenerator();
//                    reportGenerator.generateReport();
//                    System.out.println("job finished: Success");
                }
            }
        }
    }
}