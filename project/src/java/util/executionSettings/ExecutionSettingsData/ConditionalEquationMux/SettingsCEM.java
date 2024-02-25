package project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.ExecutionSettingsData.Settings;

public class SettingsCEM extends Settings {
	@JsonProperty("inference_parameters")
	public InferenceParameters inferenceParameters;
}