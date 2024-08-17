package project.src.java.userInterface;

import project.src.java.util.customExceptions.InvalidCommandException;
import project.src.java.util.messages.Error;

import java.security.InvalidParameterException;
import java.util.*;

public class Parameter {

	private String parameter = "";
	private HashMap<String, String> flags = null;
	private HashMap<String, String> flagType = null;

	public Parameter(){
	}

	public Parameter(List<String> flags, List<String> type){
		this.flags = new HashMap<>();
		this.flagType = new HashMap<>();

		for (int index = 0; index < flags.size(); index++) {
			this.flags.put(flags.get(index), "");
			this.flagType.put(flags.get(index), type.get(index));
		}
	}

	public void add(String parameter, String key, String value) throws InvalidCommandException {

		if (Objects.equals(this.parameter, "")){
			this.parameter = parameter;
		} else {
			/* this block is executed when the constructor without parameters is called */
			if (this.flags == null){
				this.flags = new HashMap<>();
				this.flags.put(key, value);
			}

			/* executed when the constructor with parameters are called*/
			else {
				if ((this.flagType.get(key).equals("numeric") && !isNumericInt(value)) || (this.flagType.get(key).equals("text") && isNumericInt(value))) {
					/* error throws when the flag have a specific type declared but this value have another type */
					throw new InvalidCommandException(Error.INVALID_FLAG_VALUE.replace("x", key));
				} else {
					if (this.flags.get(key).isEmpty()){
						this.flags.replace(key, value);
					} else {
						/* error throws when the same flag is used more tha once */
						throw new InvalidCommandException(Error.MULTIPLE_USE_FLAG.replace("x", key));
					}
				}
			}
		}
	}

	public String getParameter(){
		return this.parameter;
	}


	public HashMap<String, String> getValue(){
		return this.flags;
	}

	public void verify(){
		if (!this.flags.isEmpty()){
			Set<String> keys = this.flags.keySet();

			for (String key:keys){
				if (this.flags.get(key).isEmpty()){
					System.out.printf("O parametro %s é invalido\n", key);
				}
			}
		}
	}

	public boolean validityFlag(String flag){
		return this.flags.containsKey(flag);
	}

	private boolean isNumericInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
