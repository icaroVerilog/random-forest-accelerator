package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.executionSettingsData.GeneralParameters;
import project.src.java.util.executionSettings.executionSettingsData.InferenceParameters;
import project.src.java.util.executionSettings.executionSettingsData.TrainingParameters;

public class ExecutionSettings {

    @JsonProperty("general_parameters")
    GeneralParameters generalParameters;

    @JsonProperty("training_parameters")
    TrainingParameters trainingParameters;

    @JsonProperty("inference_parameters")
    InferenceParameters inferenceParameters;


    public GeneralParameters getGeneralParameters() {
        return generalParameters;
    }

    public TrainingParameters getTrainingParameters() {
        return trainingParameters;
    }

    public InferenceParameters getInferenceParameters() {
        return inferenceParameters;
    }
}
