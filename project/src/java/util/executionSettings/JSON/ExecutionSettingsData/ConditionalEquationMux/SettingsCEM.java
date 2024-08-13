package project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ConditionalEquationMux;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Settings;

public class SettingsCEM extends Settings {
	@JsonProperty("inference_parameters")
	public InferenceParameters inferenceParameters;
}