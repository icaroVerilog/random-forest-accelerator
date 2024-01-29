package project.src.java.util.executionSettings.ExecutionSettingsData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "approach", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Conditional.class, name = "conditional"),
        @JsonSubTypes.Type(value = Table.class, name = "table")
})
public abstract class Settings {

    @JsonProperty("approach")
    public String approach;

    @JsonProperty("dataset")
    public String dataset;

    @JsonProperty("precision")
    public String precision;

    @JsonProperty("mode")
    public String mode;

    @JsonProperty("training_parameters")
    public TrainingParameters trainingParameters;

}
