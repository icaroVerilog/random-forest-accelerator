package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.executionSettingsData.GeneralParameters;
import project.src.java.util.executionSettings.executionSettingsData.InferenceParameters;
import project.src.java.util.executionSettings.executionSettingsData.TrainingParameters;

public class ExecutionSettings {

    @JsonProperty("general_parameters")
    public GeneralParameters generalParameters;

    @JsonProperty("training_parameters")
    public TrainingParameters trainingParameters;

    @JsonProperty("inference_parameters")
    public InferenceParameters inferenceParameters;
}
