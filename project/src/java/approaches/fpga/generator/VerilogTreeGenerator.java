package project.src.java.approaches.fpga.generator;

import project.src.java.approaches.fpga.datasetParser.datasetStructure.DatasetStructure;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class VerilogTreeGenerator {

    private DatasetStructure dataset;

    private String treeName;

    public VerilogTreeGenerator(String treeName, DatasetStructure dataset) {
        this.treeName = treeName;
        this.dataset = dataset;
    }

    public boolean generate() throws IOException {

        FileWriter file = new FileWriter(System.getProperty("user.dir") + "/project/" + treeName + "10" + ".v");
        PrintWriter writeFile = new PrintWriter(file);

        writeHeader(writeFile);
        writePortDeclaration(writeFile);
        writeFile.close();

        return true;
    }

    private void writeHeader(PrintWriter writeFile){

        writeFile.println("module " + treeName + "10" + "(");
        writeFile.println(writeIndentation(1) + "clock, voted_class, ");
        writeFile.print(writeIndentation(1));
        writeFile.print(writeIO("integral", "ft"));
        writeFile.print("\n");
        writeFile.print(writeIndentation(1));
        writeFile.print(writeIO("fractional", "ft"));
        writeFile.print("\n");
        writeFile.print(writeIndentation(1));
        writeFile.print(writeIO("integral", "th"));
        writeFile.print("\n");
        writeFile.print(writeIndentation(1));
        writeFile.print(writeIO("fractional", "th"));
        writeFile.print("\n");
        writeFile.println(");");

    }

    private void writePortDeclaration(PrintWriter writeFile){
        writeFile.println(writeIndentation(1) + "input wire clock;");
        writeFile.println(writeIndentation(1) + "input wire clock;");

        writeFile.print(writeIndentation(1));
        writeFile.println(writePort("integral", "ft"));
        writeFile.print(writeIndentation(1));
        writeFile.println(writePort("integral", "th"));
        writeFile.print(writeIndentation(1));
        writeFile.println(writePort("fractional", "ft"));
        writeFile.print(writeIndentation(1));
        writeFile.println(writePort("fractional", "th"));


    }

    private void writeTreeLogic(){

    }

    private String writePort(String name, String type){
        String port = "";
        String placeholder = "input wire [31:0] ";

        for (int index = 0; index < this.dataset.getRowsSize(); index++){
            port = port.concat(placeholder + type + index + "_" + name + "\n" + writeIndentation(1));
        }

        return port;
    }

    private String writeIO(String name, String type){
        String IO = "";

        for (int index = 0; index < this.dataset.getRowsSize(); index++){
            IO = IO.concat(type + index + "_" +  name + ", ");
        }

        return IO;
    }

    private String writeIndentation(Integer size){

        Integer identLeap = 4;
        String identation = "";

        for (int index = 0; index < size * identLeap; index ++){
            identation = identation.concat(" ");
        }

        return identation;
    }
}
