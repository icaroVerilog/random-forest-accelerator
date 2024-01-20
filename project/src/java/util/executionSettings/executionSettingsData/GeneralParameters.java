package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralParameters {
    @JsonProperty("dataset")
    private String dataset;

    @JsonProperty("precision")
    private String precision;


    public String getDataset() {
        return dataset;
    }

    public String getPrecision() {
        return precision;
    }
}

