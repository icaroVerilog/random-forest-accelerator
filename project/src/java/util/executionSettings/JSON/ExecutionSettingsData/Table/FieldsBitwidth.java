package project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsBitwidth {
    @JsonProperty("threshold")
    public Integer comparedValue;

    @JsonProperty("compared_column")
    public Integer comparedColumn;

    @JsonProperty("index")
    public Integer index;
}
