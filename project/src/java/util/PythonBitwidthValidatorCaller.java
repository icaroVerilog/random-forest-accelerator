package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonBitwidthValidatorCaller {

    private static final String scriptName = "bitwidth_validator_script";
    private static final String runtime = "python3";

    public int execute(String basePath, String datasetName, int bitwidth) {
        String pythonScriptPath = String.format("%s/project/src/python/%s.py", basePath, scriptName);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    runtime,
                    pythonScriptPath,
                    basePath,
                    datasetName,
                    Boolean.toString(true),
                    Integer.toString(bitwidth)
            );

            processBuilder.redirectErrorStream(true);
            Process pythonProcess = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
            }
            reader.close();
            return pythonProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
