package project.src.java.approaches.fpga.conditionalEquationMultiplexer;

import project.src.java.approaches.fpga.BasicGenerator;
import project.src.java.dotTreeParser.treeStructure.Comparison;

import java.util.ArrayList;
import java.util.List;

public class BaseTreeGenerator extends BasicGenerator {

    protected String generateHeader(String module_name, int featureQnt){

        String src = "";

        String[] basicIOPorts = {"voted_class"};

        ArrayList<String> ioPorts = new ArrayList<>(List.of(basicIOPorts));

        for (int index = 0; index < featureQnt; index++) {
            ioPorts.add(String.format("feature%d", index));
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

    protected String generateComparison(Comparison comparison, int comparedValueBitwidth){
        var threshold = comparison.getThreshold().toString().split("\\.");

        String src = "";

        src += "feature" + comparison.getColumn() + " " + comparison.getComparisonType() + " " + toBinary(Integer.parseInt(threshold[0]), comparedValueBitwidth);
        return src;
    }

}
