package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsBitwidth {

    @JsonProperty("threshold")
    public int comparedValue;

    @JsonProperty("compared_column")
    public int comparedColumn;

    @JsonProperty("index")
    public int index;
}
