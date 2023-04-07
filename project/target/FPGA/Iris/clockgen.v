`include "controller.v"

module clockgen();

    reg clock;

    controller controller(.clock(clock));

    initial begin

        $dumpfile("wave.vcd");
        $dumpvars;

        clock = 1'b0;
    end

    always #2 clock = ~clock;
endmodule