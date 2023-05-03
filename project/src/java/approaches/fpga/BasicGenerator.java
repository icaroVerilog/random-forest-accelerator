package project.src.java.approaches.fpga;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicGenerator {

    protected final Integer FEATURE_BITWIDTH = 32;

    /* RESERVED WORDS */

    protected final String WIRE = "wire";
    protected final String REGISTER = "reg";
    protected final String INTEGER = "integer";

    protected final String OUTPUT = "output";
    protected final String INPUT = "input";
    protected final String NONE = "";

    /* */

    protected final String MODULE_INSTANCE = "\nind" + "moduleName moduleName(\nports \nind);\n";
    protected final String ALWAYS_BLOCK = "\nind" + "always (posedge clk) begin \nsrc \nindend";
    protected final String CONDITIONAL = "\nind" + "if (x) begin\n ind2 y \n indend";

    protected String generatePort(String name, String type, String direction, Integer bitwidth, Boolean lineBreak){

        String io = "";

        if (Objects.equals(direction, NONE)) {
            if (bitwidth == 1){
                io = type + " " + name;
            } else {
                int aux = bitwidth - 1;
                io = direction + type + " [" + aux + ":" + 0 + "] " + name;
            }
        } else {
            if (bitwidth == 1){
                io = direction + " " + type + " " + name;
            } else {
                int aux = bitwidth - 1;
                io = direction + " " + type + " [" + aux + ":" + 0 + "] " + name;
            }
        }

        io += ";";

        if (lineBreak){
            io += "\n";
        }

        return io;
    }

    protected String generateModule(String moduleName, ArrayList<String> ports){
        String sourceCode = "";

        sourceCode += "module " + moduleName + "(\n";
        sourceCode += IntStream.range(0, ports.size())
                .mapToObj(index -> generateIndentation(1) + ports.get(index))
                .collect(Collectors.joining(",\n"));
        sourceCode += "\n);";

        return sourceCode;
    }

    protected String generateIndentation(int tab){
        return IntStream.range(0, tab)
                .mapToObj(t -> "\t")
                .collect(Collectors.joining("")
        );
    }

    protected String generateBinaryNumber(int value, int bitwidth){
        return String.format("%" + bitwidth + "s", Integer.toBinaryString(value)).replaceAll(" ", "0");
    }
}
