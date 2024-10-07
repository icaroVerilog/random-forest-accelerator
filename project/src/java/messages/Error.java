package project.src.java.messages;

public class Error {
	public static final String INVALID_FILE      = "file cannot be found";
	public static final String INVALID_FILENAME  = "you cannot use one flag as filename";
	public static final String INVALID_COMMAND   = "no such command: 'x' type 'help' to see all commands";
	public static final String INVALID_FLAG      = "unknown flag 'x' type 'help' to see all commands";
	public static final String INVALID_FLAG_VALUE = "invalid value of flag 'x' type 'help' to see all commands";
	public static final String MULTIPLE_USE_FLAG = "flag 'x' already used";
	public static final String NOT_LOADED_SETTINGS = "arquivos de configurações não carregados, use read_settings <arquivo.json> antes de executar";

	public static final String NOT_LOADED_DATASET = "please load the dataset before start training, type 'help' to see all commands";
	public static final String NOT_TRAINED_NOT_LOADED_DATASET = "please load the dataset and train the model, type 'help' to see all commands";
//	public static final String
}
