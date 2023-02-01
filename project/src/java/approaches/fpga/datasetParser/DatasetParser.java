package project.src.java.approaches.fpga.datasetParser;

import project.src.java.approaches.fpga.datasetParser.datasetStructure.DatasetStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DatasetParser {

//    private static List<String> featuresNames;
//    private static Set<String> classesNames;

    public DatasetStructure readDataset(String datasetName) throws FileNotFoundException {

        var path = System.getProperty("user.dir") + "/project/assets/datasets/" + datasetName + ".csv";
        var scanner = new Scanner(new File(path));

        List<String> aux = Arrays.stream(scanner.nextLine().split(",")).toList();
        DatasetStructure dataset = new DatasetStructure(new ArrayList<String>(aux));

        while(scanner.hasNext()){
            aux = Arrays.stream(scanner.nextLine().split(",")).toList();
            ArrayList<String> fileLine = new ArrayList<String>(aux);

            fileLine.remove(0);
            fileLine.remove(fileLine.size() - 1);

            dataset.addRow(fileLine);

        }
        scanner.close();
        return dataset;
    }
}
