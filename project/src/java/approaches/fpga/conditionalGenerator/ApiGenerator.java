package project.src.java.approaches.fpga.conditionalGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ApiGenerator extends BasicGenerator {

    public void execute(Integer featureQnt, Integer classQnt, Boolean debugMode, String dataset){

        String sourceCode = "";

        System.out.println("generating API");

        sourceCode += generateImport("controller");
        sourceCode += generateIO(featureQnt);
        sourceCode += generateModuleInstantiation(featureQnt, "controller");
        sourceCode += generateAlwaysBlock(featureQnt, classQnt);

        FileBuilder.execute(sourceCode, "FPGA/" + dataset + "/api.v");
    }

    private String generateImport(String moduleName){
        return ("`include \"" + moduleName + ".v\"\n\n");
    }

    private String generateIO(Integer featureQnt){
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
        sourceCode += tab + generatePort("data_read_done",  WIRE, INPUT, featureQnt, true);
        sourceCode += tab + generatePort("data_write_done", WIRE, INPUT, featureQnt, true);
        sourceCode += tab + generatePort("available_write", WIRE, INPUT, featureQnt, true);
        sourceCode += tab + generatePort("data_read_valid", WIRE, INPUT, featureQnt, true);
        sourceCode += tab + generatePort("data_read_request",  REGISTER, OUTPUT, featureQnt, true);
        sourceCode += tab + generatePort("data_write_request", REGISTER, OUTPUT, featureQnt, true);
        sourceCode += "\n";
        sourceCode += tab + generatePort("data_read", WIRE, INPUT, featureQnt * FEATURE_BITWIDTH, true);
        sourceCode += tab + generatePort("data_write", REGISTER, OUTPUT, featureQnt * FEATURE_BITWIDTH, true);
        sourceCode += "\n";
        sourceCode += tab + generatePort("voted", WIRE, NONE, 2, true);
        sourceCode += tab + generatePort("read_request", REGISTER, NONE, 2, true);
        sourceCode += tab + generatePort("write_request", REGISTER, NONE, 2, true);
        sourceCode += tab + generatePort("features", REGISTER, NONE, featureQnt * FEATURE_BITWIDTH, true);

        return sourceCode;
    }

    private String generateModuleInstantiation(Integer featureQnt, String moduleName){

        String ind = generateIndentation(1);
        String ind2 = generateIndentation(2);

        String moduleFeatureExponent = ".ftZ_exponent(features[Y:X]),";
        String moduleFeatureFraction = ".ftZ_fraction(features[Y:X]),";

        String sourceCode = "";
        String processed = "";
        String module = MODULE_INSTANCE;

        int counter = 0;
        int numberIndexCounter = featureQnt - 1;

        sourceCode += ind2 + ".clock(clock),\n";
        sourceCode += ind2 + ".voted(voted),";

        for (int index = 0; index < featureQnt * 2; index++){
            if (index % 2 == 0){
                if (index == 0){
                    processed = moduleFeatureFraction
                            .replace("Z", Integer.toString(numberIndexCounter))
                            .replace("Y", Integer.toString(FEATURE_BITWIDTH - 1))
                            .replace("X", Integer.toString(0));
                }
                else {
                    processed = moduleFeatureFraction
                            .replace("Z", Integer.toString(numberIndexCounter))
                            .replace("Y", Integer.toString((FEATURE_BITWIDTH * index + FEATURE_BITWIDTH) - 1))
                            .replace("X", Integer.toString(FEATURE_BITWIDTH * index));
                }
            }
            else {
                processed = moduleFeatureExponent
                        .replace("Z", Integer.toString(numberIndexCounter))
                        .replace("Y", Integer.toString((FEATURE_BITWIDTH * counter + FEATURE_BITWIDTH) - 1))
                        .replace("X", Integer.toString(FEATURE_BITWIDTH * counter));

                if (index == (featureQnt * 2) - 1){
                    int commaPosition = processed.lastIndexOf(",");
                    processed = processed.substring(0, commaPosition);
                }
            }

            sourceCode += "\n";
            sourceCode += ind2 + processed;
            counter++;

            if (index % 2 != 0){
                numberIndexCounter--;
            }
        }

        module = module
                .replace("moduleName", moduleName)
                .replace("ports", sourceCode)
                .replace("ind", ind);

        return module;
    }

    public String generateAlwaysBlock(Integer featureQnt, Integer classQnt){

        int bitwidth = (int) Math.ceil(Math.sqrt(classQnt));
        System.out.println(bitwidth);

        String controlValue = "";
        for (int index = 0; index < featureQnt; index++){
            controlValue += "x";
        }

        System.out.println(controlValue);

        String ind2 = generateTab(2);
        String ind3 = generateTab(3);
        String ind4 = generateTab(4);


        String always = ALWAYS_BLOCK;

        String resetBlock            = CONDITIONAL;
        String startBlock            = CONDITIONAL;
        String requestReadBlock      = CONDITIONAL;
        String dataTransmissionBlock = CONDITIONAL;
        String doneBlock             = CONDITIONAL;


        String requestReadBody = "";
        requestReadBody += ind4 + "data_read_request <= " + bitwidth + "'bxx;".replace("x", "1");

        String requestReadBlockExp = "";
        requestReadBlockExp += "data_read_valid == " + featureQnt + "'b" + controlValue.replace("x", "1") + " && ";
        requestReadBlockExp += "available_write == " + featureQnt + "'b" + controlValue.replace("x", "1");

        requestReadBlock = requestReadBlock
                .replace("x", requestReadBlockExp)
                .replace("y", requestReadBody)
                .replace("ind2", "")
                .replace("ind", ind3);


        String dataTransmissionBody = "";
        dataTransmissionBody += ind4 + "features <= data_read;\n";
        dataTransmissionBody += ind4 + "data_write <= voted;\n";

        /*
            Devido ao fato que ao especificar as entradas e as saidas no algoritmo gerador, não é possivel
            definir tamanhos diferentes entre a entrada e a saida, portanto utilizamos so o primeiro endereço da saida
        */
        dataTransmissionBody += ind4 + "write_request[0] <= 1'b1;";

        String dataTransmissionBlockExp = "data_read_request == " + featureQnt + "'b" + controlValue.replace("x", "1");
        dataTransmissionBlock = dataTransmissionBlock
                .replace("x", dataTransmissionBlockExp)
                .replace("y", dataTransmissionBody)
                .replace("ind2", "")
                .replace("ind", ind3);

        String doneBody = "";
        doneBody += ind4 + "done <= 1'b1;";
        String doneBlockExp = "data_write_done[0] == 1'b1";

        doneBlock = doneBlock
                .replace("x", doneBlockExp)
                .replace("y", doneBody)
                .replace("ind2", "")
                .replace("ind", ind3);

        String resetBody = "";
        resetBody += ind3 + "data_read_request  <= " + featureQnt + "'b" + controlValue.replace("x", "0") + ";\n";
        resetBody += ind3 + "data_write_request <= " + featureQnt + "'b" + controlValue.replace("x", "0") + ";\n";
        resetBody += ind3 + "done               <= 1'b0;\n";
        resetBody += ind3 + "data_write         <= " + (FEATURE_BITWIDTH * featureQnt) + "'b0;";

        resetBlock = resetBlock
                .replace("x", "reset")
                .replace("y", resetBody)
                .replace("ind2", "")
                .replace("ind", ind2);


        String startBody = "";
        startBody += ind3 + "data_read_request  <= " + featureQnt + "'b" + controlValue.replace("x", "0") + ";\n";
        startBody += ind3 + "data_write_request <= " + featureQnt + "'b" + controlValue.replace("x", "0") + ";\n";

        startBlock = startBlock
                .replace("x", "start")
                .replace("y", startBody + requestReadBlock + dataTransmissionBlock + doneBlock)
                .replace("ind2", "")
                .replace("ind", ind2);

        always = always.replace("clk", "clock");
        always = always.replace("ind", generateIndentation(1));
        always = always.replace("src", resetBlock + startBlock);
        always += "\nendmodule"; /* provisorio */

        return always;
    }

    public String generateTab(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }
}
