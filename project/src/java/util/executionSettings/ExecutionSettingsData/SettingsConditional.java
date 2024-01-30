package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SettingsConditional extends Settings {
    @JsonProperty("inference_parameters")
    public ConditionalInferenceParameters inferenceParameters;
}
