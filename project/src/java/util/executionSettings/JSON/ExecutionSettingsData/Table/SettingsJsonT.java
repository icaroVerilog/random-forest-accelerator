package project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Settings;

public class SettingsJsonT extends Settings {
	@JsonProperty("inference_parameters")
	public InferenceParameters inferenceParameters;
}
