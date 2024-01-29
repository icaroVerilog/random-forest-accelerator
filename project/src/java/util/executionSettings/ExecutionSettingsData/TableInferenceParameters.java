package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableInferenceParameters {
    @JsonProperty("fields_bitwidth")
    public TableFieldsBitwidth fieldsBitwidth;
}
