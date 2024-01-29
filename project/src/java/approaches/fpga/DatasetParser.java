package project.src.java.approaches.fpga;

import project.src.java.util.FileBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DatasetParser {

    private static final int BITWIDTH = 12;
    public int readDataset(String dataset) throws IOException {

        ArrayList<String> featuresVal = new ArrayList<>();

        var path = System.getProperty("user.dir") + "/project/assets/datasets/" + dataset + ".csv";
        var scanner = new Scanner(new File(path));
        var line = scanner.nextLine().split(",");

        int datasetDepth = 0;

        String data = "";

        while (scanner.hasNext()){
            line = scanner.nextLine().split(",");
            ArrayList<String> lineArray = new ArrayList<>(List.of(line));

            lineArray.remove(lineArray.size() - 1);
            String aux = "";

            for (int index = 0; index < lineArray.size(); index++){

                var splitedValues = lineArray.get(index).split("\\.");
                String exponent = valueToBinary(Integer.valueOf(splitedValues[0]), BITWIDTH);
                String fraction = valueToBinary(Integer.valueOf(splitedValues[1]), BITWIDTH);

                aux += exponent + fraction;
            }

            aux+= "\n";
            data += aux;

            datasetDepth++;
        }

        scanner.close();

//        FileBuilder.execute(data, String.format("FPGA/%s_conditional_run/controller.v", settings.dataset)));

        return datasetDepth;
    }

    private String valueToBinary(Integer value, Integer bitwidth){
        return String.format("%" + bitwidth + "s", Integer.toBinaryString(value)).replaceAll(" ", "0");
    }
}
