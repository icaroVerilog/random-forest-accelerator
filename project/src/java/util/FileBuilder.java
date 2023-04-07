package project.src.java.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    public static boolean createDir(String path){

        var folderPath = System.getProperty("user.dir") + "/project/target/" + path;
        File folder = new File(folderPath);
        return folder.mkdir();

    }
}
