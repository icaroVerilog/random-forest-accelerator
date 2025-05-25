package project.src.java.core.randomForest;

import project.src.java.core.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.CLI.ConditionalEquationMux.SettingsCli;

public class AdderGenerator extends BasicGenerator {

    public void execute(int treeQnt, SettingsCli settings){
        System.out.println("generating adder");

        String src = "";

        src += generateHeader(treeQnt);
        src += generateIO(treeQnt);
        src += generateAssign(treeQnt);
        src += "endmodule";

        FileBuilder.execute(
            src, String.format(
                "output/%s_%s_%dtree_%sdeep_run/adder.v",
                settings.dataset,
                settings.approach,
                settings.trainingParameters.estimatorsQuantity,
                settings.trainingParameters.maxDepth
            ),
            false
        );
    }

    private String generateHeader(int treeQnt){

        String src = "";

        src += "module adder (\n";
        for (int index = 0; index < treeQnt; index++) {
            src += String.format("vote%d,\n", index);
        }
        src += tab(1) + "sum\n";
        src += ");\n";

        return src;
    }

    private String generateIO(int treeQnt){
        String src = "";

        for (int index = 0; index < treeQnt; index++){
            src += tab(1) + generatePort(String.format("vote%d", index), WIRE, INPUT, 1, true);
        }
        src += "\n";

        int bitwidth = (int) Math.ceil(Math.sqrt(treeQnt));

        src += tab(1) + generatePort(String.format("sum"), WIRE, OUTPUT, bitwidth, true);
        src += "\n";

        return src;
    }

    private String generateAssign(int treeQnt){
        String src = "";

        src += tab(1) + "assign sum = ";

        for (int index = 0; index < treeQnt; index++) {
            if (index == treeQnt - 1){
                src += String.format("vote%d;", index);
            } else {
                src += String.format("vote%d + ", index);
            }
        }
        src += "\n";
        return src;
    }
}
