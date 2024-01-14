package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonDatasetParserCaller {

    /* Substituir por configurações a partir de um JSON ou XML */

    private static final String scriptName = "dataset_parser_script";
    private static final String runtime = "python3";

    public int execute(String basePath, String datasetName, String approach) {
        String pythonScriptPath = String.format("%s/project/src/python/%s.py", basePath, scriptName);
        try {
//            Process process = Runtime
//                    .getRuntime()
//                    .exec(String.format(
//                            "%s %s %s %s %b %d %s",
//                            runtime,
//                            pythonScriptPath,
//                            basePath,
//                            datasetName,
//                            true,
//                            12,
//                            approach
//                    ));

            ProcessBuilder processBuilder = new ProcessBuilder(
                    runtime,
                    pythonScriptPath,
                    basePath,
                    datasetName,
                    Boolean.toString(true),
                    Integer.toString(12),
                    approach
//                    String.format(
//                            "%s %s %s %s %b %d %s",
//                            runtime,
//                            pythonScriptPath,
//                            basePath,
//                            datasetName,
//                            true,
//                            12,
//                            approach
//                    )
            );

            processBuilder.redirectErrorStream(true);
            Process processoPython = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(processoPython.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            return processoPython.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
