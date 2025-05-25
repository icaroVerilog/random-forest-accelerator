package project.src.java.core.parsers;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class XGBoostJsonTreeParser {
	private final ObjectMapper objectMapper;

	public XGBoostJsonTreeParser() {
		this.objectMapper = new ObjectMapper();
	}

	public void execute(String filePath) {
		JsonNode forest = readJsonFromFile(filePath);
		if (forest != null) {
			printNode(forest);
		} else {
			System.err.println("Falha ao ler o arquivo JSON.");
		}
	}

	private void printNode(JsonNode node) {
		if (node.isObject()) {
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (field.getKey().equals("nodeid")){
					System.out.println("node:" + field.getValue());
				}
				if (field.getKey().equals("leaf")) {
					System.out.println("============ NO FOLHA ===========");
					System.out.println("valor:" + field.getValue());
				} else if (field.getKey().equals("split")) {
					System.out.println("============ NO INTERNO ===========");
					System.out.println("coluna: " + field.getValue());
					while (fields.hasNext()) {
						field = fields.next();
						if (field.getKey().equals("split_condition")) {
							System.out.println("threshold " + field.getValue());
						}
						if (field.getKey().equals("children")) {
							printNode(field.getValue());
						}
					}
				}
			}
		} else if (node.isArray()) {
			for (JsonNode n : node) {
				printNode(n);
			}
		}
	}

	private JsonNode readJsonFromFile(String filePath) {
		try {
			return objectMapper.readTree(new File(filePath));
		} catch (JsonMappingException e) {
			System.err.println("Erro no mapeamento do JSON: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.err.println("Erro de I/O ao ler o arquivo: " + e.getMessage());
			return null;
		}
	}
}
