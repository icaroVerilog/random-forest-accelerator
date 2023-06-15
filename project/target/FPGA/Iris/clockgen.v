`include "controller.v"

module clockgen();

    integer counter;

    reg clock;
    reg [255:0] feature [0:149];
    reg [255:0] feature_aux;

    wire [1:0] voted;


    controller controller(
        .clock(clock),
		.ft3_fraction(feature_aux[31:0]),
		.ft3_exponent(feature_aux[63:32]),
		.ft2_fraction(feature_aux[95:64]),
		.ft2_exponent(feature_aux[127:96]),
		.ft1_fraction(feature_aux[159:128]),
		.ft1_exponent(feature_aux[191:160]),
		.ft0_fraction(feature_aux[223:192]),
		.ft0_exponent(feature_aux[255:224]),
        .voted(voted)
    );

    initial begin

        $dumpfile("wave.vcd");
        $dumpvars;

        counter = 32'b0;
        clock = 1'b0;


        $readmemb("dataset/data.bin", feature);
    
    end

    always @(posedge clock) begin

        if (counter == 150) begin
            $finish;
        end

        feature_aux <= feature[counter];

        counter = counter + 1;
    end

    always #2 clock = ~clock;
endmodule
