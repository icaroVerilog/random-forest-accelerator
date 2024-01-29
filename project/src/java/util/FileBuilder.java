package project.src.java.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBuilder {

    public static void execute(String content, String filePath){
        try {
            var path = System.getProperty("user.dir") + "/project/target/" + filePath;
            var fileWriter = new FileWriter(path);
            BufferedWriter out = new BufferedWriter(fileWriter);
            out.write(content);
            out.close();
        }
        catch(IOException e){
            System.err.println("Error writing file '" + filePath + "'");
            System.err.println(e.toString());
        }
    }

    public static void setupFolders(){
        var folderPath = System.getProperty("user.dir") + "/project/target/";
        File folder = new File(folderPath);

        if (!folder.exists()){
            folder.mkdir();
        }
    }

    public static boolean createDir(String path){
        List<String> directories = Arrays.asList(path.split("/"));

        String incrementalPath = "";

        for (int index = 0; index < directories.size(); index++) {
            incrementalPath += "/" + directories.get(index);

            var folderPath = System.getProperty("user.dir") + "/project/target/" + incrementalPath;
            File folder = new File(folderPath);

            if (!folder.exists()){
                folder.mkdir();
            }
        }

        return true;

//        System.out.println(path);

    }
}
