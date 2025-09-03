package project.src.java.util;

import project.src.java.relatory.ReportGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonTreeGeneratorCaller {

    private static final String scriptName = "tree_generator";
    private static final String runtime = "python3";

    public int execute(
        String basePath,
        String dataset,
        Integer datasetTestPercent,
        Integer estimatorQnt,
        Integer max_depth,
        Double normalizeMaxValue,
        Double normalizeMinValue
    ) {
        String pythonScriptPath = String.format("%s/scripts/%s.py", basePath, scriptName);

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
                Integer.toString(max_depth),
                Double.toString(normalizeMaxValue),
                Double.toString(normalizeMinValue)
            );


            processBuilder.redirectErrorStream(true);
            Process pythonProcess = processBuilder.start();

            ReportGenerator reportGenerator = new ReportGenerator();
            BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("accuracy")){
                    reportGenerator.setAccuracy(line);
                }
            }
            reader.close();
            return pythonProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
