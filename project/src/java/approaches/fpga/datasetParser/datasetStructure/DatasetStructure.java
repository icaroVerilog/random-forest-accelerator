package project.src.java.approaches.fpga.datasetParser.datasetStructure;

import java.util.ArrayList;

public class DatasetStructure {

    private final static Integer USELESSCOLUMNS = 2; // class column and id column

    private ArrayList<String>   featuresNames;
    private ArrayList<Row> datasetRows;

    private Integer rowsQuantity;
    private Integer rowsSize;

    public DatasetStructure(ArrayList<String> featuresNames){
        this.datasetRows = new ArrayList<Row>();
        this.featuresNames = featuresNames;
        this.rowsQuantity = 0;
        this.rowsSize = featuresNames.size() - USELESSCOLUMNS;
    }

    public void addRow(ArrayList<String> datasetRowValues){

        ArrayList<Value> convertedValueRow = new ArrayList<Value>();

        for (String datasetRowValue : datasetRowValues) {
            Value aux = doubleStringToBinary(datasetRowValue);
            convertedValueRow.add(aux);
        }

        datasetRows.add(new Row(convertedValueRow));

        this.rowsQuantity++;
    }

    public ArrayList<Row> getRow(){
        return this.datasetRows;
    }

    public Integer getRowsQuantity(){
        return this.rowsQuantity;
    }

    public Integer getRowsSize(){
        return this.rowsSize;
    }


    private Value doubleStringToBinary(String value){
        Value newValue = new Value();

        var splitedString = value.split("\\.");

        String integral   = Integer.toBinaryString(Integer.parseInt(splitedString[0]));
        String fractional = Integer.toBinaryString(Integer.parseInt(splitedString[1]));

        String integral32bits = String.format("%32s", integral).replaceAll(" ", "0");
        String fractional32bits = String.format("%32s", fractional).replaceAll(" ", "0");

        newValue.integral   = integral32bits;
        newValue.fractional = fractional32bits;

        return newValue;
    }
}
