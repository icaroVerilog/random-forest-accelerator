package project.src.java.userInterface;

import project.src.java.util.customExceptions.InvalidCommandException;
import project.src.java.core.randomForest.messages.Error;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class UserInterface {

	private final Scanner scanner = new Scanner(System.in);

	private final String MESSAGE = """
			\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557
			\u2551 ROAR - Random fOrest Accelerator generatoR                     \u2551
			\u2551 repository: github.com/icaroVerilog/random-forest-accelerator  \u2551
			\u2551																 \u2551
			\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d
			""";

	public UserInterface(){
		this.renderHeader();
	}

	public Parameter execute() throws IOException {
		try {
			System.out.print("ROAR> ");
			String commandLine = this.scanner.nextLine();

			if (!commandLine.equals("")){
				Parameter parameter = this.parseCommands(commandLine);

				if (parameter == null){
					return null;
				} else {
					if (parameter.getParameter().equals(ValidParameters.CLEAR)){
						System.out.println("lipar");
						renderHeader();
						return null;
					}
					return parameter;
				}
			}
			return null;
		} catch (InvalidCommandException error) {
			System.out.println(error.getMessage());
			return null;
		}
	}

	private Parameter parseCommands(String commandLine) throws InvalidCommandException {
		String[] commands = commandLine.split(" ");

		Boolean nextIsFile = false;
		Parameter parameter = null;
		String currentParameter = "";

		for (int index = 0; index < commands.length; index++) {
			if (index == 0){
				switch (commands[index]){
					case ValidParameters.READ_SETTINGS:
						nextIsFile = true;
						parameter = new Parameter();
						parameter.add(ValidParameters.READ_SETTINGS, "", "");
						break;
					case ValidParameters.RUN_SETTINGS:
						parameter = new Parameter();
						parameter.add(ValidParameters.RUN_SETTINGS, "", "");
						return parameter;
					case ValidParameters.READ_DATASET:
						nextIsFile = true;
						parameter = new Parameter();
						parameter.add(ValidParameters.READ_DATASET, "", "");
						break;
					case ValidParameters.BINARIZE_DATASET:
						nextIsFile = true;
						parameter = new Parameter(
							Arrays.asList("-bw"),
							Arrays.asList("numeric")
						);
						parameter.add(ValidParameters.BINARIZE_DATASET, "", "");
						break;
					case ValidParameters.START_TRAINING:
						parameter = new Parameter(
							Arrays.asList("-e", "-tp", "-d"),
							Arrays.asList("numeric", "numeric", "numeric")
						);
						parameter.add(ValidParameters.START_TRAINING, "", "");
						break;
					case ValidParameters.START_IF_INFERENCE:
						parameter = new Parameter(
							Arrays.asList("-p"),
							Arrays.asList("text")
						);
						parameter.add(ValidParameters.START_IF_INFERENCE, "", "");
						break;
					case ValidParameters.START_IF_PIPELINED_INFERENCE:
						parameter = new Parameter(
							Arrays.asList("-p"),
							Arrays.asList("text")
						);
						parameter.add(ValidParameters.START_IF_PIPELINED_INFERENCE, "", "");
						break;
					case ValidParameters.START_MUX_INFERENCE:
						parameter = new Parameter(
							Arrays.asList("-p"),
							Arrays.asList("text")
						);
						parameter.add(ValidParameters.START_MUX_INFERENCE, "", "");
						break;
					case ValidParameters.START_EQUATION_INFERENCE:
						parameter = new Parameter(
							Arrays.asList("-p"),
							Arrays.asList("text")
						);
						parameter.add(ValidParameters.START_EQUATION_INFERENCE, "", "");
						break;
					case ValidParameters.START_TABLE_INFERENCE:
						parameter = new Parameter(
							Arrays.asList("-tbw", "-cbw", "-ibw"),
							Arrays.asList("numeric", "numeric", "numeric")
						);
						parameter.add(ValidParameters.START_TABLE_INFERENCE, "", "");
						break;
					case ValidParameters.EXIT:
						parameter = new Parameter();
						parameter.add(ValidParameters.EXIT, "", "");
						return parameter;
					case ValidParameters.HELP:
						parameter = new Parameter();
						parameter.add(ValidParameters.HELP, "", "");
						return parameter;
					default:
						String commandError = Error.INVALID_COMMAND.replace("x", commands[index]);
						throw new InvalidCommandException(commandError);
				}
			} else {
				if (nextIsFile){
					try {
						if (parameter.validityFile(commands[index])){
							parameter.add(currentParameter, "filename", commands[index]);
							nextIsFile = false;
						} else {
							throw new InvalidCommandException(Error.INVALID_FILENAME);
						}
					} catch (InvalidCommandException exeption) {
						throw new InvalidCommandException(exeption.getMessage());
					}

				} else {
					try {
						if (!parameter.validityFlag(commands[index])) {
							throw new InvalidCommandException(Error.INVALID_FLAG.replace("x", commands[index]));
						}

						parameter.add(parameter.getParameter(), commands[index], commands[index + 1]);
						index++;
					} catch (IndexOutOfBoundsException error) {
						throw new InvalidCommandException(Error.INVALID_FLAG_VALUE.replace("x", commands[index]));
					}
				}
			}
		}
		if (!parameter.verify()){
			return null;
		}
		return parameter;
	}

	private void renderHeader(){
		System.out.print(this.MESSAGE);
		System.out.print("type help to see all the commands\n");
	}

	private void readDot(String filename){

	}
}
