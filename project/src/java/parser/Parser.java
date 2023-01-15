package project.src.java.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class Parser {
    private String datasetName;
    private String path;

    public Parser(String datasetName){
        this.datasetName = datasetName;
    }

    public void read(){

        this.path = System.getProperty("user.dir") + "/project/assets/trees/" + this.datasetName + "/tree0.txt";

        try {
            File file = new File(this.path);
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
