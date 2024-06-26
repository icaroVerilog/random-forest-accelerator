package project.src.java.jsonCumlTreeParser;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ParserCUML {

	private final ObjectMapper objectMapper;

	public ParserCUML() {
		this.objectMapper = new ObjectMapper();
	}

	public void execute(String json){
		JsonNode forest = readJson(json);

		printNode(forest);
	}

	private void printNode(JsonNode node){
		if (node.isObject()){
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()){
				Map.Entry<String, JsonNode> field = fields.next();

//				if (field.getKey().equals("nodeid")){
//					System.out.println(field.getValue());
//				}
				if (field.getKey().equals("leaf_value")){
					System.out.println("============ NO FOLHA ===========");
					System.out.println("valor:" + field.getValue());
				} else if (field.getKey().equals("split_feature")){
					System.out.println("============ NO INTERNO ===========");
					System.out.println("coluna: " + field.getValue());
					while(fields.hasNext()){
						field = fields.next();
						if (field.getKey().equals("split_threshold")){
							System.out.println("threshold " + field.getValue());
						}
						if (field.getKey().equals("children")){
							printNode(field.getValue());
						}
					}
				}
			}
		}
		else if (node.isArray()){
			for (JsonNode n: node){
				printNode(n);
			}
		} else {
			System.out.println("");
		}
	}

	private JsonNode readJson(String json){
		try {
			return objectMapper.readTree(json);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}

}
