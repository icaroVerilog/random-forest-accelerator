package project.src.java.dotTreeParser.treeStructure;

public class Comparisson {
    private String  featureName;
    private Float threshold;
    private Integer column;
    private String comparissonType;

    public Comparisson(String featureName, Float threshold, Integer column, String comparissonType) {
        this.featureName = featureName;
        this.threshold = threshold;
        this.column = column;
        this.comparissonType = comparissonType;
    }
    
    public Comparisson() {
    }

    @Override
    public String toString() {
        return "Feature [name=" + featureName + ", threshold=" + threshold + ", column=" + column + ", comparissonType="
                + comparissonType + "]";
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
    public String getComparissonType() {
        return comparissonType;
    }
    public void setComparissonType(String comparissonType) {
        this.comparissonType = comparissonType;
    }


}
