package project.src.java.approaches.fpga;

import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationsMux.Settings;

import java.util.ArrayList;
import java.util.List;

public class AdderGenerator extends BasicGenerator {

    private final String MODULE_NAME = "adder";

    public void execute(int treeQnt, Settings settings){
        System.out.println("generating adder");

        String src = "";

        src += generateHeader(this.MODULE_NAME, treeQnt);
        src += generateIO(treeQnt);
        src += generateAssign(treeQnt);
        src += "endmodule";

        FileBuilder.execute(src, String.format("FPGA/%s_%s_run/adder.v", settings.dataset, settings.approach));
    }

    private String generateHeader(String module_name, int treeQnt){

        String src = "";

        String[] basicIOPorts = {"sum"};

        ArrayList<String> ioPorts = new ArrayList<>(List.of(basicIOPorts));

        for (int index = 0; index < treeQnt; index++) {
            ioPorts.add(String.format("vote%d", index));
        }

        src += String.format("module %s (\n", module_name);

        for (int index = 0; index <= ioPorts.size(); index++){
            if (index == ioPorts.size()){
                src += ");\n\n";
            }
            else if (index == ioPorts.size() - 1){
                src += tab(1) + ioPorts.get(index) + "\n";
            }
            else {
                src += tab(1) + ioPorts.get(index) + ",\n";
            }
        }
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
