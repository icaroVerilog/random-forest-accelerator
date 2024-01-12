package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLOutput;

public class PythonDatasetParserCaller {

    /* Substituir por configurações a partir de um JSON ou XML */

    private static final String scriptName = "dataset_parser_script";
    private static final String runtime = "python3";

    public int execute(String basePath, String datasetName) {
        String pythonScriptPath = String.format("%s/project/src/python/%s.py", basePath, scriptName);
        System.out.println(basePath);
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec(String.format(
                            "%s %s %s %s %b %d",
                            runtime,
                            pythonScriptPath,
                            basePath,
                            datasetName,
                            true,
                            12
                    ));

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            return process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
