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

import project.src.java.approaches.fpga.FPGA;
import project.src.java.dotTreeParser.Parser;
import project.src.java.dotTreeParser.treeStructure.Tree;
import project.src.java.jsonCumlTreeParser.ParserCUML;
import project.src.java.util.*;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table.SettingsT;
import project.src.java.util.executionSettings.ExecutionSettingsParser;
import project.src.java.util.relatory.ReportGenerator;

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
//        System.out.println("starting FPGA random forest generator");
//
//        ParserCUML jsonCumlParser = new ParserCUML();
//        jsonCumlParser.execute("[\n" +
//                "{\"nodeid\": 0, \"split_feature\": 2, \"split_threshold\": -0.468885541, \"gain\": 0.117744744, \"instance_count\": 800, \"yes\": 1, \"no\": 2, \"children\": [\n" +
//                "  {\"nodeid\": 1, \"split_feature\": 5, \"split_threshold\": -1.293064, \"gain\": 0.018344976, \"instance_count\": 342, \"yes\": 3, \"no\": 4, \"children\": [\n" +
//                "    {\"nodeid\": 3, \"split_feature\": 1, \"split_threshold\": -1.04627514, \"gain\": 0.246910572, \"instance_count\": 34, \"yes\": 7, \"no\": 8, \"children\": [\n" +
//                "      {\"nodeid\": 7, \"leaf_value\": [0.571428597, 0.428571433, 0, 0], \"instance_count\": 7},\n" +
//                "      {\"nodeid\": 8, \"leaf_value\": [0, 0, 1, 0], \"instance_count\": 27}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 4, \"split_feature\": 4, \"split_threshold\": -1.44984889, \"gain\": 0.0384900942, \"instance_count\": 308, \"yes\": 9, \"no\": 10, \"children\": [\n" +
//                "      {\"nodeid\": 9, \"leaf_value\": [0, 0.0833333358, 0.0833333358, 0.833333313], \"instance_count\": 12},\n" +
//                "      {\"nodeid\": 10, \"leaf_value\": [0.44932431, 0.0337837823, 0.493243247, 0.0236486495], \"instance_count\": 296}\n" +
//                "    ]}\n" +
//                "  ]},\n" +
//                "  {\"nodeid\": 2, \"split_feature\": 2, \"split_threshold\": 0.238180876, \"gain\": 0.0229998436, \"instance_count\": 458, \"yes\": 5, \"no\": 6, \"children\": [\n" +
//                "    {\"nodeid\": 5, \"split_feature\": 5, \"split_threshold\": 1.43305457, \"gain\": 0.0408207327, \"instance_count\": 128, \"yes\": 11, \"no\": 12, \"children\": [\n" +
//                "      {\"nodeid\": 11, \"leaf_value\": [0.203389823, 0.211864412, 0.177966103, 0.406779647], \"instance_count\": 118},\n" +
//                "      {\"nodeid\": 12, \"leaf_value\": [0.800000012, 0, 0.200000003, 0], \"instance_count\": 10}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 6, \"split_feature\": 4, \"split_threshold\": 0.337322503, \"gain\": 0.0383936837, \"instance_count\": 330, \"yes\": 13, \"no\": 14, \"children\": [\n" +
//                "      {\"nodeid\": 13, \"leaf_value\": [0.0452261306, 0.582914591, 0.0552763827, 0.316582918], \"instance_count\": 199},\n" +
//                "      {\"nodeid\": 14, \"leaf_value\": [0.0839694664, 0.259541988, 0.114503816, 0.541984737], \"instance_count\": 131}\n" +
//                "    ]}\n" +
//                "  ]}\n" +
//                "]},\n" +
//                "{\"nodeid\": 0, \"split_feature\": 4, \"split_threshold\": -1.12736738, \"gain\": 0.0134663451, \"instance_count\": 800, \"yes\": 1, \"no\": 2, \"children\": [\n" +
//                "  {\"nodeid\": 1, \"split_feature\": 3, \"split_threshold\": -0.634608746, \"gain\": 0.0861610025, \"instance_count\": 46, \"yes\": 3, \"no\": 4, \"children\": [\n" +
//                "    {\"nodeid\": 3, \"split_feature\": 2, \"split_threshold\": 0.532207608, \"gain\": 0.0990598127, \"instance_count\": 30, \"yes\": 7, \"no\": 8, \"children\": [\n" +
//                "      {\"nodeid\": 7, \"leaf_value\": [0, 0.0769230798, 0.0384615399, 0.884615362], \"instance_count\": 26},\n" +
//                "      {\"nodeid\": 8, \"leaf_value\": [0, 0.75, 0, 0.25], \"instance_count\": 4}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 4, \"split_feature\": 6, \"split_threshold\": 0.28401497, \"gain\": 0.160653412, \"instance_count\": 16, \"yes\": 9, \"no\": 10, \"children\": [\n" +
//                "      {\"nodeid\": 9, \"leaf_value\": [0.272727281, 0.181818187, 0.0909090936, 0.454545468], \"instance_count\": 11},\n" +
//                "      {\"nodeid\": 10, \"leaf_value\": [0.200000003, 0, 0.800000012, 0], \"instance_count\": 5}\n" +
//                "    ]}\n" +
//                "  ]},\n" +
//                "  {\"nodeid\": 2, \"split_feature\": 1, \"split_threshold\": -0.191326976, \"gain\": 0.141880527, \"instance_count\": 754, \"yes\": 5, \"no\": 6, \"children\": [\n" +
//                "    {\"nodeid\": 5, \"split_feature\": 3, \"split_threshold\": -0.126712233, \"gain\": 0.0376978703, \"instance_count\": 340, \"yes\": 11, \"no\": 12, \"children\": [\n" +
//                "      {\"nodeid\": 11, \"leaf_value\": [0.656565666, 0.141414136, 0.181818187, 0.0202020202], \"instance_count\": 99},\n" +
//                "      {\"nodeid\": 12, \"leaf_value\": [0.452282161, 0.493775934, 0.0539419092, 0], \"instance_count\": 241}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 6, \"split_feature\": 4, \"split_threshold\": 0.439960927, \"gain\": 0.032270547, \"instance_count\": 414, \"yes\": 13, \"no\": 14, \"children\": [\n" +
//                "      {\"nodeid\": 13, \"leaf_value\": [0.05111821, 0.127795532, 0.533546329, 0.287539929], \"instance_count\": 313},\n" +
//                "      {\"nodeid\": 14, \"leaf_value\": [0.0792079195, 0, 0.306930691, 0.613861382], \"instance_count\": 101}\n" +
//                "    ]}\n" +
//                "  ]}\n" +
//                "]},\n" +
//                "{\"nodeid\": 0, \"split_feature\": 2, \"split_threshold\": -0.0725142956, \"gain\": 0.122627936, \"instance_count\": 800, \"yes\": 1, \"no\": 2, \"children\": [\n" +
//                "  {\"nodeid\": 1, \"split_feature\": 4, \"split_threshold\": -1.16023755, \"gain\": 0.0402289517, \"instance_count\": 373, \"yes\": 3, \"no\": 4, \"children\": [\n" +
//                "    {\"nodeid\": 3, \"split_feature\": 3, \"split_threshold\": -0.457189888, \"gain\": 0.145263016, \"instance_count\": 37, \"yes\": 7, \"no\": 8, \"children\": [\n" +
//                "      {\"nodeid\": 7, \"leaf_value\": [0, 0.0357142873, 0.25, 0.714285731], \"instance_count\": 28},\n" +
//                "      {\"nodeid\": 8, \"leaf_value\": [0.333333343, 0.444444448, 0.222222224, 0], \"instance_count\": 9}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 4, \"split_feature\": 4, \"split_threshold\": -0.817565918, \"gain\": 0.0114319064, \"instance_count\": 336, \"yes\": 9, \"no\": 10, \"children\": [\n" +
//                "      {\"nodeid\": 9, \"leaf_value\": [0.692307711, 0, 0.0769230798, 0.230769232], \"instance_count\": 13},\n" +
//                "      {\"nodeid\": 10, \"leaf_value\": [0.41795665, 0.0526315793, 0.507739961, 0.021671826], \"instance_count\": 323}\n" +
//                "    ]}\n" +
//                "  ]},\n" +
//                "  {\"nodeid\": 2, \"split_feature\": 1, \"split_threshold\": -0.0369066, \"gain\": 0.193837956, \"instance_count\": 427, \"yes\": 5, \"no\": 6, \"children\": [\n" +
//                "    {\"nodeid\": 5, \"split_feature\": 4, \"split_threshold\": -0.652814746, \"gain\": 0.0496984795, \"instance_count\": 200, \"yes\": 11, \"no\": 12, \"children\": [\n" +
//                "      {\"nodeid\": 11, \"leaf_value\": [0, 0.285714298, 0, 0.714285731], \"instance_count\": 14},\n" +
//                "      {\"nodeid\": 12, \"leaf_value\": [0.188172042, 0.758064508, 0.0483870953, 0.00537634408], \"instance_count\": 186}\n" +
//                "    ]},\n" +
//                "    {\"nodeid\": 6, \"split_feature\": 3, \"split_threshold\": -0.0869798735, \"gain\": 0.0658717006, \"instance_count\": 227, \"yes\": 13, \"no\": 14, \"children\": [\n" +
//                "      {\"nodeid\": 13, \"leaf_value\": [0, 0.389999986, 0.0799999982, 0.529999971], \"instance_count\": 100},\n" +
//                "      {\"nodeid\": 14, \"leaf_value\": [0.00787401572, 0, 0.125984251, 0.866141737], \"instance_count\": 127}\n" +
//                "    ]}\n" +
//                "  ]}\n" +
//                "]}\n" +
//                "]");

        ExecutionSettingsParser settingsParser     = new ExecutionSettingsParser();
        InputJsonValidator inputJsonValidator = new InputJsonValidator();

        ExecutionSettings executionsSettings = settingsParser.execute(path);
        inputJsonValidator.execute(executionsSettings);

        FileBuilder.setupFolders();

        PythonBitwidthValidatorCaller bitwidthValidatorCaller = new PythonBitwidthValidatorCaller();
//        PythonTreeGeneratorCaller treeGeneratorCaller     = new PythonTreeGeneratorCaller();
//        PythonDatasetParserCaller datasetParserCaller     = new PythonDatasetParserCaller();

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
//                    treeGeneratorCaller.execute(
//                        path,
//                        settings.dataset,
//                        settings.trainingParameters.trainingPercent,
//                        settings.trainingParameters.estimatorsQuantity,
//                        settings.trainingParameters.maxDepth,
//                        settings.precision
//                    );
                    estimatorsGenerationController.put(
                        executionsSettings.executionsSettings.get(index).dataset,
                        true
                    );
                }
            } else {
//                treeGeneratorCaller.execute(
//                    path,
//                    settings.dataset,
//                    settings.trainingParameters.trainingPercent,
//                    settings.trainingParameters.estimatorsQuantity,
//                    settings.trainingParameters.maxDepth,
//                    settings.precision
//                );
            }

            List<Tree> trees = Parser.execute(settings.dataset);

            FPGAGenerator.execute(
                settings,
                trees,
                Parser.getClassQuantity(),
                Parser.getFeatureQuantity()
            );

            if (settings instanceof SettingsT){
//                datasetParserCaller.execute(
//                    path,
//                    ((SettingsT) settings).inferenceParameters.fieldsBitwidth.comparedValue,
//                    settings.dataset,
//                    settings.approach,
//                    settings.precision,
//                    settings.trainingParameters.maxDepth,
//                    settings.trainingParameters.estimatorsQuantity
//                );
            }
            if (settings instanceof SettingsCEM){
//                datasetParserCaller.execute(
//                    path,
//                    ((SettingsCEM) settings).inferenceParameters.fieldsBitwidth.comparedValue,
//                    settings.dataset,
//                    settings.approach,
//                    settings.precision,
//                    settings.trainingParameters.maxDepth,
//                    settings.trainingParameters.estimatorsQuantity
//                );
            }
        }
        ReportGenerator reportGenerator = new ReportGenerator();
        reportGenerator.generateReport();
        System.out.println("job finished: Success");
    }
}