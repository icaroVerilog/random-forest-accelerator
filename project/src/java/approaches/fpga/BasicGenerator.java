package project.src.java.approaches.fpga;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicGenerator {
    /* RESERVED WORDS */

    protected final String WIRE = "wire";
    protected final String REGISTER = "reg";
    protected final String INTEGER = "integer";

    protected final String OUTPUT = "output";
    protected final String INPUT = "input";
    protected final String NONE = "";

    /* TODO: REFATORAR, DEIXAR APENAS UMA STRING CONSTRUTORA PARA CADA BLOCO CONDICIONAL */
    /* TODO: REFATORAR, DEIXAR TODOS OS CAMPOS QUE LIDAM COM STRING UTILIZANDO String.format()*/

    protected final String MODULE_INSTANCE = "\nind" + "moduleName moduleName(\nports \nind);\n";
    protected final String ALWAYS_BLOCK = "\nind" + "always @(posedge clk) begin \nsrc \nindend";
    protected final String CONDITIONAL = "\nind" + "if (x) begin\nind2y \n indend";

    protected final String CONDITIONAL_ELSE = "\nind" + "else begin\ny indend";
    protected final String ALWAYS_BLOCK2 = "\nind" + "always @(border signal) begin \nsrc \nindend";
    protected final String CONDITIONAL2 = "ind" + "if (x) begin\ny indend";

    protected final String MODULE_VARIABLE_INSTANCE = "\nind" + "moduleName moduleVariableName(\nports \nind);\n";


    protected String generatePort(String name, String type, String direction, Integer bitwidth, Boolean lineBreak){
        String io = "";

        if (Objects.equals(direction, NONE)) {
            if (bitwidth == 1){
                io = String.format("%s %s", type, name);
            } else {
                int aux = bitwidth - 1;
                io = String.format("%s [%d:%d] %s", type, bitwidth - 1, 0, name);
            }
        } else {
            if (bitwidth == 1){
                io = String.format("%s %s %s",direction, type, name);
            } else {
                io = String.format("%s %s [%d:%d] %s", direction, type, bitwidth - 1, 0, name);
            }
        }
        io += ";";
        if (lineBreak) io += "\n";

        return io;
    }

    protected String generateMemory(String name, String type, String direction, Integer bitwidth, Integer size, Boolean linebreak){
        String bus = "";

        if (Objects.equals(direction, NONE)) {
            bus = String.format("%s [%d:0] %s [%d:0];", type, bitwidth-1, name, size-1);
        } else {
            bus = String.format("%s %s [%d:0] %s [%d:0];", type, direction, bitwidth-1, name, size-1);
        }
        if (linebreak) bus += "\n";

        return bus;
    }

    protected String tab(int tab){
        return IntStream.range(0, tab)
            .mapToObj(t -> "\t")
            .collect(Collectors.joining("")
        );
    }

    protected String toBinary(int value, int bitwidth){
        return String.format("%" + bitwidth + "s", Integer.toBinaryString(value)).replaceAll(" ", "0");
    }

    protected String generateEndDelimiters(){
        return "endmodule";
    }
}
