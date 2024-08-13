package project.src.java.util.executionSettings.JSON.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.ConditionalEquationMux.SettingsCEM;
import project.src.java.util.executionSettings.JSON.ExecutionSettingsData.Table.SettingsT;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "approach", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SettingsCEM.class, name = "conditional"),
        @JsonSubTypes.Type(value = SettingsCEM.class, name = "equation"),
        @JsonSubTypes.Type(value = SettingsCEM.class, name = "multiplexer"),
        @JsonSubTypes.Type(value = SettingsCEM.class, name = "pipeline"),
        @JsonSubTypes.Type(value = SettingsT.class, name = "table")
})
public abstract class Settings {

    @JsonProperty("approach")
    public String approach;

    @JsonProperty("dataset")
    public String dataset;

    @JsonProperty("precision")
    public String precision;

    @JsonProperty("target")
    public String target;

    @JsonProperty("platform")
    public Platform platform;

    @JsonProperty("training_parameters")
    public TrainingParameters trainingParameters;

}
