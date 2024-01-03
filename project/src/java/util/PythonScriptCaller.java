package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonScriptCaller {
    public int execute(String path, String dataset) {

        String pythonScriptPath = path + "/project/src/python/tree_generator.py";

        try {
            Process process = Runtime.getRuntime().exec("python3 " + pythonScriptPath + " " + dataset + " " + path);
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
