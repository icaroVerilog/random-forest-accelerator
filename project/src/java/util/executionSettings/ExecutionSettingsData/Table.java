package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Table extends Settings {
    @JsonProperty("inference_parameters")
    public TableInferenceParameters inferenceParameters;
}
