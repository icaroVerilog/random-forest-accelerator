package project.src.java.util.executionSettings.JSON.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainingParameters {
    @JsonProperty("estimators")
    public Integer estimatorsQuantity;

    @JsonProperty("training_percent")
    public Integer trainingPercent;

    @JsonProperty("max_depth")
    public Integer maxDepth;
}
