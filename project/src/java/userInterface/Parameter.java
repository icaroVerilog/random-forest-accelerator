package project.src.java.userInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Parameter {

	private String parameter = "";
	private HashMap<String, String> values = new HashMap<>();

	public void add(String parameter, String key, String value){
		if (Objects.equals(this.parameter, "")){
			this.parameter = parameter;
		} else {
			this.values.put(key, value);
		}
	}

	public String getParameter(){
		return this.parameter;
	}

	public HashMap<String, String> getValue(){
		return this.values;
	}

	public void print(){
		System.out.println(parameter);
		System.out.println(this.values.toString());
	}
}
