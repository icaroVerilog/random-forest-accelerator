package project.src.java.util.executionSettings.executionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldsBitwidth {

    @JsonProperty("threshold")
    private int threshold;

    @JsonProperty("compared_column")
    private int comparedColumn;

    @JsonProperty("index")
    private int index;


    public int getThreshold() {
        return threshold;
    }

    public int getComparedColumn() {
        return comparedColumn;
    }

    public int getIndex() {
        return index;
    }
}
