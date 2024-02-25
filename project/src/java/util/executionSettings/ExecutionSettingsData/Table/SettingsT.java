package project.src.java.util.executionSettings.ExecutionSettingsData.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;

public class SettingsT extends Settings {
	@JsonProperty("inference_parameters")
	public InferenceParameters inferenceParameters;
}
