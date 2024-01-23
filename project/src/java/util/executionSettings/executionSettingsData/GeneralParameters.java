package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralParameters {
    @JsonProperty("dataset")
    public String datasetName;

    @JsonProperty("precision")
    public String precision;
}

