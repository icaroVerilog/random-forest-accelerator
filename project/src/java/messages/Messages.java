package project.src.java.messages;

import project.src.java.userInterface.ValidParameters;

public class Messages {
	public static String HELP = String.format("""
		
		batch execution (work in progress)
			read_settings   [filename]  read all execution settings contained in json file
			run_settings 			    run all execution cases readed using read_settings command
		manual execution
			%s    [filename]  read the dataset  	
			%s  [options]   train the model generating the estimators     
				-e						defines the quantity of estimators (trees) of the random forest
				-tp						defines the percentage of the dataset used in the training
				-d       				defines the maximum depth of the estimators (trees)
				-m0 [optional]			defines the max value to normalize the dataset
				-m1 [optional]			defines the min value to normalize the dataset
				
			%s   [options]   generate the inference design in verilog using conditional constructs
			%s  [options]   generate the inference design in verilog using conditional constructs with pipeline
			%s  [options]   generate the inference design in verilog using multiplexers
			%s   [options]   generate the inference design in verilog using boolean equations
			%s  [options]   generate the inference design in verilog using multiple tables
				-p			[value] 	defines the precision of the numerical representation
				 \u21B3 int4: 4 bits unsigned integer (experimental)
				 \u21B3 e4m3:   NVIDIA 8 bits floating point https://docs.nvidia.com/deeplearning/transformer-engine/user-guide/examples/fp8_primer.html
				 \u21B3 half:   IEE754 16 bits floating point
				 \u21B3 normal: IEE754 32 bits floating point
				 \u21B3 double: IEE754 64 bits floating point
				
			%s   [options]   generate the inference design in verilog using validation tables (work in progress)
				-tbw	[value] 	defines the precision of the numerical representation
				  \u21B3 int4: 4 bits unsigned integer (experimental)
				  \u21B3 e4m3:   NVIDIA 8 bits floating point https://docs.nvidia.com/deeplearning/transformer-engine/user-guide/examples/fp8_primer.html
				  \u21B3 half:   IEE754 16 bits floating point
				  \u21B3 normal: IEE754 32 bits floating point
				  \u21B3 double: IEE754 64 bits floating point
				  
				-ibw					defines the node index bitwidth
				-cbw					defines the bitwidth of the column who represent one dataset column.
				
	""",
			ValidParameters.READ_DATASET,
			ValidParameters.START_TRAINING,
			ValidParameters.START_IF_INFERENCE,
			ValidParameters.START_IF_PIPELINED_INFERENCE,
			ValidParameters.START_MUX_INFERENCE,
			ValidParameters.START_EQUATION_INFERENCE,
			ValidParameters.START_PARALLEL_TABLE_INFERENCE,
			ValidParameters.START_TABLE_INFERENCE
	);
}
