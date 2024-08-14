package project.src.java.userInterface;

import project.src.java.util.customExceptions.InvalidCommandException;
import project.src.java.util.messages.Error;

import java.io.IOException;
import java.security.InvalidParameterException;
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

				if (parameter.getParameter().equals(ValidParameters.CLEAR)){
					System.out.println("lipar");
					renderHeader();
					return null;
				}

				return parameter;
			}
			return null;
		} catch (InvalidCommandException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	private Parameter parseCommands(String commandLine) throws InvalidCommandException {
		String[] commands = commandLine.split(" ");

		Boolean nextIsFile = false;
		Parameter parameter = null;
		String currentParameter = "";

		try {
			for (int index = 0; index < commands.length; index++) {
				if (index == 0){
					switch (commands[index]){
						case ValidParameters.READ_SETTINGS:
							nextIsFile = true;
							parameter = new Parameter();
							parameter.add(ValidParameters.READ_SETTINGS, "", "");
							break;
						case ValidParameters.RUN_SETTINGS:
							parameter.add(ValidParameters.RUN_SETTINGS, "", "");
							return parameter;
						case ValidParameters.START_TRAINING:
							parameter = new Parameter(
								Arrays.asList("-e", "-tp", "-d"),
								Arrays.asList("numeric", "numeric", "numeric")
							);
							parameter.add(ValidParameters.START_TRAINING, "", "");
							break;
						case ValidParameters.START_IF_INFERENCE:
							parameter.add(ValidParameters.START_IF_INFERENCE, "", "");
							break;
						case ValidParameters.START_MUX_INFERENCE:
							parameter.add(ValidParameters.START_MUX_INFERENCE, "", "");
							break;
						case ValidParameters.START_EQUATION_INFERENCE:
							parameter.add(ValidParameters.START_EQUATION_INFERENCE, "", "");
							break;
						case ValidParameters.START_TABLE_INFERENCE:
							parameter.add(ValidParameters.START_TABLE_INFERENCE, "", "");
							break;
						case ValidParameters.EXIT:
							parameter.add(ValidParameters.EXIT, "", "");
							return parameter;
						case ValidParameters.HELP:
							parameter = new Parameter();
							parameter.add(ValidParameters.HELP, "", "");
							return parameter;
						default:
							parameter.add(commands[index], "", "");
					}
				} else {
					if (nextIsFile){
						parameter.add(currentParameter, "filename", commands[index]);
						return parameter;
					} else {
						if (parameter.getParameter().equals(ValidParameters.START_TRAINING)){
							try {
								if (!parameter.validityFlag(commands[index])) {
									throw new InvalidCommandException(Error.INVALID_FLAG.replace("x", commands[index]));
								}

								parameter.add(parameter.getParameter(), commands[index], commands[index + 1]);
								index++;
							} catch (IndexOutOfBoundsException error) {
								throw new InvalidCommandException(String.format("O parametro %s é invalido\n", commands[index]));
							}
						}
					}
				}
			}
			parameter.verify();
			return parameter;
		} catch (InvalidParameterException error) {
			throw new InvalidCommandException(error.getMessage());
		}
	}

	private void renderHeader(){
		System.out.print(this.MESSAGE);
		System.out.print("type help to see all the commands\n");
	}

	private void readDot(String filename){

	}
}
