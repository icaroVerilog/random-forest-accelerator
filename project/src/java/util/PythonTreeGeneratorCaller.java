package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonTreeGeneratorCaller {

    private static final String scriptName = "tree_generator_script";
    private static final String runtime = "python3";

    public int execute(String basePath, String dataset, int datasetTestPercent, int estimatorQnt, String max_depth, String precision) {
        String pythonScriptPath = String.format("%s/project/src/python/%s.py", basePath, scriptName);

        System.out.println("\nstarting training");
        System.out.printf("dataset: %s\n", dataset);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                runtime,
                pythonScriptPath,
                dataset,
                basePath,
                Integer.toString(datasetTestPercent),
                Integer.toString(estimatorQnt),
                precision,
                max_depth
            );

            processBuilder.redirectErrorStream(true);
            Process pythonProcess = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            return pythonProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
