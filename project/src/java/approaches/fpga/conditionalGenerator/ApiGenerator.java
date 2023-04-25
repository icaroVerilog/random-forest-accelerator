package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApiGenerator extends BasicGenerator {
    public void execute(Integer featureQnt, Boolean debugMode, String dataset){
        String sourceCode = "";
        sourceCode += generateImport("controller");
        sourceCode += generateIO();

        FileBuilder.execute(sourceCode, "FPGA/" + dataset + "/api.v");


    }

    private String generateImport(String moduleName){
        return "`include " + moduleName + ".v\n";
    }

    private String generateIO(){

        String tab = generateTab(1);
        String sourceCode = "";

        ArrayList<String> moduleIO = new ArrayList<>(
                Arrays.asList(
                        "clock", "start", "reset", "done", "data_read_done", "data_write_done", "available_write",
                        "data_read_valid", "data_read_request", "data_read", "data_write"
                )
        );

        sourceCode += generateModule("api", moduleIO);
        sourceCode += "\n";
        sourceCode += tab + generatePort("clock", WIRE,      INPUT, 1, true);
        sourceCode += tab + generatePort("start", WIRE,      INPUT, 1, true);
        sourceCode += tab + generatePort("reset", WIRE,      INPUT, 1, true);
        sourceCode += tab + generatePort("done" , REGISTER, OUTPUT, 1, true);
        sourceCode += "\n";
        sourceCode += tab + generatePort("data_read_done",  WIRE, INPUT, 2, true);
        sourceCode += tab + generatePort("data_write_done", WIRE, INPUT, 2, true);
        sourceCode += tab + generatePort("available_write", WIRE, INPUT, 2, true);
        sourceCode += tab + generatePort("data_read_valid", WIRE, INPUT, 2, true);
        sourceCode += tab + generatePort("data_read_request",  REGISTER, OUTPUT, 2, true);
        sourceCode += tab + generatePort("data_write_request", REGISTER, OUTPUT, 2, true);
        sourceCode += "\n";
        sourceCode += tab + generatePort("data_read", WIRE, INPUT, 256, true);
        sourceCode += tab + generatePort("data_write", REGISTER, OUTPUT, 256, true);
        sourceCode += "\n";
        sourceCode += tab + generatePort("most_voted", WIRE, NONE, 2, true);
        sourceCode += tab + generatePort("read_request", REGISTER, NONE, 2, true);
        sourceCode += tab + generatePort("write_request", REGISTER, NONE, 2, true);
        sourceCode += tab + generatePort("features", REGISTER, NONE, 128, true);

        return sourceCode;
    }

    private String generateModuleInstantiation(){

        String ind = generateIndentation(1);
        String ind2 = generateIndentation(2);

        String sourceCode = "";

        sourceCode += ind + "controller controller (\n";
//        sourceCode += ind2



        return sourceCode;
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }


}
