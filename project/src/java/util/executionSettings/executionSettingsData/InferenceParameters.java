package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InferenceParameters {

    @JsonProperty("approach")
    public String approach;

    @JsonProperty("mode")
    public String mode;

    @JsonProperty("conditional")
    public ConditionalParameters conditional;

    @JsonProperty("table")
    public TableParameters table;

}
