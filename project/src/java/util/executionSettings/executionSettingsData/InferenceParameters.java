package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;
import project.src.java.util.executionSettings.executionSettingsData.FieldsBitwidth;

public class InferenceParameters {

    @JsonProperty("approach")
    public String approach;

    @JsonProperty("mode")
    public String mode;

    @JsonProperty("fields_bitwidth")
    public FieldsBitwidth fieldsBitwidth;
}
