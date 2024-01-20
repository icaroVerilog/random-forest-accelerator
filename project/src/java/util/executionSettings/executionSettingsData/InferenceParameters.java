package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.executionSettingsData.FieldsBitwidth;

public class InferenceParameters {

    @JsonProperty("approach")
    private String approach;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("fields_bitwidth")
    private FieldsBitwidth fieldsBitwidth;


    public String getApproach() {
        return approach;
    }

    public String getMode() {
        return mode;
    }

    public FieldsBitwidth getFieldsBitwidth() {
        return fieldsBitwidth;
    }
}
