package project.src.java.approaches.fpga.tableGenerator;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.util.FileBuilder;

public class ControllerGenerator extends BasicGenerator {
    public void execute(int classBitwidth, int featureQuantity, String datasetName, boolean offlineMode){
        System.out.println("generating controller");

        var configs = new Configurations();

        String src = "";
        src += generateHeader("controller", offlineMode);

        src += generatePortInstantiation(
            configs.INTEGER_PART_BITWIDTH,
            configs.DECIMAL_PART_BITWIDTH,
            featureQuantity,
            classBitwidth,
            offlineMode
        );

        src += generateValidationTableInstantiation(
            configs.INTEGER_PART_BITWIDTH,
            configs.DECIMAL_PART_BITWIDTH,
            featureQuantity,
            offlineMode
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
        int integerPartFeatureBitwidth,
        int decimalPartFeatureBitwidth,
        int featureQuantity,
        int classBitwidth,
        boolean offlineMode
    ){
        String src = "";

        src += tab(1) + generatePort("clock", WIRE, INPUT, 1, true);
        src += tab(1) + generatePort("reset", WIRE, INPUT, 1, true);
        src += "\n";
        int featuresBusBitwidth = (integerPartFeatureBitwidth + decimalPartFeatureBitwidth) * featureQuantity;

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
        int integerPartFeatureBitwidth,
        int decimalPartFeatureBitwidth,
        int featureQuantity,
        boolean offlineMode
    ){
        int featureIntegerBusBitwidth = integerPartFeatureBitwidth * featureQuantity;
        int featureDecimalBusBitwidth = decimalPartFeatureBitwidth * featureQuantity;

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

        src += tab(2) + String.format(
                ".%s(%s[%d:%d]),\n",
                "feature_integer",
                "features",
                (featureDecimalBusBitwidth - 1) + featureIntegerBusBitwidth,
                featureDecimalBusBitwidth
        );
        src += tab(2) + String.format(
                ".%s(%s[%d:%d])\n",
                "feature_decimal",
                "features",
                featureDecimalBusBitwidth - 1,
                0
        );
        src += tab(1) + ");\n";

        return src;
    }
}
