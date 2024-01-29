package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainingParameters {
    @JsonProperty("estimators")
    public int estimatorsQuantity;

    @JsonProperty("training_percent")
    public int trainingPercent;

    @JsonProperty("max_depth")
    public String maxDepth;
}
