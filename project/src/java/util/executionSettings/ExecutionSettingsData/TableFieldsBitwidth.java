package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableFieldsBitwidth {
    @JsonProperty("threshold")
    public int comparedValue;

    @JsonProperty("compared_column")
    public int comparedColumn;

    @JsonProperty("index")
    public int index;
}
