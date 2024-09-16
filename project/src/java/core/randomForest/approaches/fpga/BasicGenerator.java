package project.src.java.core.randomForest.approaches.fpga;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicGenerator {
    /* RESERVED WORDS */

    protected static final int HALF_PRECISION = 16;
    protected static final int NORMAL_PRECISION = 32;
    protected static final int DOUBLE_PRECISION = 64;

    protected final String WIRE = "wire";
    protected final String REGISTER = "reg";
    protected final String INTEGER = "integer";

    protected final String OUTPUT = "output";
    protected final String INPUT = "input";
    protected final String NONE = "";

    /* TODO: REFATORAR, DEIXAR TODOS OS CAMPOS QUE LIDAM COM STRING UTILIZANDO String.format()*/

    protected final String MODULE_INSTANCE = "\nind" + "moduleName moduleName(\nports \nind);\n";

    protected final String CONDITIONAL_ELSE_BLOCK = "\nind" + "else begin\ny indend";
    protected final String ALWAYS_BLOCK = "\nind" + "always @(border signal) begin \nsrc \nindend\n";
    protected final String CONDITIONAL_BLOCK = "ind" + "if (x) begin\n` indend";

    protected final String MODULE_VARIABLE_INSTANCE = "\nind" + "moduleName moduleVariableName(\nports \nind);\n";


    protected String generatePort(String name, String type, String direction, Integer bitwidth, Boolean lineBreak){
        String io = "";

        if (Objects.equals(direction, NONE)) {
            if (bitwidth == 1){
                io = String.format("%s %s", type, name);
            } else {
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

    protected String toBin(int value, int bitwidth){
        return String.format("%" + bitwidth + "s", Integer.toBinaryString(value)).replaceAll(" ", "0");
    }

    protected String toIEEE754(float value, int precision){
        if (precision == 32){
            int intBits = Float.floatToIntBits(value);
            return String.format("%32s", Integer.toBinaryString(intBits)).replace(' ', '0');
        }
        else if (precision == 16){
            // Obtenha os bits em formato de 32 bits IEEE 754
            int intBits = Float.floatToIntBits(value);

            // Extraia o sinal, expoente e mantissa dos 32 bits
            int sign = (intBits >> 31) & 0x1;
            int exponent = ((intBits >> 23) & 0xFF) - 127 + 15; // Reajustar o bias (127 para 15)
            int mantissa = (intBits >> 13) & 0x3FF; // Pegar os 10 bits mais significativos da mantissa

            // Verifique se o expoente está fora do intervalo da half-precision
            if (exponent <= 0) {
                // Número subnormal ou zero
                if (exponent < -10) {
                    exponent = 0;
                    mantissa = 0; // Aproximar para zero
                } else {
                    mantissa = (mantissa | 0x400) >> (1 - exponent); // Ajustar mantissa
                    exponent = 0;
                }
            } else if (exponent >= 31) {
                // Exponent overflow (infinito ou NaN)
                exponent = 31;
                mantissa = 0;
            }

            // Combine os componentes para formar a representação de 16 bits
            int halfPrecisionBits = (sign << 15) | (exponent << 10) | mantissa;

            // Construa a string binária de 16 bits
            StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(halfPrecisionBits));

            // Preencha com zeros à esquerda se a string for menor que 16 bits
            while (binaryString.length() < 16) {
                binaryString.insert(0, "0");
            }

            // Retorne a string binária de 16 bits
            return binaryString.toString();
        }
        else {
            return "";
        }
    }

    protected String generateEndDelimiters(){
        return "endmodule";
    }
}
