package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ExecutionSettings {

    @JsonProperty("runs")
    public ArrayList<Settings> executionsSettings;
}
