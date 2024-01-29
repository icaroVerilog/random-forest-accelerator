package project.src.java.util.executionSettings;

import com.fasterxml.jackson.databind.ObjectMapper;
import project.src.java.util.executionSettings.ExecutionSettingsData.ExecutionSettings;

import java.io.File;

public class ExecutionSettingsParser {

    private final ObjectMapper objectMapper;

    public ExecutionSettingsParser() {
        objectMapper = new ObjectMapper();
    }

    public ExecutionSettings execute(String basePath) {
        try {
            File JSON = new File(basePath + "/execution_parameters.json");
            return objectMapper.readValue(JSON, ExecutionSettings.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
