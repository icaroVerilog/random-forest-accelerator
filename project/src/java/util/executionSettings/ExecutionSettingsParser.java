package project.src.java.util.executionSettings;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;

import java.io.File;
import java.io.IOException;

public class ExecutionSettingsParser {

    private final ObjectMapper objectMapper;

    public ExecutionSettingsParser() {
        objectMapper = new ObjectMapper();
    }

    public ExecutionSettings execute(String basePath) {
        try {
            File JSON = new File(basePath + "/execution_parameters.json");
            return objectMapper.readValue(JSON, ExecutionSettings.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
