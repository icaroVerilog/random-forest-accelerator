package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "approach", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings.class, name = "conditional"),
        @JsonSubTypes.Type(value = project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings.class, name = "equation"),
        @JsonSubTypes.Type(value = project.src.java.util.executionSettings.ExecutionSettingsData.ConditionalEquationMux.Settings.class, name = "multiplexer"),
        @JsonSubTypes.Type(value = project.src.java.util.executionSettings.ExecutionSettingsData.Table.Settings.class, name = "table")
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
    public String platform;

    @JsonProperty("training_parameters")
    public TrainingParameters trainingParameters;

}
