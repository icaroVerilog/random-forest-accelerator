package project.src.java.util.messages;

import project.src.java.userInterface.ValidParameters;

public class Messages {
	public static String HELP = String.format("""
		
		batch execution
			read_settings   [filename]  read all execution settings contained in json file
			run_settings 			    run all execution cases readed using read_settings command
		manual execution
			%s  [options]   train the model generating the estimators     
				-e						defines the quantity of estimators (trees) of the random forest
				-tp						defines the percentage of the dataset used in the training
				-d       				defines the maximum depth of the estimators (trees)
				
			%s   [options]   generate the inference design in verilog using conditional constructs
			%s  [options]   generate the inference design in verilog using conditional constructs with pipeline
			%s  [options]   generate the inference design in verilog using multiplexers
			%s   [options]   generate the inference design in verilog using boolean equations
				-bw						defines the bitwidth of the threshold value register
				
			%s   [options]   generate the inference design in verilog using validation tables
				-tbw					defines the threshold bitwidth i.e the bitwidth of the values who are compared
				-ibw					defines the node index bitwidth
				-cbw					defines the bitwidth of the column who represent one dataset column
				
	""",
			ValidParameters.START_TRAINING,
			ValidParameters.START_IF_INFERENCE,
			ValidParameters.START_IF_PIPELINED_INFERENCE,
			ValidParameters.START_MUX_INFERENCE,
			ValidParameters.START_EQUATION_INFERENCE,
			ValidParameters.START_TABLE_INFERENCE
	);
}
