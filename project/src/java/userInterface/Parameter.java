package project.src.java.userInterface;

import project.src.java.util.messages.Error;

import java.security.InvalidParameterException;
import java.util.*;

public class Parameter {

	private String parameter = "";
	private HashMap<String, String> values = new HashMap<>();
	private HashMap<String, String> flags;
	private HashMap<String, String> flagType;

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

	public void add(String parameter, String key, String value) throws InvalidParameterException {

		if (Objects.equals(this.parameter, "")){
			this.parameter = parameter;
		} else {
			if ((this.flagType.get(key).equals("numeric") && !isNumericInt(value)) || (this.flagType.get(key).equals("text") && isNumericInt(value))) {
				throw new InvalidParameterException(Error.INVALID_FLAG_VALUE.replace("x", key));
			} else {
				if (this.flags.get(key).isEmpty()){
					this.flags.replace(key, value);
				} else {
					throw new InvalidParameterException(String.format("a flag %s ja foi usada\n", key));
				}
			}
		}
	}

	public String getParameter(){
		return this.parameter;
	}


	public HashMap<String, String> getValue(){
		return this.values;
	}

	public void verify(){
		Set<String> keys = this.flags.keySet();

		for (String key:keys){
			if (this.flags.get(key).isEmpty()){
				System.out.printf("O parametro %s é invalido\n", key);
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

	public void print(){
		System.out.println(parameter);
		System.out.println(this.values.toString());
	}
}
