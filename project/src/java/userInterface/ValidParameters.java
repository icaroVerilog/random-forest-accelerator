package project.src.java.userInterface;

public class ValidParameters {

	/* commands */

	public static final String READ_SETTINGS   				= "read_settings";
	public static final String RUN_SETTINGS    				= "run_settings";
	public static final String READ_DATASET					= "read_dataset";
	public static final String START_TRAINING  				= "start_training";

	public static final String START_IF_INFERENCE 			= "gen_if_design";
	public static final String START_MUX_INFERENCE 			= "gen_mux_design";
	public static final String START_EQUATION_INFERENCE 	= "gen_eq_design";
	public static final String START_TABLE_INFERENCE    	= "gen_tb_design";
	public static final String START_IF_PIPELINED_INFERENCE = "gen_ifp_design";

	public static final String HELP 		   				= "help";
	public static final String EXIT 		   				= "exit";
	public static final String CLEAR		   				= "clear";

	/* flags */

	/* training */

	public static final String ESTIMATORS = "-e";
	public static final String TRAINING_PERCENT = "-tp";
	public static final String MAX_DEPTH = "-d";

	/* inference */

	public static final String BITWIDTH = "-bw";

//	public static final String

	public static final String THRESHOLD_BITWIDTH = "-tb";

//	public static final String
}
