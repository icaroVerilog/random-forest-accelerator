package project.src.java.util.executionSettings.JSON;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ExecutionSettings;

import java.io.File;
import java.io.IOException;

public class ExecutionSettingsParser {

    private final ObjectMapper objectMapper;

    public ExecutionSettingsParser() {
        objectMapper = new ObjectMapper();
    }

    public ExecutionSettings execute(String basePath, String file) throws IOException {
        try {
            File JSON = new File(basePath + "/" + file);
            return objectMapper.readValue(JSON, ExecutionSettings.class);
        } catch (JsonMappingException error) {
            throw error;
        } catch (IOException error){
            throw error;
        }
    }
}
