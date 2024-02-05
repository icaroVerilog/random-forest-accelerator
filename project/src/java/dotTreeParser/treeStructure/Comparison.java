package project.src.java.dotTreeParser.treeStructure;

public class Comparison {
    private String  featureName;
    private Float threshold;
    private Integer column;
    private String comparisonType;

    public Comparison(String featureName, Float threshold, Integer column, String comparisonType) {
        this.featureName = featureName;
        this.threshold = threshold;
        this.column = column;
        this.comparisonType = comparisonType;
    }
    
    public Comparison() {
    }

    @Override
    public String toString() {
        return "Feature [name=" + featureName + ", threshold=" + threshold + ", column=" + column + ", comparissonType="
                + comparisonType + "]";
    }

    public String getFeatureName() {
        return featureName;
    }
   
    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }
    public Float getThreshold() {
        return threshold;
    }
    public void setThreshold(Float threshold) {
        this.threshold = threshold;
    }
    public Integer getColumn() {
        return column;
    }
    public void setColumn(Integer column) {
        this.column = column;
    }
    public String getComparisonType() {
        return comparisonType;
    }
    public void setComparisonType(String comparisonType) {
        this.comparisonType = comparisonType;
    }


}
