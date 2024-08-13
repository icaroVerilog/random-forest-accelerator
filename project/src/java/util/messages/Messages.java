package project.src.java.util.messages;

public class Messages {
	public static String HELP = """
		
		batch execution
			read_settings   [filename]  read all execution settings contained in json file
			run_settings 			    run all execution cases readed using read_settings command
		manual execution
			start_training  [options]   train the model generating the estimators     
				-e						define the quantity of estimators (trees) of the random forest
				-tp						define the percentage of the dataset used in the training
				-d       				define the maximum depth of the estimators (trees)
			start_inference [options]   generate the inference design in verilog
	""";
}
