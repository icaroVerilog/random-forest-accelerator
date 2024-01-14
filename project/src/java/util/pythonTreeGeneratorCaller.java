package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class pythonTreeGeneratorCaller {

    private static final String scriptName = "tree_generator_script";
    private static final String runtime = "python3";

    public int execute(String basePath, String dataset) {
        String pythonScriptPath = String.format("%s/project/src/python/%s.py", basePath, scriptName);
        try {
            Process process = Runtime
                .getRuntime()
                .exec(String.format(
                     "%s %s %s %s",
                     runtime,
                     pythonScriptPath,
                     dataset,
                     basePath
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
