package project.src.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PythonScriptCaller {
    public void execute(String path, String argument) {

        String pythonScriptPath = path + "/project/src/python/tree_generator.py";

        try {
            Process process = Runtime.getRuntime().exec("python3 " + pythonScriptPath + " " + argument);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            int exitCode = process.waitFor();
            System.out.println("Código de retorno: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
