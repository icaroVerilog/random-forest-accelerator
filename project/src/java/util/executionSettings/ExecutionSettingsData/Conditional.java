package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Conditional extends Settings {
    @JsonProperty("inference_parameters")
    public ConditionalInferenceParameters inferenceParameters;
}
