package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;
import project.src.java.util.executionSettings.ExecutionSettingsData.Table;

public class ControllerGenerator extends BasicGenerator {

    private final String MODULE_NAME = "controller";

    private int comparedValueBitwidth;
    private int comparedColumnBitwidth;
    private int tableIndexerBitwidth;
    private int voteCounterBitwidth;
    private String precision;

    public void execute(int classBitwidth, int featureQuantity, Table settings, boolean offlineMode){
        System.out.println("generating controller");

        this.comparedValueBitwidth  = settings.inferenceParameters.fieldsBitwidth.comparedValue;
        this.comparedColumnBitwidth = settings.inferenceParameters.fieldsBitwidth.comparedColumn;
        this.tableIndexerBitwidth   = settings.inferenceParameters.fieldsBitwidth.index;
        this.precision              = settings.precision;

        String src = "";
        src += generateHeader(MODULE_NAME, offlineMode);

        src += generatePortInstantiation(
            featureQuantity,
            classBitwidth,
            offlineMode
        );

        src += generateValidationTableInstantiation(
            featureQuantity,
            offlineMode
        );

        src += "endmodule";

        FileBuilder.execute(src, String.format("FPGA/%s_table_run/%s.v", settings.dataset, this.MODULE_NAME));
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
        int featureQuantity,
        int classBitwidth,
        boolean offlineMode
    ){
        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";

        int featuresBusBitwidth = 0;

        if (this.precision.equals("integer")){
            featuresBusBitwidth = this.comparedValueBitwidth  * featureQuantity;
        }
        if (this.precision.equals("decimal")){
            featuresBusBitwidth = (this.comparedValueBitwidth * 2) * featureQuantity;
        }

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
        int featureQuantity,
        boolean offlineMode
    ){

        int featureBusBitwidth = this.comparedValueBitwidth * featureQuantity;

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

        if (this.precision.equals("integer")){
            src += tab(2) + String.format(
                    ".feature(features[%d:%d])\n",
                    (featureBusBitwidth - 1),
                    0
            );
        }
        if (this.precision.equals("decimal")){
            src += tab(2) + String.format(
                    ".feature_integer(features[%d:%d]),\n",
                    (featureBusBitwidth - 1) + featureBusBitwidth,
                    featureBusBitwidth
            );
            src += tab(2) + String.format(
                    ".feature_decimal(features[%d:%d])\n",
                    featureBusBitwidth - 1,
                    0
            );
        }
        src += tab(1) + ");\n";

        return src;
    }
}
