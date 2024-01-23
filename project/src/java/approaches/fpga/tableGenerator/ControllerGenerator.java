package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.executionSettingsData.ExecutionSettings;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    public void execute(int classBitwidth, int featureQuantity, String datasetName, ExecutionSettings settings, boolean offlineMode){
        System.out.println("generating controller");

        String src = "";
        src += generateHeader(MODULE_NAME, offlineMode);

        src += generatePortInstantiation(
            settings.inferenceParameters.fieldsBitwidth.comparedValue,
            featureQuantity,
            classBitwidth,
            offlineMode,
            settings.generalParameters.precision
        );

        src += generateValidationTableInstantiation(
            settings.inferenceParameters.fieldsBitwidth.comparedValue,
            featureQuantity,
            offlineMode,
            settings.generalParameters.precision
        );

        src += "endmodule";

        FileBuilder.execute(src, String.format("FPGA/table/%s/controller.v", datasetName));
    }

    private String generateHeader(String module_name, boolean offlineMode){
        String[] ioPorts = {
            "clock",
            "reset",
            "features",
            "voted",
            "read_new_sample",
            "new_table_entry",
            "new_table_entry_counter",
            "compute_vote_flag"
        };
        if (offlineMode) {
            ioPorts = new String[]{
                "clock",
                "reset",
                "features",
                "voted",
                "read_new_sample",
                "compute_vote_flag"
            };
        }

        String header = String.format("module %s (\n", module_name);
        String ports = "";

        for (int index = 0; index <= ioPorts.length; index++){
            if (index == ioPorts.length){
                ports += ");\n\n";
            }
            else if (index == ioPorts.length - 1){
                ports += tab(1) + ioPorts[index] + "\n";
            }
            else {
                ports += tab(1) + ioPorts[index] + ",\n";
            }
        }
        return header + ports;
    }

    private String generatePortInstantiation(
        int featureBitwidth,
        int featureQuantity,
        int classBitwidth,
        boolean offlineMode,
        String precision
    ){
        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        int featuresBusBitwidth = (featureBitwidth * 2) * featureQuantity;

        src += tab(1) + generatePort("features", WIRE, INPUT, featuresBusBitwidth, true);

        if (!offlineMode){
            src += tab(1) + generatePort("new_table_entry", WIRE, INPUT, 64, true);
            src += tab(1) + generatePort("new_table_entry_counter", WIRE, INPUT, 16, true);
        }
        src += "\n";
        src += tab(1) + generatePort("voted", WIRE, OUTPUT, classBitwidth, true);
        src += tab(1) + generatePort("compute_vote_flag", WIRE, OUTPUT, 1, true);
        src += tab(1) + generatePort("read_new_sample", WIRE, OUTPUT, 1, true);
        src += "\n";

        return src;
    }

    private String generateValidationTableInstantiation(
        int featureBitwidth,
        int featureQuantity,
        boolean offlineMode,
        String precision
    ){

        /*TODO: refatorar os nomes abaixo*/

        int featureIntegerBusBitwidth = featureBitwidth * featureQuantity;
        int featureDecimalBusBitwidth = featureBitwidth * featureQuantity;

        String src = "";

        src += tab(1) + "validation_table validation_table (\n";
        src += tab(2) + String.format(".%s(%s),\n", "clock", "clock");
        src += tab(2) + String.format(".%s(%s),\n", "reset", "reset");
        src += tab(2) + String.format(".%s(%s),\n", "forest_vote", "voted");

        if (!offlineMode){
            src += tab(2) + String.format(".%s(%s),\n", "read_new_sample", "read_new_sample");
            src += tab(2) + String.format(".%s(%s),\n", "new_table_entry", "new_table_entry");
        }
        src += tab(2) + String.format(".%s(%s),\n", "compute_vote_flag", "compute_vote_flag");
        src += tab(2) + String.format(".%s(%s),\n", "read_new_sample", "read_new_sample");

        if (precision.equals("integer")){
            src += tab(2) + String.format(
                    ".feature(features[%d:%d])\n",
                    (featureDecimalBusBitwidth - 1) + featureIntegerBusBitwidth,
                    0
            );
        }
        if (precision.equals("decimal")){
            src += tab(2) + String.format(
                    ".feature_integer(features[%d:%d]),\n",
                    (featureDecimalBusBitwidth - 1) + featureIntegerBusBitwidth,
                    featureDecimalBusBitwidth
            );
            src += tab(2) + String.format(
                    ".feature_decimal(features[%d:%d]),\n",
                    featureDecimalBusBitwidth - 1,
                    0
            );
        }
        src += tab(1) + ");\n";

        return src;
    }
}
