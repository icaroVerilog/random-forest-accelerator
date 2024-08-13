package project.src.java.util.executionSettings.JSON.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class ExecutionSettings {

    @JsonProperty("estimators_generation_policy")
    public String estimatorsGenerationPolicy;

    @JsonProperty("runs")
    public ArrayList<Settings> executionsSettings;
}
