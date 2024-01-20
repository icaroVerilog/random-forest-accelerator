package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TrainingParameters {

    @JsonProperty("estimators")
    private int estimatorsQuantity;

    @JsonProperty("training_percent")
    private int trainingPercent;

    @JsonProperty("max_depth")
    private String maxDepth;


    public int getEstimatorsQuantity() {
        return estimatorsQuantity;
    }

    public int getTrainingPercent() {
        return trainingPercent;
    }

    public String getMaxDepth() {
        return maxDepth;
    }
}
