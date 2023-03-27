package project.src.java.approaches.fpga;

import project.src.java.util.FileBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DatasetParser {

    private static final int BITWIDTH = 32;
    public int readDataset(String dataset) throws IOException {

        ArrayList<String> integralFeaturesVal = new ArrayList<>();
        ArrayList<String> fractionalFeaturesVal = new ArrayList<>();

        var path = System.getProperty("user.dir") + "/project/assets/datasets/" + dataset + ".csv";
        var scanner = new Scanner(new File(path));
        var line = scanner.nextLine().split(",");

        int datasetDepth = 0;

        for (int index = 0; index < line.length - 1; index++){
            integralFeaturesVal.add(index, "");
            fractionalFeaturesVal.add(index, "");
        }

        while(scanner.hasNext()){
            line = scanner.nextLine().split(",");

            for (int index = 0; index < line.length - 1; index++){

                String aux = integralFeaturesVal.get(index);
                integralFeaturesVal.remove(index);

                integralFeaturesVal.add(index, aux + ValueToBinary(line[index], true) + "\n");
                fractionalFeaturesVal.add(index, aux + ValueToBinary(line[index], false) + "\n");

            }

            datasetDepth++;
        }

        scanner.close();

        for (int index = 0; index < integralFeaturesVal.size(); index++) {
            FileBuilder.execute(integralFeaturesVal.get(index), "FPGA/dataset/" + "feature" + index + "_exponent.bin");
            FileBuilder.execute(fractionalFeaturesVal.get(index), "FPGA/dataset/" + "feature" + index + "_fraction.bin");
        }

        return datasetDepth;
    }

    private String ValueToBinary(String value, boolean integralPart){

        String splitedValue = null;

        if (integralPart) {
            splitedValue = value.substring(0, value.indexOf("."));
        }
        else {
            splitedValue = value.substring(value.indexOf(".") + 1);
        }

        String binaryValue = Integer.toBinaryString(Integer.parseInt(splitedValue));
        int missedZeros = BITWIDTH - binaryValue.length();
        if (missedZeros > 0) {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < missedZeros; i++) {
                sb.append("0");
            }
            sb.append(binaryValue);
            binaryValue = sb.toString();
        }
        return binaryValue;
    }
}
