package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ExecutionSettings {

    @JsonProperty("regenerate_estimators")
    public boolean regenerateEstimators;

    @JsonProperty("runs")
    public ArrayList<Settings> executionsSettings;
}
