package project.src.java.util.executionSettings.ExecutionSettingsData.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Settings extends project.src.java.util.executionSettings.ExecutionSettingsData.Settings {
    @JsonProperty("inference_parameters")
    public InferenceParameters inferenceParameters;
}
